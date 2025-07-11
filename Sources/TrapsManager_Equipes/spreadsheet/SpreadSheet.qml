import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1
import QtQml 2.12


FocusScope {

    id: spreadSheet

    property int fontSize: 18
    property bool scrollOvershoot: true
    property var spreadSheetModel: demoSpreadSheetModel
    property int headerHeight: spreadSheet.fontSize*2.3
    property int rowHeight: spreadSheet.fontSize*2.4
    property bool cellHighlightHovering: true
    property bool rowHighlightHovering: false
    property int firstColumnHighlightHovering: -1
    property int lastColumnHighlightHovering: -1

    property color primaryColor: Material.color(Material.Blue)
    property color colorShade100: Material.color(Material.Blue, Material.Shade100)
    property color colorShade700: Material.color(Material.Blue, Material.Shade700)

    property var columnWidthList: spreadSheetModel.columnWidthList
    property int _leftContentWidth: 0
    property int _rightContentWidth: 0
    property var _leftColumnSum: []
    property var _rightColumnSum: []

    onColumnWidthListChanged: {
        _leftContentWidth = 0
        _rightContentWidth = 0
        _leftColumnSum = []
        _rightColumnSum = []
        var sum = 0;
        for (var i=0; i<columnWidthList.length; i++) {
            if (i===spreadSheetModel.leftColumnCount) sum = 0;
            sum+=columnWidthList[i]*spreadSheet.fontSize

            if (i<spreadSheetModel.leftColumnCount) {
                _leftContentWidth+=columnWidthList[i]*spreadSheet.fontSize
                _leftColumnSum[i]=sum
            }
            else {
                _rightContentWidth+=columnWidthList[i]*spreadSheet.fontSize
                _rightColumnSum[i-spreadSheetModel.leftColumnCount]=sum
            }

        }
        rightTable.contentX = rightHeaderRow.contentX
    }


    focus: true

    property int _selectedColumn: -1
    property bool _editionInProgress: textFieldEditor.visible || comboBoxEditor.visible
    property int _rightHoveredRowIndex: rightTable.hoveredIndexRow
    property int _leftHoveredRowIndex: leftTable.hoveredIndexRow


    Rectangle {
        anchors.fill: parent
        color: spreadSheet.primaryColor

    }

    Item {

        id: leftSpreadSheet
        anchors.top: parent.top
        anchors.bottom: parent.bottom
        anchors.left: parent.left
        width: _leftContentWidth
        visible: width > 0

        HeaderRow {
            id: leftHeaderRow
            anchors.top: parent.top
            anchors.left: parent.left
            anchors.right: parent.right
            columnWidthList: spreadSheetModel.columnWidthList.slice(0, spreadSheetModel.leftColumnCount)
            sortEnabledColumnList: spreadSheetModel.sortEnabledColumnList.slice(0, spreadSheetModel.leftColumnCount)
            model: spreadSheetModel.columnNameList.slice(0, spreadSheetModel.leftColumnCount)
            height: spreadSheet.headerHeight
            onSortByColumn: function(index, asc) {
                spreadSheetModel.sortByColumn(index, asc)
            }
            boundsBehavior: scrollOvershoot?Flickable.DragAndOvershootBounds:Flickable.StopAtBounds
        }

        SpreadSheetTable {
            id: leftTable

            tableId: 0
            anchors.top: leftHeaderRow.bottom
            anchors.left: parent.left
            anchors.right: parent.right
            anchors.bottom: parent.bottom
            boundsBehavior: scrollOvershoot?Flickable.DragAndOvershootBounds:Flickable.StopAtBounds
            model: spreadSheetModel
            columnCount: spreadSheetModel.leftColumnCount
            columnWidthList: spreadSheetModel.columnWidthList.slice(0, spreadSheetModel.leftColumnCount)
            contentWidth: parent.width
            firstIndex: 0
            rowHeight: spreadSheet.rowHeight
            syncHoveredIndexRow: spreadSheet._rightHoveredRowIndex

            onContentYChanged: { // sync both tables
                if (leftTable.movingVertically) rightTable.contentY = leftTable.contentY
            }
            onCurrentIndexChanged: {
                rightTable.currentIndex = leftTable.currentIndex
            }

        }

        HeaderMouseArea {
            id: leftHeaderMouseArea
            anchors.fill: leftHeaderRow

            columnWidthSum: _leftColumnSum
            columnWidthList: spreadSheetModel.columnWidthList.slice(0, spreadSheetModel.leftColumnCount)
            resizableColumnList: spreadSheetModel.resizableColumnList.slice(0, spreadSheetModel.leftColumnCount)

            onWidthChangeRequest: function(index, width) {
                spreadSheetModel.setColumnWidth(index, width/spreadSheet.fontSize)
            }
        }

    }

    Item {

        id: rightSpreadSheet

        anchors.top: parent.top
        anchors.bottom: parent.bottom
        anchors.right: parent.right
        anchors.left: leftSpreadSheet.right
        anchors.leftMargin: 3

        width: _rightContentWidth

        HeaderRow {
            id: rightHeaderRow
            anchors.top: parent.top
            anchors.left: parent.left
            anchors.right: parent.right
            columnWidthList: spreadSheetModel.columnWidthList.slice(spreadSheetModel.leftColumnCount)
            sortEnabledColumnList: spreadSheetModel.sortEnabledColumnList.slice(spreadSheetModel.leftColumnCount)
            height: spreadSheet.headerHeight
            model: spreadSheetModel.columnNameList.slice(spreadSheetModel.leftColumnCount)
            onSortByColumn: function(index, asc) {
                spreadSheetModel.sortByColumn(index+spreadSheetModel.leftColumnCount, asc)
            }
            onContentXChanged: {
                if (rightHeaderRow.movingHorizontally) rightTable.contentX = rightHeaderRow.contentX
            }
            boundsBehavior: scrollOvershoot?Flickable.DragAndOvershootBounds:Flickable.StopAtBounds

        }

        SpreadSheetTable {
            id: rightTable

            tableId: 1
            anchors.top: rightHeaderRow.bottom
            anchors.left: parent.left
            anchors.right: parent.right
            anchors.bottom: parent.bottom
            flickableDirection: Flickable.AutoFlickIfNeeded
            boundsBehavior: scrollOvershoot?Flickable.DragAndOvershootBounds:Flickable.StopAtBounds
            rowHeight: spreadSheet.rowHeight

            ScrollBar.vertical: ScrollBar {
                id: rightVerticalScrollBar
                parent: rightTable.parent
                anchors.top: rightTable.top
                anchors.right: _leftContentWidth>0?rightTable.left:rightTable.right
                anchors.bottom: rightTable.bottom
            }


            ScrollBar.horizontal: ScrollBar {
                id: rightHorizontalScrollBar
                parent: rightTable.parent
                anchors.bottom: rightTable.bottom
                anchors.left: rightTable.left
                anchors.right: rightTable.right
            }

            model: spreadSheetModel
            columnCount: spreadSheetModel.tableColumnCount-spreadSheetModel.leftColumnCount
            firstIndex: spreadSheetModel.leftColumnCount
            columnWidthList: spreadSheetModel.columnWidthList.slice(spreadSheetModel.leftColumnCount)
            contentWidth: _rightContentWidth
            syncHoveredIndexRow: spreadSheet._leftHoveredRowIndex

            onContentYChanged: { // sync both tables
                if (rightTable.movingVertically || rightVerticalScrollBar.pressed) leftTable.contentY = rightTable.contentY
            }
            onContentXChanged: { // sync with header
                if (rightTable.movingHorizontally  || rightHorizontalScrollBar.pressed) rightHeaderRow.contentX = rightTable.contentX
            }
            onCurrentIndexChanged: {
                leftTable.currentIndex = rightTable.currentIndex
            }

        }

        HeaderMouseArea {
            id: rightHeaderMouseArea
            anchors.fill: rightHeaderRow

            xShift: rightHeaderRow.contentX

            columnWidthSum: _rightColumnSum
            columnWidthList: spreadSheetModel.columnWidthList.slice(spreadSheetModel.leftColumnCount)
            resizableColumnList: spreadSheetModel.resizableColumnList.slice(spreadSheetModel.leftColumnCount)

            onWidthChangeRequest: function(index, width) {
                var xcontent = rightTable.contentX
                spreadSheetModel.setColumnWidth(index+spreadSheetModel.leftColumnCount, width/spreadSheet.fontSize)
                rightTable.contentX = xcontent
                rightHeaderRow.contentX = rightTable.contentX

            }

        }

    }


    Rectangle {
        color: Material.primary
        opacity: 0.25
        anchors.top: parent.top
        anchors.bottom: parent.bottom
        x: leftHeaderMouseArea.borderX>-1?leftHeaderMouseArea.borderX:rightHeaderMouseArea.borderX>0?rightHeaderMouseArea.borderX+rightSpreadSheet.x:rightSpreadSheet.x
        width: leftHeaderMouseArea.cursorX>-1?(leftHeaderMouseArea.cursorX-leftHeaderMouseArea.borderX):rightHeaderMouseArea.borderX>0?rightHeaderMouseArea.cursorX-rightHeaderMouseArea.borderX:rightHeaderMouseArea.cursorX
        visible: leftHeaderMouseArea.cursorX>-1 || rightHeaderMouseArea.cursorX>-1

        Rectangle {
            color: "black"
            width: 1
            anchors.top: parent.top
            anchors.bottom: parent.bottom
            anchors.right: parent.right

        }

    }

    Rectangle {
        id: textFieldEditor
        visible: false
        color: spreadSheet.colorShade100

        property int rowIndex: -1
        property int columnIndex: -1
        property alias text: textField.text

        onVisibleChanged: {
            if (textFieldEditor.visible) textField.forceActiveFocus()
        }

        TextField {
            id: textField
            anchors.fill: parent
            padding: 5
            selectByMouse: true
            font.pixelSize: spreadSheet.fontSize
            onAccepted: {
                textFieldEditor.visible = false
                spreadSheetModel.requestTextChange(textFieldEditor.rowIndex,
                                                 textFieldEditor.columnIndex,
                                                 textField.displayText)
            }

            Keys.onEscapePressed: {
                abortEditor()
            }

        }

    }

    Rectangle {
        id: comboBoxEditor

        property alias model: comboBox.model
        property alias currentIndex: comboBox.currentIndex
        property int rowIndex: -1
        property int columnIndex: -1

        visible: false
        color: "white"

        onVisibleChanged: {
            if (comboBoxEditor.visible) {
                comboBox.forceActiveFocus()

            }
        }

        ComboBox {
            id: comboBox
            anchors.fill: parent
            font.pixelSize: spreadSheet.fontSize
            popup.onClosed: {

                spreadSheetModel.requestComboIndexChange(comboBoxEditor.rowIndex,
                                               comboBoxEditor.columnIndex,
                                               comboBox.currentIndex)
            }

        }

    }

    Menu {
        id: contextMenu
        property alias contextMenuList: contextMenuRepeater.model
        property int row: -1
        property int column: -1

        Repeater {
            id: contextMenuRepeater
            MenuItem {
                id: menuItem
                text: modelData
                onTriggered: spreadSheetModel.contextMenuAction(contextMenu.row, contextMenu.column, modelData)
                Component.onCompleted: if (menuItem.implicitWidth>contextMenu.width) contextMenu.width = menuItem.implicitWidth
            }
        }

    }

    Connections {
        target: spreadSheetModel
        function onPopupContextMenu(rowIndex, columnIndex, menuList) {
            contextMenu.width = 100
            contextMenu.contextMenuList = []
            contextMenu.contextMenuList = menuList
            contextMenu.row = rowIndex
            contextMenu.column = columnIndex
            contextMenu.popup()
        }
    }

    function popupTextFieldEditor(table, x, y, width, height,
                                  rowIndex, columnIndex, text) {
        textFieldEditor.x = table*leftSpreadSheet.width+x+2
        textFieldEditor.y = y+rowHeight+4
        textFieldEditor.width = width
        textFieldEditor.height = height+5
        textFieldEditor.visible = true
        textFieldEditor.rowIndex = rowIndex
        textFieldEditor.columnIndex = columnIndex
        textFieldEditor.text = text

    }

    function popupComboBoxEditor(table, x, y, width, height, model,
                                 rowIndex, columnIndex, currentIndex) {

        comboBoxEditor.x = table*leftSpreadSheet.width+x
        comboBoxEditor.y = y+headerHeight
        comboBoxEditor.width = width
        comboBoxEditor.height = height
        comboBoxEditor.visible = true
        comboBoxEditor.model = model
        comboBoxEditor.rowIndex = rowIndex
        comboBoxEditor.columnIndex = columnIndex
        comboBoxEditor.currentIndex = currentIndex
    }

    function abortEditor() {
        textFieldEditor.visible = false
        comboBoxEditor.visible = false

    }

    ListModel {
        id: demoSpreadSheetModel

        property int tableColumnCount: 3
        property var columnWidthList: [8, 8, 8]
        property int leftColumnCount: 0
        property var columnNameList: ["Column A", "Column B", "Column C"]
        property var resizableColumnList: [true, true, true]
        property var sortEnabledColumnList: [true, true, true]

    }

}

