#ifndef VIEWCONTROLLER_H
#define VIEWCONTROLLER_H

#include <QObject>
#include "View/dialogbox.h"
#include "View/filechooser.h"

class ViewController : public QObject
{

    Q_OBJECT
    Q_PROPERTY(int fontSize READ fontSize NOTIFY fontSizeChanged)
    Q_PROPERTY(QString statusText READ statusText NOTIFY statusTextChanged)
    Q_PROPERTY(QString folder READ folder CONSTANT)
    Q_PROPERTY(bool showChrono READ showChrono CONSTANT)
    Q_PROPERTY(int appWindowWidth READ appWindowWidth CONSTANT)
    Q_PROPERTY(int appWindowHeight READ appWindowHeight CONSTANT)
    Q_PROPERTY(int bibCount READ bibCount NOTIFY bibCountChanged)


public:
    ViewController(const QStringList &hostList, int requestedTcpPort);

    int fontSize() const { return _fontSize; }
    QString statusText() const { return _statusText; }
    QString folder() const { return _folder; }
    bool showChrono() const { return _showChrono; }
    int bibCount() const { return _bibCount; }

    int appWindowWidth() const { return _appWindowWidth; }
    int appWindowHeight() const { return _appWindowHeight; }

signals:

    void fontSizeChanged(int fontSize);
    void statusTextChanged(QString statusText);
    void quit();
    void popup(QObject* dialogBox);
    void popFileChooser(QString title, QStringList nameFilters);
    void requestPCE(QString filename, bool reset);
    void requestTXT(QString filename, bool reset);
    void requestPenaltyClear();
    void requestChronoClear();
    void toast(QString text, int delay); // delay in msec
    void requestTcpServer(QString host, int port);
    void selectedAddress(QString host);
    void bibCountChanged(int bibCount);
    void watchdog();
    void openSoftwareUpdate();
    void checknewVersion(bool force);

public slots:

    void incFontSize(int step);
    void setAppWindowWidth(int width);
    void setAppWindowHeight(int height);
    void setFolder(const QString &folder);
    void setStatusText(const QString& statusText);
    void openDialogBox(DialogBox* data);
    void dialogRejected();
    void dialogAccepted();
    void dialogButtonClicked(int index);
    void openFileChooser(FileChooser* data);
    void selectedFilePath(QString filePath);
    void setShowChrono(bool showChrono);
    void setBibCount(int bibCount);
    void about();
    void loadPCE();
    void loadTXT();
    void clearPenalties();
    void clearChronos();
    void printError(const QString& title, const QString& message);
    void broadcastError();
    void setTcpPort(int tcpPort);
    void viewReady(); // called when QML Component.onCompleted is called
    void tcpServerStarFailure();

private:

    int _fontSize;
    QString _statusText;
    QString _folder;
    QStringList _hostList;
    QString _selectedHost;
    int _runningTcpPort;
    int _requestedTcpPort;
    int _bibCount;
    DialogBox* _dialogBox;
    FileChooser* _fileChooser;
    bool _dialogBoxOpened;
    bool _fileChooserOpened;
    bool _showChrono;

    void refreshStatusText();

    int _appWindowWidth;
    int _appWindowHeight;
};

#endif // VIEWCONTROLLER_H
