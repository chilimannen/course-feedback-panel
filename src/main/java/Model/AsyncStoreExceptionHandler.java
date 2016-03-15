package Model;

/**
 * Created by Robin on 2016-03-15.
 *
 * Handles exceptions in the store.
 */
public interface AsyncStoreExceptionHandler {
    void handle(AccountException e);
}
