package Controller;

import Model.AccountStore;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * @Author Robin Duda
 */
class APIRouter {
    private AccountStore accounts;

    public void register(Router router, AccountStore accounts) {
        this.accounts = accounts;

        router.post("/api/register").handler(this::register);
        router.post("/api/authenticate").handler(this::authenticate);
        router.get("/api/list").handler(this::list);
        router.post("/api/create").handler(this::create);
        router.post("/api/terminate").handler(this::terminate);
    }

    private void register(RoutingContext context) {
        context.response().end();
    }

    private void authenticate(RoutingContext context) {
        context.response().end(context.data().toString());
    }

    private void list(RoutingContext context) {
        context.response().end(context.getBody());
    }

    private void create(RoutingContext context) {
        context.response().end(context.getBody());
    }

    private void terminate(RoutingContext context) {
        context.response().end(context.getBody());
    }
}
