import Configuration.Configuration;
import Controller.WebServer;
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
 *
 * Test the webserver servs.
 */
@RunWith(VertxUnitRunner.class)
public class WebServerTest {
    private Vertx vertx;

    @Rule
    public Timeout timeout = Timeout.seconds(15);

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(new WebServer(new AccountDBMock(), new ControllerClientMock()), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void getFileNotFound(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .getNow(Configuration.WEB_PORT, "localhost", "/404", response -> {

                    context.assertEquals(404, response.statusCode());

                    response.bodyHandler(event -> {
                        JsonObject json = event.toJsonObject();

                        context.assertTrue(json.containsKey("page"));
                        context.assertEquals(404, json.getInteger("page"));

                        async.complete();
                    });
                });
    }

    @Test
    public void getStartPage(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .getNow(Configuration.WEB_PORT, "localhost", "/", response -> {
                    context.assertEquals(200, response.statusCode());
                    async.complete();
                });
    }

    @Test
    public void getStaticResource(TestContext context) {
        Async async = context.async();

        vertx.createHttpClient()
                .getNow(Configuration.WEB_PORT, "localhost", "/resources/script/application.js", response -> {
                    context.assertEquals(200, response.statusCode());
                    async.complete();
                });
    }
}
