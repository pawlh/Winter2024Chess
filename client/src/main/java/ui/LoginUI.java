package ui;

import data.DataCache;
import model.AuthData;
import model.UserData;

import java.util.Locale;

public class LoginUI implements UI {

    @Override
    public String eval(String cmd, String[] args) {
        return switch (cmd) {
            case "l", "login" -> login(args);
            case "r", "register" -> register(args);
            case "q", "quit" -> "quit";
            case "h", "help" -> help();
            default -> help();
        };
    }


    @Override
    public String help() {
        return """
        Options:
        Login as an existing user: \"l\", \"login\" <USERNAME> <PASSWORD>
        Register a new user: \"r\", \"register\" <USERNAME> <PASSWORD> <EMAIL>
        Exit the program: \"q\", \"quit\"
        Print this message: \"h\", \"help\"
        """;
    }


    @Override
    public String getPromptText() {
        return "Chess Login";
    }


    private String login(String[] args) {
        if(args.length != 2) return "Usage: login <USERNAME> <PASSWORD>";
        UserData request = new UserData(args[0], args[1], null);
        DataCache.getInstance().getFacade().login(request);
        DataCache.getInstance().setState(DataCache.State.LOGGED_IN);
        return "Successfully logged in.";
    }



    private String register(String[] args) {
        if(args.length != 3) return "Usage: register <USERNAME> <PASSWORD> <EMAIL>";
        UserData request = new UserData(args[0], args[1], args[2]);
        DataCache.getInstance().getFacade().register(request);
        DataCache.getInstance().setState(DataCache.State.LOGGED_IN);
        return "Successfully registered.";
    }

}
