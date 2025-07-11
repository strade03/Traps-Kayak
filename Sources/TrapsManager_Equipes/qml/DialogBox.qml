import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1

Dialog {
    id: dialogRoot

    property alias message: contentMessage.text
    property var customButtonLabels: []
    property int presetButtons: -1
    property string iconName
    property int fontSize: 18

    signal buttonClicked(int index)

    focus: true
    topMargin: (parent.height-contentHeight)/2
    width: parent.width
    contentHeight: textItem.height>dialogIcon.implicitHeight?textItem.height:dialogIcon.implicitHeight
    modal: true
    closePolicy: Popup.CloseOnEscape

    header: Label {
        text: title
        font.pixelSize: fontSize*1.2
        height: fontSize*2
        font.weight: Font.Medium
        padding: 15
    }

    footer: Item {

        DialogButtonBox {
            visible: presetButtons>-1
            anchors.top: parent.bottom
            anchors.topMargin: -5
            width: dialogRoot.width
            alignment: Qt.AlignHCenter

            standardButtons: presetButtons
            onAccepted: {
                dialogRoot.accepted()
                dialogRoot.close()
            }
            onRejected: {
                dialogRoot.rejected()
                dialogRoot.close()
            }
        }
    }

    Image {
        id: dialogIcon
        anchors.left: parent.left
        anchors.top: parent.top
        height: 80
        width: height
        visible: iconName!=""
        sourceSize.width: height
        sourceSize.height: width
        fillMode: Image.PreserveAspectFit
        source:visible?"images/"+iconName:""
    }

    Item {

        id: textItem
        anchors.left: dialogIcon.right
        anchors.leftMargin: 15
        anchors.right: parent.right
        anchors.top: parent.top
        anchors.topMargin: 15
        height: contentMessage.implicitHeight+customList.height

        Label {
            id: contentMessage
            anchors.left: parent.left
            anchors.right: parent.right
            anchors.top: parent.top
            font.pixelSize: fontSize
            wrapMode: Text.Wrap
        }


        ListView {
            id: customList
            focus: true
            anchors.left: parent.left
            anchors.right: parent.right
            anchors.top: contentMessage.bottom
            anchors.topMargin: 15
            height: customButtonLabels.length>0?customList.contentHeight+15:0
            visible: customButtonLabels.length>0

            model: customButtonLabels
            orientation: ListView.Vertical
            delegate: MenuRow {
                width: parent.width
                fontSize: dialogRoot.fontSize
                height: fontSize*2.5
                horizontalAlignment: Text.AlignHCenter
                selected: index === customList.currentIndex
                text: modelData
                onHovered: customList.currentIndex = index
                onClicked: {
                    buttonClicked(customList.currentIndex)
                    dialogRoot.close()
                }

            }
        }
    }

}
