#include "penalty.h"
#include <QDateTime>

Penalty::Penalty() :
    _value(-1),
    _canvas(false),
    _teammate(false),
    _gate(-1)
{
    _spot1.x = -1;
    _spot1.y = -1;
    _spot2.x = -1;
    _spot2.y = -1;
    _timestamp = QDateTime::currentSecsSinceEpoch();

}

Penalty::Penalty(const QString &bib, int gate) :
    _bib(bib),
    _value(-1),
    _canvas(false),
    _teammate(false),
    _gate(gate)
{
    _spot1.x = -1;
    _spot1.y = -1;
    _spot2.x = -1;
    _spot2.y = -1;
    _timestamp = QDateTime::currentSecsSinceEpoch();

}

Penalty::Penalty(const QString &bib, int gate, int value) :
    _bib(bib),
    _value(value),
    _canvas(false),
    _teammate(false),
    _gate(gate)
{
    _spot1.x = -1;
    _spot1.y = -1;
    _spot2.x = -1;
    _spot2.y = -1;
    _timestamp = QDateTime::currentSecsSinceEpoch();

}

Penalty::Penalty(const QJsonObject &json) :
    _value(-1),
    _canvas(false),
    _teammate(false),
    _gate(-1)
{

    _bib = json.value("bib").toString();
    _gate = json.value("gate").toInt();
    _value = json.value("value").toInt();
    _teammate = json.value("teammate").toBool();
    _canvas = json.value("canvas").toBool();
    _spot1.x = json.value("xspot1").toInt(-1);
    _spot1.y = json.value("yspot1").toInt(-1);
    _spot2.x = json.value("xspot2").toInt(-1);
    _spot2.y = json.value("yspot2").toInt(-1);
    _timestamp = json.value("timestamp").toString().toInt();

}

Penalty::Penalty(const Penalty &other) {

    _bib = other.bib();
    _gate = other.gate();
    _value = other.value();
    _canvas = other.canvas();
    _timestamp = other.timestamp();
    _teammate = other.teammate();
    _spot1 = other.spot1();
    _spot2 = other.spot2();
}

Penalty &Penalty::operator =(const Penalty &other) {

    Penalty penalty(other);
    return penalty;

}

void Penalty::setValue(int value) {
    _value = value;
}

QString Penalty::toString() const {
    switch (_value) {
        case 0: return "0";
        case 2: return "2";
        case 50: return "50";
    }
    return "";
}

void Penalty::setSpot1(double x, double y) {
    _spot1.x = x;
    _spot1.y = y;
}

void Penalty::setSpot2(double x, double y) {
    _spot2.x = x;
    _spot2.y = y;
}


void Penalty::setTeammate(bool teammate) {
    _teammate = teammate;
}

void Penalty::setCanvas(bool canvas) {
    _canvas = canvas;
}

void Penalty::setTimestamp(qint64 timestamp) {
    _timestamp = timestamp;
}

QJsonObject Penalty::jsonObject() const {
    QJsonObject obj;

    obj.insert("bib", _bib);
    obj.insert("gate", _gate);
    obj.insert("value", _value);
    obj.insert("xspot1", _spot1.x);
    obj.insert("yspot1", _spot1.y);
    obj.insert("xspot2", _spot2.x);
    obj.insert("yspot2", _spot2.y);
    obj.insert("teammate", _teammate);
    obj.insert("canvas", _canvas);
    obj.insert("timestamp", QString::number(_timestamp));
    return obj;
}

void Penalty::setBib(const QString &bib)
{
    _bib = bib;
}

void Penalty::copy(const Penalty &other) {

    _bib = other.bib();
    _gate = other.gate();
    _value = other.value();
    _canvas = other.canvas();
    _timestamp = other.timestamp();
    _teammate = other.teammate();
    _spot1 = other.spot1();
    _spot2 = other.spot2();

}

