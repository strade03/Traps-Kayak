#include <QCoreApplication>
#include "requestmapper.h"
#include "filelogger.h"
#include "staticfilecontroller.h"
#include "controller/dumpcontroller.h"
#include "controller/formcontroller.h"
#include "controller/fileuploadcontroller.h"
#include "controller/penaltyrequestprocessor.h"

/** Redirects log messages to a file */
extern stefanfrings::FileLogger* logger;

/** Controller for static files */
extern stefanfrings::StaticFileController* staticFileController;

RequestMapper::RequestMapper(QObject* parent)
    :stefanfrings::HttpRequestHandler(parent)
{
    qDebug("RequestMapper: created");
}


RequestMapper::~RequestMapper()
{
    qDebug("RequestMapper: deleted");
}


void RequestMapper::service(stefanfrings::HttpRequest& request, stefanfrings::HttpResponse& response)
{
    QByteArray path=request.getPath();
    qDebug("RequestMapper: path=%s",path.data());

    if (path.startsWith("/trapsmanager/setpenaltylist")) {
        PenaltyRequestProcessor::handle().service(request, response);
    }
    else if (path.startsWith("/dump")) {
        DumpController().service(request, response);
    }

    else if (path.startsWith("/form")) {
        FormController().service(request, response);
    }

    else if (path.startsWith("/file")) {
        FileUploadController().service(request, response);
    }

    // All other paths are mapped to the static file controller.
    // In this case, a single instance is used for multiple requests.
    else {
        staticFileController->service(request, response);
    }

    qDebug("RequestMapper: finished request");

    // Clear the log buffer
    if (logger) logger->clear();

}
