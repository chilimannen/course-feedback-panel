package Controller;

import Configuration.Configuration;
import Model.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * @Author Robin Duda
 */
class APIRouter {
    private AsyncAccountStore accounts;
    private Vertx vertx;
    private TokenFactory clientToken;
    private TokenFactory serverToken;

    public void register(Router router, AsyncAccountStore accounts, Vertx vertx) {
        this.accounts = accounts;
        this.vertx = vertx;

        clientToken = new TokenFactory(Configuration.CLIENT_SECRET);
        serverToken = new TokenFactory(Configuration.SERVER_SECRET);

        router.post("/api/register").handler(this::register);
        router.post("/api/authenticate").handler(this::authenticate);
        router.get("/api/list").handler(this::list);
        router.post("/api/create").handler(this::create);
        router.post("/api/terminate").handler(this::terminate);
    }

    private void register(RoutingContext context) {
        HttpServerResponse response = context.response();
        Account account = (Account) Serializer.unpack(context.getBodyAsJson(), Account.class);
        Future<Account> future = Future.future();

        future.setHandler(result -> {
            try {
                if (future.succeeded())
                    sendAuthentication(result.result(), context, true);
                else
                    throw future.cause();

            } catch (AccountExistsException e) {
                response.setStatusCode(HttpResponseStatus.CONFLICT.code()).end();
            } catch (Throwable e) {
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            }
        });
        accounts.register(account, future);
    }

    private void authenticate(RoutingContext context) {
        HttpServerResponse response = context.response();
        Account account = (Account) Serializer.unpack(context.getBodyAsJson(), Account.class);
        Future<Account> future = Future.future();

        future.setHandler(result -> {
            try {
                if (future.succeeded())
                    sendAuthentication(result.result(), context, false);
                else
                    throw future.cause();

            } catch (AccountMissingException e) {
                response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
            } catch (AccountPasswordException e) {
                response.setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
            } catch (Throwable e) {
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            }
        });
        accounts.authenticate(account, future);
    }

    private void sendAuthentication(Account account, RoutingContext context, boolean registered) {
        Token token = new Token(clientToken, account.getUsername());
        context.response()
                .setStatusCode(HttpResponseStatus.OK.code())
                .end(Serializer.pack(new Authentication(account, token, registered)));
    }

    private void create(RoutingContext context) {
        if (authorized(context)) {
            JsonObject query = new JsonObject()
                    .put("token", getServerToken())
                    .put("voting", context.getBodyAsJson().getJsonObject("vote"));

            vertx.createHttpClient().post(Configuration.CONTROLLER_PORT, "localhost", "/api/create", res -> {
                context.response().setStatusCode(res.statusCode()).end();
            }).end(query.encode());
        }
    }

    private void list(RoutingContext context) {
        if (authorized(context)) {
            JsonObject query = new JsonObject()
                    .put("token", new JsonObject(Json.encode(getServerToken())))
                    .put("voting", context.getBodyAsJson().getJsonObject(""));

            /**
             *
             *
             *
             * ------------------------------------------------
             *
             *
             *
             */

            vertx.createHttpClient().post(Configuration.CONTROLLER_PORT, "localhost", "/api/list", res -> {
                context.response().setStatusCode(res.statusCode()).end();
            }).end(query.encode());
        }
    }

    private void terminate(RoutingContext context) {
        if (authorized(context)) {
            JsonObject query = new JsonObject()
                    .put("token", new JsonObject(Json.encode(getServerToken())))
                    .put("voting", context.getBodyAsJson().getJsonObject(""));

            vertx.createHttpClient().post(Configuration.CONTROLLER_PORT, "localhost", "/api/terminate", res -> {
                context.response().setStatusCode(res.statusCode()).end();
            }).end(query.encode());
        }
    }

    private JsonObject getServerToken() {
        return new JsonObject(Json.encode(new Token(serverToken, Configuration.SERVER_NAME)));
    }

    private boolean authorized(RoutingContext context) {
        boolean authorized = clientToken.verifyToken((Token)
                Serializer.unpack(context.getBodyAsJson().getJsonObject("token"), Token.class));

        if (!authorized) {
            context.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
        }

        return authorized;
    }
}
