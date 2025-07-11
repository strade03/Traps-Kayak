import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1
import QtQuick.Dialogs 1.0


ApplicationWindow {

    width: 800
    height: 600
    color: "blue"

    DialogBox {
        id: dialog
        onAccepted: print("Accepted")
        onRejected: print("Rejected")
        onButtonClicked: print("Button clicked: "+index)
        title: "What a great dialog box !"
        iconName: "information"
        //message: "Hello"
        message: "Lorem ipsum dolor sit amet, diam varius fugiat at molestie nunc, parturient phasellus. Phasellus sodales tristique, sociosqu risus vehicula varius sollicitudin lacinia, interdum sodales ultrices, maecenas integer donec mattis enim vel nec, id ligula. Dis iusto, diam viverra et non, ut nisl montes adipiscing, augue pellentesque. Leo tellus eu nullam. Elit ultricies sociis molestie eget, ante et tristique vulputate lectus aliquam eget, varius auctor nam tempus, magna soluta arcu sodales wisi pellentesque ea."
        customButtonLabels: ["This is your first choice", "But you can have this one", "Hey wait ! Here is a third one"]
        //presetButtons: DialogButtonBox.Ok | DialogButtonBox.Cancel


    }

    Component.onCompleted: {
        dialog.open()

    }


}
