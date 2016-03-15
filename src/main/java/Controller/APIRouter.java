package Controller;

import Model.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * @Author Robin Duda
 */
class APIRouter {
    private AsyncAccountStore accounts;

    public void register(Router router, AsyncAccountStore accounts) {
        this.accounts = accounts;

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
                if (future.succeeded()) {
                    Token token = new Token(result.result().getUsername(), System.currentTimeMillis());
                    response
                            .setStatusCode(HttpResponseStatus.OK.code())
                            .end(Json.encode(new Authentication(result.result(), token)));
                } else {
                    throw future.cause();
                }
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
                if (future.succeeded()) {
                    Token token = new Token(result.result().getUsername(), System.currentTimeMillis());
                    response
                            .setStatusCode(HttpResponseStatus.OK.code())
                            .end(Json.encode(new Authentication(result.result(), token)));
                } else
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

    private void list(RoutingContext context) {
        context.response().end(context.getBody());
    }

    private void create(RoutingContext context) {
        context.response().end(context.getBody());
    }

    private void terminate(RoutingContext context) {
        context.response().end(context.getBody());
    }
}
