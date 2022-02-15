package tcpclient;
import java.net.*;
import java.io.*;

public class TCPClient {
    // Shutdown: Om true, stäng socket outgoing
    
    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
	
    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {
	
	int fromServerBuff = 0;
	ByteArrayOutputStream toClientStream = new ByteArrayOutputStream(); 
	
	Socket clientSock = new Socket(hostname, port); // Skapa socket & anslut till server
	clientSock.getOutputStream().write(toServerBytes); // Skicka data till socket
	
	InputStream fromServerStream = clientSock.getInputStream(); // Inputstream från socket
	
       	while(true) {
	    fromServerBuff = fromServerStream.read();
	    // Om vi får eom 
	    if (fromServerBuff == -1) {
		break;
	    }
	    toClientStream.write(fromServerBuff); // Buffert -> outputstream
	}
	return toClientStream.toByteArray();
    }    
}

}
