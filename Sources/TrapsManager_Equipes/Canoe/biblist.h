#ifndef BIBLIST_H
#define BIBLIST_H

#include <QObject>
#include <QAbstractListModel>
#include "bib.h"
#include <QJsonArray>
#include "Database/database.h"
#include "penaltylistmodel.h"

#define BIB_ID_NAME "bibData"
#define BIB_FINISHTIME_NAME "finishTimeData"
#define BIB_STARTTIME_NAME "startTimeData"
#define BIB_RUNNINGTIME_NAME "runningTimeData"
#define BIB_SCHEDULE_NAME "scheduleData"
#define BIB_CATEG_NAME "categData"
#define BIB_LOCKED_NAME "lockedData"


class BibList : public QAbstractListModel
{

    Q_OBJECT

    Q_PROPERTY(int scheduling READ scheduling CONSTANT)

public:

    enum {
        BibId = Qt::UserRole,
        BibStartTime,
        BibFinishTime,
        BibRunningTime,
        BibSchedule,
        BibCateg,
        BibLocked
    };

    explicit BibList();

    QAbstractListModel* penaltyListModel() const { return (QAbstractListModel*)&_penaltyListModel;}
    int bibCount() const;
    Bib* bibAtIndex(int index) const;
    Bib* bibWithId(const QString& bibId) const;
    Bib* bibWithIdNumber(int id) const;
    int bibIndex(const QString& bibId) const;
    int bibIndex(int bibnumber) const;
    int scheduling() const { return _scheduling; }

signals:
    void error(QString title, QString message);
    void toast(QString text, int delay);
    void bibCountChanged(int);
    // used to send data to third parties
    void penaltyReceived(int bib, int gateId, int value); // bib, gate, value
    void chronoReceived(int bib, int value); // bib, value

public slots:

    void processIncomingPenaltyList(QList<Penalty> penaltyList);
    void processIncomingPenalty(int, QHash<int, int>);
    void processIncomingStartTime(int bibnumber, qint64 startTime);
    void processIncomingFinishTime(int bibnumber, qint64 finishTime);
    void processIncomingLapTime(int bibnumber, int lap, qint64 time);

    void selectPenalty(int bibIndex, int gateIndex);
    void setScheduling(int criteria);
    void lock(int firstRow, int lastRow);
    void unlock(int firstRow, int lastRow);
    void forwardBib(int firstRow, int lastRow);

    QJsonArray jsonArray(qint64 timestamp) const;
    QList<int> bibNumberList() const;

    void processPCE(const QString& filename, bool reset=true);
    void processTXT(const QString& filename, bool reset=true);
    void clearPenalties();
    void clearChronos();

private:

    QList<Bib*> _bibList;
    PenaltyListModel _penaltyListModel;

    Database _db;
    int _scheduling;

    void rebuildPenaltyList();

    static bool numberLessThan(Bib* bib1, Bib* bib2);
    static bool entryLessThan(Bib* bib1, Bib* bib2);
    static bool scheduleLessThan(Bib* bib1, Bib* bib2);

    void reloadFromDataBase();
    void orderBibList();



public:
    int rowCount(const QModelIndex &parent) const;
    QVariant data(const QModelIndex &index, int role) const;
    QHash<int, QByteArray> roleNames() const;

};

#endif // BIBLIST_H
