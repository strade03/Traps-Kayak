import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1

ListView {

    id: spreadSheetTable

    property int tableId: 0
    property bool leftSide: false
    property int firstIndex: 0
    property int columnCount: 1
    property var columnWidthList: [ 8 ]
    property int rowHeight: spreadSheet.fontSize*2.4
    property int hoveredIndexRow: -1
    property int syncHoveredIndexRow: -1

    property int _hoveredIndexColumnX1: -1
    property int _hoveredIndexColumnX2: -1

    focus: true

    snapMode: ListView.SnapToItem
    clip: true


    delegate: Item {

        id: itemDelegate
        property int listViewIndex: index

        height: spreadSheetTable.rowHeight
        width: listRow.width

        Row {
            id: listRow
            anchors.top: parent.top
            anchors.left: parent.left
            height: parent.height

            Repeater {
                id: columnRepeater
                model: columnCount

                Cell {
                    id: cell

                    selected: spreadSheetTable.currentIndex === listViewIndex && spreadSheet._selectedColumn === index+firstIndex
                    text: comboModelList[index+firstIndex].length>0?comboModelList[index+firstIndex][comboIndexList[index+firstIndex]]:textList[index+firstIndex]
                    hovered: cellMouseArea.containsMouse && spreadSheet.cellHighlightHovering
                    width: columnWidthList[index]*spreadSheet.fontSize
                    visible: width > 0
                    height: parent.height
                    focus: selected
                    checkable: checkableList[index+firstIndex]
                    checked: checkedList[index+firstIndex]
                    horizontalAlignment: textAlignmentList[index+firstIndex]
                    bgColor: bgColorList[index+firstIndex]===""?"white":bgColorList[index+firstIndex]
                    textColor: textColorList[index+firstIndex]===""?"black":textColorList[index+firstIndex]
                    fontFamily: fontFamilyList[index+firstIndex]===""?"Helvetica":fontFamilyList[index+firstIndex]

                    MouseArea {
                        id: cellMouseArea
                        anchors.fill: parent
                        hoverEnabled: true
                        acceptedButtons: Qt.LeftButton | Qt.RightButton

                        onHoveredChanged: {
                            if (cellMouseArea.containsMouse) {
                                spreadSheetTable.hoveredIndexRow = listViewIndex
                                if (index+firstIndex>=spreadSheet.firstColumnHighlightHovering && index+firstIndex<=spreadSheet.lastColumnHighlightHovering) {
                                    spreadSheetTable._hoveredIndexColumnX1 = cell.x-spreadSheetTable.contentX
                                    spreadSheetTable._hoveredIndexColumnX2 = spreadSheetTable._hoveredIndexColumnX1+cell.width
                                }
                            }
                            else {
                                spreadSheetTable.hoveredIndexRow = -1
                                spreadSheetTable._hoveredIndexColumnX1 = -1
                                spreadSheetTable._hoveredIndexColumnX2 = -1
                            }
                        }

                        onPressed: function(mouse) {
                            if (mouse.button === Qt.RightButton) {
                                spreadSheetModel.requestContextMenu(listViewIndex, index+firstIndex)

                            }
                        }

                        onClicked: function(mouse) {
                            if (mouse.button === Qt.RightButton) return
                            if (mouse.button === Qt.LeftButton) {
                                if (mouse.modifiers & Qt.ControlModifier) {
                                    spreadSheetModel.selectRow(listViewIndex)
                                }
                                else if (mouse.modifiers & Qt.ShiftModifier) {
                                    spreadSheetModel.selectRowRange(spreadSheetTable.currentIndex, listViewIndex)
                                }
                                else {
                                    spreadSheetModel.clearRowSelection()
                                }
                            }


                            spreadSheetTable.currentIndex = listViewIndex
                            spreadSheet._selectedColumn = index+firstIndex
                            if (cell.checkable) spreadSheetModel.requestCheckedChange(listViewIndex, index+firstIndex, !cell.checked)
                            else if (comboModelList[index+firstIndex].length>0) {
                                spreadSheet.popupComboBoxEditor(tableId, cell.x-spreadSheetTable.contentX,
                                                      itemDelegate.y-spreadSheetTable.contentY,
                                                      cell.width,
                                                      cell.height,
                                                      comboModelList[index+firstIndex],
                                                      listViewIndex,
                                                      index+firstIndex,
                                                      comboIndexList[index+firstIndex])
                            }
                        }

                        onDoubleClicked: function(mouse) {
                            if (mouse.button === Qt.RightButton) return
                            if (actionList[index+firstIndex]) {
                                spreadSheetModel.requestAction(listViewIndex, index+firstIndex)
                                return
                            }
                            if (readOnlyList[index+firstIndex]) return
                            if (checkableList[index+firstIndex]) return

                            else {
                                spreadSheet.popupTextFieldEditor(tableId, cell.x-spreadSheetTable.contentX,
                                                      itemDelegate.y-spreadSheetTable.contentY,
                                                      cell.width,
                                                      cell.height,
                                                      listViewIndex,
                                                      index+firstIndex,
                                                      cell.text)
                            }


                        }



                    }

                    Keys.onLeftPressed: {
                        if (spreadSheet._selectedColumn>firstIndex) spreadSheet._selectedColumn--

                    }
                    Keys.onRightPressed: {
                        if (spreadSheet._selectedColumn<firstIndex+columnCount-1) spreadSheet._selectedColumn++
                    }


                }

            }
        }

        Rectangle { // thin line as row separator
            anchors.bottom: parent.bottom
            anchors.left: parent.left
            anchors.right: parent.right
            color: "grey"
            height: 1
        }


        Item {  // highlight row hover
            anchors.fill: parent
            visible: (spreadSheetTable.hoveredIndexRow==listViewIndex || spreadSheetTable.syncHoveredIndexRow==listViewIndex) && spreadSheet.rowHighlightHovering

            Rectangle {
                anchors.top: parent.top
                anchors.topMargin: -1
                anchors.left: parent.left
                anchors.right: parent.right
                color: spreadSheet.primaryColor
                height: 3
            }
            Rectangle {
                anchors.bottom: parent.bottom
                anchors.left: parent.left
                anchors.right: parent.right
                color: spreadSheet.primaryColor
                height: 3
            }
        }

    }

    MouseArea {
        id: spreadSheetTableArea
        anchors.fill: parent
        propagateComposedEvents: true

        onPressed: function(mouse) {
            spreadSheet.abortEditor()
            mouse.accepted = false
            spreadSheetTable.forceActiveFocus()
        }

    }

    Rectangle {
        x: spreadSheetTable._hoveredIndexColumnX1
        visible: x>-1
        anchors.top: parent.top
        anchors.bottom: parent.bottom
        color: "grey"
        width: 1
    }
    Rectangle {
        x: spreadSheetTable._hoveredIndexColumnX2
        visible: x>-1
        anchors.top: parent.top
        anchors.bottom: parent.bottom
        color: "grey"
        width: 1
    }

}
