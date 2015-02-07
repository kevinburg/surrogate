package surrogate;

import java.util.*;
import java.io.*;
import java.net.*;

public class Surrogate {
	
	private static ServerSocket serverSocket;

	

	public static void main(String[] args) throws IOException {
		serverSocket = new ServerSocket(8080); // Start, listen on port 8080
		while (true) {
			try {
				Socket s = serverSocket.accept(); 
				new ClientHandler(s); 
			} catch (Exception x) {
				System.out.println(x);
			}
		}
	}

}
