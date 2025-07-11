#ifndef NETWORKJSONREPLY_H
#define NETWORKJSONREPLY_H

#include <QObject>
#include <functional>
class QNetworkReply;

class JsonNetworkReply : public QObject
{
    Q_OBJECT
public:
    explicit JsonNetworkReply(QNetworkReply* networkReply);
    ~JsonNetworkReply();
    JsonNetworkReply* onResult(std::function<void(QJsonDocument)> resultCallback);
    JsonNetworkReply* onError(std::function<void(int, QString)> errorCallback);

signals:

public slots:
    void processFinished();

private:

    QNetworkReply* _networkReply;
    std::function<void(QJsonDocument)> _resultCallback;
    std::function<void(int, QString)> _errorCallback;

};

#endif // NETWORKJSONREPLY_H
