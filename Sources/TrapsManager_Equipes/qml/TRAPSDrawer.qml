import QtQuick 2.10
import QtQuick.Controls 2.3
import QtQuick.Controls.Material 2.1


Drawer {
    id: drawer
    width: 250
    height: 600

    ListView {
        id: drawerListView
        anchors.fill: parent
        focus: true

        model: ListModel {
            ListElement { functionName: "Charger liste de dossards avec fichier PCE\n(export CompetFFCK)"; funtionId: 0 }
            ListElement { functionName: "Charger liste de dossards avec fichier CSV\n(manuel ou export Excel)"; functionId: 1 }
            ListElement { functionName: "Effacer pénalités"; functionId: 8 }
            ListElement { functionName: "Effacer chronos"; functionId: 9 }
            ListElement { functionName: "Police plus grande (CTRL +)"; functionId: 3 }
            ListElement { functionName: "Police plus petite (CTRL -)"; functionId: 4 }
            ListElement { functionName: "A propos"; functionId: 5 }
            ListElement { functionName: "Quitter"; functionId: 6 }
        }

        delegate: MenuRow {
            width: parent.width
            fontSize: viewcontroller.fontSize
            selected: index === drawerListView.currentIndex
            text: functionName
            onHovered: drawerListView.currentIndex = index
            onClicked: {
                print("Click menu id "+functionId)
                switch (functionId) {
                    case 3: {
                        viewcontroller.incFontSize(1);
                        break
                    }
                    case 4: {
                        viewcontroller.incFontSize(-1);
                        break
                    }
                    case 5: {
                        drawer.close();
                        viewcontroller.about();
                        break
                    }
                    case 6: {
                        drawer.close();
                        viewcontroller.quit();
                        break;
                    }
                    case 0: {
                        drawer.close();
                        viewcontroller.loadPCE();
                        break;
                    }
                    case 8: {
                        drawer.close();
                        viewcontroller.clearPenalties();
                        break;
                    }
                    case 9: {
                        drawer.close();
                        viewcontroller.clearChronos();
                        break;
                    }
                    case 1: {
                        drawer.close();
                        viewcontroller.loadTXT();
                        break;
                    }



                }
            }
        }

    }
}
