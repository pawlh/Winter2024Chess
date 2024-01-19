package services;

import chess.ChessGame;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.AdminService;
import service.ChessServerException;
import service.GameService;
import service.UnauthorizedException;

public class CreateGameServiceTest {
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
    public void createGamePass() throws ChessServerException, DataAccessException {

        AuthData token = new AuthData("totallyRandomAuth", "testusername");

        dataAccess.insertAuth(token);

        GameData request = new GameData(0, null, null, "Super Exciting Chess Game!", new ChessGame());

        GameData result = new GameService(dataAccess).createGame(request, token.authToken());
        
        Assertions.assertTrue(result.gameID() >= 0);

        
        GameData game = dataAccess.findGame(result.gameID());
        

        Assertions.assertEquals(request.gameName(), game.gameName());
        Assertions.assertNull(game.blackUsername());
        Assertions.assertNull(game.whiteUsername());
        Assertions.assertNotNull(game.game());

    }

    @Test
    public void createGameFail() throws ChessServerException {

        GameData request = new GameData(0, null, null, "Super Exciting Chess Game Failure!", new ChessGame());

        Assertions.assertThrows(UnauthorizedException.class,
                () -> new GameService(dataAccess).createGame(request, null));
    }
}
