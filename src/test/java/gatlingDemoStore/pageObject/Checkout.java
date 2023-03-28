package gatlingDemoStore.pageObject;

import io.gatling.javaapi.core.ChainBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.core.CoreDsl.substring;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class Checkout {
    public static final ChainBuilder viewCart =
            doIf(session -> !session.getBoolean("customerLoggedIn"))
                    .then(exec(Customer.login))
                    .exec(http("Load Cart Page")
                            .get("/cart/view")
                            .check(status().is(200))
                            .check(css("#grandTotal").isEL("$#{cartTotal}")));


    public static final ChainBuilder completeCheckout =
            exec(
                    http("Checkout Cart")
                            .get("/cart/checkout")
                            .check(substring("Thanks for your order! See you soon!")));
}
