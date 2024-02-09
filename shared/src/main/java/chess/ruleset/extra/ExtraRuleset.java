package chess.ruleset.extra;

import chess.*;

import java.util.Collection;

public interface ExtraRuleset {
    void setBoard(ChessBoard board);

    void moveMade(ChessMove move, ChessBoard board);

    boolean moveMatches(ChessMove move, ChessBoard board);

    Collection<ChessMove> validMoves(ChessBoard board, ChessPosition position);

    void performMove(ChessMove move, ChessBoard board) throws InvalidMoveException;
}
