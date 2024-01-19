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
import service.RequestItemTakenException;
import service.UserService;

public class RegisterServiceTest {
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

        UserData user = dataAccess.findUser(request.username());
        AuthData token = dataAccess.findAuth(result.authToken());
        

        Assertions.assertEquals(request.password(), user.password());
        Assertions.assertEquals(request.username(), user.username());
        Assertions.assertEquals(request.email(), user.email());
        Assertions.assertEquals(token.username(), user.username());
    }

    @Test
    public void registerFail() throws ChessServerException {

        UserData request = new UserData("SuperUniqueusername", "SuperSecurePa$$w0rd", "noreply@byu.edu");
        new UserService(dataAccess).register(request);
        Assertions.assertThrows(RequestItemTakenException.class,
                () ->  new UserService(dataAccess).register(request));

    }
}
