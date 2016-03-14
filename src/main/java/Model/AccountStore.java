package Model;

/**
 * Created by Robin on 2016-03-14.
 *
 *
 */
public interface AccountStore {
    Account find(String username) throws AuthenticationAccountMissingException;
    Account authenticate(String username, String password) throws AuthenticationPasswordMismatchException, AuthenticationException;
    Account register(String username, String password) throws AuthenticationExistsException;
}
