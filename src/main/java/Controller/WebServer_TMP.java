package Controller;

import Configuration.Configuration;
import Model.AccountDB;
import Model.AsyncAccountStore;
import Model.AsyncControllerClient;
import Model.ControllerClient;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.JadeTemplateEngine;

/**
 * @author Robin Duda
 *         <p/>
 *         Sets up the routing for the admin web-interface
 *         and listens for requests.
 */
public class WebServer_TMP implements Verticle {
    private AsyncAccountStore accounts;
    private AsyncControllerClient client;
    private Vertx vertx;

    public WebServer_TMP() {
    }

    public WebServer_TMP(AsyncAccountStore accounts, AsyncControllerClient client) {
        this.client = client;
        this.accounts = accounts;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;

        if (accounts == null) {
            accounts = new AccountDB(
                    MongoClient.createShared(vertx,
                            new JsonObject()
                                    .put("connection_string", Configuration.CONNECTION_STRING)
                                    .put("db_name", Configuration.DB_NAME)));
        }

        if (client == null)
            client = new ControllerClient(vertx);
    }

    @Override
    public void start(Future<Void> future) throws Exception {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        new APIRouter().register(router, accounts, client);
        setTemplating(router);
        setResources(router);
        setCatchAll(router);

        server.requestHandler(router::accept).listen(Configuration.WEB_PORT);
        future.complete();
    }

    private void setTemplating(Router router) {
        JadeTemplateEngine jade = JadeTemplateEngine.create();

        router.route("/").handler(context -> {
            jade.render(context, "templates/index", result -> {
                if (result.succeeded())
                    context.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html").end(result.result());
                else
                    context.fail(result.cause());
            });
        });
    }

    private void setResources(Router router) {
        router.route("/resources/*").handler(StaticHandler.create()
                .setCachingEnabled(true));
    }

    private void setCatchAll(Router router) {
        router.route().handler(context -> {
            HttpServerResponse response = context.response();
            response.setStatusCode(404);
            response.putHeader("content-type", "application/json");
            response.end("{\"page\" : 404}");
        });
    }

    @Override
    public void stop(Future<Void> future) throws Exception {
        future.complete();
    }
}
