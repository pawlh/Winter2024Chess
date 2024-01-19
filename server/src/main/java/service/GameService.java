package service;

import chess.ChessGame;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.JoinGameRequest;
import model.ListGamesResponse;

import java.util.Collection;
import java.util.HashSet;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public GameData createGame(GameData request, String authToken) throws ChessServerException {
        try {
            authorization(authToken);

            if (request.gameName() == null) throw new BadRequestException("Game name cannot be null");

            GameData game = new GameData(0, null, null, request.gameName(), new ChessGame());
            game = dataAccess.insertGame(game);

            return game;
        } catch (DataAccessException e) {
            throw new ChessServerException(e);
        }
    }


    public ListGamesResponse listGames(String authToken) throws ChessServerException {
        try {
            authorization(authToken);

            return new ListGamesResponse(dataAccess.findAllGames());
        } catch (DataAccessException e) {
            throw new ChessServerException(e);
        }
    }


    public synchronized void joinGame(JoinGameRequest request, String authToken) throws ChessServerException {
        try {
            GameData game = dataAccess.findGame(request.gameID());
            if (game == null) throw new BadRequestException("Error: Game not found");

            AuthData auth = authorization(authToken);

            if (request.playerColor() == ChessGame.TeamColor.WHITE && game.whiteUsername() != null ||
                    request.playerColor() == ChessGame.TeamColor.BLACK && game.blackUsername() != null) {
                throw new RequestItemTakenException("Error: Player color taken");
            }

            if (request.playerColor() == ChessGame.TeamColor.WHITE) {
                game = new GameData(game.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game());
            }
            if (request.playerColor() == ChessGame.TeamColor.BLACK) {
                game = new GameData(game.gameID(), game.whiteUsername(), auth.username(), game.gameName(), game.game());
            }

            dataAccess.updateGame(game);
        }catch (DataAccessException e) {
            throw new ChessServerException(e);
        }
    }

    private AuthData authorization(String authtoken) throws ChessServerException {
        try {
            AuthData auth = dataAccess.findAuth(authtoken);
            if (auth == null) throw new UnauthorizedException("Error: Unauthorized");
            return auth;
        } catch (DataAccessException e) {
            throw new ChessServerException(e);
        }
    }
}
