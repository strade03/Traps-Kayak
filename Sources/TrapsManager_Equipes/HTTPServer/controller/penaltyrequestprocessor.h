#ifndef PENALTYREQUESTPROCESSOR_H
#define PENALTYREQUESTPROCESSOR_H

#include "httprequest.h"
#include "httpresponse.h"
#include "httprequesthandler.h"
#include "Canoe/penalty.h"

class PenaltyRequestProcessor : public stefanfrings::HttpRequestHandler
{
    Q_OBJECT

public:
    static PenaltyRequestProcessor& handle() {
        static PenaltyRequestProcessor instance;
        return instance;
    }
    PenaltyRequestProcessor(PenaltyRequestProcessor const&) = delete;
    void operator=(PenaltyRequestProcessor const&) = delete;

    // HttpRequestHandler interface
    void service(stefanfrings::HttpRequest &request, stefanfrings::HttpResponse &response);

signals:

    void incomingPenaltyList(QList<Penalty> penaltyList);

private:
    PenaltyRequestProcessor() {}


};

#endif // PENALTYREQUESTPROCESSOR_H
