package Model;

import Configuration.Configuration;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

/**
 * Created by Robin on 2016-03-17.
 */
public class ControllerClient implements AsyncControllerClient {
    private Vertx vertx;
    private TokenFactory tokens = new TokenFactory(Configuration.SERVER_SECRET);

    public ControllerClient(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void create(Future<Void> future, Voting voting, Token token) {
        vertx.createHttpClient().post(Configuration.CONTROLLER_PORT, "localhost", "/api/create", result -> {

            if (result.statusCode() == HttpResponseStatus.OK.code()) {
                future.complete();
            } else
                future.fail(new ControllerFailureException());
        }).end(
                new JsonObject()
                        .put("voting", Serializer.json(voting))
                        .put("token", getServerToken())
                        .encode()
        );
    }

    @Override
    public void terminate(Future<Void> future, Voting voting, Token token) {
        vertx.createHttpClient().post(Configuration.CONTROLLER_PORT, "localhost", "/api/terminate", result -> {

            if (result.statusCode() == HttpResponseStatus.OK.code()) {
                future.complete();
            } else
                future.fail(new ControllerFailureException());
        }).end(
                new JsonObject()
                        .put("voting", Serializer.json(voting))
                        .put("token", getServerToken())
                        .put("owner", token.getDomain())
                        .encode()
        );
    }

    @Override
    public void list(Future<VotingList> future, Account account, Token token) {
        vertx.createHttpClient().post(Configuration.CONTROLLER_PORT, "localhost", "/api/list", result -> {

            if (result.statusCode() == HttpResponseStatus.OK.code()) {
                result.bodyHandler(body -> {
                    VotingList votings = (VotingList) Serializer.unpack(body.toJsonObject(), VotingList.class);

                    future.complete(votings);
                });
            } else
                future.fail(new ControllerFailureException());
        }).end(
                new JsonObject()
                        .put("owner", Serializer.json(token))
                        .put("token", getServerToken())
                        .encode()
        );
    }

    private JsonObject getServerToken() {
        return Serializer.json(new Token(tokens, Configuration.SERVER_NAME));
    }
}
