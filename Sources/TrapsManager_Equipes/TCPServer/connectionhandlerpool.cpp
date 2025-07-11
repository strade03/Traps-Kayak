#include "connectionhandlerpool.h"
#include "connectionhandler.h"

#define CLEANUP_TIMER 1000 // 3 min
#define MAX_THREAD 100
#define MIN_THREAD 1

ConnectionHandlerPool::ConnectionHandlerPool(QObject *bibList) : QObject(),
    _bibList(bibList)
{

    _cleanupTimer.start(CLEANUP_TIMER);
    connect(&_cleanupTimer, SIGNAL(timeout()), SLOT(cleanup()));
}

ConnectionHandlerPool::~ConnectionHandlerPool() {
    // delete all connection handlers and wait
    foreach(ConnectionHandler* handler, _pool) {
        delete handler;
    }
    qDebug("ConnectionHandlerPool (%p): destroyed", this);
}


ConnectionHandler* ConnectionHandlerPool::getConnectionHandler() {
    ConnectionHandler* freeHandler=0;
    _mutex.lock();
    // find a free handler in pool
    foreach(ConnectionHandler* handler, _pool) {
        if (!handler->isBusy()) {
            freeHandler=handler;
            freeHandler->setBusy();
            qDebug() << "Reusing handler " << freeHandler;
            break;
        }
    }
    // create a new handler, if necessary
    if (!freeHandler) {
        int maxConnectionHandlers=MAX_THREAD;
        if (_pool.count()<maxConnectionHandlers) {
            freeHandler = new ConnectionHandler(_bibList);
            connect(freeHandler, SIGNAL(incomingPenalty(int,QHash<int,int>)), _bibList, SLOT(processIncomingPenalty(int,QHash<int,int>)));
            connect(freeHandler, SIGNAL(incomingStartTime(int,qint64)), _bibList, SLOT(processIncomingStartTime(int,qint64)));
            connect(freeHandler, SIGNAL(incomingFinishTime(int,qint64)), _bibList, SLOT(processIncomingFinishTime(int,qint64)));
            connect(freeHandler, SIGNAL(incomingLapTime(int,int,qint64)), _bibList, SLOT(processIncomingLapTime(int,int,qint64)));
            freeHandler->setBusy();
            _pool.append(freeHandler);
            qDebug() << "Creating new handler " << freeHandler;
        }
    }
    _mutex.unlock();
    qDebug() << "Handler pool size is " << _pool.size();
    return freeHandler;
}



void ConnectionHandlerPool::cleanup() {
    int maxIdleHandlers=MIN_THREAD;
    int idleCounter=0;
    _mutex.lock();
    foreach(ConnectionHandler* handler, _pool) {
        if (!handler->isBusy()) {
            if (++idleCounter > maxIdleHandlers) {
                _pool.removeOne(handler);
                delete handler;
                qDebug("ConnectionHandlerPool: Removed connection handler (%p), pool size is now %i",handler,_pool.size());
                break; // remove only one handler in each interval
            }
        }
    }
    _mutex.unlock();
}
