#ifndef DIALOGBOX_H
#define DIALOGBOX_H

#include <QObject>
#include <functional>

#define DIALOGBOX_OK     0x00000400
#define DIALOGBOX_CANCEL 0x00400000
#define DIALOGBOX_NO     0x00010000
#define DIALOGBOX_YES    0x00004000

#define DIALOGBOX_QUESTION "question.svg"
#define DIALOGBOX_INFORMATION "information.svg"
#define DIALOGBOX_ALERT "alert.svg"


class DialogBox : public QObject
{
    Q_OBJECT
    Q_PROPERTY(QString title READ title CONSTANT)
    Q_PROPERTY(QString message READ message CONSTANT)
    Q_PROPERTY(QString iconName READ iconName CONSTANT)
    Q_PROPERTY(int presetButtons READ presetButtons CONSTANT)
    Q_PROPERTY(QStringList customButtonLabels READ customButtonLabels CONSTANT)

public:
    explicit DialogBox(const QString& title, const QString& message, const QString& iconName, int presetButtons);
    explicit DialogBox(const QString& title, const QString& message, const QString& iconName, const QStringList& customButtonLabels);

    QString title() const { return _title; }
    QString message() const { return _message; }
    QString iconName() const { return _iconName; }
    int presetButtons() const { return _presetButtons; }
    QStringList customButtonLabels() const { return _customButtonLabels; }
    void onAccepted(std::function<void()> callback);
    void onRejected(std::function<void()> callback);
    void onButtonClicked(std::function<void(int)> callback);

signals:

    void rejected();
    void accepted();
    void buttonClicked(int);

private:

    QString _title;
    QString _message;
    QString _iconName;
    int _presetButtons;
    QStringList _customButtonLabels;

};

#endif // DIALOGBOX_H
