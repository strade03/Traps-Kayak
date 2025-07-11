import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1


MouseArea {
    id: headerMouseArea

    property int xShift: 0

    property var columnWidthSum: []
    property var columnWidthList: []
    property var resizableColumnList: []
    property int borderX: -1
    property int cursorX: -1

    property int _pressedX: -1
    property int _pressedIndex: -1

    hoverEnabled: true
    propagateComposedEvents: true

    signal widthChangeRequest(int index, int width)

    onPressed: function(mouse) {
        for (var i=0; i<columnWidthSum.length; i++) {
            if (headerMouseArea.mouseX+xShift>columnWidthSum[i]-6
                    && headerMouseArea.mouseX+xShift<columnWidthSum[i]+6
                    && resizableColumnList[i]) {
                _pressedIndex = i
                _pressedX = headerMouseArea.mouseX
                borderX = i>0?columnWidthSum[i-1]-xShift:0
                cursorX = headerMouseArea.mouseX
                break
            }
        }
        mouse.accepted = false
    }

    onReleased: {
        if (_pressedIndex>-1 && _pressedX>-1) {
            var newWidth = columnWidthList[_pressedIndex]*spreadSheet.fontSize+headerMouseArea.mouseX-_pressedX
            if (newWidth>0) {
                if (newWidth<spreadSheet.fontSize*3) newWidth = spreadSheet.fontSize*3
                widthChangeRequest(_pressedIndex, newWidth)
            }
        }
        _pressedIndex = -1
        _pressedX = -1
        borderX = -1
        cursorX = -1

    }

    onMouseXChanged: function(mouse) {
        headerMouseArea.cursorShape = Qt.ArrowCursor
        for (var i=0; i<columnWidthSum.length; i++) {
            if (headerMouseArea.mouseX+xShift>columnWidthSum[i]-6
                    && headerMouseArea.mouseX+xShift<columnWidthSum[i]+6
                    && resizableColumnList[i]) {
                headerMouseArea.cursorShape = Qt.SizeHorCursor
                break
            }
        }
        if (_pressedIndex>-1) {
            cursorX = headerMouseArea.mouseX
            headerMouseArea.cursorShape = Qt.SizeHorCursor
            mouse.accepted = true
        }
    }

}
