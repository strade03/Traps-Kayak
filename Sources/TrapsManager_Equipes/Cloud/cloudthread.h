#ifndef CLOUDTHREAD_H
#define CLOUDTHREAD_H

#include <QObject>
#include <QTimer>
#include <QThread>
#include "Canoe/penalty.h"
#include <QNetworkAccessManager>
#include <QNetworkReply>

class CloudThread : public QThread
{

    Q_OBJECT

public:
    CloudThread();
    void sync(const QString& user, const QString& password);
    void stop();
    bool isSync() const { return _sync; }

signals:

    void incomingPenaltyList(QList<Penalty> penaltyList);
    void incomingTimeList(QString bib, QList<Penalty> penaltyList);
    void error(QString error);


public slots:

    void postBiblist(const QJsonArray& biblist, const QString& user, const QString& password);

private slots:

    void requestPenaltyList();
    void requestChronoList();
    void replyFinished(QNetworkReply* reply);

private:

    QTimer _penaltyTimer;
    QTimer _chronoTimer;
    bool _sync;
    QString _user;
    QString _password;
    QString _penaltyUrl;
    QString _chronoUrl;
    int _penaltyTimestamp;
    int _chronoTimestamp;

    QNetworkAccessManager _networkManager;

    void processPenalty(const QJsonObject& jsonPenaltyList);
    void processTime(const QJsonObject& jsonTimeList);

};

#endif // CLOUDTHREAD_H
