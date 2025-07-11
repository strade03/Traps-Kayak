import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1


Dialog {
    id: softwareUpdate

    focus: true
    width: parent.width
    contentHeight: parent.height*0.9
    topMargin: (parent.height-contentHeight)/2
    modal: true
    closePolicy: Popup.CloseOnEscape

    property int fontSize: 18

    property int _progress: 0

    Connections {
        target: softwareupdate
        onProgress: {
            _progress = percent
        }
    }


    Image {
        id: trapsIcon
        width: 128
        height: 128
        anchors.top: parent.top
        anchors.left: parent.left
        anchors.margins: 30
        source: "qrc:/qml/images/traps240.png"

    }

    Text {
        id: titleText
        color: "black"
        font.pixelSize: fontSize*1.5
        anchors.left: trapsIcon.right
        anchors.right: parent.right
        anchors.top: parent.top
        anchors.margins: 30
        text: softwareupdate.appTitle
    }

    Text {
        id: contentText
        color: "black"
        font.pixelSize: fontSize*0.8
        anchors.left: trapsIcon.right
        anchors.right: parent.right
        anchors.top: titleText.bottom
        anchors.margins: 30
        text: softwareupdate.appInfo
        wrapMode: Text.Wrap
    }

    Text {
        id: linkText
        anchors.top: contentText.bottom
        anchors.left: trapsIcon.right
        anchors.right: parent.right
        anchors.topMargin: 15
        anchors.leftMargin: 30
        font.pixelSize: fontSize*0.8
        wrapMode: Text.Wrap
        color: linkTextArea.containsMouse?Material.primary:"black"
        text: "Modification à partir de Traps Manager par Verdier Stéphane"

    }


    Image {
        id: updateIcon
        sourceSize.height: 60
        sourceSize.width: 60
        anchors.horizontalCenter: trapsIcon.horizontalCenter
        anchors.top: linkText.bottom
        anchors.topMargin: 30
        source: "qrc:/qml/images/update80.png"
        visible: softwareupdate.versionUptodate | softwareupdate.newVersionAvailable
    }

    Text {
        id: versionUptodate
        visible: softwareupdate.versionUptodate
        anchors.left: linkText.left
        anchors.right: updateButton.left
        anchors.rightMargin: 30
        anchors.top: updateIcon.top
        color: "black"
        font.pixelSize: fontSize
        text: "L'application est à jour. Il s'agit de la version la plus récente."
    }

    Text {
        id: newVersionText
        visible: softwareupdate.newVersionAvailable
        anchors.left: linkText.left
        anchors.right: updateButton.left
        anchors.rightMargin: 30
        anchors.top: updateIcon.top
        color: "black"
        font.pixelSize: fontSize
        text: softwareupdate.newDate
              +"\nNouvelle version disponible: "
              +softwareupdate.newVersion
              +"\n\nQuoi de neuf:"

    }

    Text {
        id: whatsnewText
        visible: softwareupdate.newVersionAvailable
        anchors.left: linkText.left
        anchors.right: updateButton.left
        anchors.rightMargin: 30
        anchors.top: newVersionText.bottom
        color: "black"
        font.pixelSize: fontSize*0.7
        text: softwareupdate.whatsnew
    }

    Button {
        id: updateButton
        visible: softwareupdate.newVersionAvailable && !softwareupdate.downloading
        anchors.right: parent.right
        anchors.rightMargin: 30
        anchors.verticalCenter: newVersionText.verticalCenter
        text: "  Mettre à jour  "
        onClicked: softwareupdate.download();

    }

    ProgressBar {
        id: progressBar
        anchors.left: newVersionText.left
        anchors.right: updateButton.left
        anchors.top: whatsnewText.bottom
        anchors.topMargin: 30
        visible: softwareupdate.downloading
        to: 100
        value: softwareUpdate._progress

    }


    Text {
        id: progressPercent
        anchors.top: progressBar.bottom
        anchors.left: progressBar.left
        font.pixelSize: fontSize*0.7
        visible: progressBar.visible
        text: "Chargement: "+softwareUpdate._progress+"%"
    }

    Text {
        id: filePathLabel
        anchors.top: progressPercent.bottom
        anchors.left: progressBar.left
        font.pixelSize: fontSize
        visible: softwareUpdate._progress==100
        text: "Fichier de mise à jour disponible ici:\n"+softwareupdate.filePath
    }


    DialogButtonBox {
        id: dialogButtonBox
        anchors.bottom: parent.bottom
        anchors.left: parent.left
        anchors.right: parent.right

        alignment: Qt.AlignHCenter
        standardButtons: DialogButtonBox.Close
        onRejected: softwareUpdate.reject()

    }

}
