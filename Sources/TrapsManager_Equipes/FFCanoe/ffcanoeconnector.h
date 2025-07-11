#ifndef FFCANOECONNECTOR_H
#define FFCANOECONNECTOR_H

#include <QTcpSocket>

// delay between each penalty sent to ffcanoe
#define FFCANOE_DELAY 500

class FFCanoeConnector : public QObject {

    Q_OBJECT

public:
    explicit FFCanoeConnector();


signals:
    void connectedToServer(bool connected);
    void error(QString title, QString message);
    void penaltySent();
    void chronoSent();


public slots:
    void sendPenalty(int bib, int gateId, int penalty);
    void sendTime(int bib, int time);
    void connectToServer(const QString &host, int port, int runId);
    void disconnectFromServer();
    void errorHandler(QAbstractSocket::SocketError socketError);

    
private:

    QTcpSocket* _socket;
    bool _connected;
    int _runId;

};

#endif // FFCANOECONNECTOR_H
