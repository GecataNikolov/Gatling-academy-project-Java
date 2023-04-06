package gatlingDemoStoreRestApis.pageObjectModel;

import io.gatling.javaapi.core.Choice;
import io.gatling.javaapi.core.ScenarioBuilder;

import static gatlingDemoStoreRestApis.SimulationVariables.TEST_DURATION;
import static io.gatling.javaapi.core.CoreDsl.*;

public class Scenario {
    public static ScenarioBuilder defaultScn =
            scenario("Default Load Test scenario")
                    .during(TEST_DURATION).
                    on(
                            randomSwitch().on(
                                    Choice.withWeight(20d, exec(UserJourney.admin)),
                                    Choice.withWeight(40d, exec(UserJourney.priceScrapper)),
                                    Choice.withWeight(40d, exec(UserJourney.priceUpdater))
                            )
                    );

    public static ScenarioBuilder noAdminScn = scenario("Load test without admin")
            .during(TEST_DURATION)
            .on(
                    randomSwitch().on(
                            Choice.withWeight(40d, UserJourney.priceScrapper),
                            Choice.withWeight(60d, UserJourney.priceUpdater)
                    )
            );
}
