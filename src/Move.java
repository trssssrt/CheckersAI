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
    AI_Move parent;
    int score;

    AI_Move(int oldRow, int oldColumn, int newRow, int newColumn, AI_Move parent) {
        super(oldRow,oldColumn,newRow,newColumn);
        this.parent = parent;
    }
    AI_Move(int oldRow, int oldColumn, int newRow, int newColumn, AI_Move parent, int score) {
        super(oldRow,oldColumn,newRow,newColumn);
        this.parent = parent;
        this.score = score;
    }
    AI_Move(int oldRow, int oldColumn, int newRow, int newColumn) {
        super(oldRow,oldColumn,newRow,newColumn);
        this.parent = null;
    }
    AI_Move(AI_Move ai_move) {
        super(ai_move.fromRow, ai_move.fromCol, ai_move.toRow, ai_move.toCol);
        this.parent = ai_move.parent;
        this.score = ai_move.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setParent(AI_Move parent) {
        this.parent = parent;
    }

    public int getScore() {
        return score;
    }

    public Move getParent() {
        return parent;
    }
    // Print solution path to console.
    AI_Move getRootParent() {
        if (parent != null) {
            parent.getRootParent();
        }
        System.out.println("*************************");
        System.out.println(this);
        return this;

    }
}

