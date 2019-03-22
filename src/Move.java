class Move {
    int fromRow, fromCol;  // Current piece's position
    int toRow, toCol;      // Tile to which the piece moves

    /**
     * Move (object) stores game piece moves.
     * The legality of moves are determined elsewhere.
     *
     * @param oldRow    The 'current' piece's row position
     * @param oldColumn The 'current' piece's column position
     * @param newRow    The 'new' piece's row position
     * @param newColumn The 'new' piece's column position
     */
    Move(int oldRow, int oldColumn, int newRow, int newColumn) {
        this.fromRow = oldRow;
        this.fromCol = oldColumn;
        this.toRow = newRow;
        this.toCol = newColumn;
    }

    // Check if move is a jump (assume jump is legal).
    // Non-jump moves only move diagonally 1 tile at a time
    boolean isJump() {
//        return (fromRow - toRow == 2 || fromRow - toRow == -2);
        return Math.abs(fromRow - toRow) == 2;
    }
}

class AI_Move extends Move {
    Move parent;

    AI_Move(int oldRow, int oldColumn, int newRow, int newColumn, Move parent) {
        super(oldRow,oldColumn,newRow,newColumn);
    }
    AI_Move(int oldRow, int oldColumn, int newRow, int newColumn) {
        super(oldRow,oldColumn,newRow,newColumn);
        this.parent = null;
    }

}

