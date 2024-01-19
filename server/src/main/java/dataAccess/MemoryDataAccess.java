package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {

    private static final Map<String, AuthData> authTokens = new HashMap<>();

    private static final Map<Integer, GameData> games = new HashMap<>();

    private static final Map<String, UserData> users = new HashMap<>();


    @Override
    public void clearAll() throws DataAccessException {
        authTokens.clear();
        games.clear();
        users.clear();
    }


    @Override
    public void insertAuth(AuthData authToken) throws DataAccessException {
        if (authTokens.containsKey(authToken.authToken())) throw new DataAccessException("Authtoken already exists");
        authTokens.put(authToken.authToken(), authToken);
    }


    @Override
    public AuthData findAuth(String authToken) throws DataAccessException {
        return authTokens.get(authToken);
    }


    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (!authTokens.containsKey(authToken)) throw new DataAccessException("Authtoken does not exist");
        authTokens.remove(authToken);
    }


    @Override
    public void deleteGame(int gameID) throws DataAccessException {
        if (!games.containsKey(gameID)) throw new DataAccessException("Game does not exist");
        games.remove(gameID);
    }


    @Override
    public GameData findGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }


    @Override
    public Collection<GameData> findAllGames() throws DataAccessException {
        return Collections.unmodifiableCollection(games.values());
    }


    @Override
    public GameData insertGame(GameData game) throws DataAccessException {
        int gameID = 1;
        while (games.get(gameID) != null) gameID++;
        game = new GameData(gameID, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        games.put(gameID, game);
        return game;
    }


    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!games.containsKey(game.gameID())) throw new DataAccessException("Game does not exist");
        games.remove(game.gameID());
        games.put(game.gameID(), game);
    }


    @Override
    public void insertUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) throw new DataAccessException("User already exists");
        users.put(user.username(), user);
    }


    @Override
    public UserData findUser(String username) throws DataAccessException {
        return users.get(username);
    }

}
