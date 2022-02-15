package tcpclient;
import java.net.*;
import java.io.*;

public class TCPClient {

    // Flaggor
    private boolean shutdown; // Stäng socket (alltså outgoing) efter data har skickats
    private Integer timeout; // Hur länge en read() väntar innan ett exception kastas
    private Integer limit; // Hur många byte som kan läsas innan askServer returnerar
    private Integer sizeOfReceivedData; // Hur många byte som har mottagits frpn servern
    
    public static final int BUFFER_SIZE = 1024; // Storlek på buffert mellan socket och applikation
    
    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
	this.shutdown = shutdown;
	this.timeout = timeout;
	this.limit = limit;
	this.sizeOfReceivedData = 0;
    }

    // Returnerar om det finns plats för att lägga till sizeOfNewData bytes innan limit nås
    public boolean fitInLimit(int sizeOfNewData) {
	return sizeOfReceivedData + sizeOfNewData < limit;
    }

    // Returnerar hur många byte som kan läsas innan limit nås
    public int spaceLeft() {
	if (limit != null) {
	    return limit - sizeOfReceivedData;
	} else {
	    return 1; // Om limitflaggan ej användes finns det alltid plats
	}
    }

    // Inkrementera sizeOfReceivedData
    public void recordRead(int size) {
	sizeOfReceivedData += size;
    }
    
    // Läser data från socket och skriver till toClientStream
    // Returnerar 0 (ok) eller -1 (fail)
    // Denna metod anropas tills det inte finns mer data att läsa, timeout eller limit nås
    // Om det finns fler bytes än BUFFER_SIZE skriver metoden till toClientStream
    public int readSocket(int packetLen, InputStream fromServerStream, OutputStream toClientStream, byte[] fromServerBuff)
	throws IOException, SocketException {
	// När det finns utrymme för mer data och data att läsa
	while (spaceLeft() > 0 && fromServerStream.available() != 0) {
	    try {
		packetLen = fromServerStream.available(); // "Sparar" hur mycket av bufferten som används
	    } catch (IOException ex) {
		System.out.println("at packetLen in readSocket");
		System.out.println(ex);
		return -1;
	    }
	    
	    if (limit != null && !fitInLimit(packetLen)) { packetLen = spaceLeft(); } // Om all data inte får plats i limit
	    // Buffert kan endast ta BUFFER_SIZE bytes åt gången
	    try {
		if (packetLen < BUFFER_SIZE) {
		    fromServerStream.read(fromServerBuff, 0, packetLen); // Flyttar data från socket till buffert
		    toClientStream.write(fromServerBuff, 0, packetLen - 1); // Flyttar data från buffert till toClientStream (nollindexerat därav -1)
		    recordRead(packetLen);
		    return 0; // Finns ej mer data att läsa
		} else {
		    packetLen = BUFFER_SIZE; // Läs endast så mycket data som får plats i fromServerBuff
		    fromServerStream.read(fromServerBuff, 0, packetLen); // Flyttar data från socket till buffert
		    toClientStream.write(fromServerBuff, 0, packetLen); // Loopen kommer köras igen, det finns mer data på socket
		    recordRead(packetLen);
		}
	    } catch(SocketException ex) {
		System.out.println("Read timed out");
		return -1;
	    } catch(IOException ex) {
		System.out.println("IO exception in readSocket");
		return -1;
	    }
	}
	return 0; // Finns ej mer data att läsa eller inget utrymme kvar
    }
    

    // Anropas av TCPAsk
    // Returnerar en byte array av data från en TCP förbindelse
    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException, SocketException {

	Socket clientSock = new Socket(hostname, port); // Skapa socket & anslut till server
		
	byte[] fromServerBuff = new byte[BUFFER_SIZE]; // Buffert mellan socket och applikation
	InputStream fromServerStream = clientSock.getInputStream(); // Ström mellan buffert och applikation
	ByteArrayOutputStream toClientStream = new ByteArrayOutputStream();  // Ström mellan applikation och klient
	
	clientSock.getOutputStream().write(toServerBytes); // Skicka given data på socket
	
	if (this.shutdown) { clientSock.shutdownOutput(); } // Stänger socketen om --shutdown flaggan användes

	if (this.timeout != null) { clientSock.setSoTimeout(timeout); }	// Sätt timer för socket
	
	// Gränssnitt mellan socket och applikation
	while (true) {
	    
	    int packetLen = 0; // Lagrar hur mycket av buffert som används

	    // Flyttar data från socket till toClientStream tills ingen mer data finns, timeout, limit nås eller IO exception
	    try {
		if (readSocket(packetLen, fromServerStream, toClientStream, fromServerBuff) == -1) {
		    break; // 
		}
	    } catch(IOException ex) {
		System.out.println("In readSocket");
		System.out.println(ex);
	    } 	   
	    
	    if (spaceLeft() == 0) { break; } // Kolla om det finns utrymme kvar innan limit nås
	    
	    // Kolla om paketet är slut
	    // Måste göra write för att se till att checkEOP byte inte förloras om det ej är '-1'
	    if (fromServerStream.available() == 0) {
		try {
		    byte checkEOP = (byte)fromServerStream.read();
		    if (checkEOP == -1) {
			break;
		    } else {
			toClientStream.write(checkEOP); // "Fånga" byte som inte är '-1'
		    } 
		} catch(IOException ex) {
		    System.out.println(ex);
		    break;
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

