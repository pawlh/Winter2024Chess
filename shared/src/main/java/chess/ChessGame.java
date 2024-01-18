package chess;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private final boolean[] castlingOptions;

    private ChessBoard board;

    private TeamColor teamTurn;

    private ChessPosition enPassantPosition;

    private int halfMoveClock;

    private int fullMoves;


    public ChessGame() {
        board = new ChessBoard();
        teamTurn = TeamColor.WHITE;

        castlingOptions = new boolean[4];
        Arrays.fill(castlingOptions, true);

        enPassantPosition = null;
        halfMoveClock = 0;
        fullMoves = 1;

        board.resetBoard();
    }


    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }


    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }


    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE, BLACK
    }


    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) return null;

        //if (piece.getTeamColor() != teamTurn) return new HashSet<>();

        //Get all possible moves for the piece
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);

        moves.addAll(castling(startPosition));
        moves.addAll(enPassant(startPosition));

        //If making the move resulted in the king being placed in check, it's not legal
        Iterator<ChessMove> iterator = moves.iterator();
        while (iterator.hasNext()) {
            try {
                ChessBoard backupBoard = board;
                board = hypotheticalMove(iterator.next());
                if (isInCheck(piece.getTeamColor())) {
                    iterator.remove();
                }
                board = backupBoard;
            } catch (InvalidMoveException e) {
                iterator.remove();
            }
        }
        return moves;
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        //If there is no piece at the starting location or if said piece is the wrong color, the move is invalid
        if (piece == null) throw new InvalidMoveException("No piece at starting position");
        else if (piece.getTeamColor() != teamTurn) throw new InvalidMoveException("Piece of wrong color for turn");
        else {
            //If the piece can't actually make that move, it's invalid
            Collection<ChessMove> moves = validMoves(move.getStartPosition());
            if (!moves.contains(move)) {
                throw new InvalidMoveException("Not a valid move");
            }
        }

        boolean capture = board.getPiece(move.getEndPosition()) != null;
        boolean pawnMove = piece.getPieceType() == ChessPiece.PieceType.PAWN;

        board = hypotheticalMove(move);

        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (piece.getTeamColor() == TeamColor.WHITE) {
                castlingOptions[0] = false;
                castlingOptions[1] = false;
            } else {
                castlingOptions[2] = false;
                castlingOptions[3] = false;
            }
        } else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            ChessPosition startPos = move.getStartPosition();
            if (castlingOptions[0] && startPos.getRow() == 1 && startPos.getColumn() == 8) castlingOptions[0] = false;

            if (castlingOptions[1] && startPos.getRow() == 1 && startPos.getColumn() == 1) castlingOptions[1] = false;

            if (castlingOptions[2] && startPos.getRow() == 8 && startPos.getColumn() == 8) castlingOptions[2] = false;

            if (castlingOptions[3] && startPos.getRow() == 8 && startPos.getColumn() == 1) castlingOptions[3] = false;
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                Math.abs(move.getStartPosition().getRow() - move.getEndPosition().getRow()) == 2)
            enPassantPosition = move.getEndPosition();
        else enPassantPosition = null;

        if (capture || pawnMove) halfMoveClock = 0;
        else halfMoveClock++;

        if (teamTurn == TeamColor.WHITE) teamTurn = TeamColor.BLACK;
        else {
            teamTurn = TeamColor.WHITE;
            fullMoves++;
        }
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition king = null;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor &&
                        piece.getPieceType() == ChessPiece.PieceType.KING) {
                    king = pos;
                    break;
                }
            }
        }

        if (king == null) return false;

        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    for (ChessMove move : piece.pieceMoves(board, pos)) {
                        if (move.getEndPosition().equals(king)) return true;
                    }
                }
            }
        }

        return false;
    }


    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) return false;

        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    if (!validMoves(pos).isEmpty()) return false;
                }
            }
        }

        return true;
    }


    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (halfMoveClock >= 50) return true;
        boolean validMove = false;
        int material = 0;
        //Go through every single piece on the board
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(position);

                if (piece != null) {
                    if (piece.getTeamColor() == teamColor && !validMoves(position).isEmpty()) validMove = true;
                    material += switch (piece.getPieceType()) {
                        case KING -> 0;
                        case QUEEN, ROOK, PAWN -> 2;
                        case BISHOP, KNIGHT -> 1;
                    };
                    if (validMove && material > 1) return false;
                }
            }
        }
        //If none of the pieces of that color could move, it is a stalemate
        return true;
    }


    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        ChessPiece whiteKing = board.getPiece(new ChessPosition(1, 5));
        ChessPiece whiteRookK = board.getPiece(new ChessPosition(1, 8));
        ChessPiece whiteRookQ = board.getPiece(new ChessPosition(1, 1));

        if (whiteKing == null || whiteKing.getPieceType() != ChessPiece.PieceType.KING) {
            castlingOptions[0] = false;
            castlingOptions[1] = false;
        } else {
            castlingOptions[0] = (whiteRookK != null && whiteRookK.getPieceType() == ChessPiece.PieceType.ROOK);
            castlingOptions[1] = (whiteRookQ != null && whiteRookQ.getPieceType() == ChessPiece.PieceType.ROOK);
        }

        ChessPiece blackKing = board.getPiece(new ChessPosition(8, 5));
        ChessPiece blackRookK = board.getPiece(new ChessPosition(8, 8));
        ChessPiece blackRookQ = board.getPiece(new ChessPosition(8, 1));

        if (blackKing == null || blackKing.getPieceType() != ChessPiece.PieceType.KING) {
            castlingOptions[2] = false;
            castlingOptions[3] = false;
        } else {
            castlingOptions[2] = (blackRookK != null && blackRookK.getPieceType() == ChessPiece.PieceType.ROOK);
            castlingOptions[3] = (blackRookQ != null && blackRookQ.getPieceType() == ChessPiece.PieceType.ROOK);
        }
    }


    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }


    private ChessBoard hypotheticalMove(ChessMove move) throws InvalidMoveException {
        ChessBoard ret = new ChessBoard(board);
        ChessPiece piece = ret.getPiece(move.getStartPosition());

        //If there is no piece at the starting location or if said piece is the wrong color, the move is invalid
        if (piece == null) throw new InvalidMoveException("No piece at starting position");


        if (move.getPromotionPiece() == null) {
            //Castling
            if (ret.getPiece(move.getStartPosition()).getPieceType() == ChessPiece.PieceType.KING &&
                    Math.abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn()) == 2) {

                int oldColumn = (move.getStartPosition().getColumn() > move.getEndPosition().getColumn()) ? 1 : 8;
                ChessPosition oldPosition = new ChessPosition(move.getEndPosition().getRow(), oldColumn);

                ChessPosition newPosition = new ChessPosition(move.getEndPosition().getRow(),
                        (move.getStartPosition().getColumn() + move.getEndPosition().getColumn()) / 2);

                ret.addPiece(newPosition, ret.getPiece(oldPosition));
                ret.addPiece(oldPosition, null);
            }

            //En Passant
            if (enPassantPosition != null &&
                    ret.getPiece(move.getStartPosition()).getPieceType() == ChessPiece.PieceType.PAWN &&
                    !Objects.equals(move.getStartPosition().getColumn(), move.getEndPosition().getColumn()) &&
                    ret.getPiece(move.getEndPosition()) == null) {
                if (!Objects.equals(move.getStartPosition().getRow(), enPassantPosition.getRow()) ||
                        !Objects.equals(move.getEndPosition().getColumn(), enPassantPosition.getColumn())) {
                    throw new InvalidMoveException("En passant at wrong position");
                }
                ret.addPiece(enPassantPosition, null);
            }


            piece = ret.getPiece(move.getStartPosition());
            ret.addPiece(move.getStartPosition(), null);
            ret.addPiece(move.getEndPosition(), piece);

        } else { //Promotions
            if (piece.getPieceType() != ChessPiece.PieceType.PAWN)
                throw new InvalidMoveException("Move with promotion piece not on pawn");
            if ((piece.getTeamColor() == ChessGame.TeamColor.WHITE && move.getEndPosition().getRow() != 8) ||
                    (piece.getTeamColor() == ChessGame.TeamColor.BLACK && move.getEndPosition().getRow() != 1))
                throw new InvalidMoveException("Move with promotion piece doesn't end to correct end of ret");
            if (ret.getPiece(move.getEndPosition()) != null) ret.addPiece(move.getEndPosition(), null);

            ChessGame.TeamColor color = ret.getPiece(move.getStartPosition()).getTeamColor();
            ret.addPiece(move.getStartPosition(), null);
            ret.addPiece(move.getEndPosition(), new ChessPiece(color, move.getPromotionPiece()));
        }

        return ret;
    }


    private Collection<ChessMove> castling(ChessPosition position) {
        Collection<ChessMove> ret = new HashSet<>();

        ChessPiece piece = board.getPiece(position);

        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.KING || position.getColumn() != 5 ||
                ((piece.getTeamColor() == TeamColor.WHITE && position.getRow() != 1) ||
                        (piece.getTeamColor() == TeamColor.BLACK && position.getRow() != 8))) {
            return ret;
        }

        int offset = piece.getTeamColor() == TeamColor.WHITE ? 0 : 2;

        if (castlingOptions[offset]) {
            ChessMove kingSideCastle = singleCastlingMove(piece.getTeamColor(), true);
            if (kingSideCastle != null) ret.add(kingSideCastle);
        }
        if (castlingOptions[offset + 1]) {
            ChessMove queenSideCastle = singleCastlingMove(piece.getTeamColor(), false);
            if (queenSideCastle != null) ret.add(queenSideCastle);
        }

        return ret;
    }


    private ChessMove singleCastlingMove(TeamColor color, boolean kingSide) {
        if (isInCheck(color)) return null;

        int row = (color == TeamColor.WHITE) ? 1 : 8;
        int col = (kingSide) ? 6 : 4;

        for (int i = col; i < 8 && i > 1; i += col - 5) {
            if (board.getPiece(new ChessPosition(row, i)) != null) return null;
        }

        ChessBoard backupBoard = board;
        try {
            ChessPosition orig = new ChessPosition(row, 5);
            board = hypotheticalMove(new ChessMove(orig, new ChessPosition(row, col)));

            if (isInCheck(color)) return null;

            return new ChessMove(orig, new ChessPosition(row, 5 + 2 * (col - 5)));
        } catch (InvalidMoveException e) {
            return null;
        } finally {
            board = backupBoard;
        }
    }


    private Collection<ChessMove> enPassant(ChessPosition position) {
        Collection<ChessMove> ret = new HashSet<>();

        if (enPassantPosition == null) return ret;

        ChessPiece piece = board.getPiece(position);

        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.PAWN) return ret;

        if (enPassantPosition.getRow() == position.getRow() &&
                Math.abs(enPassantPosition.getColumn() - position.getColumn()) == 1) {
            int row = position.getRow();
            if (piece.getTeamColor() == TeamColor.WHITE) row++;
            else row--;

            ret.add(new ChessMove(position, new ChessPosition(row, enPassantPosition.getColumn())));

        }

        return ret;
    }


    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(board.toString());
        out.append(' ');

        out.append((teamTurn == TeamColor.WHITE) ? 'w' : 'b');
        out.append(' ');

        out.append((castlingOptions[0]) ? 'K' : '-');
        out.append((castlingOptions[1]) ? 'Q' : '-');
        out.append((castlingOptions[2]) ? 'k' : '-');
        out.append((castlingOptions[3]) ? 'q' : '-');
        out.append(' ');

        if (enPassantPosition == null) out.append('-');
        else out.append((char) (enPassantPosition.getRow() + 'a' - 1)).append(enPassantPosition.getColumn());
        out.append(' ');

        out.append(halfMoveClock);
        out.append(' ');

        out.append(fullMoves);
        out.append(' ');

        return out.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChessGame chessGame = (ChessGame) o;

        if (halfMoveClock != chessGame.halfMoveClock) return false;
        if (fullMoves != chessGame.fullMoves) return false;
        if (!Arrays.equals(castlingOptions, chessGame.castlingOptions)) return false;
        if (!Objects.equals(board, chessGame.board)) return false;
        if (teamTurn != chessGame.teamTurn) return false;
        return Objects.equals(enPassantPosition, chessGame.enPassantPosition);
    }


    @Override
    public int hashCode() {
        int result = Arrays.hashCode(castlingOptions);
        result = 31 * result + (board != null ? board.hashCode() : 0);
        result = 31 * result + (teamTurn != null ? teamTurn.hashCode() : 0);
        result = 31 * result + (enPassantPosition != null ? enPassantPosition.hashCode() : 0);
        result = 31 * result + halfMoveClock;
        result = 31 * result + fullMoves;
        return result;
    }

}
