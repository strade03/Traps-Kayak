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
2. Ouvrez le projet dans Android Studio
3. Créer la clé pour la signature :
   1. si vous utilser docker :
         docker run -v ./:/app -it android_builder /bin/bash
   2. placer dans le dossier app pour y créer la clé.
   3. keytool -genkey -v -keystore trapsapp-release-key.keystore -alias trapsapp_key -keyalg RSA -keysize 2048 -validity 10000
   4. renseigner le fichier keystore.properties (storePassword et keyPassword)

4. Avec docker pour compiler :
      docker run --rm  -v ./:/app android_builder gradle assembleRelease --stacktrace -x lint



## Guide de Démarrage avec Android Studio

### Prérequis
1. Assurez-vous d'avoir installé :
   - La dernière version d'Android Studio
   - Java Development Kit (JDK)
   - Android SDK

### Ouverture du Projet
1. Lancez Android Studio
2. Sélectionnez `File > Open`
3. Naviguez jusqu'au dossier du projet TRAPSApp
4. Cliquez sur `OK`
5. Attendez que Android Studio indexe le projet

### Configuration du Projet
1. Dans Android Studio, ouvrez `File > Project Structure`
   - Vérifiez que le SDK Android est correctement configuré
   - Assurez-vous que la version du JDK est compatible
2. Dans `File > Settings > Build, Execution, Deployment > Build Tools > Gradle`
   - Vérifiez que Gradle JDK est bien configuré
   - Assurez-vous que le plugin Android Gradle est à jour

### Lancement de l'Application
1. En haut à droite, configurez votre environnement de lancement :
   - Sélectionnez un appareil Android (émulateur ou physique)
   - Choisissez la configuration de lancement 'app'
2. Cliquez sur `Sync Project with Gradle Files` si nécessaire
3. Cliquez sur le bouton vert ▶️ (Run) ou utilisez le raccourci `Maj + F10`
4. L'application se lancera sur l'appareil sélectionné

### Résolution des Problèmes Courants
- Si vous rencontrez des erreurs de build :
  1. `File > Invalidate Caches / Restart`
  2. `Build > Clean Project`
  3. `Build > Rebuild Project`
- Pour les problèmes de SDK :
  1. Vérifiez que le `local.properties` contient le bon chemin SDK
  2. Vérifiez les versions SDK dans `build.gradle`

### Notes de Migration
Si vous migrez depuis une version précédente du projet, veuillez noter les changements suivants :
- Migration vers AndroidX
- Mise à jour des dépendances vers les dernières versions stables
- Configuration Java 8
- Support des nouvelles fonctionnalités Android API 33
