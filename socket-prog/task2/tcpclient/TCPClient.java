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
import java.util.Arrays;

public class TCPClient {
    
    public TCPClient() {
	
    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {

	// byte[] fromServerBuff = new byte[1024]; //b Buffert mellan socket och outputstream
	int fromServerBuff = 0;
	ByteArrayOutputStream toClientStream = new ByteArrayOutputStream(); 
	
	Socket clientSock = new Socket(hostname, port); // Skapa socket & anslut till server
	clientSock.getOutputStream().write(toServerBytes); // Skicka data till socket
	
	InputStream fromServerStream = clientSock.getInputStream(); // Inputstream från socket
	
       	while(true) {
	    fromServerBuff = fromServerStream.read();
	    //System.out.println(fromServerBuff);
	    // Om vi får eom 
	    if (fromServerBuff == -1) {
		break;
	    }
	    toClientStream.write(fromServerBuff); // Buffert -> outputstream
	}
	//System.out.println(Arrays.toString(toClientStream.toByteArray()));
	return toClientStream.toByteArray();
    }    
}
