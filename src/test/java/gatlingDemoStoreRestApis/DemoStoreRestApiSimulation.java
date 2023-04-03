package gatlingDemoStoreRestApis;

import io.gatling.javaapi.core.*;

import static io.gatling.javaapi.core.CoreDsl.*;

import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.http.HttpDsl.*;

import java.util.Map;


public class DemoStoreRestApiSimulation extends Simulation {
    private HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://demostore.gatling.io")
            .header("Cache-Control", "no-cache")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");


    private static Map<CharSequence, String> authorizationHeader = Map.ofEntries(
            Map.entry("authorization", "Bearer #{jwt}")
    );


    private static class Authenticate {
        private static ChainBuilder authenticate =
                exec(http("Authenticate")
                        .post("/api/authenticate")
                        .body(StringBody("{\"username\":\"admin\",\"password\":\"admin\"}"))
                        .check(status().is(200))
                        .check(jsonPath("$.token").saveAs("jwt"))
                );
    }

    private static class Category {
        private static ChainBuilder listCategories =
                exec(http("List Categories")
                        .get("/api/category")
                        .check(jsonPath("$[?(@.id == 6)].name").is("For Her"))
                );
        private static ChainBuilder updateCategory =
                exec(
                        http("Update category")
                                .put("/api/category/7")
                                .headers(authorizationHeader)
                                .body(StringBody("{\"name\":\"Everyone\"}"))
                                .check(status().is(200))
                                .check(jsonPath("$.name").is("Everyone")
                                ));
    }

    public static class Product {
        private static ChainBuilder listProduct =
                exec(http("List products")
                        .get("/api/product?category=7")
                        .check(jsonPath("$[?(@.categoryId != \"7\")]").notExists())
                );
        private static ChainBuilder getProduct =
                exec(http("Get product")
                        .get("/api/product/34")
                        .check(jsonPath("$.id").ofInt().is(34))
                );
        private static ChainBuilder updateProduct = exec(
                http("Update Product")
                        .put("/api/product/34")
                        .headers(authorizationHeader)
                        .body(RawFileBody("demostoreapisimulation/update-product-1.json"))
                        .check(jsonPath("$.price").is("15.99"))
        );

        private static ChainBuilder createProducts =
                repeat(3, "counter").on(
                        exec(
                                http("Create product #{counter}")
                                        .post("/api/product")
                                        .headers(authorizationHeader)
                                        .body(RawFileBody("demostoreapisimulation/create-product-#{counter}.json"))
                        )
                                .pause(2)
                );
    }


    private ScenarioBuilder scn = scenario("DemoStoreRestApiSimulation")
            .exec(Category.listCategories)
            .pause(2)
            .exec(Product.listProduct)
            .pause(2)
            .exec(Product.getProduct)
            .pause(2)
            .exec(Authenticate.authenticate)
            .pause(2)
            .exec(Product.updateProduct)
            .pause(2)
            .exec(Product.createProducts)
            .pause(2)
            .exec(Category.updateCategory);

    {
        setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    }

}
