import java.awt.geom.Ellipse2D;

/**
 * Game Piece Object
 * Stores relevant piece information.
 * Each of the four game piece types have a specific value:
 * 0   -   Empty
 * 1   -   Red
 * 2   -   Black
 * <p>
 * Oval is the drawn piece on game board
 */
class Piece {
    private int pieceType;
    private boolean king;
    private Ellipse2D oval;

    Piece(int pieceType, Ellipse2D oval, boolean king) {
        this.pieceType = pieceType;
        this.oval = oval;
        this.king = king;
    }

    int getPieceType() {
        return pieceType;
    }

    boolean isPiece(int color) {
        return getPieceType() == color;
    }

    boolean isPieceAndKing(int color) {
        return getPieceType() == color && isKing();
    }

    void setOval(Ellipse2D oval) {
        this.oval = oval;
    }

    void setPieceType(int pieceType) {
        this.pieceType = pieceType;
    }

    void setKing() {
        this.king = true;
    }

    boolean isKing() {
        return this.king;
    }

    void resetPiece(int emptyPieceType) {
        this.pieceType = emptyPieceType;
        this.oval = null;
        this.king = false;
    }
}
