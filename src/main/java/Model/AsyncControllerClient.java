package Model;

import io.vertx.core.Future;

/**
 * @author Robin Duda
 */
public interface AsyncControllerClient {
    /**
     * Request the controller to create a new voting.
     * @param future
     * @param voting the voting to be created.
     */
    void create(Future<Void> future, Voting voting);

    /**
     * Request the controller to terminate a voting.
     * @param future
     * @param voting voting to be removed containing owner and id.
     */
    void terminate(Future<Void> future, Voting voting);

    /**
     * Request the controller to list all votings with specified owner.
     * @param future
     * @param username the specified owner as username string.
     */
    void list(Future<VotingList> future, String username);
}
