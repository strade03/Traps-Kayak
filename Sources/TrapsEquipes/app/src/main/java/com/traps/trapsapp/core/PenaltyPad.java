package com.traps.trapsequipes.core;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.traps.trapsequipes.PenaltyActivity;
import com.traps.trapsequipes.R;

public class PenaltyPad implements DialogInterface.OnMultiChoiceClickListener,
        DialogInterface.OnClickListener {

    // Ces tableaux ont une taille MAX_GATE_PER_TERMINAL (nombre total de lignes de jugement)
    private Button[][] penButton = new Button[SystemParam.MAX_GATE_PER_TERMINAL][3];
    // TextView pour la lettre de jugement 'a', 'b', 'c' pour chaque ligne de jugement
    private TextView[] penTextJudgmentLetter = new TextView[SystemParam.MAX_GATE_PER_TERMINAL];
    // Vue racine pour chaque ligne de jugement (le LinearLayout de penalty_judgment_row_item.xml)
    private View[] judgmentRowView = new View[SystemParam.MAX_GATE_PER_TERMINAL];

    // Map reliant le bouton à ses infos (rowIndex, valeur de pénalité)
    private Map<Button, PenaltyButtonInfo> buttonMap = new HashMap<>();

    // gateIndex[rowIndex] stocke l'index réel de la PORTE (0 à GATE_COUNT-1)
    // à laquelle la ligne de jugement 'rowIndex' est associée.
    private int[] gateActualIndexForRow = new int[SystemParam.MAX_GATE_PER_TERMINAL];

    // penalty[rowIndex] stocke la pénalité pour la ligne de jugement 'rowIndex'
    private int[] penalty = new int[SystemParam.MAX_GATE_PER_TERMINAL];

    // gateIsSelectedForDisplay[actualGateIndex] est true si la PORTE est sélectionnée pour affichage
    private boolean[] gateIsSelectedForDisplay = new boolean[SystemParam.GATE_COUNT];

    private static String[] gateStringList = new String[SystemParam.GATE_COUNT]; // Pour le dialogue de sélection
    private static String[] gateStringListDialogue = new String[SystemParam.GATE_COUNT/3]; // Pour le dialogue de sélection
    private boolean[] tmpGateSelectionDialog = new boolean[SystemParam.GATE_COUNT/3];

    private PenaltyActivity terminal;
    // rowCount est le nombre de lignes de jugement *actuellement actives et visibles*
    // Ce sera un multiple de 3 (ou moins si pas toutes les portes max sont sélectionnées)
    private int activeJudgmentRowCount = 0;
    private TrapsDB db;
    private final char[] judgmentChars = {'a', 'b', 'c'};

    // Pour gérer les en-têtes de porte "Porte 0X"
    // On en a besoin d'au plus GATE_COUNT / 3 (arrondi sup) si MAX_GATE_PER_TERMINAL est grand,
    // ou plus simplement, on peut en créer MAX_GATE_PER_TERMINAL / 3.
    // Pour la simplicité, on va créer un nombre suffisant et les cacher/montrer.
    private final int MAX_POSSIBLE_HEADERS = (SystemParam.MAX_GATE_PER_TERMINAL / 3) + (SystemParam.MAX_GATE_PER_TERMINAL % 3 > 0 ? 1 : 0);
    private View[] gateHeaderViews = new View[MAX_POSSIBLE_HEADERS];
    private TextView[] gateHeaderTextviews = new TextView[MAX_POSSIBLE_HEADERS];
    private LinearLayout penaltyGroupsContainer; // Le conteneur du layout principal

    static {
        for (int i = 0; i < SystemParam.GATE_COUNT; i++) {
            if (i < 9) gateStringList[i] = " 0" + (i + 1) + " ";
            else gateStringList[i] = " " + (i + 1) + " ";
        }
    }
    static {
        for (int i = 0; i < SystemParam.GATE_COUNT/3; i++) {
            if (i < 9) gateStringListDialogue[i] = " 0" + (i + 1) + " ";
            else gateStringListDialogue[i] = " " + (i + 1) + " ";
        }
    }

    public int getActiveJudgmentRowCount() {
        return activeJudgmentRowCount;
    }

    private void hideAllUIElements() {
        for (int i = 0; i < MAX_POSSIBLE_HEADERS; i++) {
            if (gateHeaderViews[i] != null) gateHeaderViews[i].setVisibility(View.GONE);
        }
        for (int i = 0; i < SystemParam.MAX_GATE_PER_TERMINAL; i++) {
            if (judgmentRowView[i] != null) judgmentRowView[i].setVisibility(View.GONE);
        }
        activeJudgmentRowCount = 0;
    }

    private void setGateSelectionFromDialog(boolean[] selectedGatesFromDialog) {
        // 1. Mettre à jour notre état interne gateIsSelectedForDisplay
        System.arraycopy(selectedGatesFromDialog, 0, gateIsSelectedForDisplay, 0, Math.min(selectedGatesFromDialog.length, gateIsSelectedForDisplay.length));

        // 2. Cacher tous les éléments UI
        hideAllUIElements();

        int currentJudgmentRowIndex = 0; // Index pour nos tableaux penButton, penalty, etc. (0 à MAX_GATE_PER_TERMINAL-1)
        int currentHeaderIndex = 0;

        // 3. Parcourir toutes les portes possibles (0 à GATE_COUNT-1)
        for (int actualGateIdx = 0; actualGateIdx < SystemParam.GATE_COUNT; actualGateIdx++) {
            if (gateIsSelectedForDisplay[actualGateIdx]) { // Si cette PORTE est sélectionnée
                if (currentJudgmentRowIndex + 2 >= SystemParam.MAX_GATE_PER_TERMINAL) {
                    Log.w("PenaltyPad", "Not enough judgment rows in MAX_GATE_PER_TERMINAL to display all selected gates fully.");
                    break; // Plus de place pour afficher les 3 jugements de cette porte
                }
                if (currentHeaderIndex >= MAX_POSSIBLE_HEADERS) {
                     Log.w("PenaltyPad", "Not enough header views allocated.");
                     break;
                }

                // Afficher l'en-tête de la porte
                if (gateHeaderViews[currentHeaderIndex] != null) {
                    gateHeaderViews[currentHeaderIndex].setVisibility(View.VISIBLE);
                }
                if (gateHeaderTextviews[currentHeaderIndex] != null) {
                    gateHeaderTextviews[currentHeaderIndex].setText("Porte" + gateStringList[actualGateIdx]);
                }
                currentHeaderIndex++;

                // Configurer et afficher les 3 lignes de jugement pour cette porte
                for (int judgmentSubIndex = 0; judgmentSubIndex < 3; judgmentSubIndex++) {
                    if (judgmentRowView[currentJudgmentRowIndex] != null) {
                        judgmentRowView[currentJudgmentRowIndex].setVisibility(View.VISIBLE);
                    }
                    // Le penTextJudgmentLetter est déjà setté dans le constructeur
                    
                    gateActualIndexForRow[currentJudgmentRowIndex] = actualGateIdx; // Associer cette ligne à la porte
                    resetPenaltyForRow(currentJudgmentRowIndex); // Réinitialise les boutons de cette ligne

                    currentJudgmentRowIndex++;
                }
            }
        }
        activeJudgmentRowCount = currentJudgmentRowIndex; // Nombre total de lignes de jugement actives
        db.setGateSelection(gateIsSelectedForDisplay); // Sauvegarder la sélection de PORTES
    }


    public boolean[] getGateSelectionForDialog() {
        return gateIsSelectedForDisplay.clone();
    }

    public boolean noPenalty() {
        for (int i = 0; i < activeJudgmentRowCount; i++) {
            if (penalty[i] > -1) return false;
        }
        return true;
    }

    // Retourne la pénalité pour une ligne de jugement spécifique
    public int getPenaltyForRow(int judgmentRowIndex) {
        if (judgmentRowIndex < 0 || judgmentRowIndex >= activeJudgmentRowCount) return -1;
        return penalty[judgmentRowIndex];
    }

    /**
     * Retourne un SparseIntArray où la clé est l'index de la ligne de jugement
     * (0 à MAX_GATE_PER_TERMINAL-1, mais seulement celles actives)
     * et la valeur est la pénalité.
     * Cet index de ligne est celui que votre "moteur d'envoi" attend.
     */

    public SparseIntArray getPenaltyMap() {
        SparseIntArray values = new SparseIntArray();
        // Parcourir les lignes de jugement actuellement actives sur l'écran
        for (int judgmentRowDisplayIndex = 0; judgmentRowDisplayIndex < activeJudgmentRowCount; judgmentRowDisplayIndex++) {
            if (penalty[judgmentRowDisplayIndex] > -1) { // S'il y a une pénalité pour cette ligne affichée
                // Récupérer l'index réel de la porte associée à cette ligne d'affichage
                int actualGateIndex = gateActualIndexForRow[judgmentRowDisplayIndex];
                // Déterminer si c'est le jugement 'a', 'b', ou 'c' (0, 1, ou 2)
                // Cela est basé sur la position de la ligne DANS LE GROUPE DE 3 pour sa porte.
                // Si les judgmentRowDisplayIndex sont séquentiels pour une porte (ex: 0,1,2 pour porte X; 3,4,5 pour porte Y),
                // alors judgmentRowDisplayIndex % 3 donne le judgment_sub_index (0 pour a, 1 pour b, 2 pour c)
                // pour la porte actuellemennt traitée.
                int judgmentSubIndex = judgmentRowDisplayIndex % 3; // Ceci suppose que les lignes d'une porte sont groupées dans l'UI

                // Calculer l'index absolu de la pénalité pour le moteur d'envoi
                int absolutePenaltyIndex = (actualGateIndex * 3) + judgmentSubIndex;

                values.put(absolutePenaltyIndex, penalty[judgmentRowDisplayIndex]);
            }
        }
        return values;
    }

    /**
     * Applique les pénalités. La clé de la map est l'index de la ligne de jugement.
     */
   public void setPenaltyMap(SparseIntArray mapWithAbsoluteIndices) {
        // D'abord, réinitialiser toutes les pénalités des lignes actives affichées
        for (int i = 0; i < activeJudgmentRowCount; i++) {
            setPenaltyForRow(i, -1); // -1 pour réinitialiser
        }

        // Ensuite, appliquer celles de la map
        // Parcourir les lignes de jugement actuellement affichées sur l'écran
        for (int judgmentRowDisplayIndex = 0; judgmentRowDisplayIndex < activeJudgmentRowCount; judgmentRowDisplayIndex++) {
            int actualGateIndex = gateActualIndexForRow[judgmentRowDisplayIndex];
            int judgmentSubIndex = judgmentRowDisplayIndex % 3; //  0 pour 'a', 1 pour 'b', 2 pour 'c' de la porte affichée

            int absolutePenaltyIndexToLookup = (actualGateIndex * 3) + judgmentSubIndex;

            // Vérifier si cette pénalité absolue existe dans la map fournie
            int penaltyValue = mapWithAbsoluteIndices.get(absolutePenaltyIndexToLookup, -1); // -1 si non trouvé

            if (penaltyValue != -1) {
                setPenaltyForRow(judgmentRowDisplayIndex, penaltyValue);
            }
        }
    }


    private void resetPenaltyForRow(int judgmentRowIndex) {
        if (judgmentRowIndex < 0 || judgmentRowIndex >= SystemParam.MAX_GATE_PER_TERMINAL) {
            return;
        }
        for (int btnIdx = 0; btnIdx < 3; btnIdx++) {
            if (penButton[judgmentRowIndex][btnIdx] != null) {
                penButton[judgmentRowIndex][btnIdx].setBackgroundResource(R.drawable.penalty);
                penButton[judgmentRowIndex][btnIdx].setTextColor(Color.LTGRAY);
                penButton[judgmentRowIndex][btnIdx].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }
        }
        penalty[judgmentRowIndex] = -1;
    }

    public void setPenaltyForRow(int judgmentRowIndex, int value) {
        if (judgmentRowIndex < 0 || judgmentRowIndex >= SystemParam.MAX_GATE_PER_TERMINAL) {
            return;
        }
        resetPenaltyForRow(judgmentRowIndex); // Réinitialise la ligne

        Button targetButton = null;
        int penaltyValueToStore = -1;
        int backgroundResource = R.drawable.penalty;
        int buttonIndexToHighlight = -1;

        switch (value) {
            case 0:
                buttonIndexToHighlight = 0;
                penaltyValueToStore = 0;
                backgroundResource = R.drawable.penalty0;
                break;
            case 2:
                buttonIndexToHighlight = 1;
                penaltyValueToStore = 2;
                backgroundResource = R.drawable.penalty2;
                break;
            case 50:
                buttonIndexToHighlight = 2;
                penaltyValueToStore = 50;
                backgroundResource = R.drawable.penalty50;
                break;
            case -1: // Cas spécial pour juste réinitialiser sans sélectionner
                 penalty[judgmentRowIndex] = -1;
                 return; // Pas de bouton à mettre en surbrillance
        }

        if (buttonIndexToHighlight != -1 && penButton[judgmentRowIndex][buttonIndexToHighlight] != null) {
            targetButton = penButton[judgmentRowIndex][buttonIndexToHighlight];
            targetButton.setBackgroundResource(backgroundResource);
            targetButton.setTextColor(Color.BLACK);
            targetButton.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        penalty[judgmentRowIndex] = penaltyValueToStore;
    }

    private void setOnClickListeners() {
        for (Button button : buttonMap.keySet()) {
            if (button == null) continue;
            button.setOnClickListener(v -> {
                PenaltyButtonInfo buttonData = buttonMap.get(v);
                if (buttonData != null) {
                    setPenaltyForRow(buttonData.getJudgmentRowIndex(), buttonData.getPenaltyValue());
                    if (buttonData.getPenaltyValue() == 0) terminal.play(terminal.sndMidPitch);
                    else terminal.play(terminal.sndLowPitch);
                }
            });
        }
    }

    private void buildButtonMap() {
        buttonMap.clear();
        // Boucle sur toutes les lignes de jugement possibles (0 à MAX_GATE_PER_TERMINAL-1)
        for (int judgmentRowIdx = 0; judgmentRowIdx < SystemParam.MAX_GATE_PER_TERMINAL; judgmentRowIdx++) {
            if (penButton[judgmentRowIdx][0] != null) {
                buttonMap.put(penButton[judgmentRowIdx][0], new PenaltyButtonInfo(judgmentRowIdx, 0));
            }
            if (penButton[judgmentRowIdx][1] != null) {
                buttonMap.put(penButton[judgmentRowIdx][1], new PenaltyButtonInfo(judgmentRowIdx, 2));
            }
            if (penButton[judgmentRowIdx][2] != null) {
                buttonMap.put(penButton[judgmentRowIdx][2], new PenaltyButtonInfo(judgmentRowIdx, 50));
            }
        }
    }


    public PenaltyPad(PenaltyActivity terminal) {
        this.terminal = terminal;
        db = TrapsDB.getInstance();

        penaltyGroupsContainer = terminal.findViewById(R.id.penalty_groups_container);
        if (penaltyGroupsContainer == null) {
            Log.e("PenaltyPad", "CRITICAL: penalty_groups_container not found!");
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(terminal);

        int currentHeaderViewIndex = 0;
        // Créer toutes les vues, elles seront cachées/montrées par setGateSelectionFromDialog
        for (int judgmentRowIdx = 0; judgmentRowIdx < SystemParam.MAX_GATE_PER_TERMINAL; judgmentRowIdx++) {
            // Ajouter un en-tête de porte toutes les 3 lignes de jugement
            if (judgmentRowIdx % 3 == 0) {
                if (currentHeaderViewIndex < MAX_POSSIBLE_HEADERS) {
                    View headerInflated = inflater.inflate(R.layout.gate_header_item, penaltyGroupsContainer, false);
                    gateHeaderViews[currentHeaderViewIndex] = headerInflated;
                    gateHeaderTextviews[currentHeaderViewIndex] = headerInflated.findViewById(R.id.gate_number_header_text);
                    headerInflated.setVisibility(View.GONE);
                    penaltyGroupsContainer.addView(headerInflated);
                    currentHeaderViewIndex++;
                } else {
                    Log.w("PenaltyPad", "Ran out of pre-allocated header views during construction.");
                }
            }

            // Inflater la ligne de jugement
            View judgmentRowInflated = inflater.inflate(R.layout.penalty_judgment_row_item, penaltyGroupsContainer, false);
            judgmentRowView[judgmentRowIdx] = judgmentRowInflated;

            penTextJudgmentLetter[judgmentRowIdx] = judgmentRowInflated.findViewById(R.id.judgment_letter_item);
            if (penTextJudgmentLetter[judgmentRowIdx] != null) {
                penTextJudgmentLetter[judgmentRowIdx].setText(String.valueOf(judgmentChars[judgmentRowIdx % 3]));
            }

            penButton[judgmentRowIdx][0] = judgmentRowInflated.findViewById(R.id.penalty_button_0_item);
            penButton[judgmentRowIdx][1] = judgmentRowInflated.findViewById(R.id.penalty_button_1_item);
            penButton[judgmentRowIdx][2] = judgmentRowInflated.findViewById(R.id.penalty_button_2_item);

            judgmentRowInflated.setVisibility(View.GONE);
            penaltyGroupsContainer.addView(judgmentRowInflated);

            // Séparateur fin entre les lignes de jugement d'une même porte (sauf après 'c')
            if (judgmentRowIdx % 3 != 2 && judgmentRowIdx < SystemParam.MAX_GATE_PER_TERMINAL -1 ) {
                 View separator = new View(terminal);
                 LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1); // 1px height
                 separator.setLayoutParams(params);
                 separator.setBackgroundColor(Color.parseColor("#444444"));
                 penaltyGroupsContainer.addView(separator);
            }
            // Séparateur plus épais entre les groupes de portes (après chaque groupe de 3 lignes, sauf le dernier)
            if (judgmentRowIdx % 3 == 2 && judgmentRowIdx < SystemParam.MAX_GATE_PER_TERMINAL - 1) {
                View thickSeparator = new View(terminal);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) (3 * terminal.getResources().getDisplayMetrics().density + 0.5f)); // 3dp
                thickSeparator.setLayoutParams(params);
                thickSeparator.setBackgroundColor(Color.parseColor("#666666"));
                penaltyGroupsContainer.addView(thickSeparator);
            }
        }

        buildButtonMap(); // Construit la map avec tous les boutons possibles
        setOnClickListeners();

        // Charger la sélection de PORTES initiale
        boolean[] initialGateSelection = db.getGateSelection(); // Devrait être de taille GATE_COUNT
        if (initialGateSelection != null && initialGateSelection.length == gateIsSelectedForDisplay.length) {
            System.arraycopy(initialGateSelection, 0, gateIsSelectedForDisplay, 0, initialGateSelection.length);
        } else {
            Log.i("PenaltyPad", "No valid saved gate selection, or length mismatch. Defaulting to none.");
            // Initialise gateIsSelectedForDisplay à false
            for (int i = 0; i < gateIsSelectedForDisplay.length; i++) gateIsSelectedForDisplay[i] = false;
        }
        setGateSelectionFromDialog(gateIsSelectedForDisplay); // Applique la sélection et met à jour l'UI

        // TODO: Charger les pénalités sauvegardées. db.getPenalties() devrait retourner un SparseIntArray
        // où la clé est l'index de la ligne de jugement (0 à MAX_GATE_PER_TERMINAL-1)
        // SparseIntArray savedPenalties = db.getPenalties(); // Supposons que cette méthode existe et retourne le bon format
        // if (savedPenalties != null) {
            // setPenaltyMap(savedPenalties);
        // }
    }


    public AlertDialog getDialogGateSelection(Activity activity) {
        // Le dialogue de sélection opère sur les PORTES (gateIsSelectedForDisplay)
        tmpGateSelectionDialog = gateIsSelectedForDisplay.clone();
        return new AlertDialog.Builder(activity)
                .setTitle("Choix des portes")
                .setMultiChoiceItems(gateStringListDialogue, tmpGateSelectionDialog, this)
                .setPositiveButton("OK", this)
                .setNegativeButton("Annuler", this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        // Pour setMultiChoiceItems (sélection des PORTES)
        if (which >= 0 && which < tmpGateSelectionDialog.length) {
            tmpGateSelectionDialog[which] = isChecked;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // Pour les boutons du dialogue
        if (which == DialogInterface.BUTTON_POSITIVE) {
            int selectedPorteCount = 0;
            for (boolean selected : tmpGateSelectionDialog) {
                if (selected) selectedPorteCount++;
            }

            // MAX_GATE_PER_TERMINAL est le nombre max de lignes de jugement.
            // Le nombre max de portes affichables est donc MAX_GATE_PER_TERMINAL / 3.
            int maxPortesAffichables = SystemParam.MAX_GATE_PER_TERMINAL / 3;

            if (selectedPorteCount > maxPortesAffichables) {
                new AlertDialog.Builder(terminal)
                        .setTitle("Trop de portes sélectionnées (" + maxPortesAffichables + " max pour affichage)")
                        .setNeutralButton("OK", null)
                        .create()
                        .show();
            } else if (selectedPorteCount == 0 && maxPortesAffichables > 0) {
                new AlertDialog.Builder(terminal)
                        .setTitle("Au moins une porte doit être sélectionnée")
                        .setNeutralButton("OK", null)
                        .create()
                        .show();
            } else {
                // Appliquer la sélection des PORTES
                setGateSelectionFromDialog(tmpGateSelectionDialog);
            }
        }
    }

    // Classe interne pour stocker les infos d'un bouton de pénalité
    // Le gateActualIndex n'est plus nécessaire ici car on opère sur judgmentRowIndex
    private static class PenaltyButtonInfo {
        private int judgmentRowIndex; // L'index de la ligne de jugement (0 à MAX_GATE_PER_TERMINAL-1)
        private int penaltyValue;     // 0, 2, ou 50

        public PenaltyButtonInfo(int judgmentRowIndex, int penaltyValue) {
            this.judgmentRowIndex = judgmentRowIndex;
            this.penaltyValue = penaltyValue;
        }

        public int getJudgmentRowIndex() { return judgmentRowIndex; }
        public int getPenaltyValue() { return penaltyValue; }
    }
}