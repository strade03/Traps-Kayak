#ifndef TCPSERVER_H
#define TCPSERVER_H

#include <QTcpServer>

#include "connectionhandlerpool.h"
#include "Canoe/biblist.h"

class TCPServer : public QTcpServer {

    Q_OBJECT

public:
    explicit TCPServer(QObject *bibList);

signals:

    void serverStarted(int port);
    void startFailure();

public slots:

    void start(QString host, int port=0);
    
protected:

    /** Serves new incoming connection requests */
    void incomingConnection(qintptr socketDescriptor);

private:

    ConnectionHandlerPool _pool;

};

#endif // TCPSERVER_H
