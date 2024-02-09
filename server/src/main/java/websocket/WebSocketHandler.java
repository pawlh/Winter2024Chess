package websocket;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.UserGameCommand;

import java.io.IOException;
import java.util.Objects;

@WebSocket
public class WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);

    private final ConnectionManager connectionManager = new ConnectionManager();

    private final Gson gson = new Gson();

    private static final WebSocketHandler instance = new WebSocketHandler();

    private DataAccess dataAccess;

    private WebSocketHandler() {}


    public void setDataAccess(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public static WebSocketHandler getInstance() {
        return instance;
    }


    @OnWebSocketError
    public void error(Throwable error) {
        if (!((error instanceof EofException) ||
                error.getCause() != null && error.getCause().getMessage().contains("Connection reset by peer"))) {
            log.warn("WebSocket error: ", error);
        }
    }


    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        log.debug("Received from " + session.getRemoteAddress() + ": " + message);
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

        AuthData token;
        GameData game;
        try {
            token = dataAccess.findAuth(command.getAuthString());

            if (token == null) {
                connectionManager.sendError(session, "Error: Invalid authtoken");
                log.info("Error to " + session.getRemoteAddress() + ": Input was: " + message);
                return;
            }

            game = dataAccess.findGame(command.getGameID());

            if (game == null) {
                connectionManager.sendError(session, "Error: Invalid gameID");
                log.info("Error to " + session.getRemoteAddress() + ": Input was: " + message);
                return;
            }

        } catch (DataAccessException e) {
            log.warn("DataAccessException: ", e);
            connectionManager.sendError(session, "Error: Unknown server error occurred: " + e.getMessage());
            log.info("Error to " + session.getRemoteAddress() + ": Input was: " + message);
            return;
        }


        switch (command.getCommandType()) {
            case JOIN_PLAYER -> joinPlayer(session, command, token.username(), game);
            case JOIN_OBSERVER -> joinObserver(session, token.username(), game);
            case MAKE_MOVE -> makeMove(session, command, token.username(), game);
            case LEAVE -> leave(session, token.username(), game);
            case RESIGN -> resign(session, token.username(), game);
        }
    }


    private void joinObserver(Session session, String username, GameData game) throws IOException {
        connectionManager.addSession(game.gameID(), session);

        ServerMessage loadGame = new ServerMessage(game.game());
        String loadGameJson = gson.toJson(loadGame);
        connectionManager.sendMessage(session, loadGameJson);

        ServerMessage notify = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                "UserData " + username + " is now watching the game");
        String notifyJson = gson.toJson(notify);

        connectionManager.broadcast(notifyJson, game.gameID(), session);
    }


    private void joinPlayer(Session session, UserGameCommand command, String username, GameData game)
            throws IOException {
        boolean correctPlayer = false;
        if (command.getPlayerColor() == ChessGame.TeamColor.WHITE) {
            correctPlayer = Objects.equals(game.whiteUsername(), username);
        } else if (command.getPlayerColor() == ChessGame.TeamColor.BLACK) {
            correctPlayer = Objects.equals(game.blackUsername(), username);
        }

        if (!correctPlayer) {
            connectionManager.sendError(session, "Error: Incorrect player attempted to join");
            return;
        }

        connectionManager.addSession(game.gameID(), session);

        ServerMessage loadGame = new ServerMessage(game.game());
        String loadGameJson = gson.toJson(loadGame);
        connectionManager.sendMessage(session, loadGameJson);

        ServerMessage notify = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                "UserData " + username + " joined playing color " +
                        ((command.getPlayerColor() == ChessGame.TeamColor.WHITE) ? "white" : "black") + ".");
        String notifyJson = gson.toJson(notify);

        connectionManager.broadcast(notifyJson, game.gameID(), session);
    }


    private void leave(Session session, String username, GameData game) throws IOException {
        connectionManager.removeSession(game.gameID(), session);

        ServerMessage notify = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, "UserData " + username +
                ((Objects.equals(username, game.blackUsername()) || Objects.equals(username, game.whiteUsername())) ?
                        " has left the game" : " is no longer watching"));
        String notifyJson = gson.toJson(notify);

        connectionManager.broadcast(notifyJson, game.gameID(), session);
    }


    private void makeMove(Session session, UserGameCommand command, String username, GameData game) throws IOException {
        if (!(Objects.equals(game.blackUsername(), username) || Objects.equals(game.whiteUsername(), username))) {
            connectionManager.sendError(session, "Error: You are not a participant in this game");
            return;
        }

        if (!game.game().isActive()) {
            connectionManager.sendError(session, "Error: GameData is over");
            return;
        }

        ChessGame chessGame = game.game();
        if ((chessGame.getTeamTurn() == ChessGame.TeamColor.WHITE && !Objects.equals(game.whiteUsername(), username)) ||
                (chessGame.getTeamTurn() == ChessGame.TeamColor.BLACK &&
                        !Objects.equals(game.blackUsername(), username))) {
            connectionManager.sendError(session, "Error: It's not your turn");
            return;
        }

        try {
            chessGame.makeMove(command.getMove());
        } catch (InvalidMoveException e) {
            connectionManager.sendError(session, "Error: That's not a valid move");
            return;
        }


        String ending = "";
        if (chessGame.isInCheckmate(chessGame.getTeamTurn())) {
            ending = "Checkmate. " + username + " wins!";
            chessGame.setActive(false);
        } else if (chessGame.isInStalemate(chessGame.getTeamTurn())) {
            ending = "The game ends in a stalemate.";
            chessGame.setActive(false);
        } else if (chessGame.isInCheck(chessGame.getTeamTurn())) {
            ending = "Check.";
        }

        try {
            dataAccess.updateGame(game);
        } catch (DataAccessException e) {
            connectionManager.sendError(session, "Error: Unknown server error");
            return;
        }

        ServerMessage loadGame = new ServerMessage(game.game());
        ServerMessage notify = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                username + " makes move " + command.getMove() + ' ' + ending);

        String loadGameJson = gson.toJson(loadGame);
        String notifyJson = gson.toJson(notify);

        connectionManager.broadcast(loadGameJson, game.gameID(), null);
        connectionManager.broadcast(notifyJson, game.gameID(), session);
    }


    private void resign(Session session, String username, GameData game) throws IOException {
        if (!game.game().isActive()) {
            connectionManager.sendError(session, "Error: GameData is already over");
            return;
        }

        if (!(Objects.equals(game.blackUsername(), username) || Objects.equals(game.whiteUsername(), username))) {
            connectionManager.sendError(session, "Error: You are not a participant in this game");
            return;
        }

        game.game().setActive(false);

        String opponent =
                (Objects.equals(username, game.whiteUsername())) ? game.blackUsername() : game.whiteUsername();

        try {
            dataAccess.updateGame(game);
        } catch (DataAccessException e) {
            connectionManager.sendError(session, "Error: Unknown server error: " + e.getMessage());
            return;
        }


        ServerMessage notify = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                username + " has resigned." + ((opponent != null) ? " " + opponent + " wins!" : ""));

        String notifyJson = gson.toJson(notify);

        connectionManager.broadcast(notifyJson, game.gameID(), session);


        notify = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, "You have resigned.");
        notifyJson = gson.toJson(notify);

        connectionManager.sendMessage(session, notifyJson);
    }

    public void clear() {
        connectionManager.clear();
    }
}
