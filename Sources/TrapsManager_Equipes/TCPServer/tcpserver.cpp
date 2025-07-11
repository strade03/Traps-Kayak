#include "tcpserver.h"
#include <QTcpSocket>
#include "QDataStream"
#include "connectionhandler.h"


TCPServer::TCPServer(QObject* bibList) : QTcpServer(),
    _pool(bibList)
{

}

void TCPServer::start(QString host, int port) {
    listen(QHostAddress(host), port);
    int actualPort = serverPort();
    if (actualPort==0) {
        emit startFailure();
        return;
    }
    emit serverStarted(actualPort);
}

void TCPServer::incomingConnection(qintptr sd) {

    qDebug() << "Incoming connection !";
    ConnectionHandler* freeHandler = _pool.getConnectionHandler();

    // Let the handler process the new connection.
    if (freeHandler) {
        QMetaObject::invokeMethod(freeHandler, "handleConnection", Qt::QueuedConnection,Q_ARG(int, sd));
    }
    else {
        // Reject the connection
        qDebug("SocketListener: Too many incoming connections");

    }

}




