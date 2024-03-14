package ui;

import chess.ChessGame;
import data.DataCache;
import model.GameData;
import model.JoinGameRequest;
import model.ListGamesResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainUserInterface implements UserInterface {

    @Override
    public String eval(String cmd, String[] args) {
        return switch (cmd) {
            case "c", "create" -> create(args);
            case "l", "list" -> list();
            case "j", "join" -> join(args);
            case "w", "watch" -> watch(args);
            case "logout" -> logout();
            case "h", "help" -> help();
            default -> help();
        };
    }


    @Override
    public String help() {
        return """
                Options:
                List current games: \"l\", \"list\"
                Create a new game: \"c\", \"create\" <GAME NAME>
                Join a game: \"j\", \"join\" <GAME ID> <COLOR>
                Watch a game: \"w\", \"watch\" <GAME ID>
                Logout: \"logout\"
                """;
    }


    @Override
    public String getPromptText() {
        return "Chess";
    }


    private String create(String[] args) {
        if (args.length != 1) return "Usage: create <GAME NAME>";
        GameData request = new GameData(0, null, null, args[0], null);
        GameData response = DataCache.getInstance().getFacade().createGame(request);

        return "Successfully created game " + args[0] + " with game id " + response.gameID();
    }


    private String join(String[] args) {
        if (args.length != 2) return "Usage: join <GAME ID> <COLOR>";
        ChessGame.TeamColor color;
        try {
            color = ChessGame.TeamColor.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            return "Unable to parse " + args[1] + " as a color";
        }

        int gameID;
        try {
            gameID = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            return "Unable to parse " + args[0] + " as a game id";
        }


        JoinGameRequest request = new JoinGameRequest(color, gameID);
        DataCache.getInstance().getFacade().joinGame(request);
        DataCache.getInstance().setGameId(gameID);

        new BoardPrinter().printGame(new ChessGame(), ChessBoardColorScheme.COLOR_SCHEMES[0], color, List.of(), List.of());

        return "Joined game";
    }


    private String list() {
        ListGamesResponse result = DataCache.getInstance().getFacade().listGames();

        if (result.games().isEmpty()) {
            return "There are no open games";
        }

        List<GameData> games = new ArrayList<>(result.games());
        games.sort(Comparator.comparingInt(GameData::gameID));

        int longestId = 0;
        int longestName = 5;
        int longestWhiteUsername = 5;
        int longestBlackUsername = 5;
        for (GameData game : games) {
            if(String.valueOf(game.gameID()).length() > longestId) longestId = String.valueOf(game.gameID()).length();
            if (game.gameName().length() > longestName) longestName = game.gameName().length();
            if (game.whiteUsername() != null && game.whiteUsername().length() > longestWhiteUsername)
                longestWhiteUsername = game.whiteUsername().length();
            if (game.blackUsername() != null && game.blackUsername().length() > longestBlackUsername)
                longestBlackUsername = game.blackUsername().length();
        }

        longestId += 4;
        longestName += 4;
        longestWhiteUsername += 4;
        longestBlackUsername += 4;

        StringBuilder out = new StringBuilder();
        for (GameData game : games) {
            out.append("Game ID: ");
            out.append(game.gameID());
            out.append(" ".repeat(longestId - String.valueOf(game.gameID()).length()));

            out.append("Game name: ");
            out.append(game.gameName());
            out.append(" ".repeat(longestName - game.gameName().length()));

            if (game.whiteUsername() == null) {
                out.append("White empty" + " ".repeat(longestWhiteUsername - 4));
            } else {
                out.append("White: ");
                out.append(game.whiteUsername());
                out.append(" ".repeat(longestWhiteUsername - game.whiteUsername().length()));
            }

            if (game.blackUsername() == null) {
                out.append("Black empty" + " ".repeat(longestBlackUsername - 4));
            } else {
                out.append("Black: ");
                out.append(game.blackUsername());
                out.append(" ".repeat(longestBlackUsername - game.blackUsername().length()));
            }
            out.append("\n");
        }
        return out.toString();
    }


    private String logout() {
        DataCache.getInstance().getFacade().logout();
        DataCache.getInstance().setState(DataCache.State.LOGGED_OUT);
        return "Sucessfully logged out";
    }


    private String watch(String[] args) {
        if (args.length != 1) return "Usage: watch <GAME ID>";

        int gameID;
        try {
            gameID = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            return "Unable to parse " + args[0] + " as a game id";
        }

        JoinGameRequest request = new JoinGameRequest(null, gameID);
        DataCache.getInstance().getFacade().joinGame(request);
        DataCache.getInstance().setGameId(gameID);

        new BoardPrinter().printGame(new ChessGame(), ChessBoardColorScheme.COLOR_SCHEMES[0], ChessGame.TeamColor.WHITE, List.of(), List.of());
        return "Watching game";
    }

}
