import QtQuick.Window 2.2
import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1

FocusScope {
    id: app
    focus: true

    property int fontSize: viewcontroller.fontSize

    width: 800
    height: 600


    BibScheduling {
        id: bibScheduling
        anchors.left: parent.left
        anchors.right: displayTimeBox.left
        anchors.top: parent.top
        height: fontSize*2.5
        orderSelection: "number"
        onScheduling: bibList.setScheduling(selection)
        onTabPressed: displayTimeBox.forceActiveFocus()
    }

    CheckBox {
        id: displayTimeBox
        text: "Afficher chronos"
        height: bibScheduling.height
        anchors.right: parent.right
        anchors.top: parent.top
        anchors.rightMargin: 10
        checked: viewcontroller.showChrono
        font.pixelSize: viewcontroller.fontSize*0.9
        onCheckedChanged: viewcontroller.setShowChrono(displayTimeBox.checked)
        hoverEnabled: true
        ToolTip.delay: 1000
        ToolTip.visible: hovered
        ToolTip.text: "Affiche les colonnes départ, arrivée et chrono"

    }


    BibGrid {
        id: bibGrid
        anchors.top: bibScheduling.bottom
        anchors.left: parent.left
        anchors.right: parent.right
        anchors.bottom: comPanel.top
        penaltyGridModel: penaltyListModel
        bibListModel: bibList
        displayTimeData: displayTimeBox.checked
        fontSize: app.fontSize
        focus: true
        onTabPressed: comPanel.forceActiveFocus()

    }

    ComPanel {
        id: comPanel

        height: 206+5.45*viewcontroller.fontSize
        y: app.height - comPanel.height
        anchors.left: parent.left
        anchors.right: parent.right
        fontSize: viewcontroller.fontSize
        //incomingDataModel: incomingDataController
        onToggleOpenShut: {
            if (comPanel.opened) shut()
            else open()
        }
        onOpenRequested: {
            if (!opened) open()
        }

        onTabPressed: menuPanel.open()
        Keys.onEscapePressed: bibGrid.forceActiveFocus()

        function shut() {
            comPanel.opened = false
            comPanel.y = app.height - app.fontSize*2
        }

        function open() {
            comPanel.opened = true
            comPanel.y = app.height - comPanel.height
        }


    }


    Keys.onPressed: {
        print("App level: Pressed key "+event.key)
        if (event.modifiers & Qt.ControlModifier) switch (event.key) {
            case Qt.Key_Minus:
                viewcontroller.incFontSize(-1);
                break;
            case Qt.Key_Plus:
                viewcontroller.incFontSize(1);
                break;
        }
        else switch (event.key) {
            case Qt.Key_M:
                menuPanel.open()
                break;
            case Qt.Key_C:
                comPanel.shown ? comPanel.hide() : comPanel.show()
                break;
        }

    }

    onHeightChanged: {
        comPanel.shut()
    }


}
