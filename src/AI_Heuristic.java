import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Here we employ a
 * Heuristic
 * AND the
 * Negamax Algorithm
 * with Alpha Beta
 * Pruning
 */
public class AI_Heuristic {

    private int computerPlayerID, difficulty, numRowsAndColumns;
    private Piece[][] gameBoard;
    private int EMPTY = CheckersData.EMPTY,
            RED = CheckersData.RED,
            RED_KING = CheckersData.RED_KING,
            BLACK = CheckersData.BLACK,
            BLACK_KING = CheckersData.BLACK_KING;

    private int DEPTH = 10;

    private List<AI_Move> successorEvaluations;
    private AI_Move bestMove;

    private int NORMAL_PIECE = 100,
    KING = 175;


    AI_Heuristic(int computerPlayerID, int difficulty, Piece[][] board, int numRowsAndColumns) {
        this.computerPlayerID = computerPlayerID;
        this.difficulty = difficulty;
        this.gameBoard = deepCopy(board);
        this.numRowsAndColumns = numRowsAndColumns;
        this.successorEvaluations = new ArrayList();
    }

    Move getBestMove() {
        System.out.println("DIFFICULTY: " + difficulty);
        if (difficulty > 1) {
            this.successorEvaluations = new ArrayList<>();
//            System.out.println("--Player ID: " + computerPlayerID);
            int a = this.negamaxAB(gameBoard, null, DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, computerPlayerID);
//            System.out.println("BEST MOVE: " + bestMove);
            System.out.println("Best Move SCORE: " + a
                    +" (" + bestMove.fromRow + ", " + bestMove.fromCol +
                    ") -> (" + bestMove.toRow + ", " + bestMove.toCol + ")");
            return bestMove;
        }

        AI_Move[] legalMoves = getLegalMoves(gameBoard,null, computerPlayerID);
        return legalMoves[(int) (legalMoves.length * Math.random())];


    }

    // Remember 2 ply = 1 move
    private int negamaxAB(Piece[][] board, AI_Move ply, int depth, int alpha, int beta, int playerID) {
        AI_Move[] legalMoveList = getLegalMoves(board, ply, playerID);
        // IF we reach the end
        if (depth == 0 //){
        || legalMoveList == null) {
//            System.out.println("Player ID: " + playerID);
            return evaluateHeuristic(board, playerID);
        }

        int bestValue = Integer.MIN_VALUE;
        for (AI_Move move: legalMoveList) {
            // Create deep copy of board
            Piece[][] newBoard = deepCopy(board);
            makeMove(newBoard, move.fromRow,move.fromCol, move.toRow,move.toCol);
            // Switch Players
            if (playerID == RED) {
                playerID = BLACK;
            } else {
                playerID = RED;
            }

            int val = -negamaxAB(newBoard, move, depth - 1, -beta, -alpha, playerID);
            bestValue = Math.max(bestValue, val);
            if (val >= alpha && depth == DEPTH) {
                bestMove = move;
            }
            alpha = Math.max(alpha, val);
            if (alpha >= beta) {
                if (depth == DEPTH) {bestMove = move;}
                break;
            }
        }
        return bestValue;
    }
    private int evaluateHeuristic(Piece[][] board, int playerID) {
        return simpleScore(board, playerID);
    }

    // Returns the difference in pieces
    protected int simpleScore(Piece[][] board, int player) {
        int black = 0, red = 0;
        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
                if (board[row][col].getPieceType() == RED) {
                    red += NORMAL_PIECE;
                } else if (board[row][col].getPieceType() == RED_KING && board[row][col].isKing()) {
                    red += KING;
                } else if (board[row][col].getPieceType() == BLACK) {
                    black += NORMAL_PIECE;
                } else if (board[row][col].getPieceType() == BLACK_KING && board[row][col].isKing()) {
                    black += KING;
                }
            }
        }
        if (player == RED) {
            return red - black;
        } else {
            return black - red;
        }
    }
    private int whoIs(int playerID) {
        int id = 0;
        if (playerID == 1) {
            id =  computerPlayerID;
        } else if (playerID == -1) {
            id =  computerPlayerID == RED ? RED : BLACK;
        } else if (playerID == RED) {
            id =  computerPlayerID == RED ? 1 : -1;;
        }
        return id;
    }
    void updateGameBoard(Piece[][] localBoard) {
        this.gameBoard = deepCopy(localBoard);
    }
    private Piece[][] deepCopy(Piece[][] localBoard) {
        Piece[][] newBoard = new Piece[numRowsAndColumns][numRowsAndColumns];

        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
                newBoard[row][col] = new Piece(localBoard[row][col].getPieceType(),
                        null,
                        localBoard[row][col].isKing());
            }
        }
        return newBoard;
    }

    /**
     * This updates the gameBoard array once the player moves a piece
     * If the player's piece arrives at the end of the board we 'king' it.
     *
     * @param fromRow Row from which the Player moves
     * @param fromCol Column from which the Player moves
     * @param toRow   Row to which the Player moves
     * @param toCol   Column to which the Player moves
     */
    void makeMove(Piece[][] gameBoard, int fromRow, int fromCol, int toRow, int toCol) {
        Piece temp = gameBoard[toRow][toCol];
        temp.resetPiece(EMPTY);
        gameBoard[toRow][toCol] = gameBoard[fromRow][fromCol];
        gameBoard[fromRow][fromCol] = temp;
        if (Math.abs(fromRow - toRow) == 2) {
            // The move is a jump.  Remove the jumped piece from the board.
            int jumpRow = (fromRow + toRow) / 2;  // Row of the jumped piece.
            int jumpCol = (fromCol + toCol) / 2;  // Column of the jumped piece.
            gameBoard[jumpRow][jumpCol].resetPiece(EMPTY);//!@#$%^&*()
        }

        // If piece gets to other side of board make it into a king
        if (toRow == 0 && gameBoard[toRow][toCol].getPieceType() == RED) {
            gameBoard[toRow][toCol].setPieceType(RED_KING);
            gameBoard[toRow][toCol].setKing();
        }
        if (toRow == numRowsAndColumns - 1 && gameBoard[toRow][toCol].getPieceType() == BLACK) {
            gameBoard[toRow][toCol].setPieceType(BLACK_KING);
            gameBoard[toRow][toCol].setKing();
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
    AI_Move[] getLegalMoves(Piece[][] gameBoard, AI_Move parent,  int playerID) {
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

        ArrayList<AI_Move> moves = new ArrayList<>();  // Moves will be stored in this list.

        /*  If a jump is possible, find them first.
         *  Examine each location for a possible jump.
         *  Check if move is legal, if so, add to ArrayList
         */

        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {

                // Check if piece is current player's
                if (gameBoard[row][col].getPieceType() == playerID || gameBoard[row][col].getPieceType() == playerKingID) {

                    // Check if player can jump Northeast
                    if (isLegalJump(gameBoard, playerID, row, col, row + 1, col + 1, row + 2, col + 2)) {
                        moves.add(new AI_Move(row, col, row + 2, col + 2, parent));
                    }

                    // Check if player can jump Northwest
                    if (isLegalJump(gameBoard, playerID, row, col, row - 1, col + 1, row - 2, col + 2)) {
                        moves.add(new AI_Move(row, col, row - 2, col + 2, parent));
                    }

                    // Check if player can jump Southeast
                    if (isLegalJump(gameBoard, playerID, row, col, row + 1, col - 1, row + 2, col - 2)) {
                        moves.add(new AI_Move(row, col, row + 2, col - 2, parent));
                    }

                    // Check if player can jump Southwest
                    if (isLegalJump(gameBoard, playerID, row, col, row - 1, col - 1, row - 2, col - 2)) {
                        moves.add(new AI_Move(row, col, row - 2, col - 2, parent));
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
                    if (gameBoard[row][col].getPieceType() == playerID || gameBoard[row][col].getPieceType() == playerKingID) {
                        // Diagonal to the Northeast
                        if (isLegalMove(gameBoard, playerID, row, col, row + 1, col + 1)) {
                            moves.add(new AI_Move(row, col, row + 1, col + 1, parent));
                        }
                        // Diagonal to the Southeast
                        if (isLegalMove(gameBoard, playerID, row, col, row - 1, col + 1)) {
                            moves.add(new AI_Move(row, col, row - 1, col + 1, parent));
                        }
                        // Diagonal to the Northwest
                        if (isLegalMove(gameBoard, playerID, row, col, row + 1, col - 1)) {
                            moves.add(new AI_Move(row, col, row + 1, col - 1, parent));
                        }
                        // Diagonal to the Southwest
                        if (isLegalMove(gameBoard, playerID, row, col, row - 1, col - 1)) {
                            moves.add(new AI_Move(row, col, row - 1, col - 1, parent));
                        }
                    }
                }
            }
        }

        // If there are no moves return null, otherwise return moves as an array
        if (moves.size() == 0) {
            return null;
        } else {
            return moves.toArray(new AI_Move[moves.size()]); // Convert Move List to Move Array
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
    public Move[] getLegalJumpsFromPosition(Piece[][] gameBoard, int playerID, int currentRow, int currentCol) {
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
        if (gameBoard[currentRow][currentCol].getPieceType() == playerID || gameBoard[currentRow][currentCol].getPieceType() == playerKingID) {

            // Check if there is a legal jump to the Northeast
            if (isLegalJump(gameBoard,playerID, currentRow, currentCol, currentRow + 1, currentCol + 1, currentRow + 2, currentCol + 2)) {
                moves.add(new Move(currentRow, currentCol, currentRow + 2, currentCol + 2));
            }

            // Check if there is a legal jump to the Southeast
            if (isLegalJump(gameBoard,playerID, currentRow, currentCol, currentRow - 1, currentCol + 1, currentRow - 2, currentCol + 2)) {
                moves.add(new Move(currentRow, currentCol, currentRow - 2, currentCol + 2));
            }

            // Check if there is a legal jump to the Northwest
            if (isLegalJump(gameBoard,playerID, currentRow, currentCol, currentRow + 1, currentCol - 1, currentRow + 2, currentCol - 2)) {
                moves.add(new Move(currentRow, currentCol, currentRow + 2, currentCol - 2));
            }

            // Check if there is a legal jump to the Southwest
            if (isLegalJump(gameBoard,playerID, currentRow, currentCol, currentRow - 1, currentCol - 1, currentRow - 2, currentCol - 2)) {
                moves.add(new Move(currentRow, currentCol, currentRow - 2, currentCol - 2));
            }
        }

        // If there are no jumps return null, otherwise return moves as an array
        if (moves.size() == 0) {
            return null;
        } else {//!@#$%^&*() Why can't we just return moves? -- Because they are a different type
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
    private boolean isLegalJump(Piece[][] gameBoard, int player, int fromRow, int fromCol, int jumpRow, int jumpCol, int toRow, int toCol) { // WORKS
        // Check if jump is on the board
        if (toRow < 0 || toRow >= numRowsAndColumns || toCol < 0 || toCol >= numRowsAndColumns) {
            return false;
        }

        // Check if tile is occupied
        if (gameBoard[toRow][toCol].getPieceType() != EMPTY) {
            return false;
        }

        if (player == RED) {
            if (gameBoard[fromRow][fromCol].getPieceType() == RED && toRow > fromRow) {
                return false;  // Regular red piece can only move North.
            }
            if (gameBoard[jumpRow][jumpCol].getPieceType() != BLACK && gameBoard[jumpRow][jumpCol].getPieceType() != BLACK_KING) {
                return false;  // There is no black piece to jump.
            }
        } else {
            if (gameBoard[fromRow][fromCol].getPieceType() == BLACK && toRow < fromRow) {
                return false; // Regular black piece can only move South.
            }
            if (gameBoard[jumpRow][jumpCol].getPieceType() != RED && gameBoard[jumpRow][jumpCol].getPieceType() != RED_KING) {
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
//        if (gameBoard[toRow][toCol].getPieceType() != EMPTY) {
//            return false;
//        }
//
//        // Cannot jump over empty spaces
//        if (gameBoard[jumpRow][jumpCol].getPieceType() == EMPTY) {
//            return false;
//        }
//
//        // Check if uncrowned pieces are going in the right direction
//        if (player == RED
//                && gameBoard[fromRow][fromCol].getPieceType() == RED
//                && toRow > fromRow) { // Red only moves North
//            return false;
//        } else if (player == BLACK
//                && gameBoard[fromRow][fromCol].getPieceType() == BLACK
//                && toRow < fromRow) { // Black only moves South
//            return false;
//        }
//
//        // Cannot jump over player's own pieces
//        // Recall that "Color"_KING = "Color" + 1
//        if (gameBoard[jumpRow][jumpCol].getPieceType() == player || gameBoard[jumpRow][jumpCol].getPieceType() == player + 1) {
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
    private boolean isLegalMove(Piece[][] gameBoard, int player, int fromRow, int fromCol, int toRow, int toCol) {
        // Check if move is on the board
        if (toRow < 0 || toRow >= numRowsAndColumns || toCol < 0 || toCol >= numRowsAndColumns) {
            return false;
        }

        // Check if to location is occupied
        if (gameBoard[toRow][toCol].getPieceType() != EMPTY) {
            return false;
        }

        // Check if piece can legally move up or down
        if (player == RED && gameBoard[fromRow][fromCol].getPieceType() == RED && toRow > fromRow) {
            return false;  // Red pieces (not a king) can only move South.
        } else return gameBoard[fromRow][fromCol].getPieceType() != BLACK || toRow >= fromRow;

    }
}
