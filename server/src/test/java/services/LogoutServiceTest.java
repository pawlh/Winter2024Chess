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

public class LogoutServiceTest {

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

        Assertions.assertThrows(UnauthorizedException.class,
                () -> new UserService(dataAccess).logout("Invalid token"));

    }

}
