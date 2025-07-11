#ifndef CLOUD_H
#define CLOUD_H

#include <QObject>
#include "Cloud/cloudthread.h"

class Cloud : public QObject
{
    Q_OBJECT

    Q_PROPERTY(QString username READ username NOTIFY usernameChanged)
    Q_PROPERTY(QString password READ password NOTIFY passwordChanged)
    Q_PROPERTY(QString errorString READ errorString NOTIFY errorStringChanged)
    Q_PROPERTY(bool syncWithCloud READ syncWithCloud NOTIFY syncWithCloudChanged)

public:
    Cloud();
    ~Cloud();

    QString username() const { return _username; }
    QString password() const { return _password; }
    QString errorString() const { return _errorString; }
    bool syncWithCloud() const { return _syncWithCloud; }

signals:

    void usernameChanged(QString username);
    void passwordChanged(QString password);
    void errorStringChanged(QString errorString);
    void syncWithCloudChanged(bool syncWithCloud);
    void incomingPenaltyList(QList<Penalty> penaltyList);

public slots:

    void toggleSyncWithCloud();
    void acceptUsername(QString inputString);
    void acceptPassword(QString inputString);
    void processError(QString errorString);


private:

    CloudThread* _proxy;

    QString _username;
    QString _password;
    QString _errorString;
    bool _syncWithCloud;

};

#endif // CLOUD_H
