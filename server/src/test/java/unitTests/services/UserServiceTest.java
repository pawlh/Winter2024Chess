package unitTests.services;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.*;
import unitTests.TestFactory;

public class UserServiceTest {

    private static DataAccess dataAccess;


    @BeforeAll
    public static void beforeAll() throws ChessServerException {
        dataAccess = TestFactory.getDatabaseFactory();
    }


    @BeforeEach
    public void setUp() throws ChessServerException {
        new AdminService(dataAccess).clear();
    }


    @Test
    public void registerPass() throws ChessServerException, DataAccessException {

        UserData request = new UserData("SuperUniqueusername", "SuperSecurePa$$w0rd", "noreply@byu.edu");

        AuthData result = new UserService(dataAccess).register(request);

        Assertions.assertNotNull(result.authToken());
        Assertions.assertEquals(request.username(), result.username());

        AuthData token = dataAccess.findAuth(result.authToken());

        Assertions.assertTrue(dataAccess.verifyUser(request));
        Assertions.assertEquals(token.username(), request.username());
    }


    @Test
    public void registerFail() throws ChessServerException {

        UserData request = new UserData("SuperUniqueusername", "SuperSecurePa$$w0rd", "noreply@byu.edu");
        new UserService(dataAccess).register(request);
        Assertions.assertThrows(RequestItemTakenException.class, () -> new UserService(dataAccess).register(request));

    }


    @Test
    public void loginPass() throws ChessServerException, DataAccessException {
        UserData request = new UserData("SuperUniqueusername", "SuperSecurePa$$w0rd", "noreply@byu.edu");
        UserService userService = new UserService(dataAccess);
        userService.register(request);

        AuthData result = userService.login(request);

        Assertions.assertNotNull(result.authToken());
        Assertions.assertEquals(request.username(), result.username());

        AuthData token = dataAccess.findAuth(result.authToken());

        Assertions.assertEquals(request.username(), token.username());

    }


    @Test
    public void loginFail() {
        UserData request = new UserData("SuperUniqueusername", "SuperSecurePa$$w0rd", null);
        Assertions.assertThrows(UnauthorizedException.class, () -> new UserService(dataAccess).login(request));

    }


    @Test
    public void logoutPass() throws ChessServerException, DataAccessException {
        UserData request = new UserData("SuperUniqueusername", "SuperSecurePa$$w0rd", "noreply@byu.edu");
        UserService userService = new UserService(dataAccess);
        AuthData registerResult = userService.register(request);

        userService.logout(registerResult.authToken());

        AuthData token = dataAccess.findAuth(registerResult.authToken());
        Assertions.assertNull(token);

    }


    @Test
    public void logoutFail() {

        Assertions.assertThrows(UnauthorizedException.class, () -> new UserService(dataAccess).logout("Invalid token"));

    }

}
