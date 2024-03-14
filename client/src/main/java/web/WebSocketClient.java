package web;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import data.DataCache;
import webSocketMessages.serverMessages.ServerMessage;
import webSocketMessages.userCommands.UserGameCommand;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketClient implements MessageHandler.Whole<String> {

    private final WebSocketClientObserver observer;

    private final Session session;


    public WebSocketClient(WebSocketClientObserver observer, String host, int port)
            throws URISyntaxException, DeploymentException, IOException {
        this.observer = observer;

        URI uri = new URI("ws://" + host + ':' + port + "/connect");

        session = ContainerProvider.getWebSocketContainer().connectToServer(new Endpoint() {
            @Override
            public void onOpen(Session session, EndpointConfig endpointConfig) {}
        }, uri);

        session.addMessageHandler(this);

    }


    @Override
    public void onMessage(String s) {
        ServerMessage message = new Gson().fromJson(s, ServerMessage.class);
        observer.receiveMessage(message);
    }

    public void joinPlayer() throws IOException {
        sendMessage(new UserGameCommand(
                DataCache.getInstance().getAuthToken(),
                DataCache.getInstance().getGameId(),
                DataCache.getInstance().getPlayerColor()));
    }
    public void joinObserver() throws IOException {
        sendMessage(new UserGameCommand(
                UserGameCommand.CommandType.JOIN_OBSERVER,
                DataCache.getInstance().getAuthToken(),
                DataCache.getInstance().getGameId()));
    }
    public void makeMove(ChessMove move) throws IOException {
        sendMessage(new UserGameCommand(
                DataCache.getInstance().getAuthToken(),
                DataCache.getInstance().getGameId(),
                move));
    }
    public void leave() throws IOException {
        sendMessage(new UserGameCommand(
                UserGameCommand.CommandType.LEAVE,
                DataCache.getInstance().getAuthToken(),
                DataCache.getInstance().getGameId()));
    }
    public void resign() throws IOException {
        sendMessage(new UserGameCommand(
                UserGameCommand.CommandType.RESIGN,
                DataCache.getInstance().getAuthToken(),
                DataCache.getInstance().getGameId()));
    }


    private void sendMessage(UserGameCommand command) throws IOException {
        session.getBasicRemote().sendText(new Gson().toJson(command));
    }
}
