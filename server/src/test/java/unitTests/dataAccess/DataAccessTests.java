package unitTests.dataAccess;

import chess.ChessGame;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import unitTests.TestFactory;

import java.util.Collection;

public class DataAccessTests {
    private static DataAccess dataAccess;

    private final AuthData authOne = new AuthData("reallyCleverTokenName", "testUsername");
    private final AuthData authTwo = new AuthData("differentCleverName", "testUsername2");

    private final GameData gameOne = new GameData(-1, null, null, "superCoolGameName", new ChessGame());
    private final GameData gameTwo = new GameData(-1, null, null, "secondCoolName", new ChessGame());

    private final UserData userOne = new UserData("mdaven19", "notGonnaTe11Yo0", "mdaven19@byu.edu");
    private final UserData userTwo = new UserData("19mdavenport", "n0tG0nnaTe11Yo0", "19mdavenport@gmail.com");
    @BeforeAll
    public static void beforeAll() throws DataAccessException {
        dataAccess = TestFactory.getDatabaseFactory();
        dataAccess.clearAll();
    }

    @AfterEach
    public void tearDown() throws DataAccessException {
        dataAccess.clearAll();
    }

    @Test
    public void clearAllAuth() throws DataAccessException {
        dataAccess.insertAuth(authOne);
        dataAccess.insertAuth(authTwo);

        dataAccess.clearAll();

        Assertions.assertNull(dataAccess.findAuth(authOne.authToken()));
        Assertions.assertNull(dataAccess.findAuth(authTwo.authToken()));
    }

    @Test
    public void clearAllGame() throws DataAccessException {
        GameData insGameOne = dataAccess.insertGame(gameOne);
        GameData insGameTwo = dataAccess.insertGame(gameTwo);

        dataAccess.clearAll();

        Assertions.assertNull(dataAccess.findGame(insGameOne.gameID()));
        Assertions.assertNull(dataAccess.findGame(insGameTwo.gameID()));
    }

    @Test
    public void clearAllUser() throws DataAccessException {
        dataAccess.insertUser(userOne);
        dataAccess.insertUser(userTwo);
        dataAccess.clearAll();
        Assertions.assertFalse(dataAccess.usernameExists(userOne.username()));
        Assertions.assertFalse(dataAccess.usernameExists(userTwo.username()));
    }


    @Test
    public void insertAuthPass() throws DataAccessException {
        dataAccess.insertAuth(authOne);
        AuthData found = dataAccess.findAuth(authOne.authToken());
        Assertions.assertNotNull(found);
        Assertions.assertEquals(authOne, found);
    }
    @Test
    public void insertAuthFail() throws DataAccessException {
        dataAccess.insertAuth(authOne);
        Assertions.assertThrows(DataAccessException.class, () -> dataAccess.insertAuth(authOne));
    }


    @Test
    public void findAuthPass() throws DataAccessException {
        dataAccess.insertAuth(authOne);
        AuthData found = dataAccess.findAuth(authOne.authToken());
        Assertions.assertNotNull(found);
        Assertions.assertEquals(authOne, found);
    }
    @Test
    public void findAuthFail() throws DataAccessException {
        Assertions.assertNull(dataAccess.findAuth(authOne.authToken()));
    }


    @Test
    public void deleteAuthPass() throws DataAccessException {
        dataAccess.insertAuth(authOne);
        dataAccess.deleteAuth(authOne.authToken());
        Assertions.assertNull(dataAccess.findAuth(authOne.authToken()));
    }

    @Test
    public void deleteAuthFail() throws DataAccessException {
        dataAccess.insertAuth(authOne);
        dataAccess.deleteAuth(authTwo.authToken());
        Assertions.assertNotNull(dataAccess.findAuth(authOne.authToken()));
    }

    @Test
    public void findAllGameFail() throws DataAccessException {
        Collection<GameData> games = dataAccess.findAllGames();
        Assertions.assertEquals(0, games.size());
    }


    @Test
    public void findAllGamePass() throws DataAccessException {
        GameData insGameOne = dataAccess.insertGame(gameOne);
        GameData insGameTwo = dataAccess.insertGame(gameTwo);
        
        Assertions.assertEquals(gameOne.game(), insGameOne.game());
        Assertions.assertEquals(gameOne.gameName(), insGameOne.gameName());
        Assertions.assertEquals(gameOne.whiteUsername(), insGameOne.whiteUsername());
        Assertions.assertEquals(gameOne.blackUsername(), insGameOne.blackUsername());

        Assertions.assertEquals(gameTwo.game(), insGameTwo.game());
        Assertions.assertEquals(gameTwo.gameName(), insGameTwo.gameName());
        Assertions.assertEquals(gameTwo.whiteUsername(), insGameTwo.whiteUsername());
        Assertions.assertEquals(gameTwo.blackUsername(), insGameTwo.blackUsername());

        Collection<GameData> games = dataAccess.findAllGames();

        Assertions.assertEquals(2, games.size());
        Assertions.assertTrue(games.contains(insGameOne));
        Assertions.assertTrue(games.contains(insGameTwo));
    }


    @Test
    public void findGameFail() throws DataAccessException {
        Assertions.assertNull(dataAccess.findGame( 17));
    }


    @Test
    public void findGamePass() throws DataAccessException {
        GameData insGameOne = dataAccess.insertGame(gameOne);
        
        GameData found = dataAccess.findGame(insGameOne.gameID());
        Assertions.assertNotNull(found);

        Assertions.assertEquals(gameOne.game(), found.game());
        Assertions.assertEquals(gameOne.gameName(), found.gameName());
        Assertions.assertEquals(gameOne.whiteUsername(), found.whiteUsername());
        Assertions.assertEquals(gameOne.blackUsername(), found.blackUsername());
    }


    @Test
    public void insertGameFail() throws DataAccessException {
        Assertions.assertThrows(DataAccessException.class,
                () -> dataAccess.insertGame(new GameData(-1, null, null, null, null)));
    }


    @Test
    public void insertGamePass() throws DataAccessException {
        GameData insGameOne = dataAccess.insertGame(gameOne);

        GameData found = dataAccess.findGame(insGameOne.gameID());
        Assertions.assertNotNull(found);

        Assertions.assertEquals(gameOne.game(), found.game());
        Assertions.assertEquals(gameOne.gameName(), found.gameName());
        Assertions.assertEquals(gameOne.whiteUsername(), found.whiteUsername());
        Assertions.assertEquals(gameOne.blackUsername(), found.blackUsername());
    }


    @Test
    public void updateGameFail() throws DataAccessException {
        GameData insGameOne = dataAccess.insertGame(gameOne);

        Assertions.assertThrows(DataAccessException.class,
                () -> dataAccess.updateGame(new GameData(insGameOne.gameID(), null, null, null, null)));
    }


    @Test
    public void updateGamePass() throws DataAccessException {
        GameData insGameOne = dataAccess.insertGame(gameOne);

        GameData toUpdate = new GameData(insGameOne.gameID(), "asdf", insGameOne.blackUsername(), insGameOne.gameName(), insGameOne.game());
        dataAccess.updateGame(toUpdate);

        GameData found = dataAccess.findGame(toUpdate.gameID());
        Assertions.assertNotNull(found);
        Assertions.assertEquals(toUpdate, found);
    }


    @Test
    public void insertPass() throws DataAccessException {
        dataAccess.insertUser(userOne);
        Assertions.assertTrue(dataAccess.verifyUser(userOne));
    }
    @Test
    public void insertFail() throws DataAccessException {
        dataAccess.insertUser(userOne);
        Assertions.assertThrows(DataAccessException.class, () -> dataAccess.insertUser(userOne));
    }


    @Test
    public void usernameExistsPass() throws DataAccessException {
        dataAccess.insertUser(userOne);
        Assertions.assertTrue(dataAccess.usernameExists(userOne.username()));
    }
    @Test
    public void usernameExistsFail() throws DataAccessException {
        Assertions.assertFalse(dataAccess.usernameExists("testUsername2"));
    }


    @Test
    public void verifyUserPass() throws DataAccessException {
        dataAccess.insertUser(userOne);
        Assertions.assertTrue(dataAccess.verifyUser(userOne));
    }

    @Test
    public void verifyUserFail() throws DataAccessException {
        Assertions.assertFalse(dataAccess.verifyUser(userOne));
    }
}
