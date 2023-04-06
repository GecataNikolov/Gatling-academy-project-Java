package gatlingDemoStoreRestApis.pageObjectModel;

import io.gatling.javaapi.core.ChainBuilder;

import java.time.Duration;
import java.util.List;

import static gatlingDemoStoreRestApis.pageObjectModel.Session.initSession;
import static io.gatling.javaapi.core.CoreDsl.exec;

public class UserJourney {
    private final static Duration minTimeout = Duration.ofMillis(900);
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
                    .exec(Category.updateCategory)
                    .pause(minTimeout, maxTimeout);

    public static ChainBuilder priceScrapper =
            exec(
                    Category.listCategories,
                    Product.listAllProducts
            );
    public static ChainBuilder priceUpdater =
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
