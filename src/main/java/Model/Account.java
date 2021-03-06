package Model;

/**
 * @author Robin Duda
 *
 * Holds account data for an user used to communicate with the view.
 */
public class Account {
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String phone;
    private String email;

    public Account() {
    }

    public Account(AccountMapping account) {
        this.username = account.getUsername();
        this.firstname = account.getFirstname();
        this.lastname = account.getLastname();
        this.phone = account.getPhone();
        this.email = account.getEmail();
    }

    public String getUsername() {
        return username;
    }

    public Account setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Account setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getFirstname() {
        return firstname;
    }

    public Account setFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    public String getLastname() {
        return lastname;
    }

    public Account setLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public Account setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Account setEmail(String email) {
        this.email = email;
        return this;
    }
}
