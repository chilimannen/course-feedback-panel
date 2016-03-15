package Controller;

import Model.AccountDB;
import Model.AsyncAccountStore;
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
public class WebServer implements Verticle {
    public static final int WEB_PORT = 4096;
    private AsyncAccountStore accounts;
    private Vertx vertx;

    public WebServer() {
    }

    public WebServer(AsyncAccountStore accounts) {
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
                                    .put("connection_string", "mongodb://localhost:27017/")
                                    .put("db_name", "accounts")));
        }
    }

    @Override
    public void start(Future<Void> future) throws Exception {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        new APIRouter().register(router, accounts);
        setTemplating(router);
        setResources(router);
        setCatchAll(router);

        server.requestHandler(router::accept).listen(WEB_PORT);
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
