import Controller.Webserver;
import Controller.WebserverStartupException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 *
 * Launches the components required for the control-panel.
 */
public class Launcher extends AbstractVerticle {

    public void start(Future<Void> future) {
        vertx.deployVerticle(
                Webserver.class.getName(),
                new DeploymentOptions().setInstances(Runtime.getRuntime().availableProcessors()),

                result -> {
                    if (result.succeeded())
                        future.complete();
                    else
                        future.fail(new WebserverStartupException());
                });
    }

    public void stop(Future<Void> future) {
        future.complete();
    }
}
