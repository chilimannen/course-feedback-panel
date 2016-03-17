package Model;

import io.vertx.core.Future;

/**
 * Created by Robin on 2016-03-14.
 * <p/>
 * Asynchronous account store.
 */
public interface AsyncAccountStore {
    void find(Future<Account> future, String username);

    void authenticate(Future<Account> future, Account account);

    void register(Future<Account> future, Account account);
}
