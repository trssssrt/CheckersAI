import java.awt.geom.Ellipse2D;

/**
 * Game Piece Object
 * Stores relevant piece information.
 * Each of the four game piece types have a specific value:
 * 0   -   Empty
 * 1   -   Red
 * 2   -   Red King
 * 3   -   Black
 * 4   -   Black King
 *
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

    //!@#$%^&*() DELETE NEXT LINE
    Ellipse2D getOval() {
        return oval;
    }

    void setOval(Ellipse2D oval) {
        this.oval = oval;
    }

    void setPieceType(int pieceType) {
        this.pieceType = pieceType;
    }

    // TYLER's CODE
    void setKing() {
        this.king = true;
    }

    boolean isKing() {
        return this.king;
    }

    void resetPiece(int emptyPieceType) {
//        this.pieceType = CheckersData.EMPTY; // This WOULD work, but feels like a bad practice
        this.pieceType = 0;
        this.oval = null;
        this.king = false;
    }
}