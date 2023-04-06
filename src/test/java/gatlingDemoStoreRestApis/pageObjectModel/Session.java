package gatlingDemoStoreRestApis.pageObjectModel;

import io.gatling.javaapi.core.ChainBuilder;

import static io.gatling.javaapi.core.CoreDsl.exec;

public class Session {
    public static ChainBuilder initSession =
            exec(session -> session.set("isAuthenticated", false));
}
