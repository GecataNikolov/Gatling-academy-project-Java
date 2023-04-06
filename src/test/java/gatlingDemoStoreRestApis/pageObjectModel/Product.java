package gatlingDemoStoreRestApis.pageObjectModel;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static gatlingDemoStoreRestApis.pageObjectModel.Headers.authorizationHeader;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.core.CoreDsl.ElFileBody;
import static io.gatling.javaapi.http.HttpDsl.http;

public class Product {
    private static final FeederBuilder.Batchable<String> products = csv("demostoreapisimulation/feeders/products.csv").circular();

    public static ChainBuilder listAllProducts = exec(http("List all products")
            .get("/api/product")
            .check(jmesPath("@").ofList().saveAs("allProducts"))
    );

    public static ChainBuilder listProducts =
            exec(http("List products")
                    .get("/api/product?category=7")
                    .check(jsonPath("$[?(@.categoryId != \"7\")]").notExists())
                    .check(jmesPath("[*].id").ofList().saveAs("allProductIds"))
            );
    public static ChainBuilder getProduct =
            exec(session -> {
                List<Integer> allProducts = session.getList("allProductIds");
                return session.set("productId", allProducts.get(new Random().nextInt(allProducts.size())));
            })
                    .exec(http("Get product- #{productId}")
                            .get("/api/product/#{productId}")
                            .check(jsonPath("$.id").ofInt().isEL("#{productId}"))
                            .check(jmesPath("@").ofMap().saveAs("product"))
                    );
    public static ChainBuilder updateProduct =
            exec(Authentication.authenticate)
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

    public static ChainBuilder createProduct =
            feed(products)
                    .exec(Authentication.authenticate)
                    .exec(
                            http("Create product #{productName}")
                                    .post("/api/product")
                                    .headers(authorizationHeader)
                                    .body(ElFileBody("demostoreapisimulation/requestBodyData/create-product.json"))
                    );
}
