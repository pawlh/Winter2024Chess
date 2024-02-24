package serviceTests;

import chess.ChessGame;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import service.AdminService;
import service.ChessServerException;
import dataAccessTests.TestFactory;

public class AdminServiceTest {

    private static DataAccess dataAccess;


    @BeforeAll
    public static void beforeAll() throws ChessServerException {
        dataAccess = TestFactory.getDataAccess();
        new AdminService(dataAccess).clear();
    }


    @Test
    public void clearPass() throws ChessServerException, DataAccessException {

        UserData user = new UserData("sheila", "superSecurePa$$w0rd", "noreply@byu.edu");
        GameData game = new GameData(113, user.username(), "otherPlayer", "Really Cool Name", new ChessGame());
        AuthData token = new AuthData("totallyRandomAuth", user.username());


        dataAccess.getUserDAO().insertUser(user);
        dataAccess.getGameDAO().insertGame(game);
        dataAccess.getAuthDAO().insertAuth(token);

        Assertions.assertDoesNotThrow(() -> new AdminService(dataAccess).clear());


        Assertions.assertFalse(dataAccess.getUserDAO().usernameExists(user.username()));
        Assertions.assertNull(dataAccess.getGameDAO().findGame(game.gameID()));
        Assertions.assertNull(dataAccess.getAuthDAO().findAuth(token.authToken()));
    }

}
