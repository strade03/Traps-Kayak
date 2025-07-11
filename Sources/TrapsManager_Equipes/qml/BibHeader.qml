import QtQuick 2.7

Rectangle {

    id: bibHeader

    property bool displayTimeData: false
    property int fontSize: 18
    property color bgcolor: "gray"

    height: fontSize*1.5

    width: displayTimeData?fontSize*21.5+6:fontSize*12.5+3
    color: "white"

    Row {
        id: bibRow
        height: parent.height
        anchors.left: parent.left
        anchors.top:parent.top
        spacing: 1

        Rectangle {
            color: bgcolor
            height: parent.height
            width: fontSize*3.5
            border.width: 1
            border.color: "white"
            Text {
                id: bibIdText
                anchors.fill: parent
                verticalAlignment: Text.AlignVCenter
                horizontalAlignment: Text.AlignHCenter
                color: "white"
                font.pixelSize: fontSize*0.7
                text: "Dossard"

            }
        }
        Rectangle {
            color: bgcolor
            height: parent.height
            width: fontSize*4
            border.width: 1
            border.color: "white"
            Text {
                id: categText
                anchors.fill: parent
                verticalAlignment: Text.AlignVCenter
                horizontalAlignment: Text.AlignHCenter
                color: "white"
                font.pixelSize: fontSize*0.7
                text: "Catégorie"
            }
        }
        Rectangle {
            color: bgcolor
            height: parent.height
            width: fontSize*6
            border.width: 1
            border.color: "white"
            Text {
                id: scheduleText
                anchors.fill: parent
                verticalAlignment: Text.AlignVCenter
                horizontalAlignment: Text.AlignHCenter
                color: "white"
                font.pixelSize: fontSize*0.7
                text: "Horaire"
            }
        }
        Rectangle {
            color: bgcolor
            height: parent.height
            width: fontSize*7.5
            visible: displayTimeData
            border.width: 1
            border.color: "white"
            Text {
                id: startTimeText
                anchors.fill: parent
                verticalAlignment: Text.AlignVCenter
                horizontalAlignment: Text.AlignHCenter
                color: "white"
                font.pixelSize: fontSize*0.7
                text: "Départ"
            }
        }
        Rectangle {
            color: bgcolor
            height: parent.height
            width: fontSize*7.5
            visible: displayTimeData
            border.width: 1
            border.color: "white"
            Text {
                id: finishTimeText
                anchors.fill: parent
                verticalAlignment: Text.AlignVCenter
                horizontalAlignment: Text.AlignHCenter
                color: "white"
                font.pixelSize: fontSize*0.7
                text: "Arrivée"
            }
        }
        Rectangle {
            color: bgcolor
            height: parent.height
            width: fontSize*7.5
            visible: displayTimeData
            border.width: 1
            border.color: "white"
            Text {
                id: elapsedTimeText
                anchors.fill: parent
                verticalAlignment: Text.AlignVCenter
                horizontalAlignment: Text.AlignHCenter
                color: "white"
                font.pixelSize: fontSize*0.7
                text: "Chrono"
            }
        }
    }

}

