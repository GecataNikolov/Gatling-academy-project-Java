package gatlingDemoStoreRestApis;

import io.gatling.javaapi.core.*;

import static io.gatling.javaapi.core.CoreDsl.*;

import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.http.HttpDsl.*;

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


    private ScenarioBuilder scn = scenario("DemoStoreRestApiSimulation")
            .exec(initSession)
            .exec(Category.listCategories)
            .pause(2)
            .exec(Product.listProducts)
            .pause(2)
            .exec(Product.getProduct)
            .pause(2)
            .exec(Product.updateProduct)
            .pause(2)
            .repeat(3).on(exec(Product.createProduct))
            .pause(2)
            .exec(Category.updateCategory);

    {
        setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    }

}
