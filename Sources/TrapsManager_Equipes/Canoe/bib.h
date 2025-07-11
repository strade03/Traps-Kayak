#ifndef BIB_H
#define BIB_H

#include "penalty.h"
#include <QList>
#include <QHash>

#define GATE_MAX_COUNT 75

class Bib {

public:

    explicit Bib(int number);
    explicit Bib(int number, char letter);

    // used when loading from db
    explicit Bib(const QString &id, const QJsonObject& json);

    void init(int number, char letter);

    int entry() const { return _entry; }
    void setEntry(int entry);

    qint64 startTime() const { return _startTime; }
    bool setStartTime(qint64 startTime);
    QString startTimeStr() const;

    qint64 finishTime() const { return _finishTime; }
    bool setFinishTime(qint64 finishTime);
    QString finishTimeStr() const;

    qint64 runningTime() const;
    QString runningTimeStr() const;

    int timestamp() const { return _timestamp; }
    void setTimestamp(int timestamp);

    QHash<int, qint64> lapTimeList() const { return _lapTimeList; }
    bool setLapTime(int lap, int value); // value in 100th of sec

    QString schedule() const { return _schedule; }
    void setSchedule(const QString &schedule);

    QString categ() const { return _categ; }
    void setCateg(const QString &categ);

    bool locked() const { return _locked; }
    void setLocked(bool locked);

    QString id() const { return _id; }
    int idNumber() const { return _idNumber; }

    QJsonObject jsonParam() const;
    QJsonObject jsonLock() const;
    QJsonObject jsonTime(qint64 timestamp = 0) const;
    QJsonObject jsonPenalty(qint64 timestamp = 0) const;

    QHash<int, Penalty> penaltyList() const;
    QStringList penaltyStringList() const;
    Penalty penaltyAtGate(int gateId) const;
    bool setPenalty(const Penalty& penalty); // returns false if bib is locked
    bool setPenalty(const QList<Penalty>& penaltyList); // returns false if bib is locked
    void clearPenalties();
    void clearChronos();

    QString toString();

private:

    QString _id;
    int _idNumber;
    char _idLetter;
    int _entry;
    qint64 _startTime;
    qint64 _finishTime;
    QHash<int, Penalty> _penaltyList;
    QHash<int, qint64> _lapTimeList;
    QString _schedule;
    QString _categ;
    bool _locked;
    int _timestamp;

    void init();
    void buildId();


};

#endif // BIB_H
