# Utiliser une image de base Ubuntu
FROM ubuntu:20.04

# Mettre à jour les paquets et installer les dépendances nécessaires
RUN apt-get update && \
    apt-get install -y software-properties-common && \
    dpkg --add-architecture i386 && \
    apt-get update && \
    apt-get install -y \
    openjdk-17-jdk \
    wget \
    unzip \
    curl \
    build-essential \
    libc6:i386 \
    libncurses5:i386 \
    libstdc++6:i386 \
    lib32z1 \
    lib32gomp1 \
    lib32gcc1 \
    lib32z1-dev \
    && rm -rf /var/lib/apt/lists/*

# Installer le SDK Android
ENV ANDROID_HOME=/opt/android-sdk
RUN mkdir -p $ANDROID_HOME
RUN wget https://dl.google.com/android/repository/commandlinetools-linux-6609375_latest.zip -O /tmp/android-sdk.zip && \
    echo "SDK downloaded"
RUN unzip /tmp/android-sdk.zip -d /tmp/android-sdk && \
    echo "SDK unzipped"
RUN mv /tmp/android-sdk/tools $ANDROID_HOME/tools && \
    echo "SDK moved"
RUN rm -rf /tmp/android-sdk /tmp/android-sdk.zip && \
    echo "Temporary files removed"

# Accepter les licences et installer les composants SDK
RUN yes | $ANDROID_HOME/tools/bin/sdkmanager --sdk_root=$ANDROID_HOME --licenses || echo "License acceptance failed"
RUN $ANDROID_HOME/tools/bin/sdkmanager --sdk_root=$ANDROID_HOME "platforms;android-17" "build-tools;30.0.3" && \
    echo "SDK components installed"

# Installer Gradle
ENV GRADLE_HOME=/opt/gradle
RUN mkdir -p $GRADLE_HOME && \
    wget https://services.gradle.org/distributions/gradle-8.11.1-bin.zip -O /tmp/gradle.zip && \
    unzip /tmp/gradle.zip -d $GRADLE_HOME && \
    chmod +x $GRADLE_HOME/gradle-8.11.1/bin/gradle && \
    rm /tmp/gradle.zip && \
    echo "Gradle installed"

# Configurer les variables d'environnement
ENV PATH=$PATH:$ANDROID_HOME/tools/bin:$ANDROID_HOME/platform-tools:$GRADLE_HOME/gradle-8.11.1/bin

# Vérifier les variables d'environnement
RUN echo "PATH is $PATH" && \
    which gradle && \
    gradle --version

# Définir le répertoire de travail
WORKDIR /app

# Copier le projet Android dans le conteneur
COPY . /app

# Définir la commande par défaut
CMD ["gradle", "build"]
