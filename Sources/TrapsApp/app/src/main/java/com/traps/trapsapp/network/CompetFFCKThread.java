// Contenu pour Fichier : .\.\CompetFFCKThread.txt
package com.traps.trapsapp.network;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

import android.util.Log;

/**
 * NOTE: This class was originally for CompetFFCK but has been completely adapted
 * to handle the text-based protocol for CompetFFCK.
 * The class name "CompetFFCKThread" is kept for compatibility with the rest of the app.
 */
public class CompetFFCKThread extends Thread {

    // The C++ code for CompetFFCK had a commented-out delay of 250ms.
    // We'll use a small, safe delay. Adjust if needed. 0 might also work.
    private static final int COMPETFFCK_PAUSE = 50; // Pause in milliseconds

    private boolean connected = false;
    private LinkedBlockingQueue<CompetFFCKPacket> outputQ;
    private OutputStream outputStream;
    private Socket socket;
    
    // runId is no longer used by the protocol but kept for API compatibility.
    private int runId;


    public CompetFFCKThread(Socket socket, int runId) throws IOException {
        this.socket = socket;
        this.runId = runId;
        outputStream = socket.getOutputStream();
        connected = true;
        outputQ = new LinkedBlockingQueue<>();
        start();
    }

    public void disconnect() {
        connected = false;
        try {
            socket.close();
        } catch (Exception e) {
            // Ignore
        }
        // Add an empty packet to unblock the outputQ.take() call and allow the thread to terminate
        try {
            outputQ.put(new CompetFFCKPacket());
        } catch (Exception e) {
            // Ignore
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void addPenalty(int bibnumber, int gateIndex, int penalty) {
        try {
            outputQ.put(new CompetFFCKPenalty(bibnumber, gateIndex, penalty, this.runId));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }

    public void addChrono(int bibnumber, int chrono) {
        try {
            outputQ.put(new CompetFFCKChrono(bibnumber, chrono, this.runId));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }

    private void sleep(int pause) {
        try {
            Thread.sleep(pause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }

    public void run() {
        Log.i("CompetFFCKClient", "Starting communication thread...");
        if (!connected) {
            Log.e("CompetFFCKClient", "Client not connected. Thread will exit.");
            return;
        }

        try {
            while (connected) {
                // Wait for a packet to be available
                CompetFFCKPacket packet = outputQ.take();

                if (packet.isValid()) {
                    byte[] data = packet.getByteArray();
                    if (data != null) {
                        outputStream.write(data);
                        outputStream.flush();
                        Log.d("CompetFFCKClient", "Sent: " + new String(data).trim());
                        sleep(COMPETFFCK_PAUSE);
                    }
                } else if (!connected) {
                    // This case handles the empty packet sent during disconnect
                    break;
                }
            }
        } catch (IOException e) {
            Log.e("CompetFFCKClient", "Connection error: " + e.getMessage());
        } catch (InterruptedException e) {
            Log.w("CompetFFCKClient", "Communication thread interrupted.");
            Thread.currentThread().interrupt();
        } finally {
            connected = false;
            try {
                socket.close();
            } catch (Exception e) {
                // Ignore
            }
            Log.i("CompetFFCKClient", "Disconnected from CompetFFCK.");
        }
    }
}