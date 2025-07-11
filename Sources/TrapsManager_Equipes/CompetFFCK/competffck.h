#ifndef COMPETFFCK_H
#define COMPETFFCK_H

#include <QObject>
#include <CompetFFCK/competffckconnector.h>
#include <QThread>

class CompetFFCK : public QObject
{
    Q_OBJECT

    Q_PROPERTY(bool forwardPenalty READ forwardPenalty CONSTANT)
    Q_PROPERTY(bool forwardTime READ forwardTime CONSTANT)
    Q_PROPERTY(QString host READ host CONSTANT)
    Q_PROPERTY(int port READ port CONSTANT)
    Q_PROPERTY(int buffer READ buffer NOTIFY bufferChanged)

public:
    explicit CompetFFCK(QObject *parent = nullptr);

    bool forwardPenalty() const { return _forwardPenalty; }
    bool forwardTime() const { return _forwardTime; }
    QString host() const { return _host; }
    int port() const { return _port; }
    int buffer() const { return _buffer; }
    void exit();

signals:

    void connectToServer(QString host, int port);
    void disconnectFromServer();
    void connectedToTarget();
    void disconnectedFromTarget();
    void connecting();
    void errorChanged(QString error);
    void sendPenaltyToServer(int bib, int gateId, int penalty);
    void sendTimeToServer(int bib, int time);
    void error(QString title, QString message);
    void toast(QString message, int delay);
    void bufferChanged(int);

public slots:

    // called from QML
    void setHost(QString inputString);
    void setPort(QString inputString);

    void allowForwardPenalty(bool allow);
    void allowForwardTime(bool allow);

    void requestConnection(bool value);
    void setConnected(bool connected);
    void penaltySent();

    void sendPenalty(int bib, int gateId, int penalty);
    void sendTime(int bib, int time);


private:

    bool _connectedToTarget;
    CompetFFCKConnector _connector;
    QThread _connectorThread;

    bool _forwardPenalty;
    bool _forwardTime;
    bool _connected;
    QString _host;
    int _port;
    int _runId;
    int _buffer;


};

#endif // COMPETFFCK_H
