import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1


Rectangle {

    id: bib

    property alias bibId: bibIdText.text
    property alias categ: categText.text
    property alias schedule: scheduleText.text
    property alias startTime: startTimeText.text
    property alias finishTime: finishTimeText.text
    property alias runningTime: runningTimeText.text
    property bool displayTimeData: true
    property bool selected: false
    property bool locked: false

    property int fontSize: 18

    height: fontSize*1.5

    // default width without TimeData is 18*12+3=228
    width: bibRow.width
    color: "black"

    Row {
        id: bibRow
        height: parent.height
        anchors.left: parent.left
        anchors.top:parent.top
        spacing: 1

        Rectangle {
            color: selected?Material.primary:locked?"lightgray":"#e3f2fd"
            height: parent.height
            width: fontSize*3.5
            Text {
                id: bibIdText
                anchors.fill: parent
                verticalAlignment: Text.AlignVCenter
                horizontalAlignment: Text.AlignHCenter
                color: selected?"white":locked?"gray":"black"
                font.pixelSize: fontSize
                font.weight: Font.Bold
                font.family: "monospace"

            }
        }
        Rectangle {
            color: selected?Material.primary:locked?"lightgray":"#e3f2fd"
            height: parent.height
            width: fontSize*4
            Text {
                id: categText
                anchors.fill: parent
                verticalAlignment: Text.AlignVCenter
                horizontalAlignment: Text.AlignHCenter
                color: selected?"white":locked?"gray":"black"
                font.pixelSize: fontSize
            }
        }
        Rectangle {
            color: selected?Material.primary:locked?"lightgray":"#e3f2fd"
            height: parent.height
            width: fontSize*6
            Text {
                id: scheduleText
                anchors.fill: parent
                verticalAlignment: Text.AlignVCenter
                horizontalAlignment: Text.AlignHCenter
                color: selected?"white":locked?"gray":"black"
                font.pixelSize: fontSize
                font.family: "monospace"
            }
        }
        Rectangle {
            color: selected?Material.primary:locked?"lightgray":"white"
            height: parent.height
            width: fontSize*7.5
            visible: displayTimeData
            Text {
                id: startTimeText
                anchors.fill: parent
                anchors.rightMargin: 5
                verticalAlignment: Text.AlignVCenter
                horizontalAlignment: Text.AlignRight
                color: selected?"white":locked?"gray":"black"
                font.pixelSize: fontSize
                font.family: "monospace"
            }
        }
        Rectangle {
            color: selected?Material.primary:locked?"lightgray":"white"
            height: parent.height
            width: fontSize*7.5
            visible: displayTimeData
            Text {
                id: finishTimeText
                anchors.fill: parent
                anchors.rightMargin: 5
                verticalAlignment: Text.AlignVCenter
                horizontalAlignment: Text.AlignRight
                color: selected?"white":locked?"gray":"black"
                font.pixelSize: fontSize
                font.family: "monospace"
            }
        }
        Rectangle {
            color: selected?Material.primary:locked?"lightgray":"white"
            height: parent.height
            width: fontSize*7.5
            visible: displayTimeData
            Text {
                id: runningTimeText
                anchors.fill: parent
                anchors.rightMargin: 5
                verticalAlignment: Text.AlignVCenter
                horizontalAlignment: Text.AlignRight
                color: selected?"white":locked?"gray":"black"
                font.pixelSize: fontSize
                font.family: "monospace"
            }
        }
    }

    Rectangle {
        color: "darkGray"
        height: 1
        anchors.left: bibRow.left
        anchors.right: bibRow.right
        anchors.bottom: bibRow.bottom
    }

}

