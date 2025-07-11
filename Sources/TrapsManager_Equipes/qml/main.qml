import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1
import QtQuick.Dialogs 1.0


ApplicationWindow {

    id: appWindow
    visible: true
    width: viewcontroller.appWindowWidth
    height: viewcontroller.appWindowHeight
    minimumWidth: 700
    minimumHeight: 500
    title: "TRAPSManager Equipes"

    onWidthChanged: viewcontroller.setAppWindowWidth(appWindow.width)
    onHeightChanged: viewcontroller.setAppWindowHeight(appWindow.height)


    header: ToolBar {

        Image {
            id: watchdog
            anchors.left: parent.left
            anchors.leftMargin: 10
            anchors.verticalCenter: parent.verticalCenter
            height: parent.height*0.6
            width: height
            sourceSize.width: height
            sourceSize.height: width
            fillMode: Image.PreserveAspectFit
            source:"images/access-point-network.svg"
            opacity: 0

            PropertyAnimation {
                id: watchdogAnim
                target: watchdog
                property: "opacity"
                from: 1
                to: 0
                duration: 6000
            }

        }

        Label {
            text: viewcontroller.statusText
            font.pixelSize: viewcontroller.fontSize
            anchors.left: watchdog.right
            anchors.verticalCenter: parent.verticalCenter
            anchors.leftMargin: 20
        }

        ToolButton {
            id: toolButton
            text: "\u2630"
            anchors.right: parent.right
            font.pixelSize: viewcontroller.fontSize
            onClicked: {
                print("You just click the tool button");
                drawer.open();
            }
        }

    }

    TRAPSDrawer {
        id: drawer
        edge: Qt.RightEdge
        width: viewcontroller.fontSize * 17
        height: parent.height

    }

    App {
        id: app
        anchors.fill: parent
    }

    Toaster {
        id: toaster
        fontSize: viewcontroller.fontSize
        anchors.top: parent.top
        anchors.right: parent.right
        maxHeight: app.height
        width: fontSize*15

    }

    // About / software update dialog
    SoftwareUpdate {
        id: softwareUpdate
        fontSize: viewcontroller.fontSize
        Connections {
            target: viewcontroller
            onOpenSoftwareUpdate: softwareUpdate.open()
        }
        onRejected: {
            print("Dialog rejected");
        }

    }

    // Standard dialog box (question, info, errors)
    DialogBox {
        id: dialog
        fontSize: viewcontroller.fontSize
        onAccepted: viewcontroller.dialogAccepted()
        onRejected: viewcontroller.dialogRejected()
        onButtonClicked: viewcontroller.dialogButtonClicked(index)

        Connections {
            target: viewcontroller
            onPopup: {
                dialog.title = dialogBox.title
                dialog.iconName = dialogBox.iconName
                dialog.message = dialogBox.message
                dialog.presetButtons = dialogBox.presetButtons
                dialog.customButtonLabels = dialogBox.customButtonLabels
                dialog.open()
            }
        }

    }

    FileDialog {
        id: fileDialog
        folder: viewcontroller.folder==="" ? shortcuts.home : viewcontroller.folder
        onAccepted: {
            viewcontroller.setFolder(fileDialog.fileUrls)
            viewcontroller.selectedFilePath(fileDialog.fileUrls)
        }
        onRejected: viewcontroller.selectedFilePath("")

    }

    Connections {
        target: viewcontroller
        onPopFileChooser: {
            fileDialog.title = title
            fileDialog.nameFilters = nameFilters
            fileDialog.open()
        }
        onToast: {
            toaster.toast(text, delay)
        }
        onWatchdog: {
            watchdogAnim.running = false
            watchdogAnim.running = true
        }

    }


    Component.onCompleted: viewcontroller.viewReady()


}
