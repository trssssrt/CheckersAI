import java.util.ArrayList;
import java.util.HashMap;

/**
 * Here we employ a
 * Heuristic
 * AND the
 * Negamax Algorithm
 * with Alpha Beta
 * Pruning
 */
public class AI_Heuristic {
    private int difficulty;
    private Piece[][] gameBoard;
    private int numRowsAndColumns;
    private int computerPlayerID;
    private int EMPTY, RED, RED_KING, BLACK, BLACK_KING;


    private HashMap<String, Piece> gameBoardHashMap;

    // Piece Accomplishment Values
    private final int NORMAL_PIECE_VALUE = 10,
            KING_VALUE = 30,
            REACHED_BACK_ROW_AS_NORMAL_PIECE = 10,
            CAPTURE_NORMAL_PIECE = 50,
            CAPTURE_KING = 100,
            BECOME_KING = 90,
            LOSE_NORMAL_PIECE = -CAPTURE_NORMAL_PIECE,
            LOSE_KING = -CAPTURE_KING;

    AI_Heuristic(int computerPlayerID, int difficulty, Piece[][] gameBoard, int numRowsAndColumns, int EMPTY, int RED, int RED_KING, int BLACK, int BLACK_KING) {
        this.computerPlayerID = computerPlayerID;
        this.difficulty = difficulty;
        this.gameBoard = gameBoard;
        this.numRowsAndColumns = numRowsAndColumns;
        this.EMPTY = EMPTY;
        this.RED = RED;
        this.RED_KING = RED_KING;
        this.BLACK = BLACK;
        this.BLACK_KING = BLACK_KING;

        gameBoardHashMap = new HashMap<>();
        boardToHashMap(gameBoard);
        System.out.println(gameBoardHashMap);
    }

    private void boardToHashMap(Piece[][] board) {
        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
                if (board[row][col].getPieceType() != EMPTY) {
                    gameBoardHashMap.put("(" + row + "," + col + ")", board[row][col]);
//                    gameBoardHashMap.put(intsToString(row, col), board[row][col]);
                }
            }
        }

    }

    /**
     *
     * @param s1 row
     * @param s2 column
     * @return reutrn (s1,s2) as a string
     */
    private String intsToString(int s1, int s2) {
        return "(" + s1 + "," + s2 + ")";
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    void setGameBoard(Piece[][] gameBoard) {
        // We clone that way we can modify without
        // returning the board to it's original configuration
        this.gameBoard = gameBoard.clone();
    }

    AI_Move selectMove() {
        System.out.println("Computer ID: " + computerPlayerID);
        AI_Move[] legalMoves = getLegalMoves(computerPlayerID, null);
        negamaxAB(legalMoves[0], 1, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
        if (difficulty > 1) {
            System.out.println("CURRENT DIFFICULTY: " + difficulty);
            return legalMoves[0];
        } else {
            return legalMoves[(int) (Math.random() * legalMoves.length)];
        }
    }

    /**
     * @param aiMove   A legal move available to the player
     * @param depth    How far down should he algorithm search?
     * @param alpha    The minimum score that the maximizing player is assured of
     * @param beta     The maximum score that the minimizing player is assured of
     * @param playerID This is either 1 or -1 depending on max/min player respectively
     * @return Heuristic value of board configuration
     * <p>
     * A Sparse Array SHOULD be used in general cases, but here I just copy the board
     * <p>
     * OR alternatively, since I can't find sparse array documentation
     * <p>
     * make a dictionary where it maps coordinates to pieces and
     * if your coordinate isn't in the dictionary then nothing is there
     */
    private int negamaxAB(AI_Move aiMove, int depth, int alpha, int beta, int playerID) {
        AI_Move[] legalMoves = getLegalMoves(whoIs(playerID), aiMove);

        if (depth == 0 //|| aiMove.parent == null // If we arrived at the bottom, are only looking 1 level deep,
                || legalMoves.length == 0) {// or if it is a game Over
            return 0;
//            return evaluate_Heuristic(aiMove, localBoard, playerID); // -color * (Heuristic value of aiMove)
        }
        int bestValue = Integer.MIN_VALUE;
        for (AI_Move child : legalMoves) {
//            Piece[][] childGameBoard = gameBoard.clone();
//            fakeMove
            int val = -negamaxAB(child, depth - 1, -beta, -alpha, -playerID);
//            undoFakeMove
            bestValue = Math.max(bestValue, val);
            alpha = Math.max(alpha, val);
            if (alpha >= beta) {
                break;
            }
        }
        return 0; // Temporarily return legalMoves[1] just to keep this working
    }

    //1234567890 Include game log with messages. Make game well rounded. Make code look like I didn’t reference anything

    private int evaluate_Heuristic(AI_Move move, Piece[][] localBoard, int minOrMaxPlayer) {
        return 0;

        // simplest scoring
        /**
         * score = materialWeight * (numWhitePieces - numBlackPieces) * who2move
         * where who2move = 1 for red (player 1), and who2move = -1 for black (player 2).
         *
         * NORMAL_PIECE_VALUE
         * KING_VALUE
         */
//        switch (difficulty) {
//            case 0:
//                int[] pC = countPieces(localBoard);
//                return minOrMaxPlayer * (
//                        pC[0] * NORMAL_PIECE_VALUE +
//                                pC[1] * KING_VALUE -
//                                pC[2] * NORMAL_PIECE_VALUE -
//                                pC[3] * KING_VALUE
//                );
//            break;
//        }
    }

    private int[] countPieces(Piece[][] localBoard) {
        // pieceArray counts the number of game pieces in the following way
        // {RED, RED_KING, BLACK, BLACK_KING}
        int[] pieceArray = {0, 0, 0, 0};
        for (Piece[] boardRow : localBoard) {
            for (Piece piece : boardRow) {
                if (piece.getPieceType() == RED) {
                    pieceArray[0] += 1;
                } else if (piece.getPieceType() == RED_KING) {
                    pieceArray[1] += 1;
                } else if (piece.getPieceType() == BLACK) {
                    pieceArray[2] += 1;
                } else if (piece.getPieceType() == BLACK_KING) {
                    pieceArray[3] += 1;
                }
            }
        }
        return pieceArray;
    }

    /**
     * whoIs accepts an ID and determines the player's ID
     * whether it be RED || BLACK or 1 || -1
     * (the player's Game ID or if they are the max/min player)
     *
     * @param ID 1 or -1, OR RED or BLACK
     * @return The ID of the player (RED or BLACK)
     * OR minimizing/maximizing player's ID 1 or -1
     */
    private int whoIs(int ID) {
        if (Math.abs(ID) == 1) {
            int otherID = computerPlayerID != RED ? RED : BLACK;
            return ID > 0 ? computerPlayerID : otherID;
        } else {
            return computerPlayerID == ID ? 1 : -1;
        }
    }

    void getBestMove() {//!@#$%^&*() THIS WILL NEED TO RETURN MOVE

    }


    // I COPIED THE MOVES
    // FROM CheckersData
    // SO THAT I CAN
    // CONSTRUCT THE
    // HEURISTIC


    /**
     * This updates the gameBoard array once the player moves a piece
     * If the player's piece arrives at the end of the board we 'king' it.
     *
     * @param fromRow Row from which the Player moves
     * @param fromCol Column from which the Player moves
     * @param toRow   Row to which the Player moves
     * @param toCol   Column to which the Player moves
     */
    private void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        boolean isKing = false; //!@#$%^&*() Only used for testing. Triggers print when a piece becomes a king
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
     * Northwest           North (illegal AI_Move)            Northeast
     * \                  |                      /
     * \                 |                     /
     * West (illegal AI_Move) -------   Player's Game Piece     ------- East (illegal AI_Move)
     * /                 |                     \
     * /                  |                      \
     * Southwest           South (illegal AI_Move)            Southeast
     */
    private AI_Move[] getLegalMoves(int playerID, AI_Move parent) {
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
                    // if (gameBoardHashMap.get(intsToString(row,col)) != null)
                    //if (gameBoardHashMap.get(intsToString(row,col)).getPieceType() == playerID || gameBoardHashMap.get(intsToString(row,col)).getPieceType() == playerKingID) {

                    // Check if player can jump Northeast
                    if (isLegalJump(playerID, row, col, row + 1, col + 1, row + 2, col + 2)) {
                        moves.add(new AI_Move(row, col, row + 2, col + 2, parent));
                    }

                    // Check if player can jump Northwest
                    if (isLegalJump(playerID, row, col, row - 1, col + 1, row - 2, col + 2)) {
                        moves.add(new AI_Move(row, col, row - 2, col + 2, parent));
                    }

                    // Check if player can jump Southeast
                    if (isLegalJump(playerID, row, col, row + 1, col - 1, row + 2, col - 2)) {
                        moves.add(new AI_Move(row, col, row + 2, col - 2, parent));
                    }

                    // Check if player can jump Southwest
                    if (isLegalJump(playerID, row, col, row - 1, col - 1, row - 2, col - 2)) {
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
                        // if (gameBoardHashMap.get(intsToString(row,col)) != null)
                        //if (gameBoardHashMap.get(intsToString(row,col)).getPieceType() == playerID || gameBoardHashMap.get(intsToString(row,col)).getPieceType() == playerKingID) {
                        // Diagonal to the Northeast
                        if (isLegalMove(playerID, row, col, row + 1, col + 1)) {
                            moves.add(new AI_Move(row, col, row + 1, col + 1, parent));
                        }
                        // Diagonal to the Southeast
                        if (isLegalMove(playerID, row, col, row - 1, col + 1)) {
                            moves.add(new AI_Move(row, col, row - 1, col + 1, parent));
                        }
                        // Diagonal to the Northwest
                        if (isLegalMove(playerID, row, col, row + 1, col - 1)) {
                            moves.add(new AI_Move(row, col, row + 1, col - 1, parent));
                        }
                        // Diagonal to the Southwest
                        if (isLegalMove(playerID, row, col, row - 1, col - 1)) {
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
            return moves.toArray(new AI_Move[moves.size()]); // Convert AI_Move List to AI_Move Array
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
    private AI_Move[] getLegalJumpsFromPosition(int playerID, int currentRow, int currentCol) {
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
        ArrayList<AI_Move> moves = new ArrayList<>();

        // Check if current location is the player's piece
        if (gameBoard[currentRow][currentCol].getPieceType() == playerID || gameBoard[currentRow][currentCol].getPieceType() == playerKingID) {

            // Check if there is a legal jump to the Northeast
            if (isLegalJump(playerID, currentRow, currentCol, currentRow + 1, currentCol + 1, currentRow + 2, currentCol + 2)) {
                moves.add(new AI_Move(currentRow, currentCol, currentRow + 2, currentCol + 2));
            }

            // Check if there is a legal jump to the Southeast
            if (isLegalJump(playerID, currentRow, currentCol, currentRow - 1, currentCol + 1, currentRow - 2, currentCol + 2)) {
                moves.add(new AI_Move(currentRow, currentCol, currentRow - 2, currentCol + 2));
            }

            // Check if there is a legal jump to the Northwest
            if (isLegalJump(playerID, currentRow, currentCol, currentRow + 1, currentCol - 1, currentRow + 2, currentCol - 2)) {
                moves.add(new AI_Move(currentRow, currentCol, currentRow + 2, currentCol - 2));
            }

            // Check if there is a legal jump to the Southwest
            if (isLegalJump(playerID, currentRow, currentCol, currentRow - 1, currentCol - 1, currentRow - 2, currentCol - 2)) {
                moves.add(new AI_Move(currentRow, currentCol, currentRow - 2, currentCol - 2));
            }
        }

        // If there are no jumps return null, otherwise return moves as an array
        if (moves.size() == 0) {
            return null;
        } else {//!@#$%^&*() Why can't we just return moves? -- Because they are a different type
            return moves.toArray(new AI_Move[moves.size()]);
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
//        if (gameBoard[jumpRow][jumpCol].getPieceType() == player
//                || (
//                         gameBoard[jumpRow][jumpCol].getPieceType() == player
//                                &&
//                        gameBoard[jumpRow][jumpCol].isKing()
//                )) {
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
        if (gameBoard[toRow][toCol].getPieceType() != EMPTY) {
            return false;
        }

        // Check if piece can legally move up or down
        if (player == RED && gameBoard[fromRow][fromCol].getPieceType() == RED && toRow > fromRow) {
            return false;  // Red pieces (not a king) can only move South.
        } else return gameBoard[fromRow][fromCol].getPieceType() != BLACK || toRow >= fromRow;

    }
}
