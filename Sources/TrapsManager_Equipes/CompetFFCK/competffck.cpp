#include "competffck.h"
#include <QQmlEngine>
#include <QSettings>

#define COMPETFFCK_HOST "competFFCKHost"
#define COMPETFFCK_PORT "competFFCKPort"
#define COMPETFFCK_FORWARD_PENALTY "competFFCKForwardPenalty"
#define COMPETFFCK_FORWARD_TIME "competFFCKForwardTime"

CompetFFCK::CompetFFCK(QObject *parent) : QObject(parent),
    _forwardPenalty(false),
    _forwardTime(false),
    _connected(false),
    _host("192.168.1.x"),
    _port(7072),
    _buffer(0)
{

    QQmlEngine::setObjectOwnership(this, QQmlEngine::CppOwnership);
    QSettings settings;
    _host = settings.value(COMPETFFCK_HOST, "102.168.1.x").toString();
    _port = settings.value(COMPETFFCK_PORT, 7012).toInt();
    _forwardPenalty = settings.value(COMPETFFCK_FORWARD_PENALTY, true).toBool();
    _forwardTime = settings.value(COMPETFFCK_FORWARD_TIME, false).toBool();

    QObject::connect(&_connector, &CompetFFCKConnector::connectedToServer, this, &CompetFFCK::setConnected);
    QObject::connect(&_connector, &CompetFFCKConnector::error, this, &CompetFFCK::error);
    QObject::connect(&_connector, &CompetFFCKConnector::penaltySent, this, &CompetFFCK::penaltySent);
    QObject::connect(this, &CompetFFCK::connectToServer, &_connector, &CompetFFCKConnector::connectToServer);
    QObject::connect(this, &CompetFFCK::disconnectFromServer, &_connector, &CompetFFCKConnector::disconnectFromServer);
    QObject::connect(this, &CompetFFCK::sendPenaltyToServer, &_connector, &CompetFFCKConnector::sendPenalty);
    QObject::connect(this, &CompetFFCK::sendTimeToServer, &_connector, &CompetFFCKConnector::sendTime);

    _connector.moveToThread(&_connectorThread);
    _connectorThread.start();

}

void CompetFFCK::exit() {
    _connectorThread.exit();
}

void CompetFFCK::setHost(QString inputString) {
    qDebug() << "New host: " << inputString;
     _host = inputString;
     QSettings settings;
     settings.setValue(COMPETFFCK_HOST, _host);
}

void CompetFFCK::setPort(QString inputString) {
    _port = inputString.toInt();
     QSettings settings;
     settings.setValue(COMPETFFCK_PORT, _port);
}

void CompetFFCK::allowForwardPenalty(bool allow) {
    _forwardPenalty = allow;
     QSettings settings;
     settings.setValue(COMPETFFCK_FORWARD_PENALTY, _forwardPenalty);

}

void CompetFFCK::allowForwardTime(bool allow) {
    _forwardTime = allow;
     QSettings settings;
     settings.setValue(COMPETFFCK_FORWARD_TIME, _forwardTime);
}

void CompetFFCK::requestConnection(bool value) {
    qDebug() << "Requesting connection: "<< value;
     if (value) {
         emit connecting();
         QString host = _host;
         emit connectToServer(host, _port);
     }
     else {
         emit disconnectFromServer();
     }
}

void CompetFFCK::setConnected(bool connected) {
    _connected = connected;
     if (_connected) {
         emit connectedToTarget();
         emit toast("Connecté à CompetFFCK", 3000);
     }
     else emit disconnectedFromTarget();
}

void CompetFFCK::penaltySent() {
    if (_buffer>0) {
        _buffer--;
        emit bufferChanged(_buffer);
    }
}

void CompetFFCK::sendPenalty(int bib, int gateId, int penalty) {
    if (_forwardPenalty && _connected) {
         _buffer++;
         emit bufferChanged(_buffer);
         emit sendPenaltyToServer(bib, gateId, penalty);
     }
}

void CompetFFCK::sendTime(int bib, int time) {
    if (_forwardTime && _connected) emit sendTimeToServer(bib, time);
}
