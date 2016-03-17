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
    private static final String USERNAME = "usertest";

    @Rule
    public Timeout timeout = Timeout.seconds(15);

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(new WebServer(new AccountDBMock(), new ControllerClientMock()), context.asyncAssertSuccess());
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
                        .put("username", USERNAME)
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
                        .put("username", USERNAME)
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
                        .put("username", USERNAME + "nx")
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

                        context.assertEquals(USERNAME + "new", authentication.getAccount().getUsername());
                        context.assertTrue(body.toJsonObject().containsKey("token"));

                        async.complete();
                    });
                }).end(
                new JsonObject()
                        .put("username", USERNAME + "new")
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
                        .put("username", USERNAME)
                        .put("password", "userpass_new")
                        .encode());
    }

    @Test
    public void testCreateNewVoting(TestContext context) throws TokenException {
        Async async = context.async();

        vertx.createHttpClient()
                .post(Configuration.WEB_PORT, "localhost", "/api/create", response -> {
                    context.assertEquals(HttpResponseStatus.OK.code(), response.statusCode());
                    async.complete();
                }).end(
                new JsonObject()
                        .put("voting", getVotingConfiguration())
                        .put("token", getValidToken())
                        .encode());
    }

    @Test
    public void testTerminateVoting(TestContext context) throws TokenException {
        Async async = context.async();

        vertx.createHttpClient()
                .post(Configuration.WEB_PORT, "localhost", "/api/terminate", response -> {
                    context.assertEquals(HttpResponseStatus.OK.code(), response.statusCode());
                    async.complete();
                }).end(
                new JsonObject()
                        .put("token", getValidToken())
                        .put("voting", Serializer.json(new Voting().setId("id")))
                        .put("owner", "gosu")
                        .encode());
    }

    @Test
    public void testListVotings(TestContext context) throws TokenException {
        Async async = context.async();

        vertx.createHttpClient()
                .get(Configuration.WEB_PORT, "localhost", "/api/list", response -> {
                    context.assertEquals(HttpResponseStatus.OK.code(), response.statusCode());

                    response.bodyHandler(body -> {
                        VotingList votings = (VotingList) Serializer.unpack(body.toJsonObject(), VotingList.class);
                        Voting voting = votings.getVotings().get(0);

                        context.assertEquals(1, votings.getVotings().size());
                        context.assertEquals(2, votings.getVotings().get(0).getOptions().size());

                        context.assertEquals("Vote #1", voting.getTopic());
                        context.assertEquals("id", voting.getId());
                        context.assertEquals("gosu", voting.getOwner());
                        context.assertEquals("q1", voting.getOptions().get(0).getName());
                        context.assertEquals("q2", voting.getOptions().get(1).getName());
                        context.assertTrue(voting.getOptions().get(0).getValues().contains("a"));
                        context.assertTrue(voting.getOptions().get(0).getValues().contains("b"));
                        context.assertTrue(voting.getOptions().get(0).getValues().contains("c"));

                        async.complete();
                    });


                }).end(
                new JsonObject()
                        .put("token", getValidToken())
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

    private JsonObject getValidToken() throws TokenException {
        Token token = new Token(tokenFactory, "username");

        return Serializer.json(token);
    }

    private JsonObject getInvalidToken() {
        return new JsonObject()
                .put("key", "invalid")
                .put("domain", Configuration.SERVER_NAME)
                .put("expiry", Instant.now().getEpochSecond() + 90);
    }

    private JsonObject getVotingConfiguration() {
        return new JsonObject()
                .put("topic", "test-voting")
                .put("owner", USERNAME)
                .put("duration",
                        new JsonObject()
                                .put("begin", Instant.now().getEpochSecond())
                                .put("end", Instant.now().getEpochSecond() + 10))
                .put("options",
                        new JsonArray()
                                .add(new JsonObject()
                                        .put("name", "query 1")
                                        .put("values",
                                                new JsonArray()
                                                        .add("value 1")
                                                        .add("value 2")
                                                        .add("value 3")))
                                .add(new JsonObject()
                                        .put("name", "query 2")
                                        .put("values",
                                                new JsonArray()
                                                        .add("value a")
                                                        .add("value b")
                                                        .add("value c"))));
    }
}
