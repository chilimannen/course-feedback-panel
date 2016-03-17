package Model;

import io.vertx.core.Future;

/**
 * Created by Robin on 2016-03-17.
 */
public interface AsyncControllerClient {
    void create(Future<Void> future, Voting voting, Token token);

    void terminate(Future<Void> future, Voting voting, Token token);

    void list(Future<VotingList> future, String username);
}
