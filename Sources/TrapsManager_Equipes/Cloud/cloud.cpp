#include "cloud.h"
#include <QQmlEngine>
#include <QDebug>

Cloud::Cloud() : QObject(),
    _username("traps"),
    _password("trapspass"),
    _syncWithCloud(false),
    _proxy(new CloudThread())
{
    QQmlEngine::setObjectOwnership(this, QQmlEngine::CppOwnership);
    connect(_proxy, &CloudThread::incomingPenaltyList, this, &Cloud::incomingPenaltyList);
    connect(_proxy, &CloudThread::error, this, &Cloud::processError);

}

Cloud::~Cloud()
{
    _proxy->exit();
}


void Cloud::toggleSyncWithCloud() {
    if (_proxy->isSync()) {
        qDebug() << "Stop syncing";
        _proxy->stop();
        _syncWithCloud = false;
        emit syncWithCloudChanged(_syncWithCloud);
    }
    else {
        qDebug() << "Start syncing";
        _syncWithCloud = true;
        emit syncWithCloudChanged(_syncWithCloud);
        _proxy->sync(_username, _password);
    }
}

void Cloud::acceptUsername(QString inputString) {
    _username = inputString;
    emit usernameChanged(_username);
}

void Cloud::acceptPassword(QString inputString) {
    _password = inputString;
    emit passwordChanged(_password);
}

void Cloud::processError(QString errorString)
{
    _errorString = errorString;
    emit errorStringChanged(errorString);
}
