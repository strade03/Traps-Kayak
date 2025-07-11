#include "jsonnetworkreply.h"
#include <QDebug>
#include <QNetworkReply>
#include <QJsonParseError>

JsonNetworkReply::JsonNetworkReply(QNetworkReply *networkReply) : QObject(),
    _networkReply(networkReply),
    _resultCallback(0),
    _errorCallback(0)
{
    if (networkReply!=0) {
        connect(_networkReply, &QNetworkReply::finished, this, &JsonNetworkReply::processFinished);

    }
}

JsonNetworkReply::~JsonNetworkReply() {
    if (_networkReply!=0) _networkReply->deleteLater();
}

JsonNetworkReply* JsonNetworkReply::onResult(std::function<void(QJsonDocument)> resultCallback) {
    _resultCallback = resultCallback;
    return this;
}

JsonNetworkReply* JsonNetworkReply::onError(std::function<void(int, QString)> errorCallback) {
    _errorCallback = errorCallback;
    return this;
}

void JsonNetworkReply::processFinished() {
    deleteLater();
    if (_networkReply->error()==QNetworkReply::NetworkError::NoError) {
        QJsonParseError parseError;
        QByteArray byteArray = _networkReply->readAll();
        //qDebug() << byteArray;
        QJsonDocument jsonDoc = QJsonDocument::fromJson(byteArray, &parseError);
        if (parseError.error!=QJsonParseError::NoError) {
            QString errorString = QString("At index %0: parsing error %1: %2").arg(parseError.offset).arg(parseError.error).arg(parseError.errorString());
            qWarning() << errorString;
            if (_errorCallback!=0) _errorCallback(-1, errorString);
            return;
        }
        if (_resultCallback!=0) _resultCallback(jsonDoc);
        else qWarning() << "No result callback specified";
        return;
    }

    qWarning() << "Network error code: " << _networkReply->error() << ":" << _networkReply->errorString();
    if (_errorCallback!=0) _errorCallback(_networkReply->error(), _networkReply->errorString());
}
