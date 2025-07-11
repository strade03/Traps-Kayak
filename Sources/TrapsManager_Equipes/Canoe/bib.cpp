#include "bib.h"
#include <QHash>
#include <QJsonArray>
#include <QDateTime>
#include <QDebug>


Bib::Bib(int number) {
    init(number, ' ');
}

Bib::Bib(int number, char letter) {

    init(number, letter);
}

void Bib::init(int number, char letter) {

    _idNumber = number;
    _idLetter = letter;
    buildId();
    _startTime = 0;
    _finishTime = 0;
    _locked = false;
    _timestamp = 0;


}


Bib::Bib(const QString& id, const QJsonObject &json) :
    _id(id),
    _startTime(0),
    _finishTime(0),
    _locked(false),
    _timestamp(0)
{

    // id is 999 or 999A
    if (id.count()==3) {
        _idNumber = id.toInt();
        _idLetter = ' ';
    }
    else if (id.count()==4) {
        _idNumber = id.left(3).toInt();
        _idLetter = id.at(3).toLatin1();
    }
    else {
        _idNumber = 999;
        _idLetter = ' ';
    }
    buildId();
    _categ = json.value("categ").toString("?");
    _entry = json.value("entry").toInt(0);
    _schedule = json.value("schedule").toString("?");
    QJsonArray penaltyArray = json.value("penaltyList").toArray();
    for (int index=0; index<penaltyArray.count(); index++) {
        Penalty penalty(penaltyArray.at(index).toObject());
        setPenalty(penalty);
    }
}

void Bib::buildId() {
    _id = QString::number(_idNumber).rightJustified(3,'0');
    if (_idLetter!=' ') _id.append(_idLetter);
}


void Bib::setEntry(int entry) {
    _entry = entry;
}

bool Bib::setStartTime(qint64 startTime) {
    if (locked()) return false;
    _startTime = startTime;
    return true;
}

QString Bib::startTimeStr() const {
    if (_startTime<1) return "-";
    QDateTime time = QDateTime::fromMSecsSinceEpoch(_startTime);
    return time.toString("HH:mm:ss.zzz");
}

bool Bib::setFinishTime(qint64 finishTime) {
    if (locked()) return false;
    _finishTime = finishTime;
    return true;
}

QString Bib::finishTimeStr() const {
    if (_finishTime<1) return "-";
    QDateTime time = QDateTime::fromMSecsSinceEpoch(_finishTime);
    return time.toString("HH:mm:ss.zzz");
}

qint64 Bib::runningTime() const {
    if (_finishTime<1 || _startTime<1) return 0;
    qint64 value = _finishTime - _startTime;
    if (value<1) return 0;
    return value;
}

QString Bib::runningTimeStr() const {
    qint64 value = runningTime();
    if (value<1) return "-";
    QTime time = QTime(0,0).addMSecs(value);
    return time.toString("mm:ss.zzz");
}

bool Bib::setLapTime(int lap, int value) {
    if (locked()) return false;
    _lapTimeList.insert(lap, value);
    return true;
}


void Bib::setSchedule(const QString &schedule) {
    _schedule = schedule;
}

void Bib::setCateg(const QString &categ) {
    _categ = categ;
}

void Bib::setLocked(bool locked) {
    _locked = locked;
}

QHash<int, Penalty> Bib::penaltyList() const {
    return _penaltyList;
}

QStringList Bib::penaltyStringList() const {
    QStringList list;
    for (int gateId=1; gateId<=GATE_MAX_COUNT; gateId++) {
        list << _penaltyList.value(gateId).toString();
    }
    return list;
}

Penalty Bib::penaltyAtGate(int gateId) const {
    if (gateId<1 || gateId>GATE_MAX_COUNT) return Penalty();
    return _penaltyList.value(gateId);
}

bool Bib::setPenalty(const Penalty &penalty) {
    if (locked()) return false;
    int gate = penalty.gate();
    if (gate>0 && gate<=GATE_MAX_COUNT) {
        _penaltyList.remove(gate); // I don't know why but I need to remove first otherwise it's not replaced
        _penaltyList.insert(gate, penalty);
    }
    return true;
}

bool Bib::setPenalty(const QList<Penalty> &penaltyList) {
    if (locked()) return false;
    foreach (Penalty penalty, penaltyList) {
        setPenalty(penalty);
    }
    return true;
}

void Bib::clearPenalties() {
    _penaltyList.clear();
}

void Bib::clearChronos() {
    setStartTime(0);
    setFinishTime(0);
}

QString Bib::toString() {
    return _id+"|"+_categ+"|"+_schedule;
}

QJsonObject Bib::jsonParam() const {
    QJsonObject obj;
    obj.insert("categ", _categ);
    obj.insert("schedule", _schedule);
    obj.insert("entry", _entry);
    return obj;
}

QJsonObject Bib::jsonLock() const {
    QJsonObject obj;
    obj.insert("locked", _locked);
    return obj;
}

QJsonObject Bib::jsonTime(qint64 timestamp) const {
    QJsonObject obj;
    obj.insert("startTime", _startTime);
    obj.insert("finishTime", _finishTime);
    return obj;
}

QJsonObject Bib::jsonPenalty(qint64 timestamp) const {
    QJsonObject obj;
    QJsonArray array;
    foreach (Penalty penalty, _penaltyList) {
        if (penalty.timestamp()==0 ||
                penalty.timestamp()>timestamp)
            array.append(penalty.jsonObject());
    }
    obj.insert("penaltyList", array);
    return obj;
}

