package bankFiles;

/**
 * Provides easy way to store and access credentials to access the database
 */
public class Credentials {
	// Instance variables
		// Username and password to access database
    private final String username;
    private final String password;

    /**
     * Creates new Credentials instance
     */
    public Credentials() {
        this.username = "root";
        this.password = "password";
    }

    /**
     * Sets the username and password to the values provided
     * 
     * @param username
     * @param password
     */
    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the username
     * 
     * @return String username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the password
     * 
     * @return String password
     */
    public String getPassword() {
        return password;
    }
}
