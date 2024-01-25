package unitTests.services;

import chess.ChessGame;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.*;
import unitTests.TestFactory;

import java.util.UUID;

public class GameServiceTest {

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


    @Test
    public void listGamesPass() throws ChessServerException, DataAccessException {

        UserData user = new UserData("sheila", "superSecurePa$$w0rd", "noreply@byu.edu");

        dataAccess.insertUser(user);

        GameData game = new GameData(0, user.username(), null, "Really Cool Name", new ChessGame());
        game = dataAccess.insertGame(game);

        AuthData token = new AuthData("totallyRandomAuth", user.username());
        dataAccess.insertAuth(token);

        ListGamesResponse result = new GameService(dataAccess).listGames(token.authToken());

        Assertions.assertEquals(1, result.games().size());

        GameData foundGame = result.games().iterator().next();

        Assertions.assertEquals(game.gameName(), foundGame.gameName());
        Assertions.assertEquals(game.gameID(), foundGame.gameID());

        Assertions.assertEquals(user.username(), foundGame.whiteUsername());
        Assertions.assertNull(foundGame.blackUsername());

    }


    @Test
    public void listGamesMultiple() throws DataAccessException, ChessServerException {

        UserData user = new UserData("sheila", "superSecurePa$$w0rd", "noreply@byu.edu");
        GameData game1 = new GameData(113, null, null, "Cool Name", new ChessGame());
        GameData game2 = new GameData(114, null, null, "Really Cool Name", new ChessGame());
        GameData game3 = new GameData(115, null, null, "Super Cool Name", new ChessGame());
        AuthData token = new AuthData("totallyRandomAuth", user.username());


        dataAccess.insertUser(user);
        dataAccess.insertGame(game1);
        dataAccess.insertGame(game2);
        dataAccess.insertGame(game3);
        dataAccess.insertAuth(token);


        ListGamesResponse result = new GameService(dataAccess).listGames(token.authToken());

        Assertions.assertEquals(3, result.games().size());

    }


    @Test
    public void listGamesZero() throws DataAccessException, ChessServerException {

        AuthData token = new AuthData("totallyRandomAuth", "testusername");

        dataAccess.insertAuth(token);

        ListGamesResponse result = new GameService(dataAccess).listGames(token.authToken());

        Assertions.assertEquals(0, result.games().size());
    }


    @Test
    public void listGamesFail() {
        Assertions.assertThrows(UnauthorizedException.class,
                () -> new GameService(dataAccess).listGames(UUID.randomUUID().toString()));

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
