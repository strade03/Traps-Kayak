#ifndef FFCANOE_H
#define FFCANOE_H

#include <QObject>
#include "ffcanoeconnector.h"
#include <QTimer>
#include <QThread>

class FFCanoe : public QObject
{
    Q_OBJECT

    Q_PROPERTY(bool forwardPenalty READ forwardPenalty CONSTANT)
    Q_PROPERTY(bool forwardTime READ forwardTime CONSTANT)
    Q_PROPERTY(QString host READ host CONSTANT)
    Q_PROPERTY(int port READ port CONSTANT)
    Q_PROPERTY(int runId READ runId CONSTANT)
    Q_PROPERTY(bool localHost READ localHost CONSTANT)
    Q_PROPERTY(int buffer READ buffer NOTIFY bufferChanged)


public:
    explicit FFCanoe();
    ~FFCanoe();

    bool forwardPenalty() const { return _forwardPenalty; }
    bool forwardTime() const { return _forwardTime; }
    QString host() const { return _host; }
    int port() const { return _port; }
    int runId() const { return _runId; }
    bool localHost() const { return _localHost; }
    int buffer() const { return _buffer; }
    void exit();

signals:

    void connectToServer(QString host, int port, int runId);
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
    void setRunId(int runId);
    void setLocalHost(bool);

    void allowForwardPenalty(bool);
    void allowForwardTime(bool);

    void requestConnection(bool value);
    void setConnected(bool connected);
    void penaltySent();

    void sendPenalty(int bib, int gateId, int penalty);
    void sendTime(int bib, int time);

private:


    bool _forwardPenalty;
    bool _forwardTime;
    bool _connected;
    QString _host;
    int _port;
    int _runId;
    bool _localHost;
    int _buffer;

    FFCanoeConnector _connector;
    QThread _connectorThread;



};

#endif // FFCANOE_H
