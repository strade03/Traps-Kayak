import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1

FocusScope {

    id: tabHeader

    property alias cornerButtonText : cornerButton.text
    property int fontSize: 18
    property alias model: tabRepeater.model
    property int currentIndex: 0

    signal click(int buttonIndex)

    height: fontSize * 2
    width: 450


    MouseArea {
        anchors.fill: parent
        onClicked: {
            tabHeader.click(-1)
        }
    }

    Row {
        Repeater {
            id: tabRepeater
            delegate: TRAPSButton {
                fontSize: tabHeader.fontSize
                text: model.name
                imageSource: currentIndex == index ? model.imageSelected: model.image
                focus: true
                height: tabHeader.height
                width: (tabHeader.width * 2) / (tabRepeater.model.count * 3)
                horizontalMargin: 20
                onClicked: {
                    click(index)
                    currentIndex = index
                }
                selected: index === tabHeader.currentIndex
                onTabPressed: {
                    if (index<tabRepeater.model.length-1) tabRepeater.itemAt(index+1).forceActiveFocus()
                    else cornerButton.forceActiveFocus()
                }
            }
        }
    }

    TRAPSButton {
        id: cornerButton
        text: "\u25B2"
        anchors.right: parent.right
        anchors.top: parent.top
        height: parent.height
        fontSize: tabHeader.fontSize
        width: fontSize*3
        onClicked: {
            tabHeader.click(-1)

        }
        onTabPressed: {
            tabRepeater.itemAt(0).forceActiveFocus()

        }

    }

    Rectangle {
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.top: parent.top
        height:1
        color: "gray"
    }

    Rectangle {
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.bottom: parent.bottom
        height:1
        color: "gray"
    }


}
