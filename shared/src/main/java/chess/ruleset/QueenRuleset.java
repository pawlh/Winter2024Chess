package chess.ruleset;


import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;


public class QueenRuleset implements Ruleset {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();
        moves.addAll(new BishopRuleset().pieceMoves(board, myPosition));
        moves.addAll(new RookRuleset().pieceMoves(board, myPosition));
        return moves;
    }

}
