import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;

public class CreateOrderTest {

    private Order order;
    private OrderClient orderClient;
    private User user;
    private UserClient userClient;
    private String accessToken;

    private static final String ERROR_NULL_INGREDIENTS = "Ingredient ids must be provided";

    @Before
    public void setUp() {
        orderClient = new OrderClient();
        userClient = new UserClient();
        User user = Generator.getRandomUser();
        userClient.createUser(user);
        Response responseLogin = userClient.login(UserCredentials.from(user));
        accessToken = responseLogin.jsonPath().getString("accessToken");
    }

    @Test
    @DisplayName("Создание заказа с авторизацией")
    public void createOrderWithAuth() {
        order = Order.getDefaultOrder();
        Response orderResponse = orderClient.orderCreate(order, accessToken);

        int StatusCode = orderResponse.getStatusCode();
        Assert.assertEquals(SC_OK, statusCode);

        boolean OrderCreate = orderResponse.jsonPath().getBoolean("success");
        Assert.assertTrue(orderCreate);

        int OrderNumber = orderResponse.jsonPath().getInt("order.number");
        Assert.assertNotEquals(0, orderNumber);
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    public void creatOrderWithoutAuth() {
        order = Order.getDefaultOrder();
        Response response = orderClient.orderCreate(order, "");

        int StatusCode = response.getStatusCode();
        Assert.assertEquals(SC_OK, statusCode);

        boolean OrderCreate = response.jsonPath().getBoolean("success");
        Assert.assertTrue(orderCreate);
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    public void createOrderWithoutIngredients() {
        Order order = new Order(null);
        Response response = orderClient.orderCreate(order, "accessToken");

        int StatusCode = response.getStatusCode();
        Assert.assertEquals(SC_BAD_REQUEST, statusCode);

        boolean OrderCreate = response.jsonPath().getBoolean("success");
        Assert.assertFalse(orderCreate);

        String Message = response.jsonPath().getString("message");
        Assert.assertEquals(errorNullIngredients, message);
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    public void createOrderWithIncorrectIngredientsHash() {
        order = Order.getOrderIncorrectHash();
        Response response = orderClient.orderCreate(order, accessToken);

        int StatusCode = response.getStatusCode();
        Assert.assertEquals(SC_INTERNAL_SERVER_ERROR, statusCode);
    }

    @After
    public void tearDown() {
        userClient.delete(accessToken);
    }
}
