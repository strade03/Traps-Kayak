#include "ffcanoeconnector.h"
#include <QDataStream>
#include <QThread>

/**

16 bits: header
0
0

n bytes ending by \0 : name of the machine
0

16 bits: race id
15 (LSB)
0 (MSB)

16 bits: bib
LSB bib
MSB bib

16 bits: run id (1 or 2)
1 (LSB)
0 (MSB)

16 bits: one character
D=Au depart, E=En Course, A=A l'Arrivee, I=Intermediaire, J=Intermediaire 2, T=Temps Tournant, H=Heure de depart, F=Heure d'arriv�e, N=Heure intermediaire 1, M=Heure intermediaire	2, P=P�nalit�, G=P�nalit� Globale, U=Annulation (Undo) de la derniere operation pour le dossard, S=Nombre de Satellite, W=Heure de Synchro, K=Top de Synchro, d=Impulsion D�part, a=Impulstion Arriv�e, i=Impulsion inter 1, j=Impulsion inter 2
0

32 bits: penalty
0, 2, 50
0
0
0

32 bits: gate (starts at 1)
gateId
0
0
0

**/

FFCanoeConnector::FFCanoeConnector() : QObject(),
    _connected(false)

{

}


void FFCanoeConnector::connectToServer(const QString& host, int port, int runId) {
    qDebug() << "Connecting to " << host << ", port " << port;
    _runId = runId;
    _socket = new QTcpSocket(this);
    connect(_socket, SIGNAL(error(QAbstractSocket::SocketError)), this, SLOT(errorHandler(QAbstractSocket::SocketError)));
    _socket->connectToHost(host, port);
    if (!_socket->waitForConnected(5000)) {
        qCritical() << "Cannot connect to FFCanoe !";
        emit connectedToServer(false);
        emit error("Problème de connexion FFCanoe", QString("Impossible de se connecter à FFCanoe sur\n%0:%1").arg(host).arg(port));
        _socket->deleteLater();
        return;
    }
    _connected = true;
    emit connectedToServer(true);

}

void FFCanoeConnector::sendTime(int bib, int time) {
    if (!_connected) return;

    qDebug() << "Processing time: bib=" << bib << " time=" << time;
    if (_socket==0) {
        qCritical() << "No connection to FFCanoe. Socket is null. Abort processing";
        return;
    }
    if (!_socket->isOpen()) {
        qCritical() << "No connection to FFCanoe. Abort processing";
        emit connectedToServer(false);
        return;
    }
    QByteArray byteArray;
    QDataStream dataStream(&byteArray, QIODevice::WriteOnly);
    dataStream.setByteOrder(QDataStream::LittleEndian); // set byte order as little endian (this is the way ffcanoe works)

    dataStream << (quint16)0; // header
    dataStream << (quint8)0; // machine name "" => ended by \0
    dataStream << (quint16)70; // race id - unused. It can be anything
    dataStream << (quint16)bib; // bib id
    dataStream << (quint16)_runId; // run id
    dataStream << (quint16)'A'; // control character (80)
    dataStream << (quint32)time; //  1/1000 sec
    dataStream << (quint32)0;

    qint64 num = _socket->write(byteArray);
    _socket->flush();
    if (num!=byteArray.size()) {
        qCritical() << "Disconnect. Error while sending penalty over the network. num=" << num;
        _socket->close();
        _socket->deleteLater();
        _connected = false;
        emit connectedToServer(_connected);
        emit error("Problème de connexion FFCanoe", "Impossible d'envoyer le temps à FFCanoe");
        return;
    }
    QThread::msleep(FFCANOE_DELAY);

}

void FFCanoeConnector::sendPenalty(int bib, int gateId, int penalty) {
    if (!_connected) return;
    qDebug() << "Forwarding bib "<< bib << " gate " << gateId << " penalty " << penalty;
    if (_socket==0) {
        qCritical() << "No connection to FFCanoe. Socket is null. Abort processing";
        return;
    }
    if (!_socket->isOpen()) {
        qCritical() << "No connection to FFCanoe. Abort processing";
        _connected = false;
        emit connectedToServer(_connected);
        return;
    }

    QByteArray byteArray;
    QDataStream dataStream(&byteArray, QIODevice::WriteOnly);
    dataStream.setByteOrder(QDataStream::LittleEndian); // set byte order as little endian (this is the way ffcanoe works)

    dataStream << (quint16)0; // header
    dataStream << (quint8)0; // machine name "" => ended by \0
    dataStream << (quint16)70; // race id - unused. It can be anything
    dataStream << (quint16)bib; // bib id
    dataStream << (quint16)_runId; // run id
    dataStream << (quint16)'P'; // control character (80)
    dataStream << (quint32)penalty;
    dataStream << (quint32)gateId;

    qint64 num = _socket->write(byteArray);
    _socket->flush();
    if (num!=byteArray.size()) {
        qCritical() << "Disconnect. Error while sending penalty over the network. num=" << num;
        _socket->close();
        _socket->deleteLater();
        _connected = false;
        emit connectedToServer(_connected);
        emit error("Problème de connexion FFCanoe", "Impossible d'envoyer la pénalité à FFCanoe");
        return;
    }
    emit penaltySent();
    QThread::msleep(FFCANOE_DELAY);

}

void FFCanoeConnector::disconnectFromServer() {
    if (_connected) {
        _socket->close();
        _socket->deleteLater();
        _connected = false;
    }
    emit connectedToServer(false);

}


void FFCanoeConnector::errorHandler(QAbstractSocket::SocketError socketError) {
    qDebug() << "Handling socket error";
    qDebug() << _socket->errorString();
    emit error("Problème de connexion FFCanoe", QString("%1 (%0)").arg(socketError).arg(_socket->errorString()));
    emit connectedToServer(false);
    _socket->close();
    _socket->deleteLater();

}
