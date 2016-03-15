import Model.*;
import io.vertx.core.Future;

import java.util.ArrayList;

/**
 * Created by Robin on 2016-03-14.
 * <p/>
 * DA mock class
 */
public class AccountDBMock implements AsyncAccountStore {
    private ArrayList<Account> accounts = new ArrayList<>();

    public AccountDBMock() {
        accounts.add(new Account().setUsername("usertest").setPassword("userpass"));
    }

    @Override
    public void find(String username, Future<Account> future) {
        Account account = null;

        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getUsername().equals(username))
                account = accounts.get(i);
        }

        if (account == null)
            future.fail(new AccountMissingException());
        else
            future.complete(account);
    }

    @Override
    public void authenticate(Account account, Future<Account> future) {
        Future<Account> findFuture = Future.future();

        findFuture.setHandler(result -> {
            if (result.succeeded()) {
                if (account.getPassword().equals(result.result().getPassword()))
                    future.complete(result.result());
                else
                    future.fail(new AccountPasswordException());
            } else
                future.fail(new AccountMissingException());
        });

        find(account.getUsername(), findFuture);
    }

    @Override
    public void register(Account account, Future<Account> future) {
        Future<Account> findFuture = Future.future();

        findFuture.setHandler(result -> {
            if (result.succeeded()) {
                future.fail(new AccountExistsException());
            } else {
                accounts.add(account);
                future.complete(account);
            }
        });

        find(account.getUsername(), findFuture);
    }
}
