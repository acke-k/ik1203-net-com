package tcpclient;
import java.net.*;
import java.io.*;
    

public class TCPClient {
    
    public static final int BUFFER_SIZE = 1024; // Storlek på buffert mellan socket och applikation
    
    public TCPClient() {
	
    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {

	Socket clientSock = new Socket(hostname, port); // Skapa socket & anslut till server
		
	byte[] fromServerBuff = new byte[BUFFER_SIZE]; // Buffert mellan socket och applikation
	InputStream fromServerStream = clientSock.getInputStream(); // Ström mellan buffert och applikation
	ByteArrayOutputStream toClientStream = new ByteArrayOutputStream();  // Ström mellan applikation och klient
	
	clientSock.getOutputStream().write(toServerBytes); // Skicka given data på socket
	// Gränssnitt mellan socket och applikation
	while (true) {
	    int packetLen = 0; // Lagrar hur mycket av buffert som används

	    // Om det finns data på socket
	    while (fromServerStream.available() != 0) {
		packetLen = fromServerStream.available(); // "Sparar" hur mycket av bufferten som används
		// Buffert kan endast ta BUFFER_SIZE bytes åt gången
		if (packetLen < BUFFER_SIZE) {
		    fromServerStream.read(fromServerBuff, 0, packetLen); // Flyttar data från socket till buffert
		} else {
		    packetLen = BUFFER_SIZE;
		    fromServerStream.read(fromServerBuff, 0, packetLen); // Flyttar data från socket till buffert
		    toClientStream.write(fromServerBuff, 0, packetLen);
		}
	    }
	    toClientStream.write(fromServerBuff, 0, packetLen); // Flyttar data från buffert till socket
	    
	    // Kolla om paketet är slut
	    // Måste göra write för att se till att inga byte droppas
	    if (fromServerStream.available() == 0) {
		byte checkEOP = (byte)fromServerStream.read();
		if (checkEOP == -1) {
		    break;
		} else {
		    toClientStream.write(checkEOP);
		}   
	    }
	}
	// Stäng socket & strömmar
	clientSock.close();
	fromServerStream.close();
	toClientStream.close();
	
	return toClientStream.toByteArray(); // Returnera byte-array
    }
}
