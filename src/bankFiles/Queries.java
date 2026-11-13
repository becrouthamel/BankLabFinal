package bankFiles;

import java.sql.*;

/**
 * Interacts with the database through SQL Queries
 */
public class Queries {

	// Instance variables
		// SQL Server, Database, and URL
	public static final String SQL_SERVER = "jdbc:mysql://localhost:3306";
	public static final String DATABASE = "BankLabDatabase";
	public static final String URL = SQL_SERVER + "/" + DATABASE;

	/**
	 * Logs any transaction.
	 * 
	 * @param connection
	 * @param source     user who initiated the transaction
	 * @param dest       user who is on the receiving end of a transaction
	 * @param type       the type of transaction
	 * @param amount     the amount of money transfered
	 * @throws SQLException
	 */
	public static void logTransaction(Connection connection, String source, String dest, String type, double amount)
			throws SQLException {
		String query = "INSERT INTO TRANSACTION_RECORD (CUS_ID_SOURCE, CUS_ID_DEST, TXN_TYPE, TXN_AMOUNT, TXN_DATETIME) VALUES (?, ?, ?, ?, NOW())";

		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, source);
			ps.setString(2, dest);
			ps.setString(3, type);
			ps.setDouble(4, amount);
			ps.executeUpdate();
		}
	}

	/**
	 * Registers a new user
	 * 
	 * @param connection
	 * @param user       username of the user
	 * @param password   password for the new user
	 * @return true or false based on if the values provided can create a new user
	 */
	public static boolean register(Connection connection, String user, String password) {
		// Find if there are any other users iwth same username
		String q1 = "SELECT CUS_UNAME FROM CUSTOMER WHERE CUS_UNAME = ?";

		try (PreparedStatement ps = connection.prepareStatement(q1)) {
			ps.setString(1, user);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) 
				return false;
			

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		// Insert new values into customer table
		String q2 = "INSERT INTO CUSTOMER (CUS_UNAME, CUS_PASSWD, CUS_BALANCE) VALUES (?, ?, ?)";
		try (PreparedStatement ps = connection.prepareStatement(q2)) {
			ps.setString(1, user);
			ps.setString(2, password);
			ps.setInt(3, 0);
			ps.executeUpdate();
			
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Lets a user log in with credentials
	 * 
	 * @param connection
	 * @param user       username
	 * @param password
	 * @return true or false based on if login was successful
	 */
	public static boolean login(Connection connection, String user, String password) {
		// Select the user where user and password match provided arguments
		String query = "SELECT * FROM CUSTOMER WHERE CUS_UNAME = ? AND CUS_PASSWD = ?";

		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, user);
			ps.setString(2, password);
			ResultSet rs = ps.executeQuery();

			return rs.next();

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Returns the balance of the logged in user
	 * 
	 * @param connection
	 * @param user       username
	 * @return balance value
	 */
	public static double balance(Connection connection, String user) {
		// Select the customer balance
		String query = "SELECT CUS_BALANCE FROM CUSTOMER WHERE CUS_UNAME = ?";

		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, user);
			ResultSet rs = ps.executeQuery();

			if (rs.next())
				return rs.getDouble(1);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return 0.0;
	}

	/**
	 * Deposits a select amount into user's account
	 * 
	 * @param connection
	 * @param user       username
	 * @param amount     amount of money
	 * @return true or false based on the success of the deposit
	 */
	public static boolean deposit(Connection connection, String user, double amount) {
		// Update the customer balance
		String query = "UPDATE CUSTOMER SET CUS_BALANCE = CUS_BALANCE + ? WHERE CUS_UNAME = ?";

		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setDouble(1, amount);
			ps.setString(2, user);

			if (amount <= 0)
				return false;

			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Allows user to withdraw a select amount from account
	 * 
	 * @param connection
	 * @param user       username
	 * @param amount     amount to withdraw
	 * @return true or false based on the success of the withdraw
	 */
	public static boolean withdraw(Connection connection, String user, double amount) {

		if (amount <= 0)
			return false;

		// Get the customer balance
		String q1 = "SELECT CUS_BALANCE FROM CUSTOMER WHERE CUS_UNAME = ?";
		try (PreparedStatement checkPs = connection.prepareStatement(q1)) {
			checkPs.setString(1, user);
			ResultSet rs = checkPs.executeQuery();

			if (!rs.next())
				return false;

			double balance = rs.getDouble("CUS_BALANCE");

			if (balance < amount)
				return false;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		// Update the balance
		String q2 = "UPDATE CUSTOMER SET CUS_BALANCE = CUS_BALANCE - ? WHERE CUS_UNAME = ?";

		try (PreparedStatement ps = connection.prepareStatement(q2)) {
			ps.setDouble(1, amount);
			ps.setString(2, user);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Transfers a select amount from one account to another
	 * 
	 * @param connection
	 * @param from       user that money is being withdrawn from
	 * @param to         user that the money is going to
	 * @param amount     amount of money being transfered
	 * @return true or false based on the success of the transfer
	 */
	public static boolean transfer(Connection connection, String from, String to, double amount) {
		if (amount <= 0)
			return false;

		String q1 = "SELECT CUS_BALANCE FROM CUSTOMER WHERE CUS_UNAME = ?";
		try (PreparedStatement checkPs = connection.prepareStatement(q1)) {
			checkPs.setString(1, from);
			ResultSet rs = checkPs.executeQuery();

			// If there is no return from statement
			if (!rs.next())
				return false;

			double balance = rs.getDouble("CUS_BALANCE");

			// Check that amount does not exceed current balance
			if (balance < amount)
				return false;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		String q2 = "SELECT CUS_UNAME FROM CUSTOMER WHERE CUS_UNAME = ?";
		try (PreparedStatement checkPs = connection.prepareStatement(q2)) {
			checkPs.setString(1, to);
			ResultSet rs = checkPs.executeQuery();

			// If there is no return from statement
			if (!rs.next())
				return false;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		try {
			connection.setAutoCommit(false);

			double bal = balance(connection, from);
			if (bal < amount) {
				connection.rollback();
				return false;
			}

			String q3 = "UPDATE CUSTOMER SET CUS_BALANCE = CUS_BALANCE - ? WHERE CUS_UNAME = ?";
			try (PreparedStatement ps = connection.prepareStatement(q3)) {
				ps.setDouble(1, amount);
				ps.setString(2, from);
				ps.executeUpdate();
			}

			String q4 = "UPDATE CUSTOMER SET CUS_BALANCE = CUS_BALANCE + ? WHERE CUS_UNAME = ?";
			try (PreparedStatement ps = connection.prepareStatement(q4)) {
				ps.setDouble(1, amount);
				ps.setString(2, to);
				ps.executeUpdate();
			}

			// Commit
			connection.commit();
			connection.setAutoCommit(true);
			return true;

		} catch (SQLException e) {

			try {

				connection.rollback();

			} catch (SQLException ignored) {

			}
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Returns the full transaction history of the account
	 * 
	 * @param connection
	 * @param user       username
	 * @return String with contained account history
	 */
	public static String history(Connection connection, String user) {
		StringBuilder historyString = new StringBuilder();

		String query = "SELECT TXN_ID, CUS_ID_SOURCE, CUS_ID_DEST, TXN_TYPE, TXN_AMOUNT, TXN_DATETIME FROM TRANSACTION_RECORD WHERE CUS_ID_SOURCE = ? OR CUS_ID_DEST = ? ORDER BY TXN_DATETIME";

		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, user);
			ps.setString(2, user);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				int id = rs.getInt("TXN_ID");
				String type = rs.getString("TXN_TYPE");
				double amount = rs.getDouble("TXN_AMOUNT");
				String dateTime = rs.getTimestamp("TXN_DATETIME").toString();

				// Account for transactions that have no other user
				if (type.startsWith("TRANSFER_")) {
					String otherUser = user.equals(rs.getString("CUS_ID_SOURCE")) ? rs.getString("CUS_ID_DEST")
							: rs.getString("CUS_ID_SOURCE");

					historyString.append(String.format("%d %s %s %.2f %s%n", id, type, otherUser, amount, dateTime));
				} else {
					historyString.append(String.format("%d %s %.2f %s%n", id, type, amount, dateTime));
				}
			}

			historyString.append("OK");

		} catch (SQLException e) {
			e.printStackTrace();
			return "ERROR";
		}

		return historyString.toString();
	}
}
