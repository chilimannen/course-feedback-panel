package Model;

/**
 * Created by Robin on 2016-03-14.
 *
 */
public class AccountDB implements AccountStore {
    @Override
    public Account find(String username) {
        return null;
    }

    @Override
    public Account authenticate(String username, String password) throws AuthenticationPasswordMismatchException {
        return null;
    }

    @Override
    public Account register(String username, String password) throws AuthenticationExistsException {
        return null;
    }
}
