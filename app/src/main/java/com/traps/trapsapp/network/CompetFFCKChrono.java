// Contenu pour Fichier : .\.\CompetFFCKChrono.txt
package com.traps.trapsapp.network;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class CompetFFCKChrono extends CompetFFCKPacket {

    int chrono; // Time in milliseconds

    public CompetFFCKChrono(int bibnumber, int chrono, int runId) {
        this.bibnumber = bibnumber;
        this.chrono = chrono;
        this.runId = runId; // Not used by CompetFFCK protocol but kept for compatibility
    }

    @Override
    public String toString() {
        return "bibnumber=" + bibnumber + " | chrono=" + chrono;
    }

    @Override
    public boolean isValid() {
        if (bibnumber <= 0) return false;
        if (chrono < 0) return false;
        return true;
    }

    /**
     * Returns a byte array representing the text command for CompetFFCK.
     * Format: "chrono <bib> <time_ms>\r"
     * @return byte[] to be sent over the socket.
     */
    @Override
    public byte[] getByteArray() {
        if (!isValid()) {
            return null;
        }

        String command = String.format(Locale.US, "chrono %d %d\r", this.bibnumber, this.chrono);
        
        // Convert the string to bytes using a standard charset. UTF-8 is safe.
        return command.getBytes(StandardCharsets.UTF_8);
    }
}