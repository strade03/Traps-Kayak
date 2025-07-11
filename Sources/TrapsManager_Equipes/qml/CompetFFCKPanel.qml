import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1

FocusScope {
    id: competFFCKPanel

    property int fontSize: 18
    property alias connected: connectionSwitch.checked

    signal tabPressed

    focus: true
    width: 400


    Connections {
        target: competFFCK
        onConnectedToTarget: {
            connectionSwitch.checked = true
            busyIndicator.running = false
            busyIndicator.visible = false

        }
        onDisconnectedFromTarget: {
            connectionSwitch.checked = false
            busyIndicator.running = false
            busyIndicator.visible = false

        }
        onConnecting:{
            busyIndicator.visible = true
            busyIndicator.running = true
        }
    }


    FocusScope {
        id: competFFCKSettings

        anchors.left: parent.left
        anchors.top: parent.top
        width: childrenRect.width
        height: childrenRect.height
        anchors.topMargin: 10
        anchors.leftMargin: 10

        Label {
            id: remoteHostButton
            font.pixelSize: competFFCKPanel.fontSize
            anchors.left: parent.left
            anchors.top: parent.top
            anchors.topMargin: 10
            anchors.leftMargin: 10
            text: "Adresse réseau et port de CompetFFCK :"
        }

        TextField {
            id: ipAddress
            font.pixelSize: competFFCKPanel.fontSize
            anchors.left: parent.left
            anchors.leftMargin: 10
            anchors.top: remoteHostButton.bottom
            anchors.topMargin: 10
            selectByMouse: true
            text: competFFCK.host
            width: font.pixelSize*8
            Keys.onReturnPressed: port.forceActiveFocus()
            Keys.onEnterPressed: port.forceActiveFocus()
            onEditingFinished: competFFCK.setHost(ipAddress.displayText)
            enabled: !competFFCKPanel.connected
            ToolTip.delay: 1000
            ToolTip.visible: hovered
            ToolTip.text: "Recopiez l'adresse xxx.xxx.xxx.xxx donnée par la fenêtre\nd'info CompetFFCK une fois démarré l'écran de gestion de course"
            Keys.onEscapePressed: {
                ipAddress.text = competFFCK.host
                ipAddress.focus = false
            }

        }

        Label {
            id: columnLabel
            font.pixelSize: competFFCKPanel.fontSize
            anchors.baseline: ipAddress.baseline
            anchors.left: ipAddress.right
            text: ":"

        }

        TextField {
            id: port
            font.pixelSize: competFFCKPanel.fontSize
            anchors.left: columnLabel.right
            anchors.leftMargin: 15
            anchors.baseline: columnLabel.baseline
            width: font.pixelSize*3
            horizontalAlignment: TextInput.AlignHCenter
            selectByMouse: true
            text: competFFCK.port
            hoverEnabled: true
            ToolTip.delay: 1000
            ToolTip.visible: hovered
            ToolTip.text: "Numéro du port spécifié dans la configuration\ndu périphérique traps.lua (7012 par défaut)"
            onEditingFinished: competFFCK.setPort(port.displayText)
            enabled: !competFFCKPanel.connected
            Keys.onReturnPressed: forwardPenaltyBox.forceActiveFocus()
            Keys.onEnterPressed: forwardPenaltyBox.forceActiveFocus()
            Keys.onEscapePressed: {
                port.text = competFFCK.port
                port.focus = false
            }

        }

        Column {
            id: forwardCheckGroup
            anchors.left: parent.left
            anchors.top: ipAddress.bottom
            anchors.topMargin: 5

            CheckBox {
                id: forwardPenaltyBox
                font.pixelSize: competFFCKPanel.fontSize
                text: "Faire suivre les pénalités"
                checked: competFFCK.forwardPenalty
                hoverEnabled: true
                ToolTip.delay: 1000
                ToolTip.visible: hovered
                ToolTip.text: "Chaque pénalité reçue par TRAPSManager sera renvoyée à CompetFFCK"
                onCheckedChanged: competFFCK.allowForwardPenalty(forwardPenaltyBox.checked)
            }
            CheckBox {
                id: forwardTimeBox
                font.pixelSize: competFFCKPanel.fontSize
                text: "Faire suivre les chronos"
                checked: competFFCK.forwardTime
                hoverEnabled: true
                ToolTip.delay: 1000
                ToolTip.visible: hovered
                ToolTip.text: "Chaque chrono reçu par TRAPSManager sera renvoyé à FFCanoe"
                onCheckedChanged: competFFCK.allowForwardTime(forwardTimeBox.checked)
            }
        }

        Row {
            anchors.left: parent.left
            anchors.top: forwardCheckGroup.bottom
            anchors.leftMargin: 7
            height: connectionSwitch.height
            spacing: 10

            Label {
                id: connectionLabel
                font.pixelSize: competFFCKPanel.fontSize
                text: "Connection à CompetFFCK"
                anchors.verticalCenter: parent.verticalCenter

            }

            Switch {
                id: connectionSwitch
                checked: false
                hoverEnabled: true
                ToolTip.delay: 1000
                ToolTip.visible: hovered
                ToolTip.text: "Activez pour connecter TRAPSManager à CompetFFCK"
                onClicked: competFFCK.requestConnection(connectionSwitch.checked)

            }

            BusyIndicator {
                id: busyIndicator
                running: true
                visible: false
            }

            Label {
                id: bufferLabel
                visible: bufferValue.visible
                font.pixelSize: competFFCKPanel.fontSize
                text: "Tampon: "
                anchors.verticalCenter: parent.verticalCenter
            }

            Label {
                id: bufferValue
                visible: competFFCK.buffer > 0
                font.pixelSize: competFFCKPanel.fontSize
                text: competFFCK.buffer
                anchors.verticalCenter: parent.verticalCenter
            }


        }

    }



    Rectangle {
        id: separator
        color: "gray"
        anchors.left: competFFCKSettings.right
        anchors.top: parent.top
        anchors.leftMargin: 15
        anchors.topMargin: 15
        anchors.bottom: parent.bottom
        width: 2

    }

    Row {
        id: explain1
        anchors.top: parent.top
        anchors.left: separator.right
        anchors.leftMargin: 15
        anchors.topMargin: 15
        spacing: 10
        height: childrenRect.height

        Image {
            source: "images/gestionbouton.png"
        }

        Text {
            font.pixelSize: competFFCKPanel.fontSize
            text: "Dans la fenêtre 'Gestion des pénalités' de CompetFFCK,\nvous trouverez une fenêtre d'information qui\nindiquera l'adresse et le port d'écoute pour TRAPS."
        }


    }

    Text {
        id: line2
        anchors.top: explain1.bottom
        anchors.left: separator.right
        anchors.leftMargin: 15
        anchors.topMargin: 5
        font.pixelSize: competFFCKPanel.fontSize
        text: "Par exemple :"
    }



    Image {
        id: image2
        anchors.top: line2.bottom
        anchors.left: separator.right
        anchors.leftMargin: 15
        anchors.topMargin: 5
        source: "images/infoipport.png"
    }

    Text {
        id: line3
        anchors.top: image2.bottom
        anchors.left: separator.right
        anchors.leftMargin: 15
        anchors.topMargin: 5
        font.pixelSize: competFFCKPanel.fontSize
        text: "Recopiez ces valeurs dans le panneau ci-contre."
    }


    Text {
        id: linkText
        anchors.top: line3.bottom
        anchors.left: separator.right
        anchors.leftMargin: 15
        anchors.topMargin: 15
        font.pixelSize: competFFCKPanel.fontSize
        color: linkTextArea.containsMouse?Material.primary:"black"
        text: "Ouvrir la page web de traps-ck.com expliquant comment\nconfigurer le périphérique TRAPS dans CompetFFCK"
        MouseArea {
            id: linkTextArea
            anchors.fill: parent
            hoverEnabled: true
            onClicked: Qt.openUrlExternally("http://www.traps-ck.com/doku.php?id=doc:configure-traps-device")
        }

    }

}
