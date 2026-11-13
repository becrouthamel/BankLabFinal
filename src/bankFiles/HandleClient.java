package bankFiles;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Handles the client
 */
public class HandleClient implements Runnable {

	// Instance variables
		// Socket, username of logged in user
	private Socket socket;
	private String loggedUser = null;

	/**
	 * Creates a new HandleClient instance
	 * 
	 * @param socket Connection with the client
	 */
	public HandleClient(Socket socket) {
		this.socket = socket;
	}

	/**
	 * Runs the client and resulting interactions
	 */
	@Override
	public void run() {

		// 
		try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				// Establish connection
				Connection connection = DriverManager.getConnection(Queries.URL, new Credentials().getUsername(),
						new Credentials().getPassword());) {

			out.println("Enter command in the format COMMAND value1 value2...");
			
			// Check for if user has logged in
			boolean loggedIn = false;

			while (true) {

				out.println("Enter command: ");
				String line = in.readLine();
				
				if (line == null)
					return;

				String[] userIn = line.split(" ");

				// Switch statement based on first part of the command
				switch (userIn[0].toUpperCase()) {
				
				// Register a new user
				case "REGISTER":
					if (userIn.length != 3) {
						out.println("ERROR Please provide three arguments (Ex. REGISTER Username Password)");
						break;
					}

					if (Queries.register(connection, userIn[1], userIn[2])) {
						out.println("Registering successful.");
					} else {
						out.println("ERROR That username is already taken.");
					}
					break;

					
				// Login as a user
				case "LOGIN":
					if (userIn.length != 3) {
						out.println("ERROR Please provide three arguments (Ex. LOGIN Username Password)");
						break;
					}

					if (Queries.login(connection, userIn[1], userIn[2])) {
						loggedIn = true;
						loggedUser = userIn[1];
						out.println("Login successful.");
					} else {
						out.println("ERROR Login failed.");
					}
					break;

					
				// View available balance
				case "BALANCE":
					if (!loggedIn) {
						out.println("ERROR Not logged in.");
						break;
					}
					out.println("Balance: " + Queries.balance(connection, loggedUser));
					break;

					
				// Deposit an amount into account
				case "DEPOSIT":
					if (!loggedIn) {
						out.println("ERROR Not logged in.");
						break;
					}
					double depositAmount;

					try {
						depositAmount = Double.parseDouble(userIn[1]);
					} catch (Exception e) {
						out.println("ERROR Please enter a double value.");
						break;
					}

					if (Queries.deposit(connection, loggedUser, depositAmount)) {
						Queries.logTransaction(connection, loggedUser, null, "DEPOSIT", depositAmount);
						out.println("Deposit successful.");
					} else {
						out.println("ERROR Deposit failed.");
					}
					break;

					
				// Withdraw an amount from account
				case "WITHDRAW":
					if (!loggedIn) {
						out.println("ERROR Not logged in.");
						break;
					}
					double withdrawAmount;

					try {
						withdrawAmount = Double.parseDouble(userIn[1]);
					} catch (Exception e) {
						out.println("ERROR Please enter a double value.");
						break;
					}

					if (Queries.withdraw(connection, loggedUser, withdrawAmount)) {
						Queries.logTransaction(connection, loggedUser, null, "WITHDRAW", withdrawAmount);
						out.println("Withdraw successful.");
					} else {
						out.println("ERROR Withdraw failed.");
					}
					break;

					
				// Transfer amount from user to user
				case "TRANSFER":
					if (!loggedIn) {
						out.println("ERROR Not logged in.");
						break;
					}
					if (userIn.length != 3) {
						out.println("ERROR Please provide three arguments (Ex. TRANSFER Username Amount)");
						break;
					}

					String target = userIn[1];
					double amount;

					try {
						amount = Double.parseDouble(userIn[2]);
					} catch (Exception e) {
						out.println("ERROR Please enter a double value.");
						break;
					}

					if (Queries.transfer(connection, loggedUser, target, amount)) {
						
						Queries.logTransaction(connection, loggedUser, target, "TRANSFER_TO", amount);
						Queries.logTransaction(connection, target, loggedUser, "TRANSFER_FROM", amount);
						out.println("Transfer successful.");
						
					} else {
						out.println("ERROR Transfer failed.");
					}
					break;

					
				// View account transaction history
				case "HISTORY":
					if (loggedUser == null) {
						out.println("ERROR Not logged in.");
						break;
					}

					out.println(Queries.history(connection, loggedUser));
					break;

					
				// Quit the application
				case "QUIT":
					out.println("Goodbye!");
					return;

					
				// Anything else, return that the command was not recognized
				default:
					out.println("Command not recognized.");
				}
			}

		// Issues connecting to database
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
