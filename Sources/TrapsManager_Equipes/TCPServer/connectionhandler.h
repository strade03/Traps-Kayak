#ifndef CONNECTIONHANDLER_H
#define CONNECTIONHANDLER_H

#include <QObject>
#include <QTcpSocket>
#include <QTimer>
#include <QThread>
#include "Canoe/biblist.h"

class ConnectionHandler : public QThread {
    Q_OBJECT
public:
    explicit ConnectionHandler(QObject *bibListProvider);
    
    virtual ~ConnectionHandler();
    bool isBusy();
    void setBusy();


signals:

    // bib number, gate. value
    void incomingPenalty(int, QHash<int, int>);
    // bib number, 0=start, 1=finish, value
    void incomingStartTime(int, qint64); // bib, value
    void incomingFinishTime(int, qint64); // bib, value
    void incomingLapTime(int, int, qint64); // bib, lap number, value


private:

    QTcpSocket _socket;
    QTimer _readTimer;
    QByteArray _byteArray;
    bool _busy;

    void run();
    void replyError();

public slots:

    void handleConnection(int sd);

private slots:

    void readTimeout();
    void read();
    void disconnected();

private:
    bool readByteArray();
    QObject* _bibListProvider;

};
    
#endif // CONNECTIONHANDLER_H
