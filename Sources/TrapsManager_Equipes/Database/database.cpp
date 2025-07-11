#include "database.h"
#include <QDebug>
#include <QSqlError>
#include <QSqlQuery>
#include <QFile>
#include <QDir>
#include <QStandardPaths>
#include <QJsonDocument>
#include <QStringList>


Database::Database(const QString &filename, const QStringList& requiredTableList) :
    _tableList(requiredTableList)
{
    QDir dir(QStandardPaths::writableLocation(QStandardPaths::DataLocation));
    if (!dir.exists()) {
        qDebug() << "Creating " << dir.absolutePath();
        bool ok = QDir().mkpath(dir.absolutePath());
        if (!ok) qWarning() << "Cannot create " << dir.absolutePath();
    }
    _filepath = QString("%0/%1").arg(dir.absolutePath()).arg(filename);
    qInfo() << "Database file: "  << _filepath;
    _db = QSqlDatabase::addDatabase("QSQLITE");
    _db.setDatabaseName(_filepath);
    if (!_db.open()) {
        qCritical() << "Cannot open db at " << _filepath;
        return;
    } else {
        qDebug() << "Database opened";
    }
    // check list of tables
    QStringList tables = tableList();
    if (tables.count()!=_tableList.count()) {
        reset();
    }
    else foreach (QString tableName, _tableList) {
        if (!tables.contains(tableName)) {
            reset();
            break;
        }
    }

}

bool Database::checkOpen() {
    if (!_db.isOpen()) {
        qCritical() << "Database not opened: " << _filepath;
        return false;
    }
    return true;
}

bool Database::checkTable(const QString &table) {
    if (!_tableList.contains(table)) {
        qCritical() << "Unknown table: " << table;
        return false;
    }
    return true;
}

bool Database::createTable(const QString &table) {
    if (!checkOpen()) return false;
    qDebug() << "Creating table " << table;
    QSqlQuery query(_db);
    if (!query.exec(QString("CREATE table `%0` (`key` VARCHAR(30) PRIMARY KEY, `value` TEXT)").arg(table))) {
        qCritical() << query.lastError().text();
        return false;
    }
    return true;
}

bool Database::dropTable(const QString &table) {
    if (!checkOpen()) return false;
    qDebug() << "Droping table " << table;
    QSqlQuery query(_db);
    if (!query.exec(QString("DROP TABLE `%0`").arg(table))) {
        qCritical() << query.lastError().text();
        qCritical() << "Couldn't drop table " << table;
        return false;
    }
    return true;

}


bool Database::reset() {

    if (!checkOpen()) return false;
    qDebug() << "Resetting DB";
    foreach (QString table, _tableList) {
        clearTable(table);
    }
    return true;

}

bool Database::setValue(const QString &table, const QString &key, const QJsonObject& value) {

    if (!checkOpen()) return false;
    if (!checkTable(table)) return false;
    QByteArray byteArray = QJsonDocument(value).toJson(QJsonDocument::Compact).replace('"', "\"\"");
    QSqlQuery query(_db);
    QString sql = QString("INSERT OR REPLACE INTO `%0` (`key`, `value`) values (\"%1\", \"%2\")")
            .arg(table, key, QString(byteArray));
    bool ok = query.exec(sql);
    if (!ok) qCritical() << query.lastError().text();
    return ok;
}

bool Database::setValueMap(const QString& table, const QHash<QString,QJsonObject>& valueMap) {

    if (!checkOpen()) return false;
    if (!checkTable(table)) return false;
    _db.transaction();
    QSqlQuery query(_db);
    query.prepare(QString("INSERT OR REPLACE INTO `%0` (`key`, `value`) values (?,?)").arg(table));
    foreach (QString key, valueMap.keys()) {
        QString value = QString(QJsonDocument(valueMap.value(key)).toJson(QJsonDocument::Compact).replace('"', "\""));
        query.bindValue(0, key);
        query.bindValue(1, value);
        bool result = query.exec();
        if (!result) {
            qWarning() << "Cannot insert key " << key << " with value " << value;
            qWarning() << query.lastError().text();
            break;
        }
    }
    _db.commit();
    return true;

}

QHash<QString, QJsonObject> Database::map(const QString &table) {

    QHash<QString, QJsonObject> hash;
    if (!checkOpen()) return hash;
    if (!checkTable(table)) return hash;
    QSqlQuery query(_db);
    query.exec(QString("SELECT * FROM `%0`").arg(table));
    QJsonParseError parserError;
    while (query.next()) {
        QString key = query.value("key").toString();
        QByteArray value = query.value("value").toByteArray();
        QJsonDocument jsonDoc = QJsonDocument::fromJson(value, &parserError);
        if (parserError.error==QJsonParseError::NoError) {
            hash.insert(key,jsonDoc.object());
        } else {
            qWarning() << "Cannot parse " << value;
            qWarning() << parserError.error << ": " << parserError.errorString();
        }
    }
    return hash;
}

QJsonObject Database::value(const QString &table, const QString &key) {

    if (!checkOpen()) return QJsonObject();
    if (!checkTable(table)) return QJsonObject();

    QSqlQuery query(_db);
    query.exec(QString("SELECT * FROM `%0` WHERE `key`==\"%1\"").arg(table, key));
    QJsonParseError parserError;
    if (query.next()) {
        QString key = query.value("key").toString();
        QByteArray value = query.value("value").toByteArray();
        QJsonDocument jsonDoc = QJsonDocument::fromJson(value, &parserError);
        if (parserError.error==QJsonParseError::NoError) {
            return jsonDoc.object();
        } else {
            qWarning() << "Cannot parse " << value;
            qWarning() << parserError.error << ": " << parserError.errorString();
        }
    }
    return QJsonObject(); // return empty object

}

QStringList Database::tableList() {

    QStringList list;
    if (!checkOpen()) return list;
    QSqlQuery query(_db);
    query.exec("SELECT * FROM sqlite_master WHERE type='table'");
    while (query.next()) list << query.value("name").toString();
    return list;
}

bool Database::clearTable(const QString& table) {
    if (!checkOpen()) return false;
    if (!checkTable(table)) return false;
    qDebug() << "Clearing table " << table;
    dropTable(table);
    createTable(table);
    return true;
}


void Database::dump(const QString &table) {
    qDebug() << "Dumping " << table;
    QHash<QString, QJsonObject> tableMap = map(table);
    foreach (QString str, tableMap.keys()) {
        qDebug() << str << ": " << QJsonDocument(tableMap.value(str)).toJson(QJsonDocument::Compact);
    }
}
