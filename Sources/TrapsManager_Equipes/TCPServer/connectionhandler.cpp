#include "connectionhandler.h"
#include <QThread>
#include <QJsonDocument>
#include <QJsonObject>
#include <QList>
#include <QJsonValue>
#include <QDataStream>
#include <QJsonArray>
#include <QDateTime>

#define READ_TIMEOUT 5000
#define COMMAND_NEEDCONF 0
#define COMMAND_VOLLEY 1
#define KEY_COMMAND "command"
#define KEY_ASKFORBIBLIST 0
#define KEY_PENALTYLIST 1
#define KEY_STARTTIME 2
#define KEY_FINISHTIME 3

#define EOT 4

ConnectionHandler::ConnectionHandler(QObject* bibListProvider) : QThread(),
    _busy(false),
    _bibListProvider(bibListProvider)
{

    moveToThread(this);
    _socket.moveToThread(this);
    _readTimer.moveToThread(this);
    connect(&_socket, SIGNAL(readyRead()), SLOT(read()));
    connect(&_socket, SIGNAL(disconnected()), SLOT(disconnected()));
    connect(&_readTimer, SIGNAL(timeout()), SLOT(readTimeout()));

    _readTimer.setSingleShot(true);
    qDebug("ConnectionHandler (%p): constructed", this);
    this->start();

}

ConnectionHandler::~ConnectionHandler() {
    _socket.close();
    quit();
    wait();
    qDebug("ConnectionHandler (%p): destroyed", this);
}


void ConnectionHandler::run() {
    qDebug("ConnectionHandler (%p): thread started", this);
    try {
        exec();
    }
    catch (...) {
        qCritical("ConnectionHandler (%p): an uncatched exception occured in the thread",this);
    }
    qDebug("ConnectionHandler (%p): thread stopped", this);
}


void ConnectionHandler::handleConnection(int sd) {
    qDebug("ConnectionHandler (%p): handle new connection", this);
    _busy = true;
    //UGLY workaround - we need to clear writebuffer before reusing this socket
    //https://bugreports.qt-project.org/browse/QTBUG-28914
    _socket.connectToHost("",0);
    _socket.abort();
    if (!_socket.setSocketDescriptor(sd)) {
        qCritical("ConnectionHandler (%p): cannot initialize socket: %s", this,qPrintable(_socket.errorString()));
        return;
    }

    _readTimer.start(READ_TIMEOUT);
    _byteArray.clear();

}


bool ConnectionHandler::isBusy() {
    return _busy;
}

void ConnectionHandler::setBusy() {
    this->_busy = true;
}


void ConnectionHandler::readTimeout() {
    qDebug("ConnectionHandler (%p): read timeout occured",this);
    _socket.disconnectFromHost();
}


void ConnectionHandler::disconnected() {
    qDebug("ConnectionHandler (%p): disconnected", this);
    _socket.close();
    _readTimer.stop();
    _busy = false;
}


/*
    input:

    {
        "command":0 (ask for list of bib)


    }
    {
        "command":1, (set penalties)
        "bib":123,
        "penaltyList": {
                "2": 0,
                "5": 2,
                "6": 50,
        }

    }
    {
        "command":2, (set start time)
        "bib":123,
        "time": 1456789

    }
    {
        "command":3, (set finish time)
        "bib":123,
        "time": 1456789

    }


    output:
    {
        "bibList":[123,45,56,453],
        "epoch":124567898
    }
    {
        "response":0
    }


*/


void ConnectionHandler::replyError() {
    QJsonObject jsonObject;
    jsonObject.insert("response", -1);
    QJsonDocument doc(jsonObject);
    QByteArray byteArray(doc.toJson(QJsonDocument::Compact));
    byteArray.append('\n');
    byteArray.append("EOT\n");   // End Of Transmission
    _socket.write(byteArray);
    _socket.flush();
    _socket.disconnectFromHost();
}


bool ConnectionHandler::readByteArray() {
    char c;
    bool eot = false;
    while (!eot) {
        bool ok = _socket.getChar(&c);
        if (!ok) break;
        if (c==(char)EOT) {
            eot = true;
            break;
        }
        if (c!='\n') _byteArray.append(c);
    }
    _readTimer.start(READ_TIMEOUT);
    return eot;
}

void ConnectionHandler::read() {

    // here read stuff
    bool eot = readByteArray();
    qDebug() << "Read from socket:" << QString(_byteArray);
    if (!eot) return;

    QJsonParseError parseError;
    QJsonDocument jsonDoc = QJsonDocument::fromJson(_byteArray, &parseError);
    _byteArray.clear(); // clear byte array, ready for another transmission
    qDebug() << jsonDoc.toJson();
    if (jsonDoc.isNull()) {
        qDebug() << "JSON Doc is null";
        replyError();
    }
    if (parseError.error==QJsonParseError::NoError) {
        QJsonObject rootObject = jsonDoc.object();
        if (rootObject.isEmpty()) {
            qDebug() << "Root object is empty";
            replyError();
        }
        int command = rootObject.value(KEY_COMMAND).toInt();
        QJsonObject response;
        switch (command) {
            case KEY_ASKFORBIBLIST: {
                QJsonArray array;
                QList<int> list;
                // reading Biblist model. Blocks until call is done. Use invokeMethod to be thread safe.
                QMetaObject::invokeMethod(_bibListProvider, "bibNumberList", Qt::BlockingQueuedConnection, Q_RETURN_ARG(QList<int>, list));
                foreach (int number, list) array << number;
                response.insert("bibList", array);
                response.insert("epoch", QDateTime::currentMSecsSinceEpoch());
                response.insert("response", 0);
                break;
            }
            case KEY_PENALTYLIST: {
                int bib = rootObject.value("bib").toInt();
                if (bib<1) replyError();
                QJsonObject penaltyObject = rootObject.value("penaltyList").toObject();
                if (penaltyObject.isEmpty()) replyError();
                QHash<int, int> penaltyMap;
                foreach (QString key, penaltyObject.keys()) {
                    bool ok;
                    int gateName = key.toInt(&ok);
                    if (!ok) replyError();
                    int value = penaltyObject.value(key).toInt(-1);
                    if (value<0) replyError();
                    penaltyMap.insert(gateName, value);
                }

                emit incomingPenalty(bib, penaltyMap);
                response.insert("response", 0);
                break;
            }
            case KEY_STARTTIME: {
                int bib = rootObject.value("bib").toInt();
                if (bib<1) replyError();
                qint64 startTime = (qint64)(rootObject.value("time").toVariant().toULongLong());
                emit incomingStartTime(bib, startTime);
                response.insert("response", 0);
                break;
            }
            case KEY_FINISHTIME: {
                int bib = rootObject.value("bib").toInt();
                if (bib<1) replyError();
                qint64 finishTime = (qint64)(rootObject.value("time").toVariant().toULongLong());
                emit incomingFinishTime(bib, finishTime);
                response.insert("response", 0);
                break;
            }
            default: {
                qDebug() << "Command not found or value is wrong";
                replyError();
            }
        }

        QJsonDocument doc(response);
        qDebug() << "sending back: " << doc.toJson(QJsonDocument::Compact);
        QByteArray byteArray(doc.toJson(QJsonDocument::Compact));
        byteArray.append('\n');
        byteArray.append("EOT\n");   // End Of Transmission
        _socket.write(byteArray);
        _socket.flush();


    } else {
        qCritical() << "Error while parsing json";
    }


    _socket.disconnectFromHost();

}
