import QtQuick 2.10

Loader {
    id: dialogLoader

    property string name: ""

    anchors.fill: parent
    asynchronous: true
    visible: name!==""

    function open(dialogName) {
        if (name==="") {
            name = dialogName
            dialogLoader.source = name+".qml"
        }
        else print("Dialog already opened: "+name)
    }

    function close() {
        if (name!=="") {
            dialogLoader.sourceComponent = undefined
            name = ""
        }
        else print("No dialog opened")

    }


}
