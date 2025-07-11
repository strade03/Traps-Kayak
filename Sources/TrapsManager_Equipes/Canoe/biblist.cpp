#include "biblist.h"
#include <QTime>
#include <QDebug>
#include <QQmlEngine>
#include <QSettings>
#include "Database/database.h"

#define DB_BIB "bibs"
#define DB_LOCKS "locks"
#define DB_TIMES "times"
#define DB_PENALTIES "penalties"


BibList::BibList() : QAbstractListModel(),
    _db("traps.db", QStringList() << DB_BIB << DB_LOCKS << DB_TIMES << DB_PENALTIES),
    _scheduling(0)

{
    QQmlEngine::setObjectOwnership(this, QQmlEngine::CppOwnership);
    QSettings settings;
    _scheduling = settings.value("scheduling").toInt();

    reloadFromDataBase();
}

int BibList::bibCount() const {
    return _bibList.count();
}

Bib *BibList::bibAtIndex(int index) const {
    if (index<0 || index>=_bibList.count()) return 0;
    return _bibList.at(index);
}

Bib *BibList::bibWithId(const QString &bibId) const {
    foreach (Bib* bib, _bibList) {
        if (bib->id()==bibId) return bib;
    }
    return 0;
}

Bib *BibList::bibWithIdNumber(int id) const {
    foreach (Bib* bib, _bibList) {
        if (bib->idNumber()==id) return bib;
    }
    return 0;
}

int BibList::bibIndex(const QString &bibId) const {
    for (int index=0; index<_bibList.count(); index++) {
        Bib* bib = _bibList.at(index);
        if (bib->id()==bibId) return index;
    }
    return -1;
}

int BibList::bibIndex(int bibnumber) const {
    for (int index=0; index<_bibList.count(); index++) {
        Bib* bib = _bibList.at(index);
        if (bib->idNumber()==bibnumber) return index;
    }
    return -1;
}


void BibList::processPCE(const QString &filename, bool reset) {

    if (filename.isEmpty()) {
        qWarning() << "No file selected. Abort.";
        return;
    }

    QString path = QUrl(filename).toLocalFile();
    qDebug() << "Loading bib list from: " << path;
    QFile file(path);
    // use QFile::exists("blabla"); !
    if (!file.open(QFile::ReadOnly)) {
        qDebug() << "Cannot open file " << path;
        emit error("Chargement fichier PCE", QString("Impossible d'ouvrir le fichier\n%0").arg(path));
        return;
    }

    QTextStream stream(&file);

    // reach boat section
    QString string;

    // first reach [resultats] section
    while (true) {
        string = stream.readLine().trimmed();
        if (string.isNull() || stream.atEnd()) {
            qDebug() << "Cannot find [resultats] section in file " << path;
            emit error("Chargement fichier PCE", QString("Impossible de trouver la section [resultats] dans le fichier\n%0").arg(path));
            return;
        }
        if (string=="[resultats]") break;
    }

    if (reset) {
        _db.reset();
        emit toast("Dossards actuels effacés", 3000);
    }
    QHash<QString,QJsonObject> bibs;
    QHash<QString,QJsonObject> locks;
    QHash<QString,QJsonObject> times;
    QHash<QString,QJsonObject> penalties;
    int row = 1;
    emit toast("Chargement des dossards...", 3000);
    while (true) {
        string = stream.readLine().trimmed();
        if (string.isNull() || stream.atEnd() || (string[0]=='[')) break;
        if (string.isEmpty()) continue;
        //qDebug() << string;
        QStringList tab = string.split(';');
        // bib number
        if (tab.length()<11) {
            qDebug() << "No bib number found in this line:" << string;
            continue;
        }

        // bib number
        bool ok;
        int bibnumber = tab[11].toInt(&ok);
        if (!ok || bibnumber<1) {
            qWarning() << "Cannot convert the bib number read in this line: " << string;
            continue;
        }

        Bib bib(bibnumber);
        bib.setCateg(tab[3]);

        //need to know how many teammate: column 12
        int mateCount = tab[12].toInt(&ok);
        if (!ok) mateCount=3;

        if (tab.length()>33) {
            if (mateCount==3) bib.setSchedule(tab[33]); 
            else if (tab.length()>48) bib.setSchedule(tab[48]);  // cas des C2
        }
        bib.setEntry(row);
        bibs.insert(bib.id(), bib.jsonParam());
        locks.insert(bib.id(), bib.jsonLock());
        times.insert(bib.id(), bib.jsonTime());
        penalties.insert(bib.id(), bib.jsonPenalty());
        //qDebug() << bib.toString();
        row++;
    }
    // save biblist in DB
    if (bibs.count()>0) {
        _db.setValueMap(DB_BIB, bibs);
        _db.setValueMap(DB_LOCKS, locks);
        _db.setValueMap(DB_TIMES, times);
        _db.setValueMap(DB_PENALTIES, penalties);

    }

    reloadFromDataBase();
    orderBibList();
    emit toast(QString("Liste de %0 dossards chargée").arg(bibs.count()), 4500);

}

void BibList::processTXT(const QString& filename, bool reset) {

    if (filename.isEmpty()) {
        qWarning() << "No file selected. Abort.";
        return;
    }

    QString path = QUrl(filename).toLocalFile();
    qDebug() << "Loading bib list from: " << path;
    QFile file(path);
    // use QFile::exists("blabla"); !
    if (!file.open(QFile::ReadOnly)) {
        qDebug() << "Cannot open file " << path;
        emit error("Chargement fichier CSV", QString("Impossible d'ouvrir le fichier\n%0").arg(path));
        return;
    }

    QTextStream stream(&file);
    QString string;

    QHash<QString,QJsonObject> bibs;
    QHash<QString,QJsonObject> locks;
    QHash<QString,QJsonObject> times;
    QHash<QString,QJsonObject> penalties;
    int row = 0;
    emit toast("Chargement des dossards...", 3000);
    bool errorRow = 0;
    while (true) {
        row++;
        string = stream.readLine().trimmed();
        if (string.isNull()) break;
        if (string.isEmpty()) continue;
        qDebug() << string;
        QStringList tabComma = string.split(',');
        QStringList tabSemiColumn = string.split(';');
        QStringList tab = tabComma.count()>tabSemiColumn.count()?tabComma:tabSemiColumn; // take the delimiter that gives the max number of elements
        // bib number
        bool ok;
        int bibnumber = tab[0].toInt(&ok);
        if (!ok || bibnumber<1) {
            qWarning() << "Cannot convert the bib number read in this line: " << string;
            errorRow = row;
            break;
        }
        QString categ = "-";
        if (tab.count()>1) categ = tab[1];
        QString schedule = "-";
        if (tab.count()>2) schedule = tab[2];

        Bib bib(bibnumber);
        bib.setCateg(categ);
        bib.setEntry(row);
        bib.setSchedule(schedule);
        bibs.insert(bib.id(), bib.jsonParam());
        locks.insert(bib.id(), bib.jsonLock());
        times.insert(bib.id(), bib.jsonTime());
        penalties.insert(bib.id(), bib.jsonPenalty());

    }

    if (errorRow>0) {
        emit error("Erreur de lecture fichier", QString("Impossible de décoder la ligne %0:\n%1").arg(errorRow).arg(string));
        return;
    }

    if (reset) {
        _db.reset();
        emit toast("Dossards actuels effacés", 3000);
    }
    // save biblist in DB
    if (bibs.count()>0) {
        _db.setValueMap(DB_BIB, bibs);
        _db.setValueMap(DB_LOCKS, locks);
        _db.setValueMap(DB_TIMES, times);
        _db.setValueMap(DB_PENALTIES, penalties);

    }

    reloadFromDataBase();
    orderBibList();
    emit toast(QString("Liste de %0 dossards chargée").arg(bibs.count()), 4500);

}

void BibList::clearPenalties() {
    foreach (Bib* bib, _bibList) {
        bib->clearPenalties();
    }
    _db.clearTable(DB_PENALTIES);
    rebuildPenaltyList();
    emit toast("Penalités effacées", 3000);
}

void BibList::clearChronos() {
    beginResetModel();
    foreach (Bib* bib, _bibList) {
        bib->clearChronos();
    }
    _db.clearTable(DB_TIMES);
    endResetModel();
    emit dataChanged(createIndex(0, 0), createIndex(_bibList.count(), 0));
    emit toast("Chrono effacés", 3000);
}

void BibList::processIncomingPenaltyList(QList<Penalty> penaltyList) {

    // TODO: to be implemented
}

void BibList::processIncomingPenalty(int bibnumber, QHash<int, int> penaltyList) {
    qDebug() << "Biblist processing incoming penalties for bib " << bibnumber;
    Bib* bib = bibWithIdNumber(bibnumber);
    if (bib==0) {
        qWarning() << QString("Cannot find bib id %0 in the list. Ignore.").arg(bibnumber);
        return;
    }
    int bibRow = bibIndex(bibnumber);
    foreach (int gateId, penaltyList.keys()) {
        int penaltyValue = penaltyList.value(gateId);
        Penalty penalty(bib->id(), gateId, penaltyValue);
        if (bib->setPenalty(penalty)) {
            qDebug() << "Biblist emiting send penalty for gate " << gateId;
            emit penaltyReceived(bibnumber, gateId, penaltyValue);
            int changedIndex = bibRow*GATE_MAX_COUNT+gateId-1;
            qDebug() << "Changing penalty at index " << changedIndex;
            _penaltyListModel.setPenalty(changedIndex, QString::number(penaltyValue));
        }
        else {
            qDebug() << "Bib is locked, cannot set penalty. abort.";
            toast(QString("Dossard %0 est vérrouillé. Pénalité porte %1 ignorée.").arg(bib->id()).arg(gateId), 3000);
        }
    }
    _db.setValue(DB_PENALTIES, bib->id(), bib->jsonPenalty());
    // Notify penalties for this bib has changed
    int firstIndex = bibRow*GATE_MAX_COUNT;
    int lastIndex = (bibRow+1)*GATE_MAX_COUNT-1;
    qDebug() << "Data changed from " << firstIndex << " to " << lastIndex;
    emit _penaltyListModel.refresh(firstIndex, lastIndex);
}

void BibList::processIncomingStartTime(int bibnumber, qint64 startTime) {
    qDebug() << "Biblist processing incoming start time for bib " << bibnumber;
    Bib* bib = bibWithIdNumber(bibnumber);
    if (bib==0) {
        qWarning() << QString("Cannot find bib id %0 in the list. Ignore.").arg(bibnumber);
        return;
    }
    if (bib->setStartTime(startTime)) {
        _db.setValue(DB_TIMES, bib->id(), bib->jsonTime());
        int runningTime = bib->runningTime();
        qDebug() << "RUNNING TIME: "<< runningTime;
        if (runningTime>0) emit chronoReceived(bibnumber, runningTime);
        int bibRow = bibIndex(bib->id());
        emit dataChanged(createIndex(bibRow, 0), createIndex(bibRow, 0));
    }
    else {
        qDebug() << "Bib is locked, cannot set start time. abort.";
        toast(QString("Dossard %0 est vérrouillé. Heure de départ ignorée.").arg(bib->id()), 3000);
    }

}

void BibList::processIncomingFinishTime(int bibnumber, qint64 finishTime) {
    qDebug() << "Biblist processing incoming finish time for bib " << bibnumber;
    Bib* bib = bibWithIdNumber(bibnumber);
    if (bib==0) {
        qWarning() << QString("Cannot find bib id %0 in the list. Ignore.").arg(bibnumber);
        return;
    }
    if (bib->setFinishTime(finishTime)) {
        _db.setValue(DB_TIMES, bib->id(), bib->jsonTime());
        int runningTime = bib->runningTime();
        if (runningTime>0) emit chronoReceived(bibnumber, runningTime);
        int bibRow = bibIndex(bib->id());
        emit dataChanged(createIndex(bibRow, 0), createIndex(bibRow, 0));
    }
    else {
        qDebug() << "Bib is locked, cannot set finish time. abort.";
        toast(QString("Dossard %0 est vérrouillé. Heure d'arrivée ignorée.").arg(bib->id()), 3000);
    }

}

void BibList::processIncomingLapTime(int bibnumber, int lap, qint64 time){

}

void BibList::selectPenalty(int bibIndex, int gateIndex) {

    qDebug() << QString("You just selected bib index %0 at gate index %1").arg(bibIndex).arg(gateIndex);

}

void BibList::setScheduling(int criteria) {


    qDebug() << "Scheduling is now " << criteria;
    _scheduling = criteria;
    QSettings settings;
    settings.setValue("scheduling", _scheduling);

    orderBibList();

}

void BibList::lock(int firstRow, int lastRow) {
    qDebug() << "Locking row " << firstRow << " to row " << lastRow;
    QHash<QString,QJsonObject> valueMap;
    for (int row=firstRow; row<=lastRow; row++) {
        Bib* bib = bibAtIndex(row);
        if (bib!=0) {
            bib->setLocked(true);
            valueMap.insert(bib->id(), bib->jsonLock());
        }
    }
    _db.setValueMap(DB_LOCKS, valueMap);
    emit dataChanged(createIndex(firstRow, 0), createIndex(lastRow, 0));
    emit toast("Dossards vérrouillés", 2000);
}

void BibList::unlock(int firstRow, int lastRow) {
    qDebug() << "Unlocking row " << firstRow << " to row " << lastRow;
    QHash<QString,QJsonObject> valueMap;
    for (int row=firstRow; row<=lastRow; row++) {
        Bib* bib = bibAtIndex(row);
        if (bib!=0) {
            bib->setLocked(false);
            valueMap.insert(bib->id(), bib->jsonLock());
        }
    }
    _db.setValueMap(DB_LOCKS, valueMap);
    emit dataChanged(createIndex(firstRow, 0), createIndex(lastRow, 0));
    emit toast("Dossards déverrouillés", 2000);
}

void BibList::forwardBib(int firstRow, int lastRow) {
    qDebug() << "Forwarding bibs from row " << firstRow << " to row " << lastRow;
    for (int row=firstRow; row<=lastRow; row++) {
        Bib* bib = bibAtIndex(row);
        if (bib!=0) {
            if (bib->runningTime()>0) emit chronoReceived(bib->idNumber(), bib->runningTime());
            QHash<int, Penalty> penaltyList = bib->penaltyList();
            foreach (int gateId, penaltyList.keys()) {
                emit penaltyReceived(bib->idNumber(), gateId, penaltyList.value(gateId).value());
            }
        }
    }
    emit toast("Dossards renvoyés", 2000);

}

QJsonArray BibList::jsonArray(qint64 timestamp = 0) const {
    QJsonArray array;

    return array;
}

QList<int> BibList::bibNumberList() const {
    QList<int> list;
    foreach (Bib* bib, _bibList) {
        int number = bib->id().toInt();
        if (number>0) list << number;
    }
    return list;
}


void BibList::rebuildPenaltyList() {

    QStringList stringList;
    foreach (Bib* bib, _bibList) {
        stringList << bib->penaltyStringList();
    }
    _penaltyListModel.reset(stringList);

}

bool BibList::numberLessThan(Bib *bib1, Bib *bib2) {
    return (bib1->id()<bib2->id());
}

bool BibList::entryLessThan(Bib *bib1, Bib *bib2) {
    return (bib1->entry()<bib2->entry());
}

bool BibList::scheduleLessThan(Bib *bib1, Bib *bib2) {
    return (bib1->schedule()<bib2->schedule());
}

void BibList::reloadFromDataBase() {

    qDebug() << "Reloading bib list from database";
    beginResetModel();
    qDeleteAll(_bibList);
    _bibList.clear();

    QHash<QString, QJsonObject> bibs = _db.map(DB_BIB);
    QHash<QString, QJsonObject> locks = _db.map(DB_LOCKS);
    QHash<QString, QJsonObject> times = _db.map(DB_TIMES);
    QHash<QString, QJsonObject> penalties = _db.map(DB_PENALTIES);
    foreach (QString bibId, bibs.keys()) {
        QJsonObject bibObj = bibs.value(bibId);
        Bib* bib = new Bib(bibId, bibObj);
        bib->setFinishTime((qint64)times.value(bibId).value("finishTime").toDouble());
        bib->setStartTime((qint64)times.value(bibId).value("startTime").toDouble());
        QJsonArray penaltyArray = penalties.value(bibId).value("penaltyList").toArray();
        foreach (QJsonValue jsonValue, penaltyArray) {
            QJsonObject penaltyObj = jsonValue.toObject();
            bib->setPenalty(Penalty(penaltyObj));
        }
        bib->setLocked(locks.value(bibId).value("locked").toBool(false));
        //qDebug() << bib->toString();
        _bibList.append(bib);
    }

    endResetModel();
    rebuildPenaltyList();
    emit bibCountChanged(_bibList.count());
}

void BibList::orderBibList() {
    beginResetModel();
    switch (_scheduling) {
        case 0 : {
            qSort(_bibList.begin(), _bibList.end(), BibList::numberLessThan); // bib number
            emit toast("Liste ordonnée selon les numéros de dossard croissants", 3000);
            break;
        }
        case 1 : {
            qSort(_bibList.begin(), _bibList.end(), BibList::scheduleLessThan); // schedule
            emit toast("Liste ordonnée selon les heures de départ", 3000);
            break;
        }
        case 2 : {
            qSort(_bibList.begin(), _bibList.end(), BibList::entryLessThan); // rank in original file
            emit toast("Liste ordonnée selon le rang dans le fichier d'origine", 3000);
            break;
        }
    }
    endResetModel();
    rebuildPenaltyList();
}

int BibList::rowCount(const QModelIndex &parent) const {
    return _bibList.count();
}

QVariant BibList::data(const QModelIndex &index, int role) const {

    Bib* bib = bibAtIndex(index.row());
    if (bib==0) return QVariant();

    switch (role) {
        case BibList::BibId : return QVariant(bib->id());
        case BibList::BibCateg : return QVariant(bib->categ());
        case BibList::BibSchedule : return QVariant(bib->schedule());
        case BibList::BibLocked : return QVariant(bib->locked());
        case BibList::BibStartTime : return QVariant(bib->startTimeStr());
        case BibList::BibFinishTime : return QVariant(bib->finishTimeStr());
        case BibList::BibRunningTime : return QVariant(bib->runningTimeStr());

    }

    return QVariant();
}

QHash<int, QByteArray> BibList::roleNames() const {
    QHash<int, QByteArray> hash;
    hash.insert(BibList::BibId, BIB_ID_NAME);
    hash.insert(BibList::BibStartTime, BIB_STARTTIME_NAME);
    hash.insert(BibList::BibFinishTime, BIB_FINISHTIME_NAME);
    hash.insert(BibList::BibSchedule, BIB_SCHEDULE_NAME);
    hash.insert(BibList::BibCateg, BIB_CATEG_NAME);
    hash.insert(BibList::BibRunningTime, BIB_RUNNINGTIME_NAME);
    hash.insert(BibList::BibLocked, BIB_LOCKED_NAME);
    return hash;
}

