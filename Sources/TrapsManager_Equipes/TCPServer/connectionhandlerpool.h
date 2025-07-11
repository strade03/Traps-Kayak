#ifndef CONNECTIONHANDLERPOOL_H
#define CONNECTIONHANDLERPOOL_H

#include <QObject>
#include <QTimer>
#include <QMutex>
#include "connectionhandler.h"

class ConnectionHandlerPool : public QObject {
    Q_OBJECT

public:
    explicit ConnectionHandlerPool(QObject* bibList);
    
    /** Destructor */
    virtual ~ConnectionHandlerPool();

    /** Get a free connection handler, or 0 if not available. */
    ConnectionHandler* getConnectionHandler();

private:

    QObject* _bibList;

    /** Pool of connection handlers */
    QList<ConnectionHandler*> _pool;

    /** Timer to clean-up unused connection handler */
    QTimer _cleanupTimer;

    /** Used to synchronize threads */
    QMutex _mutex;


private slots:

    /** Received from the clean-up timer.  */
    void cleanup();


};

#endif // CONNECTIONHANDLERPOOL_H
