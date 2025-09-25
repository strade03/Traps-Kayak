// Contenu pour Fichier : .\.\CompetFFCKPacket.txt
package com.traps.trapsapp.network;

import java.nio.charset.StandardCharsets;

/**
 * NOTE: This class and its subclasses have been adapted to generate packets
 * for the CompetFFCK protocol, which is text-based.
 */
public class CompetFFCKPacket {

    protected int bibnumber = 0;

    // runId is no longer used by the CompetFFCK protocol for penalties/chronos,
    // but kept for API compatibility.
    protected int runId = 1;

    public boolean isValid() {
        // A generic packet is not valid, only its subclasses are.
        return false;
    }

    /**
     * Returns a byte array representing the text command for CompetFFCK.
     * @return byte[] to be sent over the socket, or null if the packet is invalid.
     */
    public byte[] getByteArray() {
        return null;
    }

    public int getBibnumber() {
        return bibnumber;
    }

    public int getRunId() {
        return runId;
    }
}