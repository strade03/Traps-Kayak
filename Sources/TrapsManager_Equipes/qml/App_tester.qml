import QtQuick 2.5

Item {
    visible: true
    width: 800
    height: 600

    Item {
        id: viewcontroller
        property int fontSize: 18
        property bool displayTimeData: false

        function incFontSize() {
            viewcontroller.fontSize += 2
        }

        function decFontSize() {
            if (viewcontroller.fontSize>8) viewcontroller.fontSize -= 2
        }

    }

    Item {
        id: ffcanoeDataController

        property bool forwardPenalty: true
        property bool forwardTime: true
        property bool connectedToTarget: false
        property string host: "192.168.1.10"
        property int port: 7072
        property int runId: 1
        property string errorString: ""


        function acceptHost(inputString) {
            print("Accepting host: "+inputString)
        }


        function acceptPort(inputString) {
            print("Accepting port: "+inputString)
            outgoingDataController.port = 0
            outgoingDataController.port = 7072
        }

        function acceptRunId(inputString) {
            print("Accepting run: "+inputString)
        }

        function toggleForwardPenalty() {
            forwardPenalty = !forwardPenalty
        }
        function toggleForwardTime() {
            forwardTime = !forwardTime
        }
        function toggleConnectToTarget() {
            connectedToTarget = !connectedToTarget
        }

    }

    Item {
        id: incomingDataController
        property bool syncWithCloud: false
        property string username: "username"
        property string password: "password"
        property string errorString: ""
        property string logText: "Line 1\nLine 2\nLine 3\nLine 4\nLine 5\nLine 6\nLine 7\nLine 8\nLine 9\nLine 10\nLine 11\nLine 12\nLine 13\nLine 14\nLine 15\nLine 16"


        function acceptUsername(inputString) {
            print("Accepting username: "+inputString)
        }

        function acceptPassword(inputString) {
            print("Accepting password: "+inputString)
        }

        function toggleSyncWithCloud() {
            syncWithCloud = !syncWithCloud
        }

    }

    App {
        width: parent.width
        height: parent.height

    }


    ListModel {
        id: bibList

        function selectPenalty(row, column) {
            print("Select penalty at row "+row+", column "+column)
        }

        function setScheduling(selection) {
            print("Set scheduling to "+selection)
        }

        ListElement { bibData: "001"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "002"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "003"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "004"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "005"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "006"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "007"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "008"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "009"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "010"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "011"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "012"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "013"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "014"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "015"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "016"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "017"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "018"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "019"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }
        ListElement { bibData: "020"; categData: "K1DM"; scheduleData: "10:11:12"; startTimeData: "10:11:13.25"; finishTimeData: "10:14:11.12" }

        property var penaltyList: [
            "0",
            "0",
            "2",
            "50",
            "0",
            "0",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "0",
            "2",
            "50",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "50"


        ]

    }


}

