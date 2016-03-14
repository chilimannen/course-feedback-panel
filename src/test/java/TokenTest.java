import Model.TokenFactory;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;

/**
 * Created by Robin on 2016-03-14.
 *
 * Test token authentication.
 */
public class TokenTest {

    @Test
    public void shouldGenerateValidHmac() throws Exception {
        String username = UUID.randomUUID().toString();
        Long expiry = Instant.now().getEpochSecond() + 50;
        String token = TokenFactory.SignToken(username, expiry);

        if (!TokenFactory.VerifyToken(token, username, expiry))
            throw new Exception("Valid key not validated");
    }

    @Test
    public void shouldFailHmacForInvalidKey() throws Exception {
        String username = UUID.randomUUID().toString();
        Long expiry = Instant.now().getEpochSecond() + 50;
        String token = TokenFactory.SignToken(username, expiry);

        if (TokenFactory.VerifyToken(token + '?', username, expiry))
            throw new Exception("Invalid key not rejected");
    }

    @Test
    public void shouldFailHmacForInvalidUsername() throws Exception {
        String username = UUID.randomUUID().toString();
        Long expiry = Instant.now().getEpochSecond() + 50;
        String token = TokenFactory.SignToken(username, expiry);

        if (TokenFactory.VerifyToken(token, username + '?', expiry))
            throw new Exception("Invalid username not rejected.");
    }

    @Test
    public void shouldFailHmacForInvalidExpiry() throws Exception {
        String username = UUID.randomUUID().toString();
        Long expiry = Instant.now().getEpochSecond() + 50;
        String token = TokenFactory.SignToken(username, expiry);

        if (TokenFactory.VerifyToken(token, username, expiry + 1000))
            throw new Exception("Invalid expiry not rejected.");
    }

    @Test
    public void shouldFailHmacForExpiredToken() throws Exception {
        String username = UUID.randomUUID().toString();
        Long expiry = Instant.now().getEpochSecond() - 1000;
        String token = TokenFactory.SignToken(username, expiry);

        if (TokenFactory.VerifyToken(token, username, expiry))
            throw new Exception("Expired token not rejected");
    }

}
