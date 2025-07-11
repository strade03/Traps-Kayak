#ifndef EVENT_H
#define EVENT_H

#include <QObject>

class Event : public QObject
{
    Q_OBJECT
public:
    explicit Event(QObject *parent = 0);

signals:

public slots:
};

#endif // EVENT_H
