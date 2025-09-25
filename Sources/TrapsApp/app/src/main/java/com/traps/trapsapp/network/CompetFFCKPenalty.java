// Contenu pour Fichier : .\.\CompetFFCKPenalty.txt
package com.traps.trapsapp.network;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class CompetFFCKPenalty extends CompetFFCKPacket {

    int penalty;
    int gateIndex;

    public CompetFFCKPenalty(int bibnumber, int gateIndex, int penalty, int runId) {
        this.bibnumber = bibnumber;
        this.penalty = penalty;
        this.gateIndex = gateIndex;
        this.runId = runId; // Not used by CompetFFCK protocol but kept for compatibility
    }

    @Override
    public String toString() {
        return "bibnumber=" + bibnumber + " | gateIndex=" + gateIndex + " | penalty=" + penalty;
    }

    @Override
    public boolean isValid() {
        if (bibnumber <= 0) return false;
        if (gateIndex < 0) return false; // Gate numbers are positive
        if (penalty < 0) return false;
        return true;
    }

    /**
     * Returns a byte array representing the text command for CompetFFCK.
     * Format: "penalty <bib> <gate> 1 <penalty>\r"
     * @return byte[] to be sent over the socket.
     */
    @Override
    public byte[] getByteArray() {
        if (!isValid()) {
            return null;
        }

        // The '1' is a constant value seen in the C++ code for CompetFFCK.
        String command = String.format(Locale.US, "penalty %d %d 1 %d\r", this.bibnumber, this.gateIndex, this.penalty);
        
        // Convert the string to bytes using a standard charset. UTF-8 is safe.
        return command.getBytes(StandardCharsets.UTF_8);
    }
}