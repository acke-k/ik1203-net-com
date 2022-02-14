/*
  1) Skapa socket
  2) Skapa förbindelse
  3) Skicka
  4) Ta emot (vänta)
  5) Stäng ner
 */


package tcpclient;
import java.net.*;
import java.io.*;

public class TCPClient {
    
    public TCPClient() {
	
    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {

	//byte[] fromServerBuff = new byte[1024];
	byte[] fromServerBuff = new byte[1];
	ByteArrayOutputStream fromServerStream = new ByteArrayOutputStream(); // Flexibel storlek, spara all data läst hittils

	Socket clientSock = new Socket(hostname, port); // Skapa socket & anslut
	fromServerStream.write(fromServerBuff); // Kopplar socket till outputstream

	clientSock.getOutputStream().write(toServerBytes); // Skicka data

	//boolean EOF = false;
	
	while(fromServerBuff[0] != 10) {
	    clientSock.getInputStream().read(fromServerBuff);

	    fromServerStream.write(fromServerBuff);
	}
	clientSock.close();
	return fromServerStream.toByteArray();
    }    
}
