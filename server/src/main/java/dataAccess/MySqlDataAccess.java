package dataAccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.*;
import java.util.Collection;
import java.util.HashSet;

public class MySqlDataAccess implements DataAccess {

    public MySqlDataAccess() throws DataAccessException {
        configureDatabase();
    }


    @Override
    public void clearAll() throws DataAccessException {
        executeUpdate("TRUNCATE TABLE auth;");
        executeUpdate("TRUNCATE TABLE game;");
        executeUpdate("TRUNCATE TABLE user;");
    }


    @Override
    public void insertAuth(AuthData authToken) throws DataAccessException {
        String statement = "INSERT INTO auth (authToken, username) VALUES (?, ?);";
        executeUpdate(statement, authToken.authToken(), authToken.username());
    }


    @Override
    public AuthData findAuth(String authToken) throws DataAccessException {
        return executeQuery("SELECT * FROM auth WHERE authToken = ?;", rs -> {
            if (!rs.next()) return null;
            return new AuthData(rs.getString("authToken"), rs.getString("username"));
        }, authToken);
    }


    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        executeUpdate("DELETE FROM auth WHERE authToken = ?;", authToken);
    }


    @Override
    public void deleteGame(int gameID) throws DataAccessException {
        executeUpdate("DELETE FROM game WHERE gameID = ?;", gameID);
    }


    @Override
    public GameData findGame(int gameID) throws DataAccessException {
        return executeQuery("SELECT * FROM game WHERE gameID = ?;", rs -> {
            if (!rs.next()) return null;
            return readGame(rs);
        }, gameID);
    }


    @Override
    public Collection<GameData> findAllGames() throws DataAccessException {
        return executeQuery("SELECT * FROM game;", rs -> {
            Collection<GameData> ret = new HashSet<>();
            while (rs.next()) ret.add(readGame(rs));
            return ret;
        });
    }


    private GameData readGame(ResultSet rs) throws SQLException {
        return new GameData(rs.getInt("gameID"), rs.getString("whiteUsername"), rs.getString("blackUsername"),
                rs.getString("gameName"), new Gson().fromJson(rs.getString("game"), ChessGame.class));
    }


    @Override
    public GameData insertGame(GameData game) throws DataAccessException {
        int gameId = executeUpdate(
                "INSERT INTO game (gameName, whiteUsername, blackUsername, game) VALUES (?, ?, ?, ?);",
                game.gameName(), game.whiteUsername(), game.blackUsername(), game.game());
        return new GameData(gameId, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
    }


    @Override
    public void updateGame(GameData game) throws DataAccessException {
        executeUpdate("UPDATE game SET whiteUsername = ?, blackUsername = ?, game = ? WHERE gameID = ?",
                game.whiteUsername(), game.blackUsername(), game.game(), game.gameID());
    }


    @Override
    public void insertUser(UserData user) throws DataAccessException {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(user.password());
        String statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?);";
        executeUpdate(statement, user.username(), hashedPassword, user.email());
    }


    @Override
    public boolean usernameExists(String username) throws DataAccessException {
        return executeQuery("SELECT * FROM user WHERE username=?", ResultSet::next, username);
    }


    @Override
    public boolean verifyUser(UserData user) throws DataAccessException {
        return executeQuery("SELECT * FROM user WHERE username=?", (rs) -> {
            if(!rs.next()) return false;
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            return encoder.matches(user.password(), rs.getString("password"));
        }, user.username());
    }


    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                addParams(ps, params);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(
                    String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }


    private <T> T executeQuery(String statement, ResultSetParser<T> parser, Object... params)
            throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {
                addParams(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    return parser.parseResultSet(rs);
                }
        } catch (SQLException e) {
            throw new DataAccessException(
                    String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }


    private void addParams(PreparedStatement ps, Object[] params) throws SQLException, DataAccessException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            switch (param) {
                case String p -> ps.setString(i + 1, p);
                case Integer p -> ps.setInt(i + 1, p);
                case ChessGame p -> ps.setString(i + 1, new Gson().toJson(p));
                case null -> ps.setNull(i + 1, Types.NULL);
                default -> throw new DataAccessException("Unexpected data type: " + param.getClass());
            }
        }
    }


    private final String[] createStatements = {"""
            CREATE TABLE IF NOT EXISTS `auth` (
                `authToken` VARCHAR(64) NOT NULL PRIMARY KEY,
                `username` VARCHAR(64) NOT NULL
            )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
            """, """
            CREATE TABLE IF NOT EXISTS `game` (
                `gameID` INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                `gameName` VARCHAR(64) NOT NULL UNIQUE,
                `whiteUsername` VARCHAR(64),
                `blackUsername` VARCHAR(64),
                `game` LONGTEXT NOT NULL
            )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
            """, """
            CREATE TABLE IF NOT EXISTS `user` (
                `username` VARCHAR(64) NOT NULL PRIMARY KEY,
                `password` VARCHAR(64) NOT NULL,
                `email` VARCHAR(64) NOT NULL
            )ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
            
            """};


    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (PreparedStatement preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }


    @FunctionalInterface
    private static interface ResultSetParser<T> {

        T parseResultSet(ResultSet rs) throws SQLException;

    }

}
