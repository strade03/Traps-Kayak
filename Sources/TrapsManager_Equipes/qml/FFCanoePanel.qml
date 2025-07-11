import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1

FocusScope {
    id: ffcanoePanel

    property int fontSize: 18
    property alias connected: connectionSwitch.checked

    signal tabPressed

    focus: true
    width: 400


    Connections {
        target: ffcanoe
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
        id: ffcanoeSettings

        anchors.left: parent.left
        anchors.top: parent.top
        width: childrenRect.width
        height: childrenRect.height
        anchors.topMargin: 10
        anchors.leftMargin: 10

        RadioButton {
            id: localHostButton
            font.pixelSize: ffcanoePanel.fontSize
            anchors.left: parent.left
            anchors.top: parent.top
            checked: ffcanoe.localHost
            text: "FFCanoe tourne sur ce PC"
            hoverEnabled: true
            ToolTip.delay: 1000
            ToolTip.visible: hovered
            ToolTip.text: "FFCanoe est installé et lancé sur le même PC que TRAPSManager"
            onCheckedChanged: ffcanoe.setLocalHost(localHostButton.checked)
            enabled: !ffcanoePanel.connected
        }

        RadioButton {
            id: remoteHostButton
            font.pixelSize: ffcanoePanel.fontSize
            anchors.left: parent.left
            anchors.top: localHostButton.bottom
            checked: !ffcanoe.localHost
            text: "FFCanoe tourne sur un PC distant et son adresse IP est"
            hoverEnabled: true
            ToolTip.delay: 1000
            ToolTip.visible: hovered
            ToolTip.text: "FFCanoe est installé et lancé sur un autre PC du même réseau"
            enabled: !ffcanoePanel.connected
        }

        TextField {
            id: ipAddress
            visible: remoteHostButton.checked
            font.pixelSize: ffcanoePanel.fontSize
            anchors.left: remoteHostButton.right
            anchors.baseline: remoteHostButton.baseline
            anchors.leftMargin: 10
            selectByMouse: true
            text: ffcanoe.host
            width: font.pixelSize*8
            Keys.onReturnPressed: port.forceActiveFocus()
            Keys.onEnterPressed: port.forceActiveFocus()
            onEditingFinished: ffcanoe.setHost(ipAddress.displayText)
            enabled: !ffcanoePanel.connected
            ToolTip.delay: 1000
            ToolTip.visible: hovered
            ToolTip.text: "Numéro IP de la forme xxx.xxx.xxx.xxx"
            Keys.onEscapePressed: {
                ipAddress.text = ffcanoe.host
                ipAddress.focus = false
            }

        }

        Label {
            id: portLabel
            font.pixelSize: ffcanoePanel.fontSize
            anchors.top: remoteHostButton.bottom
            anchors.topMargin: 10
            anchors.leftMargin: 10
            text: "Numéro de port :"
            anchors.left: parent.left
            enabled: !ffcanoePanel.connected
        }

        TextField {
            id: port
            font.pixelSize: ffcanoePanel.fontSize
            anchors.left: portLabel.right
            anchors.leftMargin: 15
            anchors.baseline: portLabel.baseline
            width: font.pixelSize*3
            horizontalAlignment: TextInput.AlignHCenter
            selectByMouse: true
            text: ffcanoe.port
            hoverEnabled: true
            ToolTip.delay: 1000
            ToolTip.visible: hovered
            ToolTip.text: "Numéro du port spécifié dans la configuration réseau de FFCanoe"
            onEditingFinished: ffcanoe.setPort(port.displayText)
            enabled: !ffcanoePanel.connected
            Keys.onReturnPressed: runId.forceActiveFocus()
            Keys.onEnterPressed: runId.forceActiveFocus()
            Keys.onEscapePressed: {
                port.text = ffcanoe.port
                port.focus = false
            }

        }

        Label {
            id: runLabel
            font.pixelSize: ffcanoePanel.fontSize
            anchors.top: remoteHostButton.bottom
            anchors.left: port.right
            anchors.topMargin: 10
            anchors.leftMargin: 20
            text: "Numéro de manche :"
            enabled: !ffcanoePanel.connected
        }

        ComboBox {
            id: runId
            font.pixelSize: ffcanoePanel.fontSize
            anchors.left: runLabel.right
            anchors.leftMargin: 15
            anchors.baseline: runLabel.baseline
            model: ["1", "2"]
            hoverEnabled: true
            ToolTip.delay: 1000
            ToolTip.visible: hovered
            ToolTip.text: "Numéro de la manche en cours de FFCanoe"
            currentIndex: (ffcanoe.runId-1)
            onCurrentTextChanged: ffcanoe.setRunId(runId.currentIndex+1)
            enabled: !ffcanoePanel.connected
        }


        Row {
            id: forwardCheckGroup
            anchors.left: parent.left
            anchors.top: portLabel.bottom
            anchors.topMargin: 10

            CheckBox {
                id: forwardPenaltyBox
                font.pixelSize: ffcanoePanel.fontSize
                text: "Faire suivre les pénalités"
                checked: ffcanoe.forwardPenalty
                hoverEnabled: true
                ToolTip.delay: 1000
                ToolTip.visible: hovered
                ToolTip.text: "Chaque pénalité reçue par TRAPSManager sera renvoyée à FFCanoe"
                onCheckedChanged: ffcanoe.allowForwardPenalty(forwardPenaltyBox.checked)
            }
            CheckBox {
                id: forwardTimeBox
                font.pixelSize: ffcanoePanel.fontSize
                text: "Faire suivre les chronos"
                checked: ffcanoe.forwardTime
                hoverEnabled: true
                ToolTip.delay: 1000
                ToolTip.visible: hovered
                ToolTip.text: "Chaque chrono reçu par TRAPSManager sera renvoyé à FFCanoe"
                onCheckedChanged: ffcanoe.allowForwardTime(forwardTimeBox.checked)
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
                font.pixelSize: ffcanoePanel.fontSize
                text: "Connection à FFCanoe"
                anchors.verticalCenter: parent.verticalCenter

            }

            Switch {
                id: connectionSwitch
                checked: false
                hoverEnabled: true
                ToolTip.delay: 1000
                ToolTip.visible: hovered
                ToolTip.text: "Activez pour connecter TRAPSManager à FFCanoe"
                onClicked: ffcanoe.requestConnection(connectionSwitch.checked)

            }

            BusyIndicator {
                id: busyIndicator
                running: true
                visible: false
            }

            Label {
                id: bufferLabel
                visible: bufferValue.visible
                font.pixelSize: ffcanoePanel.fontSize
                text: "Tampon: "
                anchors.verticalCenter: parent.verticalCenter
            }

            Label {
                id: bufferValue
                visible: ffcanoe.buffer > 0
                font.pixelSize: ffcanoePanel.fontSize
                text: ffcanoe.buffer
                anchors.verticalCenter: parent.verticalCenter
            }


        }

    }

}
