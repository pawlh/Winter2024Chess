package services;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.AdminService;
import service.ChessServerException;
import service.UnauthorizedException;
import service.UserService;

public class LoginServiceTest {
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
        UserData request = new UserData("SuperUniqueusername", "SuperSecurePa$$w0rd",null);
        Assertions.assertThrows(UnauthorizedException.class,
                () ->new UserService(dataAccess).login(request));

    }
}
