#include "softwareupdate.h"
#include <QQmlEngine>
#include <QDateTime>
#include <QJsonDocument>
#include <QJsonObject>
#include <Network/jsonnetworkreply.h>
#include <QSettings>
#include <QFile>
#include <QTimer>
#include <QDebug>
#include <QProcess>

#define COCOA_PLATFORM "cocoa"  // macos
#define WINDOWS_PLATFORM "windows"  // windows
#define XCB_PLATFORM "xcb"  // X window linux platform


SoftwareUpdate::SoftwareUpdate(const QString& platform, const QString& downloadLocation) : QObject(),
    _newVersionAvailable(false),
    _platform(platform),
    _downloadLocation(downloadLocation),
    _appTimestamp(INT_MAX),
    _versionUptodate(false),
    _downloading(false)

{
    QQmlEngine::setObjectOwnership((QObject*)this, QQmlEngine::CppOwnership);
    QSettings settings;
    _checkAtStartup = settings.value("checkAtStartup", QVariant(false)).toBool();
    QFile file(":/trapsmanager.json");
    if (file.open(QIODevice::ReadOnly | QIODevice::Text)) {
        QJsonDocument doc = QJsonDocument::fromJson(file.readAll());
        QJsonObject obj = doc.object();
        int timestamp = obj.value("elapsedSecondsSinceEpoch").toInt(0);
        if (timestamp>0) {
            _appTimestamp = timestamp;
            qInfo() << "Application timestamp: " << timestamp;
            _appVersion = obj.value("version").toString();
            qInfo() << "Application version: " << _appVersion;
            _url = obj.value("url").toString();
            qInfo() << "Remote update info: " << _url;
            QDateTime date;
            date.setSecsSinceEpoch(timestamp);
            _appDate = date.toString("MMMM yyyy");
            qInfo() << "Application date: " << _appDate;
        }
    }
    else {
        qWarning() << "Cannot load qrc:/trapsmanager.json";
    }
}

SoftwareUpdate::~SoftwareUpdate() { }

QString SoftwareUpdate::appTitle() const {
    return QString("TRAPS Manager Equipes %0").arg(_appVersion);
}

QString SoftwareUpdate::appInfo() const {
    return QString("%0\nDonnées d'application:\n%1").arg(_appDate).arg(Global::appDataDir);
}

void SoftwareUpdate::checknewVersion(bool force) {
    if (!_checkAtStartup && !force) return; // no check
    qDebug() << "Checking for new version...";
    _jsonNetworkManager.getJson(QUrl(_url))
    ->onError([](int code, QString errorString) {
        qWarning() << "Couldn't check new version: error" << code << ": " << errorString;
    })
    ->onResult([this](QJsonDocument doc) {
        QJsonObject obj = doc.object();
        int timestamp = obj.value("elapsedSecondsSinceEpoch").toInt(0);
        if (timestamp>_appTimestamp) {
            QDateTime date;
            date.setSecsSinceEpoch(timestamp);
            _newDate = date.toString("MMMM yyyy");
            _newVersion = obj.value("version").toString();
            _whatsnew = obj.value("whatsnew").toString();
            _newVersionAvailable = true;
            _target = obj.value("targets").toObject().value(_platform).toString();
            QStringRef filename = _target.midRef(_target.lastIndexOf('/')+1);
            _filePath = _downloadLocation+"/"+filename;
            qDebug() << "Downloading update file to: " << _filePath;
            emit triggerOpenSoftwareUpdate();
        }
        else {
            qDebug() << "No new version. Remote timestamp is " << timestamp << ", this app timestamp is " << _appTimestamp;
            _versionUptodate = true;
        }
        emit versionChecked();

    });

}

void SoftwareUpdate::setCheckAtStartup(bool check) {

    _checkAtStartup = check;
    QSettings settings;
    settings.setValue("checkAtStartup", check);

}

void SoftwareUpdate::onError(QNetworkReply::NetworkError code) {
    if (!_downloading) {
        qDebug() << "Got error signal but not downloading";
        return;
    }
    qDebug() << "Download error code: " << code;
    _reply->deleteLater();
    delete _file;
    _downloading = false;
    emit downloadingChanged(_downloading);
    emit error(QString("Erreur de chargement: %0").arg(code));
}

void SoftwareUpdate::onFinished() {
    if (!_downloading) {
        qDebug() << "Got finished signal but not downloading";
        return;
    }
    _file->flush();
    _file->close();
    _reply->deleteLater();
    delete _file;
    _downloading = false;
    emit downloadingChanged(_downloading);
    emit progress(100);

    qDebug() << "Finished with downloading " << _filePath;

    startUpdateExitApp();

}

void SoftwareUpdate::onReadyRead() {
    if (_downloading) _file->write(_reply->readAll());

}

void SoftwareUpdate::onDownloadProgress(qint64 bytesReceived, qint64 bytesTotal) {
    emit progress((bytesReceived*100)/bytesTotal);
}

void SoftwareUpdate::startUpdateExitApp() {

    if (_platform==COCOA_PLATFORM) {
        qDebug() << "Cocoa platform: open " << _filePath;
        QProcess* process = new QProcess();
        process->start("open", QStringList() << _filePath);
        process->waitForStarted();
        emit exitApp();
        return;
    }
    if (_platform==WINDOWS_PLATFORM) {
        qDebug() << "Windows platform: " << _filePath;
        QProcess* process = new QProcess();
        process->startDetached(_filePath);
        process->waitForStarted();
        emit exitApp();
        return;
    }
    if (_platform==XCB_PLATFORM) {
        qDebug() << "XCB platform: " << _filePath;
        QProcess* process = new QProcess();
        process->startDetached("file-roller", QStringList() << _filePath);
        return;

    }


}

void SoftwareUpdate::download() {

    if (_downloading) {
        qDebug() << "File already downloading";
        return;
    }

    _file = new QFile(_filePath);
    if (_file->exists()) _file->remove();
    if (!_file->open(QIODevice::WriteOnly)) {
        qWarning() << "Cannot create file: " << _filePath;
        delete _file;
        return;
    }
    _reply = _networkManager.get(QNetworkRequest(QUrl(_target)));
    connect(_reply, &QNetworkReply::downloadProgress, this, &SoftwareUpdate::onDownloadProgress);
    connect(_reply, &QNetworkReply::finished, this, &SoftwareUpdate::onFinished);
    connect(_reply, &QNetworkReply::readyRead, this, &SoftwareUpdate::onReadyRead);
    connect(_reply, static_cast<void (QNetworkReply::*)(QNetworkReply::NetworkError)>(&QNetworkReply::error), this, &SoftwareUpdate::onError);
    _downloading = true;
    emit progress(0);
    emit downloadingChanged(_downloading);

}

