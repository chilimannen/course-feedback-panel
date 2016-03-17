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
    private AsyncControllerClient client;
    private Vertx vertx;
    private TokenFactory clientToken;
    private TokenFactory serverToken;

    public void register(Router router, AsyncAccountStore accounts, AsyncControllerClient client, Vertx vertx) {
        this.accounts = accounts;
        this.client = client;
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
        accounts.register(future, account);
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
        accounts.authenticate(future, account);
    }

    private void sendAuthentication(Account account, RoutingContext context, boolean registered) {
        Token token = new Token(clientToken, account.getUsername());
        context.response()
                .setStatusCode(HttpResponseStatus.OK.code())
                .end(Serializer.pack(new Authentication(account, token, registered)));
    }

    private void create(RoutingContext context) {
        HttpServerResponse response = context.response();
        Future<Void> future = Future.future();

        if (authorized(context)) {
            Voting voting = (Voting) Serializer.unpack(context.getBodyAsJson().getJsonObject("voting"), Voting.class);
            voting.setOwner(tokenFrom(context).getDomain());

            future.setHandler(result -> {
                if (result.succeeded())
                    response.setStatusCode(HttpResponseStatus.OK.code()).end();
                else
                    response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            });
            client.create(future, voting, tokenFrom(context));
        }
    }

    private void list(RoutingContext context) {
        HttpServerResponse response = context.response();
        Future<VotingList> controller = Future.future();

        if (authorized(context)) {
            Token token = (Token) Serializer.unpack(context.getBodyAsJson().getJsonObject("token"), Token.class);
            Future<Account> find = Future.future();

            find.setHandler(found -> {

                controller.setHandler(result -> {
                    if (result.succeeded()) {
                        response.end(Serializer.pack(result.result()));
                    } else
                        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                });

                client.list(controller, found.result(), tokenFrom(context));
            });
            accounts.find(find, token.getDomain());
        }
    }

    private void terminate(RoutingContext context) {
        HttpServerResponse response = context.response();
        Future<Void> future = Future.future();

        if (authorized(context)) {
            Voting voting = (Voting) Serializer.unpack(context.getBodyAsJson().getJsonObject("voting"), Voting.class);

            future.setHandler(result -> {
                if (result.succeeded())
                    response.setStatusCode(HttpResponseStatus.OK.code()).end();
                else
                    response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
            });
            client.terminate(future, voting, tokenFrom(context));
        }
    }

    private Token tokenFrom(RoutingContext context) {
        return (Token) Serializer.unpack(context.getBodyAsJson().getJsonObject("token"), Token.class);
    }

    private boolean authorized(RoutingContext context) {
        boolean authorized = clientToken.verifyToken(tokenFrom(context));

        if (!authorized) {
            context.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
        }

        return authorized;
    }
}
