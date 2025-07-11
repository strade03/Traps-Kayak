#ifndef JSONNETWORKMANAGER_H
#define JSONNETWORKMANAGER_H

/*
    JsonNetworkManager networkManager;
    networkManager.getJson(QUrl("http://www.traps-ck.com/update/trapsmanager.json"))
    ->onResult([](QJsonDocument jsonDoc) {
       QJsonObject obj = jsonDoc.object();
       qDebug() << "Version: " << obj.value("version").toString();
       qDebug() << "What's new: " << obj.value("whatsnew").toString();
       qDebug() << "Seconds since epoch: " << obj.value("elapsedSecondsSinceEpoch").toInt();
    })
    ->onError([](int code, QString str) {
        qWarning() << "Error " << code << ":" << str;
    });

*/

#include <QNetworkAccessManager>

class JsonNetworkReply;


class JsonNetworkManager : public QNetworkAccessManager
{
    Q_OBJECT
public:
    explicit JsonNetworkManager();
    JsonNetworkReply* getJson(const QUrl& url);


};

#endif // JSONNETWORKMANAGER_H
