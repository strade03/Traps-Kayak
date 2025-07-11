#ifndef SOFTWAREUPDATE_H
#define SOFTWAREUPDATE_H

#include <global.h>
#include "Network/jsonnetworkmanager.h"
#include <QNetworkReply>

class QFile;

class SoftwareUpdate : public QObject
{
    Q_OBJECT

    Q_PROPERTY(QString appTitle READ appTitle CONSTANT)
    Q_PROPERTY(QString appInfo READ appInfo CONSTANT)
    Q_PROPERTY(bool checkAtStartup READ checkAtStartup CONSTANT)
    Q_PROPERTY(bool newVersionAvailable READ newVersionAvailable NOTIFY versionChecked)
    Q_PROPERTY(QString newVersion READ newVersion NOTIFY versionChecked)
    Q_PROPERTY(QString newDate READ newDate NOTIFY versionChecked)
    Q_PROPERTY(QString whatsnew READ whatsnew NOTIFY versionChecked)
    Q_PROPERTY(QString filePath READ filePath NOTIFY versionChecked)
    Q_PROPERTY(bool versionUptodate READ versionUptodate NOTIFY versionChecked)

    Q_PROPERTY(bool downloading READ downloading NOTIFY downloadingChanged)


public:
    explicit SoftwareUpdate(const QString& platform, const QString& downloadLocation);
    ~SoftwareUpdate();

    QString appTitle() const;
    QString appInfo() const;

    QString newVersion() const { return _newVersion; }
    QString newDate() const { return _newDate; }
    QString whatsnew() const { return _whatsnew; }
    bool newVersionAvailable() const { return _newVersionAvailable; }
    bool checkAtStartup() const { return _checkAtStartup; }
    bool versionUptodate() const { return _versionUptodate; }
    bool downloading() const { return _downloading; }
    QString filePath() const { return _filePath; }

signals:

    void triggerOpenSoftwareUpdate();
    void versionChecked();

    void progress(int percent);
    void error(const QString& errorStr);
    void downloadingChanged(bool downloading);

    void exitApp(); // request exit app after update

public slots:

    void checknewVersion(bool force);
    void setCheckAtStartup(bool check);
    void download();

private slots:

    void onError(QNetworkReply::NetworkError code);
    void onFinished();
    void onReadyRead();
    void onDownloadProgress(qint64 bytesReceived, qint64 bytesTotal);

private:

    QString _newVersion;
    QString _newDate;
    QString _whatsnew;
    bool _newVersionAvailable;
    bool _checkAtStartup;
    JsonNetworkManager _jsonNetworkManager;
    QString _target;
    QString _platform;
    QString _downloadLocation;
    int _appTimestamp;
    bool _versionUptodate;
    QString _appVersion;
    bool _downloading;
    QString _appDate;
    QString _url;
    QString _filePath;

    QNetworkAccessManager _networkManager;
    QFile* _file;
    QNetworkReply* _reply;

    void startUpdateExitApp();



};

#endif // SOFTWAREUPDATE_H
