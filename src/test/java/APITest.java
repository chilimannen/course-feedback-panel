import Controller.WebServer;
import Model.Authentication;
import Model.Serializer;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
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

    @Rule
    public Timeout timeout = Timeout.seconds(15);

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(new WebServer(new AccountDBMock()), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }


    @Test
    public void testAuthenticationSuccess(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .post(WebServer.WEB_PORT, "localhost", "/api/authenticate", response -> {
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
                .post(WebServer.WEB_PORT, "localhost", "/api/authenticate", response -> {
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
                .post(WebServer.WEB_PORT, "localhost", "/api/authenticate", response -> {
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
                .post(WebServer.WEB_PORT, "localhost", "/api/register", response -> {
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
                .post(WebServer.WEB_PORT, "localhost", "/api/register", response -> {
                    context.assertEquals(HttpResponseStatus.CONFLICT.code(), response.statusCode());
                    async.complete();
                }).end(
                new JsonObject()
                        .put("username", "usertest")
                        .put("password", "userpass_new")
                        .encode());
    }

    @Test
    public void terminateVoting(TestContext context) {
        //
    }

    @Test
    public void terminateNonExistingVoting(TestContext context) {
        //
    }

    @Test
    public void createVoting(TestContext context) {
        //
    }

    @Test
    public void createVotingNamespaceCollision(TestContext context) {
        //
    }

    @Test
    public void listVotingInProgress(TestContext context) {
        //
    }
}
