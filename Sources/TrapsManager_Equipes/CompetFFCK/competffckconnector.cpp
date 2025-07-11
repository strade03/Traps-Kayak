#include "competffckconnector.h"
#include <QThread>

CompetFFCKConnector::CompetFFCKConnector(QObject *parent) : QObject(parent),
    _socket(0),
    _connected(false)
{


}


void CompetFFCKConnector::connectToServer(const QString& host, int port) {
    qDebug() << "Connecting to " << host << ", port " << port;
    _socket = new QTcpSocket(this);
    connect(_socket, SIGNAL(error(QAbstractSocket::SocketError)), this, SLOT(errorHandler(QAbstractSocket::SocketError)));
    _socket->connectToHost(host, port);
    if (!_socket->waitForConnected(5000)) {
        qCritical() << "Cannot connect to CompetFFCK !";
        emit connectedToServer(false);
        emit error("Problème de connexion CompetFFCK", QString("Impossible de se connecter à CompetFFCK sur\n%0:%1").arg(host).arg(port));
        _socket->deleteLater();
        return;
    }
    _connected = true;
    emit connectedToServer(true);

}

void CompetFFCKConnector::disconnectFromServer() {
    if (_connected) {
        _socket->close();
        _socket->deleteLater();
        _connected = false;
    }
    emit connectedToServer(false);

}

void CompetFFCKConnector::errorHandler(QAbstractSocket::SocketError socketError) {
    qDebug() << "Handling socket error";
    qDebug() << _socket->errorString();
    emit error("Problème de connexion CompetFFCK", QString("%1 (%0)").arg(socketError).arg(_socket->errorString()));
    emit connectedToServer(false);
    _socket->close();
    _socket->deleteLater();
}

void CompetFFCKConnector::sendPenalty(int bib, int gateId, int penalty) {

    if (!_connected) {
        qWarning() << "Cannot send penalty because not connected to competFFCK";
        return;
    }

    QByteArray byteArray;
    byteArray.append("penalty ");
    byteArray.append(QString::number(bib).toLocal8Bit());
    byteArray.append(' ');
    byteArray.append(QString::number(gateId).toLocal8Bit());
    byteArray.append(" 1 ");
    byteArray.append(QString::number(penalty).toLocal8Bit());
    byteArray.append('\r');

    qint64 num = _socket->write(byteArray);
    _socket->flush();
    if (num!=byteArray.size()) {
        qCritical() << "Disconnect. Error while sending penalty over the network. num=" << num;
        _socket->close();
        _socket->deleteLater();
        _connected = false;
        emit connectedToServer(_connected);
        emit error("Problème de connexion CompetFFCK", "Impossible d'envoyer la pénalité à CompetFFCK");
        return;
    }
    emit penaltySent();
    // QThread::msleep(250); // Add this tempo if it is too fast for competFFCK

}

void CompetFFCKConnector::sendTime(int bib, int chrono) {
    if (!_connected) {
        qWarning() << "Cannot send chrono because not connected to competFFCK";
        return;
    }

    QByteArray byteArray;
    byteArray.append("chrono ");
    byteArray.append(QString::number(bib).toLocal8Bit());
    byteArray.append(' ');
    byteArray.append(QString::number(chrono).toLocal8Bit());
    byteArray.append('\r');

    qint64 num = _socket->write(byteArray);
    _socket->flush();
    if (num!=byteArray.size()) {
        qCritical() << "Disconnect. Error while sending chrono over the network. num=" << num;
        _socket->close();
        _socket->deleteLater();
        _connected = false;
        emit connectedToServer(_connected);
        emit error("Problème de connexion CompetFFCK", "Impossible d'envoyer le temps à CompetFFCK");
        return;
    }
    // QThread::msleep(250); // Add this tempo if it is too fast for competFFCK

}
