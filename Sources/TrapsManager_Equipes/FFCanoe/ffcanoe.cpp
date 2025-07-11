#include "ffcanoe.h"
#include <QQmlEngine>
#include <QSettings>

#define FFCANOE_HOST "ffcanoeHost"
#define FFCANOE_PORT "ffcanoePort"
#define FFCANOE_RUNID "ffcanoeRunId"
#define FFCANOE_FORWARD_PENALTY "ffcanoeForwardPenalty"
#define FFCANOE_FORWARD_TIME "ffcanoeForwardTime"
#define FFCANOE_LOCALHOST "ffcanoeLocalHost"

FFCanoe::FFCanoe() : QObject(),
    _forwardPenalty(false),
    _forwardTime(false),
    _connected(false),
    _host("192.168.1.x"),
    _port(7072),
    _runId(1),
    _localHost(true),
    _buffer(0)

{
    QQmlEngine::setObjectOwnership(this, QQmlEngine::CppOwnership);

    QSettings settings;
    _host = settings.value(FFCANOE_HOST, "192.168.1.x").toString();
    _port = settings.value(FFCANOE_PORT, 7072).toInt();
    _runId = settings.value(FFCANOE_RUNID, 1).toInt();
    _forwardPenalty = settings.value(FFCANOE_FORWARD_PENALTY, true).toBool();
    _forwardTime = settings.value(FFCANOE_FORWARD_TIME, false).toBool();
    _localHost = settings.value(FFCANOE_LOCALHOST, true).toBool();

    QObject::connect(&_connector, &FFCanoeConnector::connectedToServer, this, &FFCanoe::setConnected);
    QObject::connect(&_connector, &FFCanoeConnector::error, this, &FFCanoe::error);
    QObject::connect(&_connector, &FFCanoeConnector::penaltySent, this, &FFCanoe::penaltySent);
    QObject::connect(this, &FFCanoe::connectToServer, &_connector, &FFCanoeConnector::connectToServer);
    QObject::connect(this, &FFCanoe::disconnectFromServer, &_connector, &FFCanoeConnector::disconnectFromServer);
    QObject::connect(this, &FFCanoe::sendPenaltyToServer, &_connector, &FFCanoeConnector::sendPenalty);
    QObject::connect(this, &FFCanoe::sendTimeToServer, &_connector, &FFCanoeConnector::sendTime);

    _connector.moveToThread(&_connectorThread);
    _connectorThread.start();

}

FFCanoe::~FFCanoe() {

}

void FFCanoe::exit() {
    _connectorThread.exit();
}


void FFCanoe::setHost(QString inputString) {
    qDebug() << "New host: " << inputString;
    _host = inputString;
    QSettings settings;
    settings.setValue(FFCANOE_HOST, _host);

}

void FFCanoe::setPort(QString inputString) {
    _port = inputString.toInt();
    QSettings settings;
    settings.setValue(FFCANOE_PORT, _port);
}

void FFCanoe::setRunId(int runId) {
    qDebug() << "New runId: " << runId;
    _runId = runId;
    QSettings settings;
    settings.setValue(FFCANOE_RUNID, _runId);

}

void FFCanoe::setLocalHost(bool value) {
    qDebug() << "FFCanoe running localhost: " << value;
    _localHost = value;
    QSettings settings;
    settings.setValue(FFCANOE_LOCALHOST, _localHost);
}

void FFCanoe::allowForwardPenalty(bool allow) {
    _forwardPenalty = allow;
    QSettings settings;
    settings.setValue(FFCANOE_FORWARD_PENALTY, _forwardPenalty);

}

void FFCanoe::allowForwardTime(bool allow) {
    _forwardTime = allow;
    QSettings settings;
    settings.setValue(FFCANOE_FORWARD_TIME, _forwardTime);
}


void FFCanoe::requestConnection(bool value) {
    qDebug() << "Requesting connection: "<< value;
    if (value) {
        emit connecting();
        QString host = _host;
        if (_localHost) host = "127.0.0.1";
        emit connectToServer(host, _port, _runId);
    }
    else {
        emit disconnectFromServer();
    }
}

void FFCanoe::setConnected(bool connected) {
    _connected = connected;
    if (_connected) {
        emit connectedToTarget();
        emit toast("Connecté à FFCanoe", 3000);
    }
    else emit disconnectedFromTarget();
}

void FFCanoe::penaltySent() {
    if (_buffer>0) {
        _buffer--;
        emit bufferChanged(_buffer);
    }
}


void FFCanoe::sendPenalty(int bib, int gateId, int penalty) {
    if (_forwardPenalty && _connected) {
        _buffer++;
        emit bufferChanged(_buffer);
        emit sendPenaltyToServer(bib, gateId, penalty);
    }
}

void FFCanoe::sendTime(int bib, int time) {
    if (_forwardTime && _connected) emit sendTimeToServer(bib, time);
}
