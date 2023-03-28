package gatlingDemoStore.pageObject;

import gatlingDemoStore.helpers.SimulationDefaults;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.ScenarioBuilder;

import java.time.Duration;

import static gatlingDemoStore.SessionId.initSession;
import static io.gatling.javaapi.core.CoreDsl.*;

public final class UserJourneys {
    private static final Duration MIN_PAUSE_TIME = Duration.ofMillis(300);
    private static final Duration MAX_PAUSE_TIME = Duration.ofMillis(600);

    public static final ChainBuilder browseStore =
            exec(initSession)
                    .exec(CmsPages.homepage)
                    .pause(MAX_PAUSE_TIME)
                    .exec(CmsPages.aboutUs)
                    .pause(MIN_PAUSE_TIME, MAX_PAUSE_TIME)
                    .repeat(5)
                    .on(
                            exec(Catalog.Category.view)
                                    .pause(MIN_PAUSE_TIME, MAX_PAUSE_TIME)
                                    .exec(Catalog.Product.view)
                                    .pause(MIN_PAUSE_TIME, MAX_PAUSE_TIME)
                    );

    public static final ChainBuilder abandonCart =
            exec(initSession)
                    .exec(CmsPages.homepage)
                    .pause(MAX_PAUSE_TIME)
                    .exec(CmsPages.aboutUs)
                    .pause(MIN_PAUSE_TIME, MAX_PAUSE_TIME)
                    .exec(Catalog.Category.view)
                    .pause(MIN_PAUSE_TIME, MAX_PAUSE_TIME)
                    .exec(Catalog.Product.view)
                    .pause(MIN_PAUSE_TIME, MAX_PAUSE_TIME)
                    .exec(Catalog.Product.add);

    public static final ChainBuilder checkout =
            exec(initSession)
                    .exec(CmsPages.homepage)
                    .pause(MAX_PAUSE_TIME)
                    .exec(CmsPages.aboutUs)
                    .pause(MIN_PAUSE_TIME, MAX_PAUSE_TIME)
                    .exec(Catalog.Category.view)
                    .pause(MIN_PAUSE_TIME, MAX_PAUSE_TIME)
                    .exec(Catalog.Product.view)
                    .pause(MIN_PAUSE_TIME, MAX_PAUSE_TIME)
                    .exec(Catalog.Product.add)
                    .pause(MIN_PAUSE_TIME, MAX_PAUSE_TIME)
                    .exec(Checkout.viewCart)
                    .pause(MIN_PAUSE_TIME, MAX_PAUSE_TIME)
                    .exec(Checkout.completeCheckout);

    public static class Scenarios {
        public static final ScenarioBuilder defaultScenario =
                scenario("Default")
                        .during(SimulationDefaults.TEST_DURATION)
                        .on(randomSwitch()
                                .on(
                                        Choice.withWeight(75.0, exec(browseStore)),
                                        Choice.withWeight(15.0, exec(abandonCart)),
                                        Choice.withWeight(10.0, exec(checkout))
                                ));

        public static final ScenarioBuilder highPurchase =
                scenario("High purchase scenario")
                        .during(SimulationDefaults.TEST_DURATION)
                        .on(
                                randomSwitch()
                                        .on(
                                                Choice.withWeight(25.0, exec(browseStore)),
                                                Choice.withWeight(25.0, exec(abandonCart)),
                                                Choice.withWeight(50.0, exec(checkout))
                                        )
                        );
    }
}
