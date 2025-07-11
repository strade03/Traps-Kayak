TARGET = TRAPSManager_Equipes
TEMPLATE = app

QT += network qml quick sql
CONFIG += c++11

unix:!macx {
        QMAKE_LFLAGS += -no-pie
}

HEADERS += \
    HTTPServer/requestmapper.h \
    HTTPServer/controller/dumpcontroller.h \
    HTTPServer/controller/formcontroller.h \
    HTTPServer/controller/fileuploadcontroller.h \
    global.h \
    Network/hellobroadcaster.h \
    Canoe/bib.h \
    Canoe/event.h \
    Database/database.h \
    Canoe/penalty.h \
    Canoe/biblist.h \
    HTTPServer/controller/penaltyrequestprocessor.h \
    FFCanoe/ffcanoe.h \
    TCPServer/connectionhandler.h \
    TCPServer/connectionhandlerpool.h \
    TCPServer/tcpserver.h \
    View/viewcontroller.h \
    Cloud/cloudthread.h \
    Cloud/cloud.h \
    Network/hellobroadcaster.h \
    CompetFFCK/competffck.h \
    CompetFFCK/competffckconnector.h \
    FFCanoe/ffcanoeconnector.h \
    View/dialogbox.h \
    View/filechooser.h \
    Canoe/penaltylistmodel.h \
    Update/softwareupdate.h \
    Network/jsonnetworkmanager.h \
    Network/jsonnetworkreply.h


SOURCES += \
    main.cpp \
    HTTPServer/requestmapper.cpp \
    HTTPServer/controller/dumpcontroller.cpp \
    HTTPServer/controller/formcontroller.cpp \
    HTTPServer/controller/fileuploadcontroller.cpp \
    global.cpp \
    Network/hellobroadcaster.cpp \
    Canoe/bib.cpp \
    Canoe/event.cpp \
    Database/database.cpp \
    Canoe/penalty.cpp \
    Canoe/biblist.cpp \
    HTTPServer/controller/penaltyrequestprocessor.cpp \
    FFCanoe/ffcanoe.cpp \
    TCPServer/connectionhandler.cpp \
    TCPServer/connectionhandlerpool.cpp \
    TCPServer/tcpserver.cpp \
    View/viewcontroller.cpp \
    Cloud/cloudthread.cpp \
    Cloud/cloud.cpp \
    CompetFFCK/competffck.cpp \
    CompetFFCK/competffckconnector.cpp \
    FFCanoe/ffcanoeconnector.cpp \
    View/dialogbox.cpp \
    View/filechooser.cpp \
    Canoe/penaltylistmodel.cpp \
    Update/softwareupdate.cpp \
    Network/jsonnetworkreply.cpp \
    Network/jsonnetworkmanager.cpp



RESOURCES += \
    resources.qrc

RC_FILE = traps.rc
ICON = traps.ico

#---------------------------------------------------------------------------------------
# The following lines include the sources of the QtWebAppLib library
#---------------------------------------------------------------------------------------

include(QtWebApp/logging/logging.pri)
include(QtWebApp/httpserver/httpserver.pri)

DISTFILES +=

