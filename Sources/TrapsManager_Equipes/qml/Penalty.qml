import QtQuick 2.7

Rectangle {

    property string value: ""
    property int fontSize: 18
    property bool selected: false

    height: fontSize*1.5
    width: fontSize*3

    color:  value == "0" ? "#6F6":
            value == "2" ? "#FF6":
            value == "50" ? "#F66":
            "white"

    border.width: selected?4:0
    border.color: "black"

    Rectangle {
        width: parent.width
        height: 1
        color: "darkGray"
        anchors.verticalCenter: parent.bottom
    }

    Rectangle {
        width: 1
        height: parent.height
        color: "darkGray"
        anchors.horizontalCenter: parent.right
    }


    Text {
        color: "black"
        anchors.fill: parent
        font.pixelSize: fontSize
        verticalAlignment: Text.AlignVCenter
        horizontalAlignment: Text.AlignHCenter
        text: value

    }


}

