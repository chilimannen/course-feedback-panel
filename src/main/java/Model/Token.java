package Model;

/**
 * Created by Robin on 2015-12-22.
 * <p/>
 * Used to request authentication by token.
 */
public class Token {
    private String key;
    private Long expiry;
    private String username;

    public Token() {
    }

    public Token(String username, Long expiry) throws TokenException {
        this.key = TokenFactory.SignToken(username, expiry);
        this.username = username;
        this.expiry = expiry;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getExpiry() {
        return expiry;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}