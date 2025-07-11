#include "penaltyrequestprocessor.h"
#include <QDebug>
#include <QByteArray>
#include <QUrl>
#include <QJsonDocument>
#include <QJsonParseError>
#include <QJsonArray>

void PenaltyRequestProcessor::service(stefanfrings::HttpRequest &request, stefanfrings::HttpResponse &response) {
    qDebug() << "Processing request " << request.getPath();

    QString jsonString = QUrl::fromPercentEncoding(request.getBody()).remove(QRegExp("[\t|\r|\n|+]"));

    QString jsonPenalty;
    // get rid of user and password in request
    QStringList stringList = jsonString.split('&');
    foreach (QString string, stringList) {
        if (string.startsWith("json=")) jsonPenalty = string.mid(5);

    }

    QJsonParseError parseError;
    QJsonDocument jsonDoc = QJsonDocument::fromJson(jsonPenalty.toLocal8Bit(), &parseError);
    if (parseError.error!=QJsonParseError::NoError) {
        qCritical() << "Cannot parse json penalty request :";
        qCritical() << jsonPenalty;
        qCritical() << parseError.errorString() << " at " << parseError.offset;
        return;
    }
    if (jsonDoc.isNull() || jsonDoc.isEmpty()) {
        qCritical() << "Penalty request is a null or empty Json Document";
        return;
    }
    QJsonArray array = jsonDoc.array();
    QList<Penalty> penaltyList;
    foreach (QJsonValue json, array) {
        QJsonObject jsonPenalty = json.toObject();
        QString bib = jsonPenalty.value("bib").toString();
        int gate = jsonPenalty.value("gate").toInt();
        int value = jsonPenalty.value("value").toInt();
        if (!bib.isEmpty() && gate>0 && value>-1) {
            Penalty penalty(bib, gate, value);
            penalty.setTeammate(jsonPenalty.value("teammate").toBool());
            penalty.setCanvas(jsonPenalty.value("canvas").toBool());
            double xspot1 = jsonPenalty.value("xspot1").toDouble();
            double yspot1 = jsonPenalty.value("yspot1").toDouble();
            double xspot2 = jsonPenalty.value("xspot2").toDouble();
            double yspot2 = jsonPenalty.value("yspot2").toDouble();
            penalty.setSpot1(xspot1, yspot1);
            penalty.setSpot2(xspot2, yspot2);
            penaltyList << penalty;
        }

    }

    QJsonObject jsonResponse;

    if (penaltyList.count()>0) {
        emit incomingPenaltyList(penaltyList);
        jsonResponse.insert("response", 0);
    }
    else {
        jsonResponse.insert("response", 1);
    }

    response.write(QJsonDocument(jsonResponse).toJson());

}

