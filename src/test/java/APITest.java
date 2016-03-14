import Controller.Webserver;
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
        vertx.deployVerticle(new Webserver(new AccountDBMock()), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }


    @Test
    public void testAuthenticationSuccess(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .post(Webserver.WEB_PORT, "localhost", "/api/authenticate", response -> {
                    context.assertEquals(200, response.statusCode());

                    response.bodyHandler(body -> {

                        System.out.println(body.toString());

                        async.complete();
                    });
                }).end(
                new JsonObject()
                        .put("username", "usern1")
                        .put("password", "passwordn1")
                        .encode());
    }

    @Test
    public void testAuthenticationFailure(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .getNow(Webserver.WEB_PORT, "localhost", "/", response -> {
                    context.assertEquals(200, response.statusCode());
                    async.complete();
                });
    }

    @Test
    public void terminateVoting(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .getNow(Webserver.WEB_PORT, "localhost", "/", response -> {
                    context.assertEquals(200, response.statusCode());
                    async.complete();
                });
    }

    @Test
    public void terminateNonExistingVoting(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .getNow(Webserver.WEB_PORT, "localhost", "/", response -> {
                    context.assertEquals(200, response.statusCode());
                    async.complete();
                });
    }

    @Test
    public void createVoting(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .getNow(Webserver.WEB_PORT, "localhost", "/", response -> {
                    context.assertEquals(200, response.statusCode());
                    async.complete();
                });
    }

    @Test
    public void createVotingNamespaceCollision(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .getNow(Webserver.WEB_PORT, "localhost", "/", response -> {
                    context.assertEquals(200, response.statusCode());
                    async.complete();
                });
    }

    @Test
    public void listVotingInProgress(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .getNow(Webserver.WEB_PORT, "localhost", "/", response -> {
                    context.assertEquals(200, response.statusCode());
                    async.complete();
                });
    }
}
