import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1

Item {

    id: cell
    property alias text: textLabel.text
    property bool selected: false
    property bool hovered: false
    property bool checkable: false
    property alias checked: checkbox.checked
    property alias horizontalAlignment: textLabel.horizontalAlignment
    property color bgColor: "white"
    property color textColor: textLabel.color
    property alias fontFamily: textLabel.font.family


    Rectangle {
        color: hovered?spreadSheet.colorShade100:bgColor
        anchors.fill: parent
    }

    Label {
        id: textLabel
        anchors.fill: parent

        width: parent.width
        font.pixelSize: spreadSheet.fontSize
        padding: 5

        verticalAlignment: Text.AlignVCenter
        visible: !checkbox.visible
        elide: Text.ElideRight

    }

    CheckBox {
        id: checkbox
        anchors.fill: parent
        checked: true
        visible: cell.checkable

    }

    Rectangle {
        color: "transparent"
        border.width: 1
        border.color: "grey"
        anchors.fill: parent
        anchors.margins: 2
        visible: selected && !spreadSheet._editionInProgress
    }
}
