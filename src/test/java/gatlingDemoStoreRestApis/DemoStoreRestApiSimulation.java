package gatlingDemoStoreRestApis;

import io.gatling.javaapi.core.*;

import static io.gatling.javaapi.core.CoreDsl.*;

import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class DemoStoreRestApiSimulation extends Simulation {
    private HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://demostore.gatling.io")
            .header("Cache-Control", "no-cache")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");


    private static Map<CharSequence, String> authorizationHeader = Map.ofEntries(
            Map.entry("authorization", "Bearer #{jwt}")
    );


    private static ChainBuilder initSession =
            exec(session -> session.set("isAuthenticated", false));

    private static class Authenticate {
        private static ChainBuilder authenticate =
                doIf(session -> !session.getBoolean("isAuthenticated")).then(
                        exec(http("Authenticate")
                                .post("/api/authenticate")
                                .body(StringBody("{\"username\":\"admin\",\"password\":\"admin\"}"))
                                .check(status().is(200))
                                .check(jsonPath("$.token").saveAs("jwt"))
                        )
                                .exec(session -> session.set("isAuthenticated", true)));
    }

    private static class Category {
        private static FeederBuilder.Batchable<String> categories = csv("demostoreapisimulation/feeders/category.csv").random();
        private static ChainBuilder listCategories =
                exec(http("List Categories")
                        .get("/api/category")
                        .check(jsonPath("$[?(@.id == 6)].name").is("For Her"))
                );
        private static ChainBuilder updateCategory =
                feed(categories)
                        .exec(Authenticate.authenticate)
                        .exec(
                                http("Update category")
                                        .put("/api/category/#{categoryId}")
                                        .headers(authorizationHeader)
                                        .body(StringBody("{\"name\":\"#{categoryName}\"}"))
                                        .check(status().is(200))
                                        .check(jsonPath("$.name").isEL("#{categoryName}")));
    }

    public static class Product {
        private static FeederBuilder.Batchable<String> products = csv("demostoreapisimulation/feeders/products.csv").circular();

        public static ChainBuilder listAllProducts = exec(http("List all products")
                .get("/api/product")
                .check(jmesPath("@").ofList().saveAs("allProducts"))
        );

        private static ChainBuilder listProducts =
                exec(http("List products")
                        .get("/api/product?category=7")
                        .check(jsonPath("$[?(@.categoryId != \"7\")]").notExists())
                        .check(jmesPath("[*].id").ofList().saveAs("allProductIds"))
                );
        private static ChainBuilder getProduct =
                exec(session -> {
                    List<Integer> allProducts = session.getList("allProductIds");
                    return session.set("productId", allProducts.get(new Random().nextInt(allProducts.size())));
                })
                        .exec(http("Get product- #{productId}")
                                .get("/api/product/#{productId}")
                                .check(jsonPath("$.id").ofInt().isEL("#{productId}"))
                                .check(jmesPath("@").ofMap().saveAs("product"))
                        );
        private static ChainBuilder updateProduct =
                exec(Authenticate.authenticate)
                        .exec(session -> {
                            Map<String, Object> product = session.getMap("product");
                            return session.set("productCategoryId", product.get("categoryId"))
                                    .set("productPrice", product.get("price"))
                                    .set("productName", product.get("name"))
                                    .set("productDescription", product.get("description"))
                                    .set("productImage", product.get("image"))
                                    .set("productId", product.get("id"));
                        })
                        .exec(
                                http("Update Product - #{productName}")
                                        .put("/api/product/#{productId}")
                                        .headers(authorizationHeader)
                                        .body(ElFileBody("demostoreapisimulation/requestBodyData/create-product.json"))
                                        .check(jsonPath("$.price").isEL("#{productPrice}"))
                        );

        private static ChainBuilder createProduct =
                feed(products)
                        .exec(Authenticate.authenticate)
                        .exec(
                                http("Create product #{productName}")
                                        .post("/api/product")
                                        .headers(authorizationHeader)
                                        .body(ElFileBody("demostoreapisimulation/requestBodyData/create-product.json"))
                        );
    }


    private static class UserJourneys {
        private final static Duration minTimeout = Duration.ofMillis(200);
        private final static Duration maxTimeout = Duration.ofSeconds(2);

        public static ChainBuilder admin =
                exec(initSession)
                        .exec(Category.listCategories)
                        .pause(minTimeout, maxTimeout)
                        .exec(Product.listProducts)
                        .pause(minTimeout, maxTimeout)
                        .exec(Product.getProduct)
                        .pause(minTimeout, maxTimeout)
                        .exec(Product.updateProduct)
                        .pause(minTimeout, maxTimeout)
                        .repeat(3).on(Product.createProduct)
                        .pause(minTimeout, maxTimeout)
                        .exec(Category.updateCategory);

        private static ChainBuilder priceScrapper =
                exec(
                        Category.listCategories,
                        pause(minTimeout, maxTimeout),
                        Product.listAllProducts
                );
        private static ChainBuilder priceUpdater =
                exec(initSession)
                        .exec(Product.listAllProducts)
                        .repeat("#{allProducts.size()}", "productIndex").on(
                                exec(session -> {
                                    int index = session.getInt("productIndex");
                                    List<Object> allProducts = session.getList("allProducts");
                                    return session.set("product", allProducts.get(index));
                                })
                                        .exec(Product.updateProduct)
                        );
    }

    private static class Scenario {
        public static ScenarioBuilder defaultScn =
                scenario("Default Load Test scenario")
                        .during(Duration.ofSeconds(60)).
                        on(
                                randomSwitch().on(
                                        Choice.withWeight(20d, exec(UserJourneys.admin)),
                                        Choice.withWeight(40d, exec(UserJourneys.priceScrapper)),
                                        Choice.withWeight(40d, exec(UserJourneys.priceUpdater))
                                )
                        );

        public static ScenarioBuilder noAdminScn = scenario("Load test without admin")
                .during(Duration.ofSeconds(60))
                .on(
                        randomSwitch().on(
                                Choice.withWeight(40d, UserJourneys.priceScrapper),
                                Choice.withWeight(60d, UserJourneys.priceUpdater)
                        )
                );

    }


    {
        setUp(
                Scenario.defaultScn.injectOpen(atOnceUsers(1)),
                Scenario.noAdminScn.injectOpen(atOnceUsers(1))
        ).protocols(httpProtocol);
    }

}
