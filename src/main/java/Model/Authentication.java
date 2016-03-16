package Model;

/**
 * @author Robin Duda
 */
public class Authentication {
    private Token token;
    private Account account;
    private boolean registered;

    public Authentication() {
    }

    public Authentication(Account account, Token token, boolean registered) {
        this.account = account;
        this.token = token;
        this.registered = registered;
    }

    public Token getToken() {
        return token;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
