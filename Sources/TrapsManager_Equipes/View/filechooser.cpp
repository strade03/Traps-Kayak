#include "filechooser.h"

FileChooser::FileChooser(const QString &title, QStringList nameFilters) : QObject(),
    _title(title),
    _nameFilters(nameFilters)
{

}

void FileChooser::onSelectedFilePath(std::function<void(QString)> callback) {
    QObject::connect(this, &FileChooser::selectedFilePath, callback);
}
