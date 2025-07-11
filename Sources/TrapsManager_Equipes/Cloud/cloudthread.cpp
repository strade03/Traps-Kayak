#include "cloudthread.h"
#include <QByteArray>
#include <QJsonDocument>
#include <QJsonParseError>
#include <QJsonArray>
#include <QDebug>

#define PENALTY_TIMER 3000
#define PENALTYLIST_STR "penaltylist"
#define TIMELIST_STR "timelist"

CloudThread::CloudThread(): QThread(),
    _sync(false),
    _penaltyTimestamp(0),
    _chronoTimestamp(0)
{
    moveToThread(this);
    _chronoTimer.moveToThread(this);
    _penaltyTimer.moveToThread(this);
    _chronoTimer.setSingleShot(true);
    _penaltyTimer.setSingleShot(true);
    _networkManager.moveToThread(this);
    _penaltyUrl = QString("http://10.194.49.233/%0/?user=%1&password=%2&timestamp=%3").arg(PENALTYLIST_STR);
    _chronoUrl = QString("http://10.194.49.233/%0/?user=%1&password=%2&timestamp=%3").arg(TIMELIST_STR);
    QObject::connect(&_penaltyTimer, SIGNAL(timeout()), SLOT(requestPenaltyList()));
    QObject::connect(&_chronoTimer, SIGNAL(timeout()), SLOT(requestChronoList()));
    QObject::connect(&_networkManager, SIGNAL(finished(QNetworkReply*)), SLOT(replyFinished(QNetworkReply*)));
    this->start();

}


void CloudThread::sync(const QString &user, const QString &password) {

    _user = user;
    _password = password;
    _sync = true;
    QMetaObject::invokeMethod(this, "requestPenaltyList", Qt::AutoConnection);

}

void CloudThread::stop() {
    _sync = false;
}

void CloudThread::postBiblist(const QJsonArray &biblist, const QString &user, const QString &password) {

}

void CloudThread::requestPenaltyList() {
    if (!_sync) return;

    QString url = _penaltyUrl.arg(_user).arg(_password).arg(_penaltyTimestamp);
    _networkManager.get(QNetworkRequest(QUrl(url)));

}

void CloudThread::requestChronoList() {
    if (!_sync) return;

}

void CloudThread::replyFinished(QNetworkReply *reply) {
    if (reply->error()!=QNetworkReply::NoError) {
        emit error(QString("%0: %1").arg(reply->error()).arg(reply->errorString()));
        _sync = false;
    }
    else {
        QJsonParseError jsonError;
        QByteArray byteArray = reply->readAll();
        QString url = reply->request().url().toString();
        QJsonDocument jsonDoc = QJsonDocument::fromJson(byteArray, &jsonError);
        if (jsonError.error==QJsonParseError::NoError && !jsonDoc.isNull()) {
            qDebug() << url;
            if (url.indexOf(PENALTYLIST_STR)>-1) {
                processPenalty(jsonDoc.object());
                _penaltyTimer.start(PENALTY_TIMER);
            }
            else if (url.indexOf(TIMELIST_STR)>-1) {
                processTime(jsonDoc.object());
                _chronoTimer.start(PENALTY_TIMER);
            }
            else {
                emit error("Got response from unknown request");
                _sync = false;
            }
        }
        else {
            emit error(QString("Json parsing error %0 at %1").arg(jsonError.error).arg(jsonError.offset));
            qDebug() << byteArray;
            _sync = false;
        }

    }
    delete reply;

}

void CloudThread::processPenalty(const QJsonObject &jsonPenaltyList)
{
    QList<Penalty> penaltyList;
    _penaltyTimestamp = jsonPenaltyList.value("timestamp").toInt();
    QJsonArray array = jsonPenaltyList.value("penaltyList").toArray();
    foreach (QJsonValue jsonValue, array) {
        QJsonObject obj = jsonValue.toObject();
        Penalty penalty(obj.value("bib").toString(), obj.value("gate").toInt(), obj.value("value").toInt());
        penalty.setCanvas(obj.value("canvas").toBool());
        penalty.setTeammate(obj.value("teammate").toBool());
        penalty.setSpot1(obj.value("xspot1").toDouble(), obj.value("yspot1").toDouble());
        penalty.setSpot2(obj.value("xspot2").toDouble(), obj.value("yspot2").toDouble());
        penaltyList << penalty;
    }

    emit incomingPenaltyList(penaltyList);

}

void CloudThread::processTime(const QJsonObject &jsonTimeList)
{

}
