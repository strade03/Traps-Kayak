#ifndef HELLOBROADCASTER_H
#define HELLOBROADCASTER_H

#include <QThread>
#include <QTimer>
#include <QUdpSocket>

#define UDP_PORT 5432

class HelloBroadcaster : public QThread {
    Q_OBJECT
public:
    HelloBroadcaster();
    static QStringList localAddressList();
    
signals:

    void broadcastError();   
    void sayHello();

public slots:
    void setAddress(const QString& address);
    void setTcpPort(int tcpPort);
    void setHttpPort(int httpPort);

private slots:

    void broadcast();


private:

    QTimer _timer;
    QUdpSocket _socket;
    QString _address;
    QHostAddress _localNetwork;
    int _tcpPort;
    int _httpPort;

    long _timestamp; // in seconds !
    QString _udpString;

    void configure();

};

#endif // HELLOBROADCASTER_H
