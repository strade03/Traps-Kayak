#include "hellobroadcaster.h"
#include <QDebug>
#include <QUdpSocket>
#include <QDateTime>
#include <QSettings>
#include <QNetworkInterface>


#define BROADCAST_INTERVAL 3000

HelloBroadcaster::HelloBroadcaster() : QThread(),
    _tcpPort(0),
    _httpPort(0)
{

    moveToThread(this);
    _timer.moveToThread(this);
    _socket.moveToThread(this);
    connect(&_timer, SIGNAL(timeout()), SLOT(broadcast()));
    QObject::connect(this, &QThread::finished, &_timer, &QTimer::stop);
    QObject::connect(this, &QThread::finished, &_socket, &QUdpSocket::close);
    this->start();

}


void HelloBroadcaster::configure() {

    if (_address.isEmpty() || _tcpPort<1) return; // I need at least address + tcp port

    qDebug() << "Processing configure UDP address: " << _address << " TCP port: " << _tcpPort << " HTTP port: "<< _httpPort;
    _localNetwork = QHostAddress(QHostAddress(_address).toIPv4Address() | 0xFF);
    _timestamp = QDateTime::currentMSecsSinceEpoch() / 1000;
    _udpString = QString::number(_timestamp);
    _udpString.append(',');
    _udpString.append(_address);
    _udpString.append(',');
    _udpString.append(QString::number(_tcpPort));
    _udpString.append(',');
    _udpString.append(QString::number(_httpPort));
    _timer.start(BROADCAST_INTERVAL);

}

void HelloBroadcaster::broadcast() {

    //qDebug() << "Broadcasting: " << _udpString;
    emit sayHello();
    qint64 byteCount = _socket.writeDatagram(_udpString.toLocal8Bit().constData(), _localNetwork, UDP_PORT);
    if (byteCount<0) emit broadcastError();

}

QStringList HelloBroadcaster::localAddressList() {

    QStringList list;
    QList<QNetworkInterface> networkInterfaceList = QNetworkInterface::allInterfaces();
    foreach (QNetworkInterface interface, networkInterfaceList) {

         // if interface is up and running and is not loopback
        if ((interface.flags() & (QNetworkInterface::IsRunning | QNetworkInterface::IsUp)) &&
            !(interface.flags() & QNetworkInterface::IsLoopBack)) {

            QList<QNetworkAddressEntry> addressList = interface.addressEntries();
            foreach (QNetworkAddressEntry addressEntry, addressList) {
                QHostAddress hostAddress = addressEntry.ip();
                if (hostAddress.toIPv4Address()) list.append(hostAddress.toString());
            }

        }
    }
    return list;

}

void HelloBroadcaster::setAddress(const QString &address) {
    _address = address;
    configure();
}

void HelloBroadcaster::setTcpPort(int tcpPort) {
    _tcpPort = tcpPort;
    configure();
}

void HelloBroadcaster::setHttpPort(int httpPort) {
    _httpPort = httpPort;
    configure();
}
