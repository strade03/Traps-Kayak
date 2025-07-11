# Conseils de compilation

## Compilation des apk

### Construction du conteneur : 
Copier le fichier dockerfile dans un dossier android_builder<br>
> cd android_builder<br>
> docker build -t  android_builder .

### Commande de compilation :
Décompresser les sources<br>
> cd TrapsApp-V3<br>
> docker run --rm  -v ./:/app android_builder gradle assembleRelease --stacktrace -x lint

Le fichier compilé se trouve ./TrapsApp-V3/app/build/outputs/apk/release

Si besoin d'ouvrir une session dans le conteneur  :<br>
- docker run -v ./:/app -it android_builder /bin/bash

## Compilation TrapsManager
Sous Windows installer Qt 5.10.1 + MinGW 32bits<br>
https://download.qt.io/new_archive/qt/5.10/5.10.1/

(Ne pas utiliser MSVC2015, et donc pas besoin d'installer VS2015)

