package org.beynet.utils.sync.impl.tcp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.beynet.utils.sync.api.SyncException;
import org.beynet.utils.sync.api.SyncHost;

public abstract class AbstractTcpSyncHost implements SyncHost {
	
	/**
	 * send the header before the command
	 * @param command
	 * @throws SyncException
	 */
	protected void sendHeader(StringBuffer command,Socket socket) throws IOException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bo);
		dos.writeInt(command.length());
		while (dos.size()!=HEADER_SIZE) {
			dos.write(0);
		}
		// size of the command to send
		socket.getOutputStream().write(bo.toByteArray());
	}
	
	/**
	 * send a command too one host
	 * @param command
	 * @throws SyncException
	 */
	protected void sendCommandOrAnswer(StringBuffer command,Socket socket) throws IOException {
		sendHeader(command,socket);
		socket.getOutputStream().write(command.toString().getBytes());
	}
	
	
	private byte[] read(Socket socket , int expected) throws IOException {
		byte [] response = new byte[expected];
		int readed = 0; 
		while (readed!=expected) {
			int r = socket.getInputStream().read(response,readed,expected-readed);
			if (r>0) {
				readed+=r;
			}
			else {
				throw new IOException("End of stream");
			}
		}
		return(response);	
	}
	
	private byte[] readHeader(Socket socket) throws IOException {
		return(read(socket,HEADER_SIZE));
	}
	
	protected byte[] readCommand(Socket socket) throws IOException {
		byte[] header = readHeader(socket);
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(header));
		int length = dis.readInt();
		return(read(socket,length));
	}
	
	
	public static final int    HEADER_SIZE     = 10         ;
}
