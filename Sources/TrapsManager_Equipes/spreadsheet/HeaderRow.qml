import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1


ListView {

    id: headerRow

    property var columnWidthList: []
    property var sortEnabledColumnList: []

    signal sortByColumn(int index, bool asc)

    property int _currentSortedColumn: -1

    height: 50
    orientation: ListView.Horizontal
    clip: true

    delegate: Item {

        width: columnWidthList[index]*spreadSheet.fontSize
        height: headerRow.height
        visible: width > 0

        Rectangle {
            id: labelBg
            anchors.fill: parent
            color: (headerLabelMouseArea.containsPress && sortEnabledColumnList[index])?spreadSheet.colorShade700:spreadSheet.primaryColor

        }

        Label {
            id: headerLabel
            color: "white"
            text: modelData
            font.pixelSize: spreadSheet.fontSize*0.9
            anchors.fill: parent
            verticalAlignment: Text.AlignVCenter
            horizontalAlignment: Text.AlignHCenter
            elide: Text.ElideRight
            height: headerRow.height
            padding: 5

        }

        Rectangle {
            anchors.top: parent.top
            anchors.bottom: parent.bottom
            anchors.right: parent.right
            width: 2
            color: "white"
        }

        Text {
            id: sortingArrow

            property bool asc: false

            color: "white"
            font.pixelSize: spreadSheet.fontSize
            verticalAlignment: Text.AlignVCenter
            anchors.top: parent.top
            anchors.bottom: parent.bottom
            anchors.right: parent.right
            anchors.rightMargin: 10
            text: _currentSortedColumn!==index?"\u25B6":sortingArrow.asc?"\u25B2":"\u25BC"
            visible: false

            onOpacityChanged: if (opacity==0) visible = false
            onAscChanged: sortingArrowAnim.restart()

            onVisibleChanged: {
                if (visible) {
                    opacity = 1;
                    sortingArrowAnim.start()
                }
            }


            SequentialAnimation {
                id: sortingArrowAnim

                PauseAnimation { duration: 1000 }
                NumberAnimation { target: sortingArrow; property: "opacity"; to:0; duration:200 }

            }


        }

        MouseArea {
            id: headerLabelMouseArea
            anchors.fill: parent
            onClicked: {
                if (!sortEnabledColumnList[index]) return // if column not sort enabled, return
                sortingArrow.visible = true
            }

            onDoubleClicked: {
                if (!sortEnabledColumnList[index]) return // if column not sort enabled, return
                sortingArrow.asc = !sortingArrow.asc
                sortByColumn(index, sortingArrow.asc)
                sortingArrow.visible = true
                _currentSortedColumn = index
            }
        }

    }
}
