import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1


FocusScope {
    id: bibGrid

    height: 600
    width: 300

    property alias penaltyGridModel: penaltyGrid.model
    property alias bibListModel: bibList.model
    property int fontSize: 18

    property bool displayTimeData: true
    property int _penaltyCellWidth: Math.round(fontSize * 1.3)
    property int _penaltyCellHeight: fontSize*1.5
    property int _penaltyCellCount: 75
    property int _bibCellWidth: displayTimeData?fontSize*36+6:fontSize*13.5+3
    property bool _shift: false
    property int _firstRowSelected: 0
    property int _lastRowSelected: 0

    signal tabPressed

    Flickable {

        id: gridContainer

        anchors.left: bibList.right
        anchors.right: parent.right
        anchors.top: parent.top
        anchors.bottom: parent.bottom
        contentWidth: _penaltyCellWidth*_penaltyCellCount
        contentHeight: parent.height

        ScrollBar.horizontal: ScrollBar {
            id: horizontalScrollBar
            parent: gridContainer.parent
            anchors.bottom: gridContainer.bottom
            anchors.left: gridContainer.left
            anchors.right: gridContainer.right
        }


        GridView {

            id: penaltyGrid

            anchors.top: penaltyGridHeader.bottom
            anchors.bottom: parent.bottom
            anchors.left: parent.left
            anchors.right: parent.right
            cellHeight: _penaltyCellHeight
            cellWidth: _penaltyCellWidth
            highlightFollowsCurrentItem: false
            focus: true

            snapMode: GridView.SnapToRow
            delegate: Penalty {
                width: _penaltyCellWidth
                height: _penaltyCellHeight
                value: penaltyValue
                selected: penaltyGrid.currentIndex === index
                fontSize: bibGrid.fontSize
            }

            MouseArea {

                anchors.fill: parent
                onClicked: {
                    print("Clicked inside bib grid")
                    bibList.contentY = penaltyGrid.contentY
                    penaltyGrid.forceActiveFocus()
                    penaltyGrid.currentIndex = penaltyGrid.indexAt(mouse.x, mouse.y+penaltyGrid.contentY);
                }
                onDoubleClicked: {
                    var localIndex = penaltyGrid.indexAt(mouse.x, mouse.y)
                    bibListModel.selectPenalty((localIndex/_penaltyCellCount)+1, localIndex%_penaltyCellCount)
                }


            }

            Keys.onReturnPressed: {
                bibListModel.selectPenalty((penaltyGrid.currentIndex/_penaltyCellCount)+1, penaltyGrid.currentIndex%_penaltyCellCount)
            }
            Keys.onRightPressed: {
                if ((penaltyGrid.currentIndex+1)%_penaltyCellCount!=0) penaltyGrid.moveCurrentIndexRight()
            }
            Keys.onLeftPressed: {
                if ((penaltyGrid.currentIndex+1)%_penaltyCellCount!=1) penaltyGrid.moveCurrentIndexLeft()
            }
            Keys.onTabPressed: bibGrid.tabPressed()

            Keys.onPressed: {
                if (event.key===Qt.Key_Shift) {
                    _shift = true
                    event.accepted = true
                }
            }
            Keys.onReleased: {
                if (event.key===Qt.Key_Shift) {
                    _shift = false
                    event.accepted = true
                }
            }

            onCurrentIndexChanged: {
                bibList.currentIndex = penaltyGrid.currentIndex / _penaltyCellCount
                if (gridContainer.width>0 && penaltyGrid.currentItem.x+_penaltyCellWidth>gridContainer.width+gridContainer.contentX) {
                    var value = penaltyGrid.currentItem.x+_penaltyCellWidth-gridContainer.width
                    if (value<0) value = 0
                    gridContainer.contentX = value
                }
                else if (gridContainer.width>0 && penaltyGrid.currentItem.x<gridContainer.contentX) {
                    gridContainer.contentX = penaltyGrid.currentItem.x
                }

            }
            onContentYChanged: { // sync bib list part with penalty grid part
                if (penaltyGrid.movingVertically) {
                    bibList.contentY = penaltyGrid.contentY
                }
            }

        }

        PenaltyHeader {
            id: penaltyGridHeader
            anchors.top: parent.top
            anchors.left: parent.left
            anchors.right: parent.right
            height: _penaltyCellHeight
            fontSize: bibGrid.fontSize
        }

    }

    ListView {
        id: bibList

        width: _bibCellWidth
        anchors.left: parent.left
        anchors.top: bibListHeader.bottom
        anchors.bottom: parent.bottom
        snapMode: ListView.SnapToItem
        highlightFollowsCurrentItem: false

        ScrollBar.vertical: ScrollBar {
            id: verticalScrollBar
            parent: bibList.parent
            anchors.top: bibList.top
            anchors.horizontalCenter: bibList.right
            anchors.bottom: bibList.bottom
        }


        delegate: Bib {
            width: _bibCellWidth
            height: _penaltyCellHeight
            bibId: bibData
            categ: categData
            schedule: scheduleData==""?"?":scheduleData
            startTime: startTimeData
            finishTime: finishTimeData
            runningTime: runningTimeData
            selected: index>=_firstRowSelected && index<=_lastRowSelected
            locked: lockedData
            displayTimeData: bibGrid.displayTimeData
            fontSize: bibGrid.fontSize
            Rectangle { // highlight
                anchors.top: parent.top
                anchors.bottom: parent.bottom
                anchors.left: parent.left
                width: bibGrid.width+2
                visible: index>=_firstRowSelected && index<=_lastRowSelected
                color: "transparent"
                border.width: 2
                border.color: Material.primary

            }


        }

        onContentYChanged: {
            if (bibList.movingVertically || verticalScrollBar.pressed) {
                penaltyGrid.contentY = bibList.contentY
            }
        }

        onCurrentIndexChanged: {
            if (_shift) {
                if (bibList.currentIndex>_firstRowSelected) _lastRowSelected = bibList.currentIndex
                else _firstRowSelected = bibList.currentIndex

            }
            else {
                _firstRowSelected = bibList.currentIndex
                _lastRowSelected = bibList.currentIndex
            }
        }

        MouseArea {
            anchors.fill: parent
            acceptedButtons: Qt.RightButton | Qt.LeftButton
            onClicked: {
                bibGrid.focus = true;
                penaltyGrid.contentY = bibList.contentY
                if (mouse.button===Qt.LeftButton) {
                    var row = bibList.indexAt(mouse.x, mouse.y+bibList.contentY)
                    bibList.currentIndex = row
                    penaltyGrid.currentIndex = _penaltyCellCount*row + penaltyGrid.currentIndex%25
                }
            }

            onPressed: {
                bibGrid.focus = true;
                if (mouse.button===Qt.RightButton) {
                    print("What to do with bibs from "+_firstRowSelected+" to "+_lastRowSelected)
                    contextMenu.popup()
                }
            }

            Menu {
                id: contextMenu
                width: fontSize*20
                MenuItem {
                    text: (_lastRowSelected-_firstRowSelected)>0?"Verrouiller les "+(_lastRowSelected-_firstRowSelected+1)+" dossards sélectionnés":"Verrouiller le dossard sélectionné"
                    font.pixelSize: fontSize
                    onClicked: bibListModel.lock(_firstRowSelected, _lastRowSelected);

                }
                MenuItem {
                    text: (_lastRowSelected-_firstRowSelected)>0?"Déverrouiller les "+(_lastRowSelected-_firstRowSelected+1)+" dossards sélectionnés":"Déverrouiller le dossard sélectionné"
                    font.pixelSize: fontSize
                    onClicked: bibListModel.unlock(_firstRowSelected, _lastRowSelected);

                }
                MenuItem {
                    text: "Faire suivre à CompetFFCK / FFCanoe"
                    font.pixelSize: fontSize
                    onClicked: bibListModel.forwardBib(_firstRowSelected, _lastRowSelected);
                }

            }

        }

    }

    BibHeader {
        id: bibListHeader
        anchors.left: parent.left
        anchors.top: parent.top
        width: _bibCellWidth
        height: _penaltyCellHeight
        displayTimeData: bibGrid.displayTimeData
        fontSize: bibGrid.fontSize

    }


}


