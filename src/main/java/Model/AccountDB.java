package Model;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 * Created by Robin on 2016-03-14.
 * <p/>
 * Implementation of asynchronous account store.
 */
public class AccountDB implements AsyncAccountStore {
    private MongoClient client;
    private static final String COLLECTION = "accounts";

    public AccountDB(MongoClient client) {
        this.client = client;
    }


    @Override
    public void find(String username, Future<Account> future) {
        JsonObject query = new JsonObject().put("username", username);

        client.findOne(COLLECTION, query, null, account -> {
            if (account.succeeded() && account.result() != null)
                future.complete((Account) Serializer.unpack(account.result(), Account.class));
            else
                future.fail(new AccountMissingException());
        });
    }


    @Override
    public void authenticate(Account unauthenticated, Future<Account> future) {
        JsonObject query = new JsonObject().put("username", unauthenticated.getUsername());

        client.findOne(COLLECTION, query, null, result -> {
            if (result.succeeded() && result.result() != null) {
                Account account = (Account) Serializer.unpack(result.result(), Account.class);
                if (account.getPassword().equals(unauthenticated.getPassword()))
                    future.complete(account);
                else
                    future.fail(new AccountPasswordException());
            } else {
                future.fail(new AccountMissingException());
            }
        });
    }


    @Override
    public void register(Account registrant, Future<Account> future) {
        JsonObject query = new JsonObject().put("username", registrant.getUsername());
        JsonObject account = new JsonObject(Serializer.pack(registrant));

        client.findOne(COLLECTION, query, null, search -> {

            if (search.succeeded() && search.result() == null) {
                client.save(COLLECTION, account, result -> {
                    if (result.succeeded()) {
                        future.complete(registrant);
                    } else
                        future.fail(result.cause());
                });
            } else {
                future.fail(new AccountExistsException());
            }

        });
    }
}
