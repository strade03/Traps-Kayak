# TRAPS App

## Description
TRAPS App est une application Android native dédiée à la gestion des compétitions de Canoë Kayak. Cette application permet d'organiser, de gérer et de suivre les événements et les compétitions de Canoë Kayak de manière efficace et intuitive.

## Technologies Utilisées
- Android SDK (API 33)
- Java 17
- Gradle 8.2.0 (Plugin Android 8.1.0)

## Versions et Compatibilité
- Version minimum Android : API 16 (Android 4.1)
- Version cible Android : API 33
- Version de l'application : 2.7
- AndroidX AppCompat : 1.6.1
- AndroidX ConstraintLayout : 2.1.4
- Material Design : 1.9.0

## Configuration Requise
- Android Studio Flamingo (2022.2.1) ou plus récent
- SDK Android (API 33)
- Java Development Kit 17 (JDK 17)
- Gradle 8.2.0

## Installation
[De préférence installer Docker pour compiler](https://github.com/strade03/Traps-Kayak/tree/main/Outils_Compilation)
1. Clonez le repository :
```bash
git clone [URL_DU_REPO]
``` 
2. Créer la clé pour la signature :
   1. si vous utilser docker :<br>
         docker run -v ./:/app -it android_builder /bin/bash
   2. placer dans le dossier app pour y créer la clé.
   3. keytool -genkey -v -keystore trapsapp-release-key.keystore -alias trapsapp_key -keyalg RSA -keysize 2048 -validity 10000
   4. Editer le fichier keystore.properties (storePassword et keyPassword)

3. Avec docker pour compiler :<br>
      docker run --rm  -v ./:/app android_builder gradle assembleRelease --stacktrace -x lint

