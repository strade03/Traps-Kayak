package com.traps.trapsapp.network;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * 16 bits: header 0 0
 * 
 * n bytes ending by \0 : name of the machine 0
 * 
 * 16 bits: race id 0: 15 (LSB) 1: 0 (MSB)
 * 
 * 16 bits: bib 2: LSB bib 3: MSB bib
 * 
 * 16 bits: run id (1 or 2) 4: 1 (LSB) 5: 0 (MSB)
 * 
 * 16 bits: one character 6: D=Au depart, E=En Course, A=A l'Arrivee,
 * I=Intermediaire, J=Intermediaire 2, T=Temps Tournant, H=Heure de depart,
 * F=Heure d'arriv�e, N=Heure intermediaire 1, M=Heure intermediaire 2,
 * P=P�nalit�, G=P�nalit� Globale, U=Annulation (Undo) de la derniere operation
 * pour le dossard, S=Nombre de Satellite, W=Heure de Synchro, K=Top de Synchro,
 * d=Impulsion D�part, a=Impulstion Arriv�e, i=Impulsion inter 1, j=Impulsion
 * inter 2 7: 0
 * 
 * 32 bits: penalty 8: 0, 2, 50 9: 0 10: 0 11: 0
 * 
 * 32 bits: gate (starts at 0) 12: gateId 13: 0 14: 0 15: 0
 **/

public class FFCanoeThread extends Thread {

	// wait for 500ms between each penalty sent
	private static final int FFCLIENT_PAUSE = 500;
	private boolean connected = false;
	private LinkedBlockingQueue<FFCPacket> outputQ;
	private OutputStream outputStream;
	private Socket socket;
	private int runId;


	public FFCanoeThread(Socket socket, int runId) throws UnknownHostException, IOException {
		this.socket = socket;
		this.runId = runId;
		outputStream = socket.getOutputStream();
		connected = true;
		outputQ = new LinkedBlockingQueue<FFCPacket>();
		start();
	}

	
	public void disconnect() {
		
		connected = false;
		
		try {
			socket.close();
		} catch (Exception e) {}
		try {
			outputQ.put(new FFCPacket());
		} catch (Exception e) {}

	}


	public boolean isConnected() {
		return connected;
	}

	public void addPenalty(int bibnumber, int gateIndex, int penalty) {
		try {
			outputQ.put(new FFCPenalty((short) bibnumber, (byte) gateIndex, (byte) penalty, runId));
		} catch (InterruptedException e) {}
	}
	
	public void addChrono(int bibnumber, int chrono) {
		try {
			outputQ.put(new FFCChrono((short) bibnumber, chrono, runId));
		} catch (InterruptedException e) {}
	}
 
 
	private void sleep(int pause) {
		try {
			Thread.sleep(pause);
		} catch (InterruptedException e) {}

	}

	public void run() {

		Log.i("FFCClient", "Starting thread...");
		if (!connected) {
			Log.i("FFCClient", "Client not connected");
			return;
		}

		try {
			while (connected) {
				// wait for a packet to be available
				FFCPacket packet = outputQ.take();
				if (packet.isValid()) {
					outputStream.write(packet.getByteArray());
					outputStream.flush();
					sleep(FFCLIENT_PAUSE);
				} 
			}

		} catch (Exception e) {} 
				
		connected = false;
		try {
			socket.close();
		} catch (Exception e) {}
		Log.i("FFCClient", "Disconnected from FFCanoe");
		
		
	}

	
}
