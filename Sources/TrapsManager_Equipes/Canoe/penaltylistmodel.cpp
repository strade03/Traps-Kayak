#include "penaltylistmodel.h"
#include <QDebug>

PenaltyListModel::PenaltyListModel()
{

}

void PenaltyListModel::reset(const QStringList &stringList) {
    beginResetModel();
    _list = stringList;
    endResetModel();

}

void PenaltyListModel::setPenalty(int index, const QString &penaltyValue) {
    if (index<0 || index>_list.count()) {
        qDebug() << "Index out of range: " << index;
        return;
    }
    _list.replace(index, penaltyValue);
}

void PenaltyListModel::refresh(int firstIndex, int lastIndex) {
    emit dataChanged(createIndex(firstIndex, 0), createIndex(lastIndex, 0));
}

int PenaltyListModel::rowCount(const QModelIndex &parent) const {
    int rowCount = _list.count();
    return rowCount;
}

QVariant PenaltyListModel::data(const QModelIndex &index, int role) const {
    int row = index.row();
    switch (role) {
        case PenaltyListModel::PenaltyValue : return QVariant(_list.value(row));
    }
}

QHash<int, QByteArray> PenaltyListModel::roleNames() const {
    QHash<int, QByteArray> hash;
    hash.insert(PenaltyListModel::PenaltyValue, PENALTY_VALUE_NAME);
    return hash;
}
