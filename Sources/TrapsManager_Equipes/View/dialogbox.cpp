#include "dialogbox.h"

/*
    DialogBox* data = new DialogBox("MY TITLE", "Message", "", BUTTON_YES | BUTTON_NO);
    data->onAccepted([data]() {
        qDebug() << "You just clicked ACCEPTED";
        data->deleteLater();
    });
    data->onRejected([data]() {
        qDebug() << "You just clicked REJECTED";
        data->deleteLater();
    });
    emit openDialogBox(data); // must be conmected to viewcontroller
*/

DialogBox::DialogBox(const QString& title, const QString& message, const QString& iconName, int presetButtons) : QObject(),
    _title(title),
    _message(message),
    _iconName(iconName),
    _presetButtons(presetButtons)
{

}

DialogBox::DialogBox(const QString &title, const QString &message, const QString &iconName, const QStringList& customButtonLabels) : QObject(),
    _title(title),
    _message(message),
    _iconName(iconName),
    _presetButtons(-1),
    _customButtonLabels(customButtonLabels)
{

}

void DialogBox::onAccepted(std::function<void()> callback) {
    QObject::connect(this, &DialogBox::accepted, callback);
}

void DialogBox::onRejected(std::function<void()> callback) {
    QObject::connect(this, &DialogBox::rejected, callback);
}

void DialogBox::onButtonClicked(std::function<void(int)> callback) {
    QObject::connect(this, &DialogBox::buttonClicked, callback);
}
