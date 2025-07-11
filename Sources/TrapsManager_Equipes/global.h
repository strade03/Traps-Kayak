#ifndef GLOBAL_H
#define GLOBAL_H

#include <QString>

class Global
{

public:

    static void init();

    static QString appDataDir;
    static QString httpConfigFile;
    static QString docroot;


private:
    Global() {}
    static void installFile(const QString& qrcSourceFilename, const QString& destinationFilename, bool forceInstall = false);
    static QString createDir(const QString& dirPath);

};

#endif // GLOBAL_H
