package services;

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

public class ClearServiceTest {
    private static DataAccess dataAccess;

    @BeforeAll
    public static void beforeAll() throws ChessServerException {
        dataAccess = TestFactory.getDatabaseFactory();
    }
    @Test
    public void clearPass() throws ChessServerException, DataAccessException {

        UserData user = new UserData("sheila", "superSecurePa$$w0rd", "noreply@byu.edu");
        GameData game = new GameData(113, user.username(), "otherPlayer", "Really Cool Name", new ChessGame());
        AuthData token = new AuthData("totallyRandomAuth", user.username());


        dataAccess.insertUser(user);
        dataAccess.insertGame(game);
        dataAccess.insertAuth(token);

        Assertions.assertDoesNotThrow(() -> new AdminService(dataAccess).clear());


        Assertions.assertNull(dataAccess.findUser(user.username()));
        Assertions.assertNull(dataAccess.findGame(game.gameID()));
        Assertions.assertNull(dataAccess.findAuth(token.authToken()));
    }
}
