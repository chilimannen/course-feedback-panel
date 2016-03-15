package Model;

import io.vertx.core.Future;

/**
 * Created by Robin on 2016-03-14.
 * <p/>
 * Asynchronous account store.
 */
public interface AsyncAccountStore {
    void find(String username, Future<Account> future);

    void authenticate(Account account, Future<Account> future);

    void register(Account account, Future<Account> future);
}
