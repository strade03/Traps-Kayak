#include "jsonnetworkmanager.h"
#include "jsonnetworkreply.h"
#include <QNetworkReply>

JsonNetworkManager::JsonNetworkManager() : QNetworkAccessManager() { }

JsonNetworkReply* JsonNetworkManager::getJson(const QUrl &url) {
    QNetworkRequest request;
    request.setUrl(url);
    return new JsonNetworkReply(get(request));
}
