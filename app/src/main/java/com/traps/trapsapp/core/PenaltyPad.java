package com.traps.trapsapp.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.traps.trapsapp.PenaltyActivity;
import com.traps.trapsapp.R;
import com.traps.trapsapp.TerminalConfigActivity; // Pour LAYOUT_MODE_KCROSS

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PenaltyPad implements DialogInterface.OnMultiChoiceClickListener,
        DialogInterface.OnClickListener {

    // Boutons et textes du Pad physique (taille SystemParam.MAX_GATE_PER_TERMINAL)
    private Button[][] penButton = new Button[SystemParam.MAX_GATE_PER_TERMINAL][3];
    private TextView[] penText = new TextView[SystemParam.MAX_GATE_PER_TERMINAL];
    private View[] rowView = new View[SystemParam.MAX_GATE_PER_TERMINAL];
    private Map<Button, PenaltyButton> buttonMap = new HashMap<>();

    // --- Constantes KCross (Affichage seulement) ---
    public static final int SECTOR_TYPE_GATE = 0;
    public static final int SECTOR_TYPE_START = -1;
    public static final int SECTOR_TYPE_FINISH = -2;
    public static final int SECTOR_TYPE_ESKIMO = -3;

    // --- Valeurs de pénalité réelles (communes à Slalom et KCross) ---
    public static final int PENALTY_VALUE_0_CLR = 0;
    public static final int PENALTY_VALUE_2_FLT = 2;
    public static final int PENALTY_VALUE_50_RAL = 50;

    // --- Mode de fonctionnement ---
    private boolean isKCrossMode;
    private PenaltyActivity terminal;
    private TrapsDB db;

    // --- Configuration Slalom ---
    private int[] gateIndex = new int[SystemParam.MAX_GATE_PER_TERMINAL];
    private int[] penalty = new int[SystemParam.MAX_GATE_PER_TERMINAL];
    private boolean[] gateSelection = new boolean[SystemParam.GATE_COUNT];
    private static String[] gateStringListSlalom = new String[SystemParam.GATE_COUNT];
    private boolean[] tmpGateSelectionSlalom = new boolean[SystemParam.GATE_COUNT];
    private int rowCountSlalom = 0;

    // --- Configuration KCross ---
    private static final int MAX_KCROSS_CONFIGURABLE_SECTORS = 9;
    private ArrayList<KCrossRowSetup> kCrossRowSetups = new ArrayList<>(MAX_KCROSS_CONFIGURABLE_SECTORS);
    private int[] padToKCrossSetupIndexMap = new int[SystemParam.MAX_GATE_PER_TERMINAL];


    static {
        for (int i = 0; i < SystemParam.GATE_COUNT; i++) {
            if (i < 9) gateStringListSlalom[i] = "  0" + (i + 1) + "  ";
            else gateStringListSlalom[i] = "  " + (i + 1) + "  ";
        }
    }

    private static class KCrossRowSetup {
        boolean isActive = false;
        int sectorType = SECTOR_TYPE_GATE;
        int gateNumber = 0; // 0-24, if sectorType is GATE
        int currentPenaltyValue = -1;
        // int originalConfigIndex; // Index de 0 à MAX_KCROSS_CONFIGURABLE_SECTORS - 1

        public KCrossRowSetup(/*int originalConfigIndex*/) {
            // this.originalConfigIndex = originalConfigIndex;
        }

        String getDisplayName() {
            if (!isActive) return "";
            switch (sectorType) {
                case SECTOR_TYPE_START: return "  S  ";
                case SECTOR_TYPE_FINISH: return "  F  ";
                case SECTOR_TYPE_ESKIMO: return "  E  ";
                case SECTOR_TYPE_GATE:
                    return (gateNumber < 9) ? "  0" + (gateNumber + 1) + "  " : "  " + (gateNumber + 1) + "  ";
                default: return " ERR ";
            }
        }
    }

    public PenaltyPad(PenaltyActivity terminal, String penaltyLayoutMode) {
        this.terminal = terminal;
        this.isKCrossMode = TerminalConfigActivity.LAYOUT_MODE_KCROSS.equals(penaltyLayoutMode);
        this.db = TrapsDB.getInstance();

        penButton[0][0] = (Button) terminal.findViewById(R.id.penalty00);
        penButton[0][1] = (Button) terminal.findViewById(R.id.penalty01);
        penButton[0][2] = (Button) terminal.findViewById(R.id.penalty02);
        penButton[1][0] = (Button)terminal.findViewById(R.id.penalty10);
		penButton[1][1] = (Button)terminal.findViewById(R.id.penalty11);
		penButton[1][2] = (Button)terminal.findViewById(R.id.penalty12);
		penButton[2][0] = (Button)terminal.findViewById(R.id.penalty20);
		penButton[2][1] = (Button)terminal.findViewById(R.id.penalty21);
		penButton[2][2] = (Button)terminal.findViewById(R.id.penalty22);
		penButton[3][0] = (Button)terminal.findViewById(R.id.penalty30);
		penButton[3][1] = (Button)terminal.findViewById(R.id.penalty31);
		penButton[3][2] = (Button)terminal.findViewById(R.id.penalty32);
		penButton[4][0] = (Button)terminal.findViewById(R.id.penalty40);
		penButton[4][1] = (Button)terminal.findViewById(R.id.penalty41);
		penButton[4][2] = (Button)terminal.findViewById(R.id.penalty42);

        penText[0] = (TextView) terminal.findViewById(R.id.gateText0);
        penText[1] = (TextView)terminal.findViewById(R.id.gateText1);
		penText[2] = (TextView)terminal.findViewById(R.id.gateText2);
		penText[3] = (TextView)terminal.findViewById(R.id.gateText3);
		penText[4] = (TextView)terminal.findViewById(R.id.gateText4);

        rowView[0] = (View) terminal.findViewById(R.id.row0);
        rowView[1] = (View)terminal.findViewById(R.id.row1);
		rowView[2] = (View)terminal.findViewById(R.id.row2);
		rowView[3] = (View)terminal.findViewById(R.id.row3);
		rowView[4] = (View)terminal.findViewById(R.id.row4);

        for(int i=0; i < SystemParam.MAX_GATE_PER_TERMINAL; ++i) padToKCrossSetupIndexMap[i] = -1;

        if (isKCrossMode) {
            loadKCrossConfigFromPrefs();

            boolean atLeastOneActive = false;
            for (KCrossRowSetup setup : kCrossRowSetups) {
                if (setup.isActive) {
                    atLeastOneActive = true;
                    break;
                }
            }
            if (!atLeastOneActive && !kCrossRowSetups.isEmpty()) {
                kCrossRowSetups.get(0).isActive = true;
                kCrossRowSetups.get(0).sectorType = SECTOR_TYPE_GATE;
                kCrossRowSetups.get(0).gateNumber = 0;
            }

            buildButtonMapKCross();
            applyKCrossConfigurationToUI();
        } else {
            resetGateSelectionSlalom();
            setGateSelectionSlalom(db.getGateSelection());
            buildButtonMapSlalom();
        }
        setOnClickListenerToButtons();
    }

    private void buildButtonMapSlalom() {
        buttonMap.clear();
        for (int padRowIndex = 0; padRowIndex < SystemParam.MAX_GATE_PER_TERMINAL; padRowIndex++) {
            buttonMap.put(penButton[padRowIndex][0], new PenaltyButton(padRowIndex, 0, PENALTY_VALUE_0_CLR));
            buttonMap.put(penButton[padRowIndex][1], new PenaltyButton(padRowIndex, 1, PENALTY_VALUE_2_FLT));
            buttonMap.put(penButton[padRowIndex][2], new PenaltyButton(padRowIndex, 2, PENALTY_VALUE_50_RAL));
        }
    }

    private void buildButtonMapKCross() {
        buttonMap.clear();
        for (int padRowIndex = 0; padRowIndex < SystemParam.MAX_GATE_PER_TERMINAL; padRowIndex++) {
            buttonMap.put(penButton[padRowIndex][0], new PenaltyButton(padRowIndex, 0, PENALTY_VALUE_0_CLR));
            buttonMap.put(penButton[padRowIndex][1], new PenaltyButton(padRowIndex, 1, PENALTY_VALUE_2_FLT));
            buttonMap.put(penButton[padRowIndex][2], new PenaltyButton(padRowIndex, 2, PENALTY_VALUE_50_RAL));
        }
    }

    private void setOnClickListenerToButtons() {
        for (View button : buttonMap.keySet()) {
            button.setOnClickListener(v -> {
                PenaltyButton penaltyButtonData = buttonMap.get(v);
                if (penaltyButtonData != null) {
                    setPenalty(penaltyButtonData.getGateIndex(), penaltyButtonData.getValue());
                    if (penaltyButtonData.getValue() == PENALTY_VALUE_0_CLR) {
                        terminal.play(terminal.sndMidPitch);
                    } else {
                        terminal.play(terminal.sndLowPitch);
                    }
                }
            });
        }
    }

    private void resetGateSelectionSlalom() {
        for (int i = 0; i < SystemParam.GATE_COUNT; i++) gateSelection[i] = false;
        rowCountSlalom = 0;
    }

    private void setGateSelectionSlalom(boolean[] selectedGlobalGates) {
        resetGateSelectionSlalom();
        int currentPadRow = 0;
        for (int globalGateIdx = 0; globalGateIdx < selectedGlobalGates.length; globalGateIdx++) {
            if (currentPadRow >= SystemParam.MAX_GATE_PER_TERMINAL) break;
            if (selectedGlobalGates[globalGateIdx]) {
                gateSelection[globalGateIdx] = true;
                gateIndex[currentPadRow] = globalGateIdx;
                rowView[currentPadRow].setVisibility(View.VISIBLE);
                penText[currentPadRow].setText(gateStringListSlalom[globalGateIdx]);
                resetPenaltyUIForPadRow(currentPadRow);
                penalty[currentPadRow] = -1;
                currentPadRow++;
            }
        }
        rowCountSlalom = currentPadRow;
        for (int i = currentPadRow; i < SystemParam.MAX_GATE_PER_TERMINAL; i++) {
            rowView[i].setVisibility(View.GONE);
        }
        db.setGateSelection(gateSelection);
    }

    private void applyKCrossConfigurationToUI() {
        for(int i=0; i < SystemParam.MAX_GATE_PER_TERMINAL; ++i) {
            padToKCrossSetupIndexMap[i] = -1;
            rowView[i].setVisibility(View.GONE);
        }

        int padRowDisplayIndex = 0;
        for (int kCrossSetupIdx = 0; kCrossSetupIdx < kCrossRowSetups.size(); kCrossSetupIdx++) {
            if (padRowDisplayIndex >= SystemParam.MAX_GATE_PER_TERMINAL) {
                break;
            }
            KCrossRowSetup setup = kCrossRowSetups.get(kCrossSetupIdx);
            if (setup.isActive) {
                rowView[padRowDisplayIndex].setVisibility(View.VISIBLE);
                penText[padRowDisplayIndex].setText(setup.getDisplayName());
                resetPenaltyUIForPadRow(padRowDisplayIndex);
                setup.currentPenaltyValue = -1; // Ensure value is reset for the setup itself

                padToKCrossSetupIndexMap[padRowDisplayIndex] = kCrossSetupIdx;
                padRowDisplayIndex++;
            }
        }
    }

    public void setPenalty(int padRowIndex, int value) {
        if (isKCrossMode) {
            setPenaltyKCross(padRowIndex, value);
        } else {
            setPenaltySlalom(padRowIndex, value);
        }
    }

    private void setPenaltySlalom(int padRowIndex, int value) {
        if (padRowIndex < 0 || padRowIndex >= rowCountSlalom) return;
        resetPenaltyUIForPadRow(padRowIndex);
        penalty[padRowIndex] = value;
        highlightButtonForPenalty(padRowIndex, value);
    }

    private void setPenaltyKCross(int padRowIndex, int value) {
        if (padRowIndex < 0 || padRowIndex >= SystemParam.MAX_GATE_PER_TERMINAL) return;
        int kCrossSetupIdx = padToKCrossSetupIndexMap[padRowIndex];
        if (kCrossSetupIdx == -1 || kCrossSetupIdx >= kCrossRowSetups.size()) return;

        resetPenaltyUIForPadRow(padRowIndex);
        kCrossRowSetups.get(kCrossSetupIdx).currentPenaltyValue = value;
        highlightButtonForPenalty(padRowIndex, value);
    }

    private void resetPenaltyUIForPadRow(int padRowIndex) {
        if (padRowIndex < 0 || padRowIndex >= SystemParam.MAX_GATE_PER_TERMINAL) return;
        for (int i = 0; i < 3; i++) {
            penButton[padRowIndex][i].setBackgroundResource(R.drawable.penalty);
            penButton[padRowIndex][i].setTextColor(Color.LTGRAY);
            penButton[padRowIndex][i].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }
    }

    private void highlightButtonForPenalty(int padRowIndex, int penaltyValue) {
        if (padRowIndex < 0 || padRowIndex >= SystemParam.MAX_GATE_PER_TERMINAL) return;
        int buttonColumnIndex = -1;
        switch (penaltyValue) {
            case PENALTY_VALUE_0_CLR: buttonColumnIndex = 0; break;
            case PENALTY_VALUE_2_FLT: buttonColumnIndex = 1; break;
            case PENALTY_VALUE_50_RAL: buttonColumnIndex = 2; break;
        }

        if (buttonColumnIndex != -1) {
            int drawableResId = R.drawable.penalty;
            if (penaltyValue == PENALTY_VALUE_0_CLR) drawableResId = R.drawable.penalty0;
            else if (penaltyValue == PENALTY_VALUE_2_FLT) drawableResId = R.drawable.penalty2;
            else if (penaltyValue == PENALTY_VALUE_50_RAL) drawableResId = R.drawable.penalty50;

            penButton[padRowIndex][buttonColumnIndex].setBackgroundResource(drawableResId);
            penButton[padRowIndex][buttonColumnIndex].setTextColor(Color.BLACK);
            penButton[padRowIndex][buttonColumnIndex].setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
    }

    public SparseIntArray getPenaltyMap() {
        SparseIntArray values = new SparseIntArray();
        if (isKCrossMode) {
            // La clé est l'index du KCrossRowSetup dans la liste kCrossRowSetups (0 à MAX_KCROSS_CONFIGURABLE_SECTORS-1)
            // Seuls les setups actifs sont inclus.
            for (int kCrossSetupIdx = 0; kCrossSetupIdx < kCrossRowSetups.size(); kCrossSetupIdx++) {
                KCrossRowSetup setup = kCrossRowSetups.get(kCrossSetupIdx);
                if (setup.isActive) {
                    values.put(kCrossSetupIdx, setup.currentPenaltyValue);
                }
            }
        } else { // Slalom
            for (int i = 0; i < rowCountSlalom; i++) {
                values.put(gateIndex[i], penalty[i]); // gateIndex[i] est l'index global de la porte
            }
        }
        return values;
    }

    public void setPenaltyMap(SparseIntArray map) {
        if (map == null) return;
        if (isKCrossMode) {
            // D'abord, s'assurer que l'UI est correcte (quelles lignes sont affichées)
            applyKCrossConfigurationToUI(); // Cela réinitialise aussi les currentPenaltyValue des setups actifs affichés

            // Ensuite, appliquer les pénalités de la map aux setups correspondants
            for (int kCrossSetupIdx = 0; kCrossSetupIdx < kCrossRowSetups.size(); kCrossSetupIdx++) {
                KCrossRowSetup setup = kCrossRowSetups.get(kCrossSetupIdx);
                if (setup.isActive) {
                    int penValueFromMap = map.get(kCrossSetupIdx, -1); // Utiliser l'index du setup comme clé
                    setup.currentPenaltyValue = penValueFromMap;

                    // Trouver si ce setup est affiché sur le pad et sur quelle ligne
                    int padRowToUpdate = -1;
                    for(int padIdx = 0; padIdx < SystemParam.MAX_GATE_PER_TERMINAL; ++padIdx) {
                        if(padToKCrossSetupIndexMap[padIdx] == kCrossSetupIdx) {
                            padRowToUpdate = padIdx;
                            break;
                        }
                    }

                    if (padRowToUpdate != -1 && penValueFromMap != -1) {
                        highlightButtonForPenalty(padRowToUpdate, penValueFromMap);
                    } else if (padRowToUpdate != -1) { // penValueFromMap est -1
                        resetPenaltyUIForPadRow(padRowToUpdate); // Assurer que l'UI est réinitialisée si pas de pénalité
                    }
                } else {
                     setup.currentPenaltyValue = -1; // Assurer que les setups inactifs ont -1
                }
            }

        } else { // Slalom
            for (int i = 0; i < rowCountSlalom; i++) {
                resetPenaltyUIForPadRow(i);
                int penValueFromMap = map.get(gateIndex[i], -1);
                penalty[i] = penValueFromMap;
                if (penValueFromMap != -1) {
                    highlightButtonForPenalty(i, penValueFromMap);
                }
            }
        }
    }


    public AlertDialog getDialogGateSelection(Activity activity) {
        if (isKCrossMode) {
            return createKCrossConfigDialog(activity);
        } else {
            tmpGateSelectionSlalom = gateSelection.clone();
            return new AlertDialog.Builder(activity)
                    .setTitle("Choix des portes")
                    .setMultiChoiceItems(gateStringListSlalom, tmpGateSelectionSlalom, this)
                    .setPositiveButton("OK", this)
                    .setNegativeButton("Annuler", this)
                    .create();
        }
    }

    private AlertDialog createKCrossConfigDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Configuration Secteurs KCross ("+MAX_KCROSS_CONFIGURABLE_SECTORS+" max)");

        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_kcross_gate_config, null);
        builder.setView(dialogView);

        final CheckBox[] activeCB = new CheckBox[MAX_KCROSS_CONFIGURABLE_SECTORS];
        final Spinner[] typeSP = new Spinner[MAX_KCROSS_CONFIGURABLE_SECTORS];
        final Spinner[] numberSP = new Spinner[MAX_KCROSS_CONFIGURABLE_SECTORS];
        final TextView[] numberLabels = new TextView[MAX_KCROSS_CONFIGURABLE_SECTORS];

        String[] typeItems = {"Porte", "Départ (S)", "Arrivée (F)", "Esquim. (E)"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, typeItems);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        String[] numberItems = new String[SystemParam.GATE_COUNT];
        for (int i = 0; i < SystemParam.GATE_COUNT; i++) numberItems[i] = String.valueOf(i + 1);
        ArrayAdapter<String> numberAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, numberItems);
        numberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (int i = 0; i < MAX_KCROSS_CONFIGURABLE_SECTORS; i++) {
            try {
                activeCB[i] = dialogView.findViewById(activity.getResources().getIdentifier("kcross_config_active_row_" + i, "id", activity.getPackageName()));
                typeSP[i] = dialogView.findViewById(activity.getResources().getIdentifier("kcross_config_type_spinner_row_" + i, "id", activity.getPackageName()));
                numberSP[i] = dialogView.findViewById(activity.getResources().getIdentifier("kcross_config_number_spinner_row_" + i, "id", activity.getPackageName()));
                numberLabels[i] = dialogView.findViewById(activity.getResources().getIdentifier("kcross_config_number_label_row_" + i, "id", activity.getPackageName()));

                if (activeCB[i] == null || typeSP[i] == null || numberSP[i] == null) {
                    Log.e("PenaltyPad", "KCross Dialog: View not found for row " + i + ". Check dialog_kcross_gate_config.xml");
                    continue;
                }

                typeSP[i].setAdapter(typeAdapter);
                numberSP[i].setAdapter(numberAdapter);

                KCrossRowSetup current = kCrossRowSetups.get(i);
                activeCB[i].setChecked(current.isActive);

                switch (current.sectorType) {
                    case SECTOR_TYPE_START: typeSP[i].setSelection(1); break;
                    case SECTOR_TYPE_FINISH: typeSP[i].setSelection(2); break;
                    case SECTOR_TYPE_ESKIMO: typeSP[i].setSelection(3); break;
                    default: typeSP[i].setSelection(0); break;
                }
                if (current.sectorType == SECTOR_TYPE_GATE && current.gateNumber >= 0 && current.gateNumber < SystemParam.GATE_COUNT) {
                    numberSP[i].setSelection(current.gateNumber);
                } else {
                    numberSP[i].setSelection(0);
                }

                final int finalI = i;
                typeSP[i].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        boolean showNumber = activeCB[finalI].isChecked() && (position == 0);
                        numberSP[finalI].setVisibility(showNumber ? View.VISIBLE : View.GONE);
                        if (numberLabels[finalI]!=null) numberLabels[finalI].setVisibility(showNumber ? View.VISIBLE : View.GONE);
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });

                activeCB[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                    typeSP[finalI].setEnabled(isChecked);
                    numberSP[finalI].setEnabled(isChecked);
                    if (numberLabels[finalI]!=null) numberLabels[finalI].setEnabled(isChecked);

                    boolean showNumber = isChecked && (typeSP[finalI].getSelectedItemPosition() == 0);
                    numberSP[finalI].setVisibility(showNumber ? View.VISIBLE : View.GONE);
                    if (numberLabels[finalI]!=null) numberLabels[finalI].setVisibility(showNumber ? View.VISIBLE : View.GONE);
                });

                boolean isRowActive = activeCB[i].isChecked();
                typeSP[i].setEnabled(isRowActive);
                numberSP[i].setEnabled(isRowActive);
                if (numberLabels[i]!=null) numberLabels[i].setEnabled(isRowActive);

                boolean showNumberSpinnerInitially = isRowActive && (typeSP[i].getSelectedItemPosition() == 0);
                numberSP[i].setVisibility(showNumberSpinnerInitially ? View.VISIBLE : View.GONE);
                if (numberLabels[i]!=null) numberLabels[i].setVisibility(showNumberSpinnerInitially ? View.VISIBLE : View.GONE);
            } catch (Exception e) {
                Log.e("PenaltyPad", "Error initializing dialog row " + i, e);
            }
        }

        builder.setPositiveButton("OK", (dialog, which) -> {
            int activeCount = 0;
            for (int i = 0; i < MAX_KCROSS_CONFIGURABLE_SECTORS; i++) {
                if (activeCB[i] == null) continue;
                if (activeCB[i].isChecked()) activeCount++;
            }

            if (activeCount > SystemParam.MAX_GATE_PER_TERMINAL && activeCount > 0) { // Seuil d'alerte
                 Utility.alert(activity, "Attention: " + activeCount + " secteurs actifs, seuls les " + SystemParam.MAX_GATE_PER_TERMINAL + " premiers seront affichés.", "Alerte");
            } else if (activeCount == 0) {
                // Permettre 0 actif, mais peut-être avertir ?
                // Utility.alert(activity, "Aucun secteur actif. Le pad sera vide.", "Info");
            }


            for (int i = 0; i < MAX_KCROSS_CONFIGURABLE_SECTORS; i++) {
                if (activeCB[i] == null) continue;
                KCrossRowSetup setup = kCrossRowSetups.get(i);
                setup.isActive = activeCB[i].isChecked();
                if (setup.isActive) {
                    int typePos = typeSP[i].getSelectedItemPosition();
                    if (typePos == 0) {
                        setup.sectorType = SECTOR_TYPE_GATE;
                        setup.gateNumber = numberSP[i].getSelectedItemPosition();
                    } else if (typePos == 1) setup.sectorType = SECTOR_TYPE_START;
                    else if (typePos == 2) setup.sectorType = SECTOR_TYPE_FINISH;
                    else if (typePos == 3) setup.sectorType = SECTOR_TYPE_ESKIMO;
                }
            }
            saveKCrossConfigToPrefs();
            applyKCrossConfigurationToUI(); // Crucial pour rafraîchir l'affichage du pad
        });
        builder.setNegativeButton("Annuler", null);
        return builder.create();
    }


    private static final String PREFS_KCROSS_CONFIG = "KCrossConfigPrefs";
    private static final String KEY_KCROSS_CONFIG_COUNT = "kcross_config_count";
    private static final String KEY_KCROSS_ROW_ACTIVE_ = "kcross_row_active_";
    private static final String KEY_KCROSS_ROW_TYPE_ = "kcross_row_type_";
    private static final String KEY_KCROSS_ROW_NUMBER_ = "kcross_row_number_";

    private void saveKCrossConfigToPrefs() {
        SharedPreferences prefs = terminal.getSharedPreferences(PREFS_KCROSS_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_KCROSS_CONFIG_COUNT, kCrossRowSetups.size());
        for (int i = 0; i < kCrossRowSetups.size(); i++) {
            KCrossRowSetup s = kCrossRowSetups.get(i);
            editor.putBoolean(KEY_KCROSS_ROW_ACTIVE_ + i, s.isActive);
            editor.putInt(KEY_KCROSS_ROW_TYPE_ + i, s.sectorType);
            editor.putInt(KEY_KCROSS_ROW_NUMBER_ + i, s.gateNumber);
        }
        editor.apply();
    }

    private void loadKCrossConfigFromPrefs() {
        SharedPreferences prefs = terminal.getSharedPreferences(PREFS_KCROSS_CONFIG, Context.MODE_PRIVATE);
        kCrossRowSetups.clear();
        int savedConfigCount = prefs.getInt(KEY_KCROSS_CONFIG_COUNT, 0);

        for (int i = 0; i < MAX_KCROSS_CONFIGURABLE_SECTORS; i++) {
            KCrossRowSetup setup = new KCrossRowSetup();
            if (i < savedConfigCount && prefs.contains(KEY_KCROSS_ROW_ACTIVE_ + i)) {
                setup.isActive = prefs.getBoolean(KEY_KCROSS_ROW_ACTIVE_ + i, false);
                setup.sectorType = prefs.getInt(KEY_KCROSS_ROW_TYPE_ + i, SECTOR_TYPE_GATE);
                setup.gateNumber = prefs.getInt(KEY_KCROSS_ROW_NUMBER_ + i, i);
            } else {
                setup.isActive = false;
                setup.sectorType = SECTOR_TYPE_GATE;
                setup.gateNumber = i;
            }
            kCrossRowSetups.add(setup);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (!isKCrossMode) {
            tmpGateSelectionSlalom[which] = isChecked;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (!isKCrossMode) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                int counter = 0;
                for (boolean selected : tmpGateSelectionSlalom) if (selected) counter++;
                if (counter > SystemParam.MAX_GATE_PER_TERMINAL) {
                    Utility.alert(terminal, "Trop de portes sélectionnées (" + SystemParam.MAX_GATE_PER_TERMINAL + " max)", "Erreur");
                } else {
                    setGateSelectionSlalom(tmpGateSelectionSlalom);
                }
            }
        }
    }
}