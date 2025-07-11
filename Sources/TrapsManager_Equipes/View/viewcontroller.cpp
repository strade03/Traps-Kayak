#include "viewcontroller.h"
#include <QQmlEngine>
#include <QDebug>
#include <QSettings>
#include "global.h"

ViewController::ViewController(const QStringList& hostList, int requestedTcpPort) : QObject(),
    _fontSize(18),
    _dialogBoxOpened(false),
    _fileChooserOpened(false),
    _hostList(hostList),
    _requestedTcpPort(requestedTcpPort),
    _runningTcpPort(0)

{
    QQmlEngine::setObjectOwnership(this, QQmlEngine::CppOwnership);
    QSettings settings;
    _folder = settings.value("folder").toString();
    _showChrono = settings.value("showChrono").toBool();
    _fontSize = settings.value("fontSize", QVariant(17)).toInt();
    qDebug() << "fontSize:" << _fontSize;
    _appWindowWidth = settings.value("appWindowWidth", QVariant(800)).toInt();
    qDebug() << "appWindowWidth:" << _appWindowWidth;
    _appWindowHeight = settings.value("appWindowHeight", QVariant(600)).toInt();
    qDebug() << "appWindowHeight:" << _appWindowHeight;

}

void ViewController::setBibCount(int bibCount) {
    _bibCount = bibCount;
    emit bibCountChanged(_bibCount);
}

void ViewController::incFontSize(int step) {
    _fontSize += step;
    if (_fontSize<8) _fontSize = 9;
    if (_fontSize>50) _fontSize = 50;
    qDebug() << "Font size: " << _fontSize;
    QSettings settings;
    settings.setValue("fontSize", _fontSize);
    emit fontSizeChanged(_fontSize);
}

void ViewController::setAppWindowWidth(int width) {
    _appWindowWidth = width;
    QSettings settings;
    settings.setValue("appWindowWidth", _appWindowWidth);
}

void ViewController::setAppWindowHeight(int height) {
    _appWindowHeight = height;
    QSettings settings;
    settings.setValue("appWindowHeight", _appWindowHeight);
}

void ViewController::setFolder(const QString& folder) {
    qDebug() << "New folder for FileDialog: " << folder;
    _folder = folder;
    QSettings settings;
    settings.setValue("folder", _folder);
}

void ViewController::setShowChrono(bool showChrono) {
    _showChrono = showChrono;
    QSettings settings;
    settings.setValue("showChrono", _showChrono);
}


void ViewController::setStatusText(const QString &statusText) {
    _statusText = statusText;
    emit statusTextChanged(statusText);
}

void ViewController::openDialogBox(DialogBox* data) {
    if (_dialogBoxOpened) {
        qDebug() << "Something went wrong: a dialog box is already opened. Overwrite.";
    }
    _dialogBoxOpened = true;
    emit popup(_dialogBox = data);
}

void ViewController::dialogRejected() {
    if (!_dialogBoxOpened) {
        qDebug() << "Something went wrong: a dialog box should be opened. abort";
        return;
    }
    emit _dialogBox->rejected();
    _dialogBoxOpened = false;
}

void ViewController::dialogAccepted() {
    if (!_dialogBoxOpened) {
        qDebug() << "Something went wrong: a dialog box should be opened. abort";
        return;
    }
    emit _dialogBox->accepted();
    _dialogBoxOpened = false;
}

void ViewController::dialogButtonClicked(int index) {
    if (!_dialogBoxOpened) {
        qDebug() << "Something went wrong: a dialog box should be opened. abort";
        return;
    }
    emit _dialogBox->buttonClicked(index);
    _dialogBoxOpened = false;
}

void ViewController::openFileChooser(FileChooser *data) {
    if (_fileChooserOpened) {
        qDebug() << "Something went wrong: a file chooser is already opened. reject.";
        emit _fileChooser->selectedFilePath("");
        return;
    }
    _fileChooserOpened = true;
    _fileChooser = data;
    emit popFileChooser(_fileChooser->title(), _fileChooser->nameFilters());

}

void ViewController::selectedFilePath(QString filePath) {
    if (!_fileChooserOpened) {
        qDebug() << "Something went wrong: a file chooser should be opened. abort";
        return;
    }
    emit _fileChooser->selectedFilePath(filePath);
    _fileChooserOpened = false;
}

void ViewController::about() {

    emit checknewVersion(true);
    emit openSoftwareUpdate();

}

void ViewController::loadPCE() {
    DialogBox* dialogBox = new DialogBox("Remplacer la liste ou ajouter des dossards ?",
                                         "Voulez-vous remplacer complètement la liste de dossards actuelle ou bien y ajouter des dossards supplémentaires ?",
                                         DIALOGBOX_QUESTION, QStringList() << "Remplacer" << "Ajouter" << "Annuler");

    dialogBox->onButtonClicked([this, dialogBox](int index) {
        if (index==2) return;
        FileChooser* fileChooser = new FileChooser("Fichier de course PCE", QStringList() << "Fichiers PCE (*.pce)" << "Tous les fichiers (*)");
        fileChooser->onSelectedFilePath([this, fileChooser, index](QString filePath) {
            qDebug() << "Selected file path: " << filePath;
            fileChooser->deleteLater();
            requestPCE(filePath, index==0); // reset (replace) if first button clicked (remplacer)
        });
        openFileChooser(fileChooser);
        dialogBox->deleteLater();
    });

    openDialogBox(dialogBox);
}

void ViewController::loadTXT() {
    DialogBox* dialogBox = new DialogBox("Remplacer la liste ou ajouter des dossards ?",
                                         "Voulez-vous remplacer complètement la liste de dossards actuelle ou bien y ajouter des dossards supplémentaires ?",
                                         DIALOGBOX_QUESTION, QStringList() << "Remplacer" << "Ajouter" << "Annuler");

    dialogBox->onButtonClicked([this, dialogBox](int index) {
        if (index==2) return;
        FileChooser* fileChooser = new FileChooser("Fichier CSV");
        fileChooser->onSelectedFilePath([this, fileChooser, index](QString filePath) {
            qDebug() << "Selected file path: " << filePath;
            fileChooser->deleteLater();
            requestTXT(filePath, index==0); // reset (replace) if first button clicked (remplacer)
        });
        openFileChooser(fileChooser);
        dialogBox->deleteLater();
    });

    openDialogBox(dialogBox);
}

void ViewController::clearPenalties() {
    DialogBox* dialogBox = new DialogBox("Effacer toutes les pénalités ?",
                                         "Notez que les pénalités ne seront effacées que de TRAPSManager, pas des systèmes tiers tels que FFCanoe ou CompetFFCK.",
                                         DIALOGBOX_QUESTION,
                                         DIALOGBOX_YES | DIALOGBOX_NO);
    dialogBox->onAccepted([this, dialogBox](){
        dialogBox->deleteLater();
        emit this->requestPenaltyClear();
    });
    dialogBox->onRejected([dialogBox](){
        dialogBox->deleteLater();
    });

    openDialogBox(dialogBox);

}

void ViewController::clearChronos() {
    DialogBox* dialogBox = new DialogBox("Effacer toutes les données chronos ?",
                                         "Notez que les chronos ne seront effacés que de TRAPSManager, pas des systèmes tiers tels que FFCanoe ou CompetFFCK.",
                                         DIALOGBOX_QUESTION,
                                         DIALOGBOX_YES | DIALOGBOX_NO);
    dialogBox->onAccepted([this, dialogBox](){
        dialogBox->deleteLater();
        emit this->requestChronoClear();
    });
    dialogBox->onRejected([dialogBox](){
        dialogBox->deleteLater();
    });

    openDialogBox(dialogBox);

}

void ViewController::printError(const QString &title, const QString &message) {
    DialogBox* dialogBox = new DialogBox(title, message, DIALOGBOX_ALERT, DIALOGBOX_OK);
    dialogBox->onAccepted([dialogBox](){
        dialogBox->deleteLater();
    });

    openDialogBox(dialogBox);
}

void ViewController::broadcastError() {
    DialogBox* dialogBox = new DialogBox("Problème réseau - TRAPS Manager doit redémarrer",
                                         "TRAPS Manager ne parvient plus à se faire connaitre sur le réseau (problème de broadcast).\nL'application va se fermer, veuillez la redémarrer.\nAucune donnée ne sera perdue pendant le temps de redémarrage.",
                                         DIALOGBOX_ALERT, DIALOGBOX_OK);
    dialogBox->onAccepted([this, dialogBox](){
        dialogBox->deleteLater();
        emit this->quit();
    });

    openDialogBox(dialogBox);
}

void ViewController::setTcpPort(int tcpPort) {
    _runningTcpPort = tcpPort;
    refreshStatusText();
}

void ViewController::viewReady() {
    if (_hostList.count()==1) {  // only one network on this machine
        _selectedHost = _hostList.value(0);
        qDebug() << "Requesting TCP server on: " << _selectedHost << ":"<< _requestedTcpPort;
        emit selectedAddress(_selectedHost);
        emit requestTcpServer(_selectedHost, _requestedTcpPort);
    }
    else if (_hostList.count()==0) {
        qDebug() << "No network, Abort";
        DialogBox* dialogBox = new DialogBox("Aucun réseau disponible",
                                             "Cette machine n'est connectée à aucun réseau. Les terminaux TRAPS envoient leur données par le réseau, il faut donc connecter cette machine au réseau par câble ou par wifi.\nVous devez redémarrer l'application.",
                                             DIALOGBOX_ALERT, DIALOGBOX_OK);
        dialogBox->onAccepted([this, dialogBox](){
            dialogBox->deleteLater();
            refreshStatusText();
            //emit this->quit();
        });

        openDialogBox(dialogBox);

    }
    else {
        qDebug() << "There are multiple ip addresses on this machine. We need to select one.";
        DialogBox* dialogBox = new DialogBox("Plusieurs réseaux disponibles",
                                             "Cette machine a des adresses sur plusieurs réseaux.\nQuelle est l'adresse correspondant au réseau des terminaux TRAPS ?",
                                             DIALOGBOX_QUESTION, _hostList);

        dialogBox->onButtonClicked([this, dialogBox](int index) {
            dialogBox->deleteLater();
            _selectedHost = _hostList.value(index);
            qDebug() << "Selected ip address: " << _selectedHost;
            qDebug() << "Requesting TCP server on: " << _selectedHost << ":"<< _requestedTcpPort;
            emit this->selectedAddress(_selectedHost);
            emit this->requestTcpServer(_selectedHost, _requestedTcpPort);

        });
        openDialogBox(dialogBox);
    }
    emit checknewVersion(false);
}

void ViewController::tcpServerStarFailure() {

    DialogBox* dialogBox = new DialogBox("Erreur fatale de démarrage du serveur",
                                         QString("TRAPS Manager n'arrive pas à démarrer son serveur sur\n%0:%1\nUne autre application utilise sans doute ce même port.\nL'application va se fermer.").arg(_selectedHost).arg(_requestedTcpPort),
                                         DIALOGBOX_ALERT, DIALOGBOX_OK);
    dialogBox->onAccepted([this, dialogBox](){
        dialogBox->deleteLater();
        emit this->quit();
    });

    openDialogBox(dialogBox);

}

void ViewController::refreshStatusText() {
    if (_hostList.count()==0) _statusText = "Aucun réseau disponible. Redémarrez l'application.";
    else _statusText = QString("En écoute des TRAPS sur %1:%2").arg(_selectedHost).arg(_runningTcpPort);
    emit statusTextChanged(_statusText);
}
