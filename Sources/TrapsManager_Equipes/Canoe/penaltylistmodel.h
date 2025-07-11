#ifndef PENALTYLISTMODEL_H
#define PENALTYLISTMODEL_H

#include <QObject>
#include <QAbstractListModel>
#include <QList>
#include "penalty.h"

#define PENALTY_VALUE_NAME "penaltyValue"

class PenaltyListModel : public QAbstractListModel
{

    Q_OBJECT

public:

    enum {
        PenaltyValue = Qt::UserRole

    };

    PenaltyListModel();
    void reset(const QStringList& stringList);
    void setPenalty(int index, const QString& penaltyValue);
    void refresh(int firstIndex, int lastIndex);


    // QAbstractItemModel interface
public:
    int rowCount(const QModelIndex &parent) const;
    QVariant data(const QModelIndex &index, int role) const;
    QHash<int, QByteArray> roleNames() const;

private:

    QStringList _list;

};

#endif // PENALTYLISTMODEL_H
