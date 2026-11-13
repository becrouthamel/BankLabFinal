package bankFiles;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Sets up the server
 */
public class ServerBank {
	
	public static final int PORT = 8000;
	

	/**
	 * Sets up the bank server from port 8000. Opens a connection and waits for clients to connect.
	 * @param args
	 */
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Bank server running on port " + PORT + ".");

            // Accept clients while server is running
            	// Create a new thread to handle new clients
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Client connected: " + client.getInetAddress());
                new Thread(new HandleClient(client)).start();
            }

            // Handle errors found when setting up server
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
