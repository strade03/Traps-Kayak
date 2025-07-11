import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1


FocusScope {

    id: bibScheduling
    property string orderSelection
    focus: true

    signal scheduling(int selection)
    signal tabPressed

    Rectangle {
        anchors.fill: parent
        color: "white"
    }

    Rectangle {
        id: bibCountBg
        height: parent.height
        width: bibCount.width+height
        color: "gray"

        Text {
            id: bibCount
            color: "white"
            font.pixelSize: viewcontroller.fontSize
            height: parent.height
            anchors.left: parent.left
            anchors.leftMargin: height/2
            verticalAlignment: Text.AlignVCenter
            font.weight: Font.Bold
            text: viewcontroller.bibCount+" dossards"
        }
    }

    Text {
        id: bibOrderText
        color: "black"
        text: "ordonnés selon :"
        font.pixelSize: viewcontroller.fontSize*0.9
        height: parent.height
        verticalAlignment: Text.AlignVCenter
        anchors.left: bibCountBg.right
        anchors.leftMargin: 10
    }

    ComboBox {
        id: bibOrderBox
        focus: true
        height: parent.height
        anchors.left: bibOrderText.right
        anchors.right: parent.right
        anchors.leftMargin: 20
        font.pixelSize: viewcontroller.fontSize
        anchors.verticalCenter: parent.verticalCenter
        currentIndex: bibList.scheduling
        model: [ "Numéro de dossard", "Heure de départ", "Rang dans le fichier d'origine" ]
        onCurrentIndexChanged: scheduling(currentIndex)

    }

}
