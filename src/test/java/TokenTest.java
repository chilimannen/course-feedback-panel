import Model.TokenFactory;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Robin Duda
 *
 * Test token authentication.
 */
public class TokenTest {
    private TokenFactory factory;

    @Before
    public void setUp() {
        factory = new TokenFactory("SECRET".getBytes());
    }

    @Test
    public void shouldGenerateValidHmac() throws Exception {
        String username = UUID.randomUUID().toString();
        Long expiry = Instant.now().getEpochSecond() + 50;
        String token = factory.signToken(username, expiry);

        if (!factory.verifyToken(token, username, expiry))
            throw new Exception("Valid key not validated");
    }

    @Test
    public void shouldFailHmacForInvalidKey() throws Exception {
        String username = UUID.randomUUID().toString();
        Long expiry = Instant.now().getEpochSecond() + 50;
        String token = factory.signToken(username, expiry);

        if (factory.verifyToken(token + '?', username, expiry))
            throw new Exception("Invalid key not rejected");
    }

    @Test
    public void shouldFailHmacForInvalidUsername() throws Exception {
        String username = UUID.randomUUID().toString();
        Long expiry = Instant.now().getEpochSecond() + 50;
        String token = factory.signToken(username, expiry);

        if (factory.verifyToken(token, username + '?', expiry))
            throw new Exception("Invalid username not rejected.");
    }

    @Test
    public void shouldFailHmacForInvalidExpiry() throws Exception {
        String username = UUID.randomUUID().toString();
        Long expiry = Instant.now().getEpochSecond() + 50;
        String token = factory.signToken(username, expiry);

        if (factory.verifyToken(token, username, expiry + 1000))
            throw new Exception("Invalid expiry not rejected.");
    }

    @Test
    public void shouldFailHmacForExpiredToken() throws Exception {
        String username = UUID.randomUUID().toString();
        Long expiry = Instant.now().getEpochSecond() - 1000;
        String token = factory.signToken(username, expiry);

        if (factory.verifyToken(token, username, expiry))
            throw new Exception("Expired token not rejected");
    }

}
