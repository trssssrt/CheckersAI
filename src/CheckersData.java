import java.util.ArrayList;

/**
 * An object of this class holds data about a game of checkers.
 * It knows what kind of piece is on each square of the checkerboard.
 * Note that RED moves "up" the board (i.e. row number decreases)
 * while BLACK moves "down" the board (i.e. row number increases).
 * Methods are provided to return lists of available legal moves.
 */
class CheckersData {
    private int numRowsAndColumns = 8;

      /*  The following constants represent the possible contents of a square
          on the board.  The constants RED and BLACK also represent players
          in the game. */

    static final int
            EMPTY = 0,
            RED = 1,
            RED_KING = 2,
            BLACK = 3,
            BLACK_KING = 4;


    int[][] board;  // board[r][c] is the contents of row r, column c.
    final Piece[][] gamePieces = new Piece[numRowsAndColumns][numRowsAndColumns];


    /**
     * Constructor.  Create the board and set it up for a new game.
     */
    CheckersData() {
        board = new int[numRowsAndColumns][numRowsAndColumns];
        setUpCheckerBoard(numRowsAndColumns);
        setUpGame();
    }

    /**
     * buildCheckerBoard
     *
     * @param numRowsAndColumns Represents number of squares forming the rows/columns
     *                          <p>
     *                          Used to create the checkerboard
     *                          <p>
     *                          TYLER's CODE //!@#$%^&*() Remove once done
     */
    void setUpCheckerBoard(int numRowsAndColumns) {
        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
                if (row % 2 == col % 2) {
                    if (row < 3) {
                        gamePieces[row][col] = new Piece(BLACK, null, false);
                    } else if (row > (numRowsAndColumns - 3 - 1)) {// Check if last 3 rows (-1 since numRowsAndColumns is indexed at 1, and -3 to represent the last 3 rows
                        gamePieces[row][col] = new Piece(RED, null, false);
                    } else {
                        gamePieces[row][col] = new Piece(EMPTY, null, false);
                    }
                } else {
                    gamePieces[row][col] = new Piece(EMPTY, null, false);
                }
            }
        }
    }

    /**
     * Set up the board with checkers in position for the beginning
     * of a game.  Note that checkers can only be found in squares
     * that satisfy  row % 2 == col % 2.  At the start of the game,
     * all such squares in the first three rows contain black squares
     * and all such squares in the last three rows contain red squares.
     */
    void setUpGame() {
        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
                if (row % 2 == col % 2) {
                    if (row < 3) {
                        board[row][col] = BLACK;
                    } else if (row > (numRowsAndColumns - 3 - 1)) { // Check if last 3 rows (-1 since numRowsAndColumns is indexed at 1, and -3 to represent the last 3 rows
                        board[row][col] = RED;
                    } else {
                        board[row][col] = EMPTY;
                    }
                } else {
                    board[row][col] = EMPTY;
                }
            }
        }
    }  // end setUpGame()


    /**
     * Make the move from (fromRow,fromCol) to (toRow,toCol).  It is
     * assumed that this move is legal.  If the move is a jump, the
     * jumped piece is removed from the board.  If a piece moves
     * the last row on the opponent's side of the board, the
     * piece becomes a king.
     */
    void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        boolean isKing = false;
        Piece temp = gamePieces[toRow][toCol];
        temp.resetPiece();
        gamePieces[toRow][toCol] = gamePieces[fromRow][fromCol];
        gamePieces[fromRow][fromCol] = temp;
//        gamePieces[fromRow][fromCol].setPieceVal(EMPTY);
        if (fromRow - toRow == 2 || fromRow - toRow == -2) {
            // The move is a jump.  Remove the jumped piece from the board.
            int jumpRow = (fromRow + toRow) / 2;  // Row of the jumped piece.
            int jumpCol = (fromCol + toCol) / 2;  // Column of the jumped piece.
//            gamePieces[jumpRow][jumpCol].setPieceVal(EMPTY);
            gamePieces[jumpRow][jumpCol].resetPiece();//setOval(null);//!@#$%^&*()
        }
        if (toRow == 0 && gamePieces[toRow][toCol].getPieceVal() == RED) {
            gamePieces[toRow][toCol].setPieceVal(RED_KING);
            gamePieces[toRow][toCol].setKing();
            isKing = true;
        }
        if (toRow == numRowsAndColumns - 1 && gamePieces[toRow][toCol].getPieceVal() == BLACK) {
            gamePieces[toRow][toCol].setPieceVal(BLACK_KING);
            gamePieces[toRow][toCol].setKing();
            isKing = true;
        }


        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = EMPTY;
        if (fromRow - toRow == 2 || fromRow - toRow == -2) {
            // The move is a jump.  Remove the jumped piece from the board.
            int jumpRow = (fromRow + toRow) / 2;  // Row of the jumped piece.
            int jumpCol = (fromCol + toCol) / 2;  // Column of the jumped piece.
            board[jumpRow][jumpCol] = EMPTY;
        }
        if (toRow == 0 && board[toRow][toCol] == RED) {
            board[toRow][toCol] = RED_KING;
            isKing = true;
        }
        if (toRow == numRowsAndColumns - 1 && board[toRow][toCol] == BLACK) {
            board[toRow][toCol] = BLACK_KING;
            isKing = true;
        }

        if (isKing){
            printBoard();
            System.out.printf("\n\n");
            printBoardPieces();
        }
    }

    /**
     * Return an array containing all the legal Moves
     * for the specified player on the current board.  If the player
     * has no legal moves, null is returned.  The value of player
     * should be one of the constants RED or BLACK; if not, null
     * is returned.  If the returned value is non-null, it consists
     * entirely of jump moves or entirely of regular moves, since
     * if the player can jump, only jumps are legal moves.
     */
    Move[] getLegalMoves(int player) { // WORKS
        if (player != RED && player != BLACK) { //!@#$%^&*() What? DO NOT PLAY IF EMPTY?
            return null;
        }

        int playerKing;  // The constant representing a King belonging to player.
        if (player == RED) {
            playerKing = RED_KING;
        } else {
            playerKing = BLACK_KING;
        }

        ArrayList<Move> moves = new ArrayList<>();  // Moves will be stored in this list.

         /*  First, check for any possible jumps.  Look at each square on the board.
          If that square contains one of the player's pieces, look at a possible
          jump in each of the four directions from that square.  If there is
          a legal jump in that direction, put it in the moves ArrayList.
          */

        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
//                if (board[row][col] == player || board[row][col] == playerKing) {
                    if (gamePieces[row][col].getPieceVal() == player || gamePieces[row][col].getPieceVal() == playerKing) {
//                if (gamePieces[row][col].getPieceVal() == player || gamePieces[row][col].getPieceVal() == playerKing) {
                    if (canJump(player, row, col, row + 1, col + 1, row + 2, col + 2)) {
                        moves.add(new Move(row, col, row + 2, col + 2));
                    }
                    if (canJump(player, row, col, row - 1, col + 1, row - 2, col + 2)) {
                        moves.add(new Move(row, col, row - 2, col + 2));
                    }
                    if (canJump(player, row, col, row + 1, col - 1, row + 2, col - 2)) {
                        moves.add(new Move(row, col, row + 2, col - 2));
                    }
                    if (canJump(player, row, col, row - 1, col - 1, row - 2, col - 2)) {
                        moves.add(new Move(row, col, row - 2, col - 2));
                    }
                }
            }
        }

         /*  If any jump moves were found, then the user must jump, so we don't
          add any regular moves.  However, if no jumps were found, check for
          any legal regular moves.  Look at each square on the board.
          If that square contains one of the player's pieces, look at a possible
          move in each of the four directions from that square.  If there is
          a legal move in that direction, put it in the moves ArrayList.
          */

        if (moves.size() == 0) {
            for (int row = 0; row < numRowsAndColumns; row++) {
                for (int col = 0; col < numRowsAndColumns; col++) {
//                    if (board[row][col] == player || board[row][col] == playerKing) {
                        if (gamePieces[row][col].getPieceVal() == player || gamePieces[row][col].getPieceVal() == playerKing) {
//                    if (gamePieces[row][col].getPieceVal() == player || gamePieces[row][col].getPieceVal() == playerKing) {
                        if (canMove(player, row, col, row + 1, col + 1)) {
                            moves.add(new Move(row, col, row + 1, col + 1));
                        }
                        if (canMove(player, row, col, row - 1, col + 1)) {
                            moves.add(new Move(row, col, row - 1, col + 1));
                        }
                        if (canMove(player, row, col, row + 1, col - 1)) {
                            moves.add(new Move(row, col, row + 1, col - 1));
                        }
                        if (canMove(player, row, col, row - 1, col - 1)) {
                            moves.add(new Move(row, col, row - 1, col - 1));
                        }
                    }
                }
            }
        }

         /* If no legal moves have been found, return null.  Otherwise, create
          an array just big enough to hold all the legal moves, copy the
          legal moves from the ArrayList into the array, and return the array. */

        if (moves.size() == 0) {
            return null;
        } else {
            Move[] moveArray = new Move[moves.size()];
            for (int i = 0; i < moves.size(); i++) {
                moveArray[i] = moves.get(i);
            }
            return moveArray;
        }

    }  // end getLegalMoves


    /**
     * Return a list of the legal jumps that the specified player can
     * make starting from the specified row and column.  If no such
     * jumps are possible, null is returned.  The logic is similar
     * to the logic of the getLegalMoves() method.
     */
    Move[] getLegalJumpsFrom(int player, int row, int col) { // WORKS
        if (player != RED && player != BLACK) {
            return null;
        }
        int playerKing;  // The constant representing a King belonging to player.
        if (player == RED) {
            playerKing = RED_KING;
        } else {
            playerKing = BLACK_KING;
        }
        ArrayList<Move> moves = new ArrayList<>();  // The legal jumps will be stored in this list.
//        if (board[row][col] == player || board[row][col] == playerKing) {
            if (gamePieces[row][col].getPieceVal() == player || gamePieces[row][col].getPieceVal() == playerKing) {
            if (canJump(player, row, col, row + 1, col + 1, row + 2, col + 2)) {
                moves.add(new Move(row, col, row + 2, col + 2));
            }
            if (canJump(player, row, col, row - 1, col + 1, row - 2, col + 2)) {
                moves.add(new Move(row, col, row - 2, col + 2));
            }
            if (canJump(player, row, col, row + 1, col - 1, row + 2, col - 2)) {
                moves.add(new Move(row, col, row + 2, col - 2));
            }
            if (canJump(player, row, col, row - 1, col - 1, row - 2, col - 2)) {
                moves.add(new Move(row, col, row - 2, col - 2));
            }
        }
        if (moves.size() == 0) {
            return null;
        } else {
            Move[] moveArray = new Move[moves.size()];
            for (int i = 0; i < moves.size(); i++) {
                moveArray[i] = moves.get(i);
            }
            return moveArray;
        }
    }  // end getLegalMovesFrom()


    /**
     * This is called by the two previous methods to check whether the
     * player can legally jump from (r1,c1) to (r3,c3).  It is assumed
     * that the player has a piece at (r1,c1), that (r3,c3) is a position
     * that is 2 rows and 2 columns distant from (r1,c1) and that
     * (r2,c2) is the square between (r1,c1) and (r3,c3).
     */
    private boolean canJump(int player, int r1, int c1, int r2, int c2, int r3, int c3) { // WORKS

        if (r3 < 0 || r3 >= numRowsAndColumns || c3 < 0 || c3 >= numRowsAndColumns) {
            return false;  // (r3,c3) is off the board.
        }
        if (board[r3][c3] != EMPTY) {
            return false;  // (r3,c3) already contains a piece.
        }

        if (player == RED) {
            // Regular red pieces
//            if (board[r1][c1] == RED && r3 > r1) {
                if (gamePieces[r1][c1].getPieceVal() == RED && r3 > r1) {
                return false;  // Regular red piece can only move  up.
            }
//            if (board[r2][c2] != BLACK && board[r2][c2] != BLACK_KING) {
                if (gamePieces[r2][c2].getPieceVal() != BLACK && gamePieces[r2][c2].getPieceVal() != BLACK_KING) {
                return false;  // There is no black piece to jump.
            }
        } else {
//            if (board[r1][c1] == BLACK && r3 < r1) {
                if (gamePieces[r1][c1].getPieceVal() == BLACK && r3 < r1) {
                    return false;  // Regular black piece can only move down.
            }
//            if (board[r2][c2] != RED && board[r2][c2] != RED_KING) {
                if (gamePieces[r2][c2].getPieceVal() != RED && gamePieces[r2][c2].getPieceVal() != RED_KING) {

                    return false;  // There is no red piece to jump.
            }
        }
        return true;  // The jump is legal.

    }  // end canJump()


    /**
     * This is called by the getLegalMoves() method to determine whether
     * the player can legally move from (r1,c1) to (r2,c2).  It is
     * assumed that (r1,r2) contains one of the player's pieces and
     * that (r2,c2) is a neighboring square.
     */
    private boolean canMove(int player, int r1, int c1, int r2, int c2) { // WORKS

        if (r2 < 0 || r2 >= numRowsAndColumns || c2 < 0 || c2 >= numRowsAndColumns) {
            return false;  // (r2,c2) is off the board.
        }

//        if (board[r2][c2] != EMPTY) {
            if (gamePieces[r2][c2].getPieceVal() != EMPTY) {
            return false;  // (r2,c2) already contains a piece.
        }

//        if (player == RED && board[r1][c1] == RED && r2 > r1) {
            if (player == RED && gamePieces[r1][c1].getPieceVal() == RED && r2 > r1) {
            return false;  // Regular red piece can only move down.
//            } else return board[r1][c1] != BLACK || r2 >= r1;
    } else return gamePieces[r1][c1].getPieceVal() != BLACK || r2 >= r1;

    }  // end canMove()

    public void printBoard() {
        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
                System.out.printf("%s ", board[row][col]);
            }
            System.out.printf("\n");
        }
    }

    public void printBoardPieces() {
        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
                System.out.printf("%s ", gamePieces[row][col].getPieceVal());
            }
            System.out.printf("\n");
        }
    }
} // end class CheckersData