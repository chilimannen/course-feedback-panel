import Model.*;
import io.vertx.core.Future;

import java.util.ArrayList;

/**
 * Created by Robin on 2016-03-17.
 */
public class ControllerClientMock implements AsyncControllerClient {
    private VotingList votings = new VotingList();
    private boolean throwException;

    public ControllerClientMock() {
        this(false);
    }

    public ControllerClientMock(boolean throwException) {
        ArrayList<Query> queries = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        this.throwException = throwException;

        values.add("a");
        values.add("b");
        values.add("c");

        queries.add(new Query()
                        .setName("q1")
                        .setValues(values)
        );

        queries.add(new Query()
                        .setName("q2")
                        .setValues(values)
        );

        Voting voting = new Voting()
                .setId("id")
                .setTopic("Vote #1")
                .setOwner("gosu")
                .setOptions(queries);

        votings.add(voting);
    }

    @Override
    public void create(Future<Void> future, Voting voting, Token token) {
        if (throwException)
            future.fail(new ControllerFailureException());
        else
            future.complete();
    }

    @Override
    public void terminate(Future<Void> future, Voting voting, Token token) {
        if (throwException)
            future.fail(new ControllerFailureException());
        else {
            future.complete();
        }
    }

    @Override
    public void list(Future<VotingList> future, String username) {
        if (throwException)
            future.fail(new ControllerFailureException());
        else
            future.complete(votings);
    }
}
