import Configuration.Configuration;
import Controller.WebServer;
import Model.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Instant;

/**
 * @author Robin Duda
 *         <p/>
 *         Test methods for the View-API controlling voting status.
 *         <p/>
 *         /api/register - register a new account.
 *         /api/authenticate - authenticate the user
 *         /api/create - creates a new voting.
 *         /api/terminate - terminates a voting.
 *         /api/list - lists all votings.
 */

@RunWith(VertxUnitRunner.class)
public class APITest {
    private Vertx vertx;
    private TokenFactory tokenFactory;

    @Rule
    public Timeout timeout = Timeout.seconds(15);

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(new WebServer(new AccountDBMock()), context.asyncAssertSuccess());
        tokenFactory = new TokenFactory(Configuration.CLIENT_SECRET);
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }


    @Test
    public void testAuthenticationSuccess(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .post(Configuration.WEB_PORT, "localhost", "/api/authenticate", response -> {
                    context.assertEquals(HttpResponseStatus.OK.code(), response.statusCode());

                    response.bodyHandler(body -> {
                        JsonObject data = body.toJsonObject();

                        context.assertTrue(data.containsKey("account"));
                        context.assertTrue(data.containsKey("token"));

                        async.complete();
                    });
                }).end(
                new JsonObject()
                        .put("username", "usertest")
                        .put("password", "userpass")
                        .encode());
    }

    @Test
    public void testAuthenticationFailure(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .post(Configuration.WEB_PORT, "localhost", "/api/authenticate", response -> {
                    context.assertEquals(HttpResponseStatus.UNAUTHORIZED.code(), response.statusCode());
                    async.complete();
                }).end(
                new JsonObject()
                        .put("username", "usertest")
                        .put("password", "userpass_wrong")
                        .encode());
    }

    @Test
    public void testMissingAccount(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .post(Configuration.WEB_PORT, "localhost", "/api/authenticate", response -> {
                    context.assertEquals(HttpResponseStatus.NOT_FOUND.code(), response.statusCode());
                    async.complete();
                }).end(
                new JsonObject()
                        .put("username", "userNX")
                        .put("password", "userpass")
                        .encode());
    }

    @Test
    public void testAccountRegister(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .post(Configuration.WEB_PORT, "localhost", "/api/register", response -> {
                    context.assertEquals(HttpResponseStatus.OK.code(), response.statusCode());

                    response.bodyHandler(body -> {
                        Authentication authentication = (Authentication) Serializer.unpack(body.toJsonObject(), Authentication.class);

                        context.assertEquals("usertest_new", authentication.getAccount().getUsername());
                        context.assertTrue(body.toJsonObject().containsKey("token"));

                        async.complete();
                    });
                }).end(
                new JsonObject()
                        .put("username", "usertest_new")
                        .put("password", "userpass_new")
                        .encode());
    }

    @Test
    public void testAccountRegisterExisting(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .post(Configuration.WEB_PORT, "localhost", "/api/register", response -> {
                    context.assertEquals(HttpResponseStatus.CONFLICT.code(), response.statusCode());
                    async.complete();
                }).end(
                new JsonObject()
                        .put("username", "usertest")
                        .put("password", "userpass_new")
                        .encode());
    }

    @Test
    public void testInvalidTokenAccessCreate(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .post(Configuration.WEB_PORT, "localhost", "/api/create", response -> {
                    context.assertEquals(HttpResponseStatus.UNAUTHORIZED.code(), response.statusCode());
                    async.complete();
                }).end(
                new JsonObject()
                        .put("token", getInvalidToken())
                        .encode());
    }

    @Test
    public void testInvalidTokenAccessTerminate(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .get(Configuration.WEB_PORT, "localhost", "/api/list", response -> {
                    context.assertEquals(HttpResponseStatus.UNAUTHORIZED.code(), response.statusCode());
                    async.complete();
                }).end(
                new JsonObject()
                        .put("token", getInvalidToken())
                        .encode());
    }

    @Test
    public void testInvalidTokenAccessList(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .post(Configuration.WEB_PORT, "localhost", "/api/terminate", response -> {
                    context.assertEquals(HttpResponseStatus.UNAUTHORIZED.code(), response.statusCode());
                    async.complete();
                }).end(
                new JsonObject()
                        .put("token", getInvalidToken())
                        .encode());
    }

    private JsonObject getInvalidToken() {
        return new JsonObject()
                .put("key", "invalid")
                .put("domain", Configuration.SERVER_NAME)
                .put("expiry", Instant.now().getEpochSecond() + 90);
    }
}
