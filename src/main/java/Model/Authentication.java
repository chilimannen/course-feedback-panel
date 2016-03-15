package Model;

/**
 * @author Robin Duda
 */
public class Authentication {
    private Token token;
    private Account account;

    public Authentication() {
    }

    public Authentication(Account account, Token token) {
        this.account = account;
        this.token = token;
    }

    public Token getToken() {
        return token;
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
