package gatlingDemoStore;

import gatlingDemoStore.helpers.SimulationDefaults;
import io.gatling.javaapi.core.ChainBuilder;

import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.http.HttpDsl.*;

public class SessionId {

    private static final char[] CANDIDATES =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int SESSION_ID_LENGTH = 10;

    static String random() {
        StringBuilder buffer = new StringBuilder(SESSION_ID_LENGTH);
        for (int i = 0; i < SESSION_ID_LENGTH; i++) {
            buffer.append(CANDIDATES[ThreadLocalRandom.current().nextInt(CANDIDATES.length)]);
        }
        return buffer.toString();
    }

    public static final ChainBuilder initSession =
            exec(flushCookieJar())
                    .exec(session -> session.set("randomNumber", ThreadLocalRandom.current().nextInt()))
                    .exec(session -> session.set("customerLoggedIn", false))
                    .exec(session -> session.set("cartTotal", 0.00))
                    .exec(addCookie(Cookie("sessionId", SessionId.random()).withDomain(SimulationDefaults.DOMAIN)));
}
