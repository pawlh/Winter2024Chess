package chess.ruleset.piece;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class BishopRuleset extends LineMoveRuleset {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        moves.addAll(lineMove(board, myPosition, 1, 1));
        moves.addAll(lineMove(board, myPosition, 1, -1));
        moves.addAll(lineMove(board, myPosition, -1, 1));
        moves.addAll(lineMove(board, myPosition, -1, -1));


        return moves;
    }

}