package services;

import chess.ChessGame;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.JoinGameRequest;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.AdminService;
import service.BadRequestException;
import service.ChessServerException;
import service.GameService;

public class JoinGameServiceTest {
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
    public void joinGamePass() throws ChessServerException, DataAccessException {

        UserData user = new UserData("sheila", "superSecurePa$$w0rd", "noreply@byu.edu");
        
        dataAccess.insertUser(user);

        GameData game = new GameData(0, null, null, "Really Cool Name", new ChessGame());
        game = dataAccess.insertGame(game);

        AuthData token = new AuthData("totallyRandomAuth", user.username());
        dataAccess.insertAuth(token);
        

        JoinGameRequest request = new JoinGameRequest(ChessGame.TeamColor.WHITE, game.gameID());

        Assertions.assertDoesNotThrow(() -> new GameService(dataAccess).joinGame(request, token.authToken()));
        
        GameData foundGameData = dataAccess.findGame(game.gameID());

        Assertions.assertEquals(game.gameName(), foundGameData.gameName());
        Assertions.assertEquals(game.gameID(), foundGameData.gameID());
        Assertions.assertNull(foundGameData.blackUsername());
        Assertions.assertEquals(user.username(), foundGameData.whiteUsername());

    }

    @Test
    public void joinGameFail() {

        JoinGameRequest request = new JoinGameRequest(ChessGame.TeamColor.WHITE, -1);

        Assertions.assertThrows(BadRequestException.class,
                () -> new GameService(dataAccess).joinGame(request, "Invalid token"));

    }
}
