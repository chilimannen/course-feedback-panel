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
    private static final String COLLECTION = "accounts";
    private MongoClient client;

    public AccountDB(MongoClient client) {
        this.client = client;
    }


    @Override
    public void find(String username, Future<Account> future) {
        JsonObject query = new JsonObject().put("username", username);

        client.findOne(COLLECTION, query, null, account -> {
            if (account.succeeded() && account.result() != null)
                future.complete(filter((AccountMapping) Serializer.unpack(account.result(), AccountMapping.class)));
            else
                future.fail(new AccountMissingException());
        });
    }

    @Override
    public void register(Account registrant, Future<Account> future) {
        JsonObject query = new JsonObject().put("username", registrant.getUsername());
        JsonObject account = new JsonObject(Serializer.pack(registrant));

        client.findOne(COLLECTION, query, null, search -> {

            if (search.succeeded() && search.result() == null) {
                String salt = HashHelper.generateSalt();
                account.put("salt", salt);
                account.put("hash", HashHelper.hash(account.getString("password"), salt));

                client.save(COLLECTION, account, result -> {
                    if (result.succeeded()) {
                        registrant.setPassword(null);
                        future.complete(registrant);
                    } else
                        future.fail(result.cause());
                });
            } else {
                future.fail(new AccountExistsException());
            }

        });
    }

    @Override
    public void authenticate(Account unauthenticated, Future<Account> future) {
        JsonObject query = new JsonObject().put("username", unauthenticated.getUsername());

        client.findOne(COLLECTION, query, null, result -> {
            if (result.succeeded() && result.result() != null) {
                AccountMapping account = (AccountMapping) Serializer.unpack(result.result(), AccountMapping.class);
                if (authenticate(account, unauthenticated)) {
                    future.complete(filter(account));
                } else
                    future.fail(new AccountPasswordException());
            } else {
                future.fail(new AccountMissingException());
            }
        });
    }

    private boolean authenticate(AccountMapping authenticated, Account unauthenticated) {
        String hash = HashHelper.hash(unauthenticated.getPassword(), authenticated.getSalt());
        return HashHelper.compare(hash, authenticated.getHash());
    }

    private Account filter(AccountMapping account) {
        return new Account(account);
    }
}
