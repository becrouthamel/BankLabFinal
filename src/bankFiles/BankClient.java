package bankFiles;

import java.io.*;
import java.net.Socket;

/**
 * Creates a new bank client
 */
public class BankClient {
	
	
	public static final int PORT = 8000;
	
	/**
	 * Connects client to the server.
	 * Sets up for user to input commands.
	 * @param args
	 */
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", PORT);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader console = new BufferedReader(
                    new InputStreamReader(System.in));

            System.out.println("Connected to Bank Server!");

            // Create new thread
            new Thread(() -> {
                try {
                    String serverOut;
                    while ((serverOut = in.readLine()) != null) {
                        System.out.println(serverOut);
                    }
                } catch (Exception e) {
                    System.out.println("Server disconnected.");
                }
            }).start();

            // Read for user input
            String userInput;
            while ((userInput = console.readLine()) != null) {
                out.println(userInput);
            }

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
