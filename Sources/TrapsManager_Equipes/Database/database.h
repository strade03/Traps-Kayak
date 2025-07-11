#ifndef DATABASE_H
#define DATABASE_H

#include <QString>
#include <QSqlDatabase>
#include <QHash>
#include <QJsonObject>


class Database {

public:

    explicit Database(const QString& filename, const QStringList& requiredTableList);

    bool setValue(const QString& table, const QString& key, const QJsonObject& value);
    bool setValueMap(const QString& table, const QHash<QString,QJsonObject>& valueMap);

    QHash<QString, QJsonObject> map(const QString& table);
    QJsonObject value(const QString& table, const QString& key);
    QStringList tableList();
    bool clearTable(const QString &table);
    bool reset();

    void dump(const QString& table);

private:

    QString _filepath;
    QStringList _tableList;
    QSqlDatabase _db;

    bool checkOpen();
    bool checkTable(const QString& table);
    bool createTable(const QString& table);
    bool dropTable(const QString& table);


};

#endif // DATABASE_H
