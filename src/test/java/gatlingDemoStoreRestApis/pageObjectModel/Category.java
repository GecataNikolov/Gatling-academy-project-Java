package gatlingDemoStoreRestApis.pageObjectModel;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class Category {
    private static final FeederBuilder.Batchable<String> categories = csv("demostoreapisimulation/feeders/category.csv").random();
    public static ChainBuilder listCategories =
            exec(http("List Categories")
                    .get("/api/category")
                    .check(jsonPath("$[?(@.id == 6)].name").is("For Her"))
            );
    public static ChainBuilder updateCategory =
            feed(categories)
                    .exec(Authentication.authenticate)
                    .exec(
                            http("Update category")
                                    .put("/api/category/#{categoryId}")
                                    .headers(Headers.authorizationHeader)
                                    .body(StringBody("{\"name\":\"#{categoryName}\"}"))
                                    .check(status().is(200))
                                    .check(jsonPath("$.name").isEL("#{categoryName}")));
}
