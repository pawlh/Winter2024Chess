package chess.ruleset;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public interface Ruleset {
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
}
