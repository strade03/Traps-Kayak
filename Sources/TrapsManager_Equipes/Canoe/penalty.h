#ifndef PENALTY_H
#define PENALTY_H

#include <QPair>
#include <QJsonObject>


class Penalty {

public:

    struct Coord {
        double x;
        double y;
    };


    explicit Penalty();
    explicit Penalty(const QString& bib, int gate);
    explicit Penalty(const QString& bib, int gate, int value);
    explicit Penalty(const QJsonObject& json);
    Penalty(const Penalty& other);
    Penalty& operator = (const Penalty& other);

    int value() const { return _value; }
    void setValue(int value);
    QString toString() const;

    Coord spot1() const { return _spot1; }
    Coord spot2() const { return _spot2; }
    void setSpot1(double x, double y);
    void setSpot2(double x, double y);

    bool teammate() const { return _teammate; }
    void setTeammate(bool teammate);

    bool canvas() const { return _canvas; }
    void setCanvas(bool canvas);

    qint64 timestamp() const { return _timestamp; }
    void setTimestamp(qint64 timestamp);

    int gate() const { return _gate; }

    QJsonObject jsonObject() const;

    QString bib() const { return _bib; }
    void setBib(const QString &bib);

private:

    QString _bib;
    int _gate; // starts at 1
    int _value; // (-1, 0, 2, 50)
    Coord _spot1;
    Coord _spot2;

    bool _teammate;
    bool _canvas;
    qint64 _timestamp; // number of seconds since epoch

    void copy(const Penalty& other);

};

#endif // PENALTY_H
