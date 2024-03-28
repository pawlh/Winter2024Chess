package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import data.DataCache;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;

public class GameUserInterface implements UserInterface {
    @Override
    public CommandOutput eval(String cmd, String[] args) {
        return switch (cmd) {
            case "hl", "highlight" -> highlight(args);
            case "m", "move", "make" -> makeMove(args);
            case "r", "redraw" -> redraw();
            case "c", "colors" -> colors(args);
            case "res", "resign" -> resign();
            case "leave" -> leave();
            case "h", "help" -> new CommandOutput(help(), true);
            default -> new CommandOutput(help(), false);
        };
    }


    @Override
    public String help() {
        return """
                Options:
                Highlight legal moves: "hl", "highlight" <position> (e.g. f5)
                Make a move: "m", "move", "make" <source> <destination> <optional promotion>(e.g. f5 e4 q)
                Redraw Chess Board: "res", "resign"
                Change color scheme: "c", "colors" <color number>
                Resign from game: "resign"
                Leave game: "leave"
                """;
    }

    @Override
    public String getPromptText() {
        return "Chess Game";
    }

    private CommandOutput highlight(String[] args) {
        if (args.length != 1) return new CommandOutput("Usage: highlight <position> (e.g. f5)", false);

        ChessPosition position = parsePosition(args[0]);
        if (position == null) return new CommandOutput("Could not parse %s as a position".formatted(args[0]), false);

        Collection<ChessMove> moves = DataCache.getInstance().getLastGame().validMoves(position);
        Collection<ChessPosition> highlight = new HashSet<>();
        for (ChessMove move : moves) {
            highlight.add(move.getEndPosition());
        }
        printGame(DataCache.getInstance().getLastGame(), highlight);
        return new CommandOutput("", true);
    }

    private CommandOutput makeMove(String[] args) {
        if (args.length < 2 || args.length > 2)
            return new CommandOutput("Usage: move <source> <destination> <optional promotion>(e.g. f5 e4 q)", false);

        ChessPosition start = parsePosition(args[0]);
        if (start == null) return new CommandOutput("Could not parse %s as a position".formatted(args[0]), false);
        ChessPosition end = parsePosition(args[1]);
        if (end == null) return new CommandOutput("Could not parse %s as a position".formatted(args[1]), false);

        ChessPiece.PieceType promotion = null;
        if (args.length == 3) {
            if (args[2].length() != 1)
                return new CommandOutput("Could not parse %s as a piece type".formatted(args[3]), false);
            switch (args[2].charAt(0)) {
                case 'q' -> promotion = ChessPiece.PieceType.QUEEN;
                case 'r' -> promotion = ChessPiece.PieceType.ROOK;
                case 'b' -> promotion = ChessPiece.PieceType.BISHOP;
                case 'n' -> promotion = ChessPiece.PieceType.KNIGHT;
                case 'k' -> {
                    return new CommandOutput("Cannot promote to king", false);
                }
                case 'p' -> {
                    return new CommandOutput("Cannot promote to pawn", false);
                }
                default -> {
                    return new CommandOutput("Could not parse %s as a piece type".formatted(args[3]), false);
                }
            }
        }

        ChessMove move = new ChessMove(start, end, promotion);
        try {
            DataCache.getInstance().getWebSocketClient().makeMove(move);
        } catch (IOException e) {
            return new CommandOutput("Could not make move: " + e.getMessage(), false);
        }
        return new CommandOutput("", true);
    }

    private ChessPosition parsePosition(String parse) {
        if (parse.length() != 2) return null;
        try {
            int row = Integer.parseInt(parse.substring(1, 2));
            int col = parse.charAt(0) - 96;
            return new ChessPosition(row, col);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private CommandOutput redraw() {
        printGame(DataCache.getInstance().getLastGame());
        return new CommandOutput("", true);
    }

    private CommandOutput colors(String[] args) {
        if (args.length != 1) return new CommandOutput("colors <color number>", false);
        try {
            int newColor = Integer.parseInt(args[0]);
            if(newColor < 1) {
                return new CommandOutput("color number cannot be less than 1", false);
            }
            int max = ChessBoardColorScheme.COLOR_SCHEMES.length;
            if(newColor > max) {
                return new CommandOutput("color number cannot be greater than %d".formatted(max), false);
            }
            DataCache.getInstance().setColorScheme(ChessBoardColorScheme.COLOR_SCHEMES[newColor - 1]);
            return new CommandOutput("Color scheme set to scheme %d".formatted(newColor), true);
        } catch (NumberFormatException e) {
            return new CommandOutput("could not parse %s as a number".formatted(args[0]), false);
        }
    }

    private CommandOutput resign() {
        System.out.print("Are you sure you want to resign? (\"y\" or \"yes\" to confirm)");
        Scanner scanner = new Scanner(System.in);
        String in = scanner.next();
        if (in.equals("y") || in.equals("yes")) {
            try {
                DataCache.getInstance().getWebSocketClient().resign();
                return new CommandOutput("", true);
            } catch (IOException e) {
                return new CommandOutput("Could not resign: " + e.getMessage(), false);
            }
        }
        else return new CommandOutput("You did not resign", true);
    }

    private CommandOutput leave() {
        try {
            DataCache.getInstance().getWebSocketClient().leave();
            DataCache.getInstance().setState(DataCache.State.LOGGED_IN);
            DataCache.getInstance().setLastGame(null);
            return new CommandOutput("", true);
        } catch (IOException e) {
            return new CommandOutput("Could not leave: " + e.getMessage(), false);
        }
    }

    public void printGame(ChessGame game) {
        if(DataCache.getInstance().getLastGame() == null) DataCache.getInstance().setLastGame(game);
        printGame(game, new HashSet<>());
    }

    private void printGame(ChessGame game, Collection<ChessPosition> highlight) {
        BoardPrinter.printGame(game, DataCache.getInstance().getColorScheme(),
                (DataCache.getInstance().getPlayerColor() == ChessGame.TeamColor.BLACK) ? ChessGame.TeamColor.BLACK :
                        ChessGame.TeamColor.WHITE, highlight,
                determineDifferences(game, DataCache.getInstance().getLastGame()));
        DataCache.getInstance().setLastGame(game);
    }

    private Collection<ChessPosition> determineDifferences(ChessGame newGame, ChessGame baseGame) {
        Collection<ChessPosition> differences = new HashSet<>();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = baseGame.getBoard().getPiece(pos);
                ChessPiece prevPiece = newGame.getBoard().getPiece(pos);

                if (!Objects.equals(piece, prevPiece)) {
                    differences.add(pos);
                }
            }
        }
        return differences;
    }
}
