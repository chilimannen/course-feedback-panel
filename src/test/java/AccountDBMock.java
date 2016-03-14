import Model.*;

import java.util.ArrayList;

/**
 * Created by Robin on 2016-03-14.
 * <p/>
 * DA mock class
 */
public class AccountDBMock implements AccountStore {
    private ArrayList<Account> accounts = new ArrayList<>();

    @Override
    public Account find(String username) throws AuthenticationAccountMissingException {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getUsername().equals(username))
                return accounts.get(i);
        }

        throw new AuthenticationAccountMissingException();
    }

    @Override
    public Account authenticate(String username, String password) throws AuthenticationException {
        Account account = find(username);
        if (account.getPassword().equals(password))
            return account;
        else
            throw new AuthenticationPasswordMismatchException();
    }

    @Override
    public Account register(String username, String password) throws AuthenticationExistsException {
        try {
            find(username);
            throw new AuthenticationExistsException();
        } catch (AuthenticationAccountMissingException e) {
            Account account = new Account()
                    .setUsername(username)
                    .setPassword(password);

            accounts.add(account);
            return account;
        }
    }
}
