import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1

Rectangle {

    id: menuRow

    property alias text: menuTextElement.text
    property int fontSize: 18
    property bool selected: false
    property alias horizontalAlignment: menuTextElement.horizontalAlignment

    signal clicked()
    signal hovered()

    color: selected ? Material.primary:"white"
    width: 250
    height: fontSize * menuTextElement.lineCount * 2

    Text {
        id: menuTextElement
        color: selected ? "white" : "black"
        font.pixelSize: fontSize
        anchors.leftMargin: 10
        wrapMode: Text.Wrap
        anchors.fill: parent
        lineHeight: 1.2
        verticalAlignment: Text.AlignVCenter
        horizontalAlignment: Text.AlignLeft
        anchors.rightMargin: 10
    }

    MouseArea {
        anchors.fill: parent
        hoverEnabled: true
        onClicked: menuRow.clicked()
        onHoveredChanged: menuRow.hovered()
    }

    Keys.onReturnPressed: menuRow.clicked()

}

