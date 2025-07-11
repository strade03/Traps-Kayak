#include "global.h"
#include <QDir>
#include <QStandardPaths>
#include <QDebug>

#define HTTPSERVER_FILENAME "HTTPServer.ini"

QString Global::appDataDir = QString();
QString Global::httpConfigFile = QString();
QString Global::docroot = QString();


void Global::init() {

    appDataDir = createDir(QStandardPaths::writableLocation(QStandardPaths::AppDataLocation));
    qInfo() << "App data location: " << appDataDir;
    docroot = createDir(appDataDir+"/docroot");
    httpConfigFile = appDataDir + "/" + HTTPSERVER_FILENAME;

    installFile("config/HTTPServer.ini", httpConfigFile);
    installFile("docroot/index.html", docroot+"/index.html", true);
    installFile("docroot/traps250x250.png", docroot+"/traps250x250.png", true);
    installFile("docroot/penaltyposter.html", docroot+"/penaltyposter.html", true);

}


void Global::installFile(const QString &qrcSourceFilename, const QString &destinationFilename, bool forceInstall) {

    if (!QFile::exists(destinationFilename) || forceInstall) {
        QFile::remove(destinationFilename);
        QFile::copy(":/"+qrcSourceFilename, destinationFilename);
        QFile::setPermissions(destinationFilename,
                    QFileDevice::ReadOwner|
                    QFileDevice::WriteOwner|
                    QFileDevice::ReadUser|
                    QFileDevice::WriteUser|
                    QFileDevice::ReadGroup|
                    QFileDevice::WriteGroup|
                    QFileDevice::ReadOther|
                    QFileDevice::WriteOther
                              );
    }

}

QString Global::createDir(const QString &dirPath) {

    QDir dir(dirPath);
    if (!dir.exists()) {
        qDebug() << "Creating " << dir.absolutePath();
        bool ok = QDir().mkpath(dir.absolutePath());
        if (!ok) qDebug() << "Cannot create " << dir.absolutePath();
    }
    return dir.absolutePath();

}
