import java.util.ArrayList;

/**
 * Stores game data:
 * - Board Size
 * - Piece Types
 * - Piece location
 * -
 * An object of this class holds data about a game of checkers.
 * It knows what kind of piece is on each square of the checkerboard.
 * Note that RED moves "up" the board (i.e. row number decreases)
 * while BLACK moves "down" the board (i.e. row number increases).
 * Methods are provided to return lists of available legal moves.
 */
class CheckersData {
    private int numRowsAndColumns = Constants.defaultNumRowsAndColumns;
    static final int
            EMPTY = Constants.EMPTY,
            RED = Constants.RED,
            RED_KING = Constants.RED_KING,
            BLACK = Constants.BLACK,
            BLACK_KING = Constants.BLACK_KING;

    final Piece[][] gamePieces;

    /**
     * Setup board for new game
     */
    CheckersData() {
        gamePieces = new Piece[numRowsAndColumns][numRowsAndColumns];
        setUpCheckerBoard(numRowsAndColumns);
    }

    /**
     * Set up board with checkers in every other position.
     * That is, pieces reside at row % 2 == col % 2.
     *
     * Starting positions are first 3 and last 3 rows
     * which hold Black and Red pieces respectively.
     *
     * @param numRowsAndColumns Represents number of tiles forming the rows/columns
     */
    public void setUpCheckerBoard(int numRowsAndColumns) {
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
     * This updates the gamePieces array once the player moves a piece
     * If the player's piece arrives at the end of the board we 'king' it.
     *
     * @param fromRow Row from which the Player moves
     * @param fromCol Column from which the Player moves
     * @param toRow   Row to which the Player moves
     * @param toCol   Column to which the Player moves
     */
    void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece temp = gamePieces[toRow][toCol];
        temp.resetPiece(EMPTY);
        gamePieces[toRow][toCol] = gamePieces[fromRow][fromCol];
        gamePieces[fromRow][fromCol] = temp;
        if (Math.abs(fromRow - toRow) == 2) {
            // The move is a jump.  Remove the jumped piece from the board.
            int jumpRow = (fromRow + toRow) / 2;  // Row of the jumped piece.
            int jumpCol = (fromCol + toCol) / 2;  // Column of the jumped piece.
            gamePieces[jumpRow][jumpCol].resetPiece(EMPTY);
        }

        // If piece gets to other side of board make it into a king
        if (toRow == 0 && gamePieces[toRow][toCol].getPieceType() == RED) {
            gamePieces[toRow][toCol].setPieceType(RED_KING);
            gamePieces[toRow][toCol].setKing();
        }
        if (toRow == numRowsAndColumns - 1 && gamePieces[toRow][toCol].getPieceType() == BLACK) {
            gamePieces[toRow][toCol].setPieceType(BLACK_KING);
            gamePieces[toRow][toCol].setKing();
        }
    }

    /**
     * @param playerID Current Player's ID (RED or BLACK)
     * @return Returns Moves array if there are any legal moves
     * <p>
     * <p>
     * First check for jumps, then moves.
     * If a jump is possible, enforce it.
     * <p>
     * <p>
     * Piece Organization:
     *         Northwest           North (illegal Move)            Northeast
     *                  \                  |                      /
     *                   \                 |                     /
     * West (illegal Move) -------   Player's Game Piece     ------- East (illegal Move)
     *                   /                 |                     \
     *                  /                  |                      \
     *         Southwest           South (illegal Move)            Southeast
     */
    Move[] getLegalMoves(int playerID) {
        // Reject if player isn't Red or Black (Should never happen)
        if (playerID != RED && playerID != BLACK) {
            return null;
        }

        int playerKingID;  // Get Player's King ID
        if (playerID == RED) {
            playerKingID = RED_KING;
        } else {
            playerKingID = BLACK_KING;
        }

        ArrayList<Move> moves = new ArrayList<>();  // Moves will be stored in this list.

        /*  If a jump is possible, find them first.
         *  Examine each location for a possible jump.
         *  Check if move is legal, if so, add to ArrayList
         */

        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {

                // Check if piece is current player's
                if (gamePieces[row][col].getPieceType() == playerID || gamePieces[row][col].getPieceType() == playerKingID) {

                    // Check if player can jump Northeast
                    if (isLegalJump(playerID, row, col, row + 1, col + 1, row + 2, col + 2)) {
                        moves.add(new Move(row, col, row + 2, col + 2));
                    }

                    // Check if player can jump Northwest
                    if (isLegalJump(playerID, row, col, row - 1, col + 1, row - 2, col + 2)) {
                        moves.add(new Move(row, col, row - 2, col + 2));
                    }

                    // Check if player can jump Southeast
                    if (isLegalJump(playerID, row, col, row + 1, col - 1, row + 2, col - 2)) {
                        moves.add(new Move(row, col, row + 2, col - 2));
                    }

                    // Check if player can jump Southwest
                    if (isLegalJump(playerID, row, col, row - 1, col - 1, row - 2, col - 2)) {
                        moves.add(new Move(row, col, row - 2, col - 2));
                    }
                }
            }
        }

        /*  If there are any legal jumps, force user to jump.
         *  Otherwise, look for regular legal moves for player's
         *  pieces. If there is a legal move, add to ArrayList
         */

        if (moves.size() == 0) {
            for (int row = 0; row < numRowsAndColumns; row++) {
                for (int col = 0; col < numRowsAndColumns; col++) {
                    if (gamePieces[row][col].getPieceType() == playerID || gamePieces[row][col].getPieceType() == playerKingID) {
                        // Diagonal to the Northeast
                        if (isLegalMove(playerID, row, col, row + 1, col + 1)) {
                            moves.add(new Move(row, col, row + 1, col + 1));
                        }
                        // Diagonal to the Southeast
                        if (isLegalMove(playerID, row, col, row - 1, col + 1)) {
                            moves.add(new Move(row, col, row - 1, col + 1));
                        }
                        // Diagonal to the Northwest
                        if (isLegalMove(playerID, row, col, row + 1, col - 1)) {
                            moves.add(new Move(row, col, row + 1, col - 1));
                        }
                        // Diagonal to the Southwest
                        if (isLegalMove(playerID, row, col, row - 1, col - 1)) {
                            moves.add(new Move(row, col, row - 1, col - 1));
                        }
                    }
                }
            }
        }

        // If there are no moves return null, otherwise return moves as an array
        if (moves.size() == 0) {
            return null;
        } else {
            return moves.toArray(new Move[moves.size()]); // Convert Move List to Move Array
        }

    }

    /**
     * Constructs an array of legal jumps for a given player
     * <p>
     * This is separate from the function looking for legal moves
     * because the player must jump if possible
     *
     * @param playerID   Player's ID (Red or Black)
     * @param currentRow Game piece's current row
     * @param currentCol Game piece's current column
     * @return Return array of legal jumps
     */
    public Move[] getLegalJumpsFromPosition(int playerID, int currentRow, int currentCol) {
        // Reject if player isn't Red or Black
        if (playerID != RED && playerID != BLACK) {
            return null;
        }
        int playerKingID;  // Get Player's King ID
        if (playerID == RED) {
            playerKingID = RED_KING;
        } else {
            playerKingID = BLACK_KING;
        }
        ArrayList<Move> moves = new ArrayList<>();

        // Check if current location is the player's piece
        if (gamePieces[currentRow][currentCol].getPieceType() == playerID || gamePieces[currentRow][currentCol].getPieceType() == playerKingID) {

            // Check if there is a legal jump to the Northeast
            if (isLegalJump(playerID, currentRow, currentCol, currentRow + 1, currentCol + 1, currentRow + 2, currentCol + 2)) {
                moves.add(new Move(currentRow, currentCol, currentRow + 2, currentCol + 2));
            }

            // Check if there is a legal jump to the Southeast
            if (isLegalJump(playerID, currentRow, currentCol, currentRow - 1, currentCol + 1, currentRow - 2, currentCol + 2)) {
                moves.add(new Move(currentRow, currentCol, currentRow - 2, currentCol + 2));
            }

            // Check if there is a legal jump to the Northwest
            if (isLegalJump(playerID, currentRow, currentCol, currentRow + 1, currentCol - 1, currentRow + 2, currentCol - 2)) {
                moves.add(new Move(currentRow, currentCol, currentRow + 2, currentCol - 2));
            }

            // Check if there is a legal jump to the Southwest
            if (isLegalJump(playerID, currentRow, currentCol, currentRow - 1, currentCol - 1, currentRow - 2, currentCol - 2)) {
                moves.add(new Move(currentRow, currentCol, currentRow - 2, currentCol - 2));
            }
        }

        // If there are no jumps return null, otherwise return moves as an array
        if (moves.size() == 0) {
            return null;
        } else {
            return moves.toArray(new Move[moves.size()]);
        }
    }


    /**
     * Check if jump is legal
     *
     * @param player  Player's ID
     * @param fromRow Row from which the Player moves
     * @param fromCol Column from which the Player moves
     * @param jumpRow The row the player jumps over
     * @param jumpCol The column the player jumps over
     * @param toRow   The row the player arrives at after jump
     * @param toCol   The colum the player arrives at after jump
     * @return True if jump is legal
     */
    //!@#$%^&*() Superior Logic, but not for Submission //!@#$%^&*()
    private boolean isLegalJump(int player, int fromRow, int fromCol, int jumpRow, int jumpCol, int toRow, int toCol) { // WORKS
        // Check if jump is on the board
        if (toRow < 0 || toRow >= numRowsAndColumns || toCol < 0 || toCol >= numRowsAndColumns) {
            return false;
        }

        // Check if tile is occupied
        if (gamePieces[toRow][toCol].getPieceType() != EMPTY) {
            return false;
        }

        if (player == RED) {
            if (gamePieces[fromRow][fromCol].getPieceType() == RED && toRow > fromRow) {
                return false;  // Regular red piece can only move North.
            }
            if (gamePieces[jumpRow][jumpCol].getPieceType() != BLACK && gamePieces[jumpRow][jumpCol].getPieceType() != BLACK_KING) {
                return false;  // There is no black piece to jump.
            }
        } else {
            if (gamePieces[fromRow][fromCol].getPieceType() == BLACK && toRow < fromRow) {
                return false; // Regular black piece can only move South.
            }
            if (gamePieces[jumpRow][jumpCol].getPieceType() != RED && gamePieces[jumpRow][jumpCol].getPieceType() != RED_KING) {
                return false; // There is no red piece to jump.
            }
        }
        return true;  // The jump is legal.

    }

//!@#$%^&*() ENABLE THIS LOGIC CHECK IN ACTUAL GAME
//    /**
    // * Check if jump is legal
//     * @param player  Player's ID (Assumed to be RED or BLACK)
//     * @param fromRow Row from which the Player moves
//     * @param fromCol Column from which the Player moves
//     * @param jumpRow The row the player jumps over
//     * @param jumpCol The column the player jumps over
//     * @param toRow   The row the player arrives at after jump
//     * @param toCol   The column the player arrives at after jump
//     * @return True if jump is legal
//     */
//    private boolean isLegalJump(int player, int fromRow, int fromCol, int jumpRow, int jumpCol, int toRow, int toCol) { // WORKS
//        // Check if jump is on the board
//        if (toRow < 0 || toRow >= numRowsAndColumns || toCol < 0 || toCol >= numRowsAndColumns) {
//            return false;
//        }
//
//        // Check if tile is occupied
//        if (gamePieces[toRow][toCol].getPieceType() != EMPTY) {
//            return false;
//        }
//
//        // Cannot jump over empty spaces
//        if (gamePieces[jumpRow][jumpCol].getPieceType() == EMPTY) {
//            return false;
//        }
//
//        // Check if uncrowned pieces are going in the right direction
//        if (player == RED
//                && gamePieces[fromRow][fromCol].getPieceType() == RED
//                && toRow > fromRow) { // Red only moves North
//            return false;
//        } else if (player == BLACK
//                && gamePieces[fromRow][fromCol].getPieceType() == BLACK
//                && toRow < fromRow) { // Black only moves South
//            return false;
//        }
//
//        // Cannot jump over player's own pieces
//        // Recall that "Color"_KING = "Color" + 1
//        if (gamePieces[jumpRow][jumpCol].getPieceType() == player || gamePieces[jumpRow][jumpCol].getPieceType() == player + 1) {
//            return false;
//        }
//
//        return true;  // The jump is legal.
//
//    }


    /**
     * Check if move is legal
     *
     * @param player  Player's ID
     * @param fromRow Row from which the Player moves
     * @param fromCol Column from which the Player moves
     * @param toRow   Row to which the Player moves
     * @param toCol   Column to which the Player Moves
     * @return If move is legal return true
     */
    private boolean isLegalMove(int player, int fromRow, int fromCol, int toRow, int toCol) {
        // Check if move is on the board
        if (toRow < 0 || toRow >= numRowsAndColumns || toCol < 0 || toCol >= numRowsAndColumns) {
            return false;
        }

        // Check if to location is occupied
        if (gamePieces[toRow][toCol].getPieceType() != EMPTY) {
            return false;
        }

//        // Check if piece can legally move up or down
//        if (player == RED && gamePieces[fromRow][fromCol].getPieceType() == RED && toRow > fromRow) {
        if (player == RED && gamePieces[fromRow][fromCol].isPiece(RED) && toRow > fromRow) {
            return false;  // Red pieces (not a king) can only move South.
        } else return gamePieces[fromRow][fromCol].getPieceType() != BLACK || toRow >= fromRow;

    }

    /**
     * Print board in console
     */
    private void printBoardPieces() {
        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
                System.out.printf("%s ", gamePieces[row][col].getPieceType());
            }
            System.out.println();
        }
    }
}