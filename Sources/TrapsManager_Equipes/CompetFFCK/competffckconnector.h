#ifndef COMPETFFCKCONNECTOR_H
#define COMPETFFCKCONNECTOR_H

#include <QObject>
#include <QTcpSocket>
#include <QAbstractSocket>

class CompetFFCKConnector : public QObject
{
    Q_OBJECT
public:
    explicit CompetFFCKConnector(QObject *parent = nullptr);


signals:
    void connectedToServer(bool connected);
    void error(QString title, QString message);
    void penaltySent();
    void chronoSent();

public slots:

    void connectToServer(const QString& host, int port);
    void disconnectFromServer();
    void errorHandler(QAbstractSocket::SocketError);
    void sendPenalty(int bib, int gateId, int penalty);
    void sendTime(int bib, int chrono); // in milliseconds


private:

    QTcpSocket* _socket;
    bool _connected;

};

#endif // COMPETFFCKCONNECTOR_H
