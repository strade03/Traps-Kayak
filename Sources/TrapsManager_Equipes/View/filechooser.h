#ifndef FILECHOOSER_H
#define FILECHOOSER_H

#include <QObject>
#include <functional>

class FileChooser : public QObject
{
    Q_OBJECT
public:
    explicit FileChooser(const QString& title, QStringList nameFilters = QStringList());

    QString title() const { return _title; }
    QStringList nameFilters() const { return _nameFilters; }
    void onSelectedFilePath(std::function<void(QString)> callback);

signals:

    void selectedFilePath(QString filePath);


private:

    QString _title;
    QStringList _nameFilters;

};

#endif // FILECHOOSER_H
