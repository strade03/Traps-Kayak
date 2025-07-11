#include <QGuiApplication>
#include <QQmlApplicationEngine>
//#include "Cloud/cloud.h"
#include <QByteArray>
#include <QTime>
#include <QDir>
#include "staticfilecontroller.h"
#include "filelogger.h"
//#include "httplistener.h"
//#include "HTTPServer/requestmapper.h"
#include "global.h"
#include "Update/softwareupdate.h"
#include "Network/hellobroadcaster.h"
#include "Canoe/penalty.h"
#include "Canoe/biblist.h"
#include <QQmlContext>
#include "FFCanoe/ffcanoe.h"
#include "Database/database.h"
#include "HTTPServer/controller/penaltyrequestprocessor.h"
#include "QtWebApp/httpserver/httplistener.h"
#include "TCPServer/tcpserver.h"
#include "View/viewcontroller.h"
#include "CompetFFCK/competffckconnector.h"
#include "CompetFFCK/competffck.h"
#include <QThread>
#include <QStandardPaths>

// Controller for static files
stefanfrings::StaticFileController* staticFileController;

// Redirects log messages to a file
stefanfrings::FileLogger* logger;

void messageHandler(QtMsgType type, const QMessageLogContext &context, const QString &msg) {
    QByteArray localMsg = msg.toLocal8Bit();
    QByteArray stringDate = QTime::currentTime().toString("hh:mm:ss.zzz").toLocal8Bit();
    switch (type) {
    case QtDebugMsg:
        fprintf(stdout, "DEBUG|%s|%s,%u|%s\n", stringDate.constData(), context.file, context.line, localMsg.constData());
        fflush(stdout);
        break;
    case QtInfoMsg:
        fprintf(stdout, "INFO|%s|%s,%u|%s\n", stringDate.constData(), context.file, context.line, localMsg.constData());
        fflush(stdout);
        break;
    case QtWarningMsg:
        fprintf(stdout, "WARNING|%s|%s,%u|%s\n", stringDate.constData(), context.file, context.line, localMsg.constData());
        fflush(stdout);
        break;
    case QtCriticalMsg:
        fprintf(stderr, "CRITICAL|%s|%s,%u|%s\n", stringDate.constData(), context.file, context.line, localMsg.constData());
        fflush(stderr);
        break;
    case QtFatalMsg:
        fprintf(stderr, "FATAL|%s|%s,%u|%s\n", stringDate.constData(), context.file, context.line, localMsg.constData());
        fflush(stderr);
        abort();
    }
    fflush(stderr);
}


int main(int argc, char *argv[]) {

    QGuiApplication app(argc, argv);
    QGuiApplication::setOrganizationName("TRAPS");
    QGuiApplication::setOrganizationDomain("traps-ck.com");
    QGuiApplication::setApplicationName("TRAPSManager4");

    qInfo() << "Platform name: " << QGuiApplication::platformName();

    qInstallMessageHandler(messageHandler);
    // the following data structures can be transported with signal / slot
    qRegisterMetaType< QList<Penalty> >("QList<Penalty>");
    qRegisterMetaType< QList<int> >("QList<int>");
    qRegisterMetaType< QHash<int,int> >("QHash<int,int>");

    Global::init();
    QSettings customSettings(QString("%0/traps.ini").arg(Global::appDataDir), QSettings::IniFormat);
    int tcpPort = customSettings.value("tcp_port", QVariant(0)).toInt();
    qInfo() << "Requested port: " << tcpPort;


    // Configure static file controller
//    QSettings* fileSettings=new QSettings(Global::httpConfigFile,QSettings::IniFormat,&app);
//    fileSettings->beginGroup("docroot");
//    staticFileController=new stefanfrings::StaticFileController(fileSettings,&app);

//    // Configure and start the TCP listener
//    QSettings* listenerSettings=new QSettings(Global::httpConfigFile,QSettings::IniFormat,&app);
//    listenerSettings->beginGroup("listener");
//    stefanfrings::HttpListener* httpListener = new stefanfrings::HttpListener(listenerSettings,new RequestMapper(&app),&app);
//    qInfo() << "HTTP server listening to port " << httpListener->serverPort();

    // HelloBroacaster
    HelloBroadcaster hello;

    FFCanoe ffcanoe;
    CompetFFCK competFFCK;

    BibList bibList;

    QObject::connect(&bibList, &BibList::penaltyReceived, &ffcanoe, &FFCanoe::sendPenalty);
    QObject::connect(&bibList, &BibList::chronoReceived, &ffcanoe, &FFCanoe::sendTime);
    QObject::connect(&bibList, &BibList::penaltyReceived, &competFFCK, &CompetFFCK::sendPenalty);
    QObject::connect(&bibList, &BibList::chronoReceived, &competFFCK, &CompetFFCK::sendTime);


    QStringList hostList = HelloBroadcaster::localAddressList();

    qInfo() << "Available ip addresses on this machine: " << hostList.join(',');

    QThread tcpServerThread;
    TCPServer tcpServer(&bibList);
    QObject::connect(&tcpServerThread, &QThread::finished, &tcpServer, &QTcpServer::close);
    tcpServer.moveToThread(&tcpServerThread);

    SoftwareUpdate softwareUpdate(QGuiApplication::platformName(), QStandardPaths::writableLocation(QStandardPaths::DownloadLocation));
    ViewController viewController(hostList, tcpPort);

    // ViewController - softwareUpdater
    QObject::connect(&viewController, &ViewController::checknewVersion, &softwareUpdate, &SoftwareUpdate::checknewVersion);
    QObject::connect(&softwareUpdate, &SoftwareUpdate::triggerOpenSoftwareUpdate, &viewController, &ViewController::openSoftwareUpdate);
    QObject::connect(&softwareUpdate, &SoftwareUpdate::exitApp, &app, &QGuiApplication::quit);

    // ViewController - Application
    QObject::connect(&viewController, &ViewController::quit, &app, &QGuiApplication::quit);

    // ViewController - BibList
    QObject::connect(&viewController, &ViewController::requestPCE, &bibList, &BibList::processPCE);
    QObject::connect(&viewController, &ViewController::requestTXT, &bibList, &BibList::processTXT);
    QObject::connect(&viewController, &ViewController::requestPenaltyClear, &bibList, &BibList::clearPenalties);
    QObject::connect(&viewController, &ViewController::requestChronoClear, &bibList, &BibList::clearChronos);

    // ViewController - TCPServer
    QObject::connect(&viewController, &ViewController::requestTcpServer, &tcpServer, &TCPServer::start);

    // TCPServer - Viewcontroller
    QObject::connect(&tcpServer, &TCPServer::serverStarted, &viewController, &ViewController::setTcpPort);
    QObject::connect(&tcpServer, &TCPServer::serverStarted, &hello, &HelloBroadcaster::setTcpPort);

    QObject::connect(&tcpServer, &TCPServer::startFailure, &viewController, &ViewController::tcpServerStarFailure);

    // BibList - ViewController
    QObject::connect(&bibList, &BibList::bibCountChanged, &viewController, &ViewController::setBibCount);
    QObject::connect(&bibList, &BibList::error, &viewController, &ViewController::printError);
    QObject::connect(&bibList, &BibList::toast, &viewController, &ViewController::toast);

    // FFcanoe - ViewController
    QObject::connect(&ffcanoe, &FFCanoe::toast, &viewController, &ViewController::toast);
    QObject::connect(&ffcanoe, &FFCanoe::error, &viewController, &ViewController::printError);

    // CompetFFCK - ViewController
    QObject::connect(&competFFCK, &CompetFFCK::toast, &viewController, &ViewController::toast);
    QObject::connect(&competFFCK, &CompetFFCK::error, &viewController, &ViewController::printError);

    // Hello broadcaster - ViewController
    QObject::connect(&hello, &HelloBroadcaster::broadcastError, &viewController, &ViewController::broadcastError);
    QObject::connect(&hello, &HelloBroadcaster::sayHello, &viewController, &ViewController::watchdog);
    QObject::connect(&viewController, &ViewController::selectedAddress, &hello, &HelloBroadcaster::setAddress);


    QQmlApplicationEngine engine;
    engine.rootContext()->setContextProperty("bibList", &bibList);
    engine.rootContext()->setContextProperty("penaltyListModel", bibList.penaltyListModel());
    engine.rootContext()->setContextProperty("ffcanoe", &ffcanoe);
    engine.rootContext()->setContextProperty("competFFCK", &competFFCK);
    engine.rootContext()->setContextProperty("softwareupdate", &softwareUpdate);
    engine.rootContext()->setContextProperty("viewcontroller", &viewController);
    engine.load(QUrl(QStringLiteral("qrc:/qml/main.qml")));

    tcpServerThread.start();
    viewController.setBibCount(bibList.bibCount());

    app.exec();

    hello.exit();
    ffcanoe.exit();
    competFFCK.exit();
    tcpServerThread.exit();

    return 0;
}

