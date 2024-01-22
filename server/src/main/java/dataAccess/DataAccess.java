package dataAccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public interface DataAccess {
    /**
     * Clears the database of all AuthTokens
     *
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    void clearAll() throws DataAccessException;


    /**
     * Inserts the provided AuthToken into the database
     *
     * @param authToken The AuthToken to insert
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    void insertAuth(AuthData authToken) throws DataAccessException;


    /**
     * Returns the AuthToken matching the provided String
     *
     * @param authToken The AuthToken to look for
     * @return The requested AuthToken, or {@code null} if it couldn't be found
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    AuthData findAuth(String authToken) throws DataAccessException;


    /**
     * Deletes the AuthToken matching the provided String from the database
     *
     * @param authToken The AuthToken to delete
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    void deleteAuth(String authToken) throws DataAccessException;

    /**
     * Deletes target game from the storage system
     *
     * @param gameID ID of game to delete
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    void deleteGame(int gameID) throws DataAccessException;


    /**
     * Returns the Game matching the provided String
     *
     * @param gameID The Game to look for
     * @return The requested Game, or {@code null} if it couldn't be found
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    GameData findGame(int gameID) throws DataAccessException;


    /**
     * Finds and returns all games currently in the database
     *
     * @return A collection containing all current games
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    Collection<GameData> findAllGames() throws DataAccessException;


    /**
     * Inserts the provided Game into the database
     *
     * @param game The Game to insert
     * @return The gameID of the inserted game
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    GameData insertGame(GameData game) throws DataAccessException;


    /**
     * Updates the game with the provided game's gameID to the provided game
     *
     * @param game Game to update
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    void updateGame(GameData game) throws DataAccessException;

    /**
     * Inserts the provided User into the database
     *
     * @param user The User to insert
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    void insertUser(UserData user) throws DataAccessException;


    /**
     * Returns the User matching the provided username
     *
     * @param username The username of the User to look for
     * @return The requested User, or {@code null} if it couldn't be found
     * @throws DataAccessException A DataAccessException is thrown if data cannot be accessed for any reason
     */
    boolean usernameExists(String username) throws DataAccessException;

    boolean verifyUser(UserData user) throws DataAccessException;
}
