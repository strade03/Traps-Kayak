import QtQuick 2.7

ListView {

    id: toaster

    property int fontSize: 18
    property int maxHeight: 600
    height: contentHeight

    spacing: 5

    width: fontSize*15

    model: ListModel {
        id: listModel
    }

    add: Transition {
        NumberAnimation { properties: "x"; from: toaster.width; duration: 500; easing.type: Easing.OutQuad  }
    }

    removeDisplaced: Transition {
        NumberAnimation { properties: "y"; duration: 200; }
    }

    remove: Transition {
        NumberAnimation { properties: "opacity"; to: 0; duration: 300; }
    }

    delegate: Rectangle {
        color: "black"
        width: toaster.width
        height: textDelegate.height+40

        Text {
            id: textDelegate
            anchors.top: parent.top
            anchors.left: parent.left
            anchors.right: parent.right
            anchors.margins: 20
            text: message
            font.pixelSize: fontSize
            color: "white"
            wrapMode: Text.Wrap
        }

    }

    Timer {
        id: cleanupTimer
        interval: 200
        repeat: false
        onTriggered: cleanupToaster()

    }

    // lifeTime is the minimum time to be displayed
    function toast(message, lifeTime) {
        var expirationDate = ((new Date()).getTime())+lifeTime
        listModel.append({"message":message, "expirationDate": expirationDate})
        if (!cleanupTimer.running) cleanupTimer.start()
    }

    function cleanupToaster() {
        var currentDate = (new Date().getTime())
        while (listModel.count>0 && listModel.get(0).expirationDate<currentDate) {  // as long as there are some toasts in the list and their date has expired
            listModel.remove(0)
        }
        if (toaster.height>maxHeight) listModel.remove(0)
        if (listModel.count==0) cleanupTimer.stop()
        else cleanupTimer.start()

    }

//    focus: true
//    Keys.onSpacePressed: {
//        toast("Another message from god !", 3000)

//    }



}
