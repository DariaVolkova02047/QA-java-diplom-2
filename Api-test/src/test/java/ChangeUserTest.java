import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChangeUserTest {
    private UserClient userClient;
    private User user;
    private String accessToken;
    private final String authErrorMessage = "You should be authorised";
    private final String existsEmailError = "User with such email already exists";


    @Before
    public void setUp() {
        user = Generator.getRandomUser();
        userClient = new UserClient();
        userClient.createUser(user);
        Response loginResponse = userClient.login(UserCredentials.from(user));
        accessToken = loginResponse.body().jsonPath().getString("accessToken");
    }

    @Test
    @DisplayName("Изменение данных")
    public void changeUserDataWithLogin() {
        User updateUser = Generator.getRandomUser();
        Response UpdateUserResponse = userClient.updateUser(updateUser, accessToken);

        int StatusCode = UpdateUserResponse.getStatusCode();
        assertThat(statusCode, equalTo(SC_OK));

        boolean IsUpdateUserResponseSuccess = UpdateUserResponse.jsonPath().getBoolean("success");
        assertTrue(isUpdateUserResponseSuccess);

        String Email = UpdateUserResponse.jsonPath().getString("user.email");
        assertEquals(updateUser.getEmail().toLowerCase(), email);

        String Name = UpdateUserResponse.jsonPath().getString("user.name");
        assertEquals(updateUser.getName(), name);
    }

    @Test
    @DisplayName("Изменение данных без авторизации")
    public void changeUserDataWithoutLogin() {
        Response UpdateUserResponse = userClient.updateUser(Generator.getRandomUser(), "");

        int StatusCode = UpdateUserResponse.getStatusCode();
        assertThat(statusCode, equalTo(SC_UNAUTHORIZED));

        String Message = UpdateUserResponse.jsonPath().getString("message");
        assertEquals(authErrorMessage, message);
    }

    @Test
    @DisplayName("Изменение email с авторизацией")
    public void ChangeUserEmailTest() {
        User updatEmailUser = new User(Generator.getRandomUser().getEmail(), user.getPassword(), user.getName());
        Response UpdateUserResponse = userClient.updateUser(updatEmailUser, accessToken);

        int StatusCode = UpdateUserResponse.getStatusCode();
        assertThat(statusCode, equalTo(SC_OK));

        boolean IsUpdateUserResponseSuccess = UpdateUserResponse.jsonPath().getBoolean("success");
        assertTrue(isUpdateUserResponseSuccess);

        String Email = UpdateUserResponse.jsonPath().getString("user.email");
        assertEquals(updatEmailUser.getEmail().toLowerCase(), email);

    }

    @Test
    @DisplayName("Изменение password с авторизацией")
    public void ChangeUserPasswordTest() {
        String NewPassword = Generator.getRandomUser().getPassword();
        User newUser = new User(user.getEmail(), newPassword, user.getName());
        var UpdateUserResponse = userClient.updateUser(newUser, accessToken);

        boolean ResponseSuccess = UpdateUserResponse.jsonPath().getBoolean("success");
        Assert.assertTrue(responseSuccess);

        int StatusCode = UpdateUserResponse.getStatusCode();
        Assert.assertEquals(statusCode, SC_OK);
    }

    @Test
    @DisplayName("Передаем почту, которая уже используется")
    public void ChangeEmailToUsedEmail() {
        User newUser = Generator.getRandomUser();
        userClient.createUser(newUser);

        Response responseLoginNewUser = userClient.login(UserCredentials.from(newUser));
        String NewUserEmail = responseLoginNewUser.body().jsonPath().getString("user.email");

        User updateExistsEmailUser = new User(NewUserEmail, user.getPassword(), user.getName());
        Response responseRegUpdateUser = userClient.updateUser(updateExistsEmailUser, accessToken);

        int StatusCode = responseRegUpdateUser.getStatusCode();
        assertThat(statusCode, equalTo(SC_FORBIDDEN));

        String Message = responseRegUpdateUser.jsonPath().getString("message");
        assertEquals(existsEmailError, message);
    }

    @After
    public void tearDown() {
        userClient.delete(accessToken);
    }
}
