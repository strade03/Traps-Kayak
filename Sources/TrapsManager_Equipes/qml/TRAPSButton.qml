import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1

Item {

    id: buttonRoot

    property alias text: textItem.text
    property alias imageSource: image.source
    property alias fontSize: textItem.font.pixelSize
    property int horizontalMargin: 0
    property int verticalMargin: 0
    property bool selected: false
    property string tooltip: ""

    signal clicked
    signal tabPressed

    width: itemRow.width + horizontalMargin*2
    height: textItem.height + verticalMargin*2

    ToolTip.text: tooltip
    ToolTip.delay: 1000
    ToolTip.visible: buttonMouseArea.containsMouse && tooltip!=""

    Rectangle {
        id: rectangleBg
        anchors.fill: parent
        color: buttonMouseArea.containsMouse ? "lightgray" : "white"
        border.width: 2
        border.color: buttonRoot.activeFocus ? Material.primary: "transparent"

    }

    Rectangle {
        id: rectangleSelection
        visible: buttonRoot.selected
        color: Material.primary
        anchors.fill: parent

    }

    Row {
        id: itemRow
        anchors.centerIn: parent
        spacing: 10

        Image {
            id: image
            visible: source==""?false:true
            height: parent.height
            width: height
            sourceSize.height: height
            sourceSize.width: width
        }

        Text {
            id: textItem
            color: buttonRoot.selected ? "white" : "black"
        }
    }

    Keys.onReturnPressed: {
        buttonRoot.clicked()
    }

    Keys.onEnterPressed: {
        buttonRoot.clicked()
    }

    Keys.onSpacePressed: {
        buttonRoot.clicked()
    }

    Keys.onTabPressed: {
        buttonRoot.tabPressed()
    }


    MouseArea {
        id: buttonMouseArea
        anchors.fill: parent
        hoverEnabled: true

        onClicked: {
            buttonRoot.clicked()
            buttonRoot.forceActiveFocus()
        }

    }
}
