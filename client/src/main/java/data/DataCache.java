package data;

import ui.LoginUserInterface;
import ui.MainUserInterface;
import ui.UserInterface;
import web.ServerFacade;

public class DataCache {
    private static DataCache instance = new DataCache();
    public static DataCache getInstance() {
        return instance;
    }
    private DataCache(){}

    public enum State {
        LOGGED_OUT, LOGGED_IN
    }

    private String serverUrl;

    private String authToken;

    private String username;

    private int gameId;

    private ServerFacade facade;

    private UserInterface userInterface = new LoginUserInterface();

    private State state;


    static {
        DataCache.getInstance().setServerUrl("http://localhost:8080");
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        facade = new ServerFacade(serverUrl);
    }


    public String getServerUrl() {
        return serverUrl;
    }


    public String getAuthToken() {
        return authToken;
    }


    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }


    public String getUsername() {
        return username;
    }


    public ServerFacade getFacade() {
        return facade;
    }




    public UserInterface getUi() {
        return userInterface;
    }



    public State getState() {
        return state;
    }


    public void setState(State state) {
        this.state = state;
        userInterface = switch (state) {
            case LOGGED_OUT -> new LoginUserInterface();
            case LOGGED_IN -> new MainUserInterface();
        };
    }


    public int getGameId() {
        return gameId;
    }


    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

}
