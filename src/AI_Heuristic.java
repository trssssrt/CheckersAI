import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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

    private int DEPTH,
            DEPTH_DIFFICULTY_FACTOR = 4;// = 16;//10;

    private List<Move> successorEvaluations;
    private Move bestMove;

    private int NORMAL_PIECE = 100,
            KING = 175,
            CORNER_KING = 25,
            NORMAL_PIECE_ROW_VALUE = 10,// Encourages Pieces to move forward
            PROTECTED_PIECE_VALUE = 5,// Encourages Pieces to be protected
            POSSIBLE_JUMP_VALUE = 2,// Encourages pieces to move to jump locations
            ADVANCED_DISTANCE_VALUE = 1 / 2;


    AI_Heuristic(int computerPlayerID, int difficulty, Piece[][] board, int numRowsAndColumns) {
        this.computerPlayerID = computerPlayerID;
        this.difficulty = difficulty;
        this.gameBoard = deepCopy(board);
        this.numRowsAndColumns = numRowsAndColumns;
        this.successorEvaluations = new ArrayList();

        this.DEPTH = difficulty * DEPTH_DIFFICULTY_FACTOR;
    }

    public int getComputerPlayerID() {
        return computerPlayerID;
    }

    Move getBestMove() {
        System.out.println("DIFFICULTY: " + difficulty);
        if (difficulty > 1) {
            this.successorEvaluations = new ArrayList<>();
//            System.out.println("--Player ID: " + computerPlayerID);
            int a = this.negamaxAB(gameBoard, DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, computerPlayerID);
//            System.out.println("BEST MOVE: " + bestMove);
            System.out.println("Best Move SCORE: " + a
                    + " (" + bestMove.fromRow + ", " + bestMove.fromCol +
                    ") -> (" + bestMove.toRow + ", " + bestMove.toCol + ")");
            return bestMove;
        }

        Move[] legalMoves = getLegalMoves(gameBoard, computerPlayerID);
        return legalMoves[(int) (legalMoves.length * Math.random())];


    }

    // Remember 2 ply = 1 move
    // To speed up negamax we need to change the board. Here, we have a 2D object array.
    //      If we converted the board to bits then we can implement this much faster
    // To change the algorithm to N players we would need to
    //      Change the Switch Players section, which would depend on the particular
    //      game's internal structure (i.e. does 1->2->3? Or can 1->3->1->2? etc.)
    private int negamaxAB(Piece[][] board, int depth, int alpha, int beta, int playerID) {
        // Switch Players if not on first iteration
        if (DEPTH != depth) {
//            playerID = playerID == RED ? BLACK : RED;
            if (playerID == RED) {
                playerID = BLACK;
            } else {
                playerID = RED;
            }
        }
        Move[] legalMoveList = getLegalMoves(board, playerID);
        // IF we reach the end
        if (depth == 0) {
//        || legalMoveList == null) {
//            System.out.println(computerPlayerID + " -- " + playerID);
            if (playerID != computerPlayerID) {
                System.out.println("HELP");
            }
            return evaluateHeuristic(board, playerID);
        }

        int bestValue = Integer.MIN_VALUE;

        if (legalMoveList != null) {
            for (Move move : legalMoveList) {
                // Create deep copy of board
                Piece[][] newBoard = deepCopy(board);
                makeMove(newBoard, move.fromRow, move.fromCol, move.toRow, move.toCol);

                int val = -negamaxAB(newBoard, depth - 1, -beta, -alpha, playerID);
                bestValue = Math.max(bestValue, val);
                if (val >= alpha && depth == DEPTH) {
                    bestMove = move;
                }
                alpha = Math.max(alpha, val);
                if (alpha >= beta) {
                    if (depth == DEPTH) {
                        bestMove = move;
                    }
                    break;
                }
            }
        }
        return bestValue;
    }

    private int evaluateHeuristic(Piece[][] board, int playerID) {
        return simpleScore(board, playerID)
                + simpleDistanceScore(board, playerID)
//                + advancedDistanceScore(board, playerID, 2) * ADVANCED_DISTANCE_VALUE // Causes Issues
                + trappedKingScore(board, playerID) * NORMAL_PIECE_ROW_VALUE
                + protectedPieceScore(board, playerID) * PROTECTED_PIECE_VALUE
                + possibleJumpsScore(board, playerID) * POSSIBLE_JUMP_VALUE;
//        if (difficulty == 1) {
//            return simpleScore(board, playerID);
//        } else if (difficulty == 2) {
//            return simpleScore(board, playerID) + simpleDistanceScore(board, playerID);
//        } else if (difficulty == 3) {
//            return simpleScore(board, playerID)
//            + simpleDistanceScore(board, playerID)
//                    + trappedKingScore(board, playerID) * NORMAL_PIECE_ROW_VALUE;
//        } else {
//            return simpleScore(board, playerID)
//            + simpleDistanceScore(board, playerID)
//                    + trappedKingScore(board, playerID) * NORMAL_PIECE_ROW_VALUE
//                + possibleJumpsScore(board, playerID) * POSSIBLE_JUMP_VALUE;
//        }
    }

    /**
     * simpleScore does nothing more than count the
     * current number of pieces (and relative values)
     * and subtract each player's pieces' values.
     *
     * @param board  Current Board State
     * @param player Current player ID
     * @return The difference is piece values
     */
    private int simpleScore(Piece[][] board, int player) {
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
        return player == RED ? red - black : black - red;
    }

    /**
     * simpleDistanceScore encourages the non-King pieces to
     * move towards their respective ends
     *
     * @param board  Current Board State
     * @param player Current player ID
     * @return The difference between how close pieces are to the back of the board.
     */
    private int simpleDistanceScore(Piece[][] board, int player) {
        int black = 0, red = 0;
        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
                if (board[row][col].getPieceType() == RED && !board[row][col].isKing()) {
                    red += row;
//                    red += (numRowsAndColumns - row);
                } else if (board[row][col].getPieceType() == BLACK && !board[row][col].isKing()) {
                    black += ((numRowsAndColumns - 1) - row); // Subtract 1 because there are numRowsAndColumns - 1 Rows
//                    black += row;
                }
            }
        }
        return player == RED ? red - black : black - red;
    }

    /**
     * advancedDistanceScore encourages
     * pieces to 'attack' or 'flee'.
     * Measures and averages the
     * distance between EVERY
     * king to EVERY opponent's piece
     *
     * @param board  Current Board State
     * @param player Current player ID
     * @return The difference between how close pieces are to the back of the board.
     */
    private int advancedDistanceScore(Piece[][] board, int player, int norm) {
        int black = 0, red = 0;

        Vector<String> redKingsCoord = new Vector<>(),
                blackKingsCoord = new Vector<>(),
                redNormalCoord = new Vector<>(),
                blackNormalCoord = new Vector<>();
        Vector<Integer> redNorm = new Vector<>(),
                blackNorm = new Vector<>();

        // Find Piece locations
        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
                if (board[row][col].getPieceType() == RED_KING && board[row][col].isKing()) {
                    redKingsCoord.add("" + row + "," + col);
//                    red += (numRowsAndColumns - row);
                } else if (board[row][col].getPieceType() == BLACK_KING && board[row][col].isKing()) {
                    blackKingsCoord.add("" + row + "," + col);
                } else if (board[row][col].getPieceType() == RED && !board[row][col].isKing()) {
                    redNormalCoord.add("" + row + "," + col);
                } else if (board[row][col].getPieceType() == BLACK && !board[row][col].isKing()) {
                    blackNormalCoord.add("" + row + "," + col);
                }
            }
        }
        for (String king : redKingsCoord) {
            int sum = 0;

            String s[] = king.split(",");
            int row = 0, col = 0;
            for (int i = 0; i < s.length; i += 2) {
                row = Integer.parseInt(s[i + 1]);
                col = Integer.parseInt((s[i]));
            }
            for (String normal : blackNormalCoord) {
                String sN[] = normal.split(",");
                for (int i = 0; i < sN.length; i += 2) {
                    int rowN = Integer.parseInt(sN[i + 1]);
                    int colN = Integer.parseInt((sN[i]));
                    sum += Math.pow(row - rowN, norm);
                    sum += Math.pow(col - colN, norm);
                }
                redNorm.add(sum ^ (1 / norm));
            }
        }


        for (String king : blackKingsCoord) {
            int sum = 0;

            String s[] = king.split(",");
            int row = 0, col = 0;
            for (int i = 0; i < s.length; i += 2) {
                row = Integer.parseInt(s[i + 1]);
                col = Integer.parseInt((s[i]));
            }
            for (String normal : redNormalCoord) {
                String sN[] = normal.split(",");
                for (int i = 0; i < sN.length; i += 2) {
                    int rowN = Integer.parseInt(sN[i + 1]);
                    int colN = Integer.parseInt((sN[i]));
                    sum += Math.pow(row - rowN, norm);
                    sum += Math.pow(col - colN, norm);
                }
                blackNorm.add(sum ^ (1 / norm));
            }
        }

        // Get average distance
        if (redNorm.size() != 0) {
            for (int i = 0; i < redNorm.size(); i++) {
                red += redNorm.get(i);
            }
            red /= redNorm.size();
        }
        // Get average distance
        if (blackNorm.size() != 0) {
            for (int i = 0; i < blackNorm.size(); i++) {
                black += blackNorm.get(i);
            }
            black /= blackNorm.size();
        }

        return player == RED ? red - black : black - red;
    }

    /**
     * trappedKingScore punishes player if a
     * king is in the corner (it could get
     * trapped)
     *
     * @param board  Current Board State
     * @param player Current player ID
     * @return
     */
    private int trappedKingScore(Piece[][] board, int player) {
        int black = 0, red = 0;
        for (int row = 0; row < numRowsAndColumns; row++) {
            if (board[row][0].getPieceType() != EMPTY && board[row][0].isKing()) {
                if (board[row][0].getPieceType() == RED_KING) {
                    red -= CORNER_KING;
                } else if (board[row][0].getPieceType() == BLACK_KING) {
                    black -= CORNER_KING;
                }
            } else if (board[row][numRowsAndColumns - 1].getPieceType() != EMPTY && board[row][numRowsAndColumns - 1].isKing()) {
                if (board[row][numRowsAndColumns - 1].getPieceType() == RED_KING) {
                    red -= CORNER_KING;
                } else if (board[row][numRowsAndColumns - 1].getPieceType() == BLACK_KING) {
                    black -= CORNER_KING;
                }
            }
        }
        return player == RED ? red - black : black - red;
    }

    /**
     * protectedPieceScore encourages
     * players to protect their pieces
     *
     * @param board  Current Board State
     * @param player Current player ID
     * @return Returns difference in protected pieces
     */
    private int protectedPieceScore(Piece[][] board, int player) {
        int black = 0, red = 0;

        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
                if (board[row][col].getPieceType() == RED && !board[row][col].isKing()) {
                    if (isOnBoard(row + 1, col + 1)) {
                        if (board[row + 1][col + 1].getPieceType() == RED && !board[row + 1][col + 1].isKing()) {
                            red++;
                        }
                    }

                    if (isOnBoard(row - 1, col + 1)) {
                        if (board[row - 1][col + 1].getPieceType() == RED && !board[row - 1][col + 1].isKing()) {
                            red++;
                        }
                    }

                    if (isOnBoard(row + 1, col - 1)) {
                        if (board[row + 1][col - 1].getPieceType() == RED && !board[row + 1][col - 1].isKing()) {
                            red++;
                        }
                    }

                    if (isOnBoard(row - 1, col - 1)) {
                        if (board[row - 1][col - 1].getPieceType() == RED && !board[row - 1][col - 1].isKing()) {
                            red++;
                        }
                    }
                } else if (board[row][col].getPieceType() == BLACK && !board[row][col].isKing()) {
                    if (isOnBoard(row + 1, col + 1)) {
                        if (board[row + 1][col + 1].getPieceType() == BLACK && !board[row + 1][col + 1].isKing()) {
                            black++;
                        }
                    }

                    if (isOnBoard(row - 1, col + 1)) {
                        if (board[row - 1][col + 1].getPieceType() == BLACK && !board[row - 1][col + 1].isKing()) {
                            black++;
                        }
                    }

                    if (isOnBoard(row + 1, col - 1)) {
                        if (board[row + 1][col - 1].getPieceType() == BLACK && !board[row + 1][col - 1].isKing()) {
                            black++;
                        }
                    }

                    if (isOnBoard(row - 1, col - 1)) {
                        if (board[row - 1][col - 1].getPieceType() == BLACK && !board[row - 1][col - 1].isKing()) {
                            black++;
                        }
                    }
                }
            }
        }
        return player == RED ? red - black : black - red;
    }

    /**
     * possibleJumpsScore encourages
     * players to move to/from
     * jump locations
     *
     * @param board  Current Board State
     * @param player Current player ID
     * @return Returns difference in possible jumps
     */
    private int possibleJumpsScore(Piece[][] board, int player) {
        int black = 0, red = 0;

        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {

                // Check if piece is current player's
                if (board[row][col].getPieceType() == RED || board[row][col].getPieceType() == RED_KING) {

                    // Check if player can jump Northeast
                    if (isLegalJump(board, RED, row, col, row + 1, col + 1, row + 2, col + 2)) {
                        red++;
                    }

                    // Check if player can jump Northwest
                    if (isLegalJump(board, RED, row, col, row - 1, col + 1, row - 2, col + 2)) {
                        red++;
                    }

                    // Check if player can jump Southeast
                    if (isLegalJump(board, RED, row, col, row + 1, col - 1, row + 2, col - 2)) {
                        red++;
                    }

                    // Check if player can jump Southwest
                    if (isLegalJump(board, RED, row, col, row - 1, col - 1, row - 2, col - 2)) {
                        red++;
                    }
                }

                // Check if piece is current player's
                if (board[row][col].getPieceType() == BLACK || board[row][col].getPieceType() == BLACK_KING) {

                    // Check if player can jump Northeast
                    if (isLegalJump(board, BLACK, row, col, row + 1, col + 1, row + 2, col + 2)) {
                        black++;
                    }

                    // Check if player can jump Northwest
                    if (isLegalJump(board, BLACK, row, col, row - 1, col + 1, row - 2, col + 2)) {
                        black++;
                    }

                    // Check if player can jump Southeast
                    if (isLegalJump(board, BLACK, row, col, row + 1, col - 1, row + 2, col - 2)) {
                        black++;
                    }

                    // Check if player can jump Southwest
                    if (isLegalJump(board, BLACK, row, col, row - 1, col - 1, row - 2, col - 2)) {
                        black++;
                    }
                }
            }
        }
        return player == RED ? red - black : black - red;
    }

    /**
     * @param row Board row
     * @param col Board column
     * @return Is (row,col) on the board?
     */
    private boolean isOnBoard(int row, int col) {
        // Check if jump is on the board
        if (row < 0 || row >= numRowsAndColumns || col < 0 || col >= numRowsAndColumns) {
            return false;
        } else return true;
    }

    /**
     * @param localBoard Update gameBoard with a new board
     */
    void updateGameBoard(Piece[][] localBoard) {
        this.gameBoard = deepCopy(localBoard);
    }

    /**
     * While deepCopy creates a full clone of the board,
     * it is actually an inefficient technique.
     * A better method would be to make a move and then
     * unmake said move.
     *
     * @param localBoard The board that will be copied
     * @return A deep copyied version of the localBoard
     */
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
    private void makeMove(Piece[][] gameBoard, int fromRow, int fromCol, int toRow, int toCol) {
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
     * Northwest           North (illegal Move)            Northeast
     * \                  |                      /
     * \                 |                     /
     * West (illegal Move) -------   Player's Game Piece     ------- East (illegal Move)
     * /                 |                     \
     * /                  |                      \
     * Southwest           South (illegal Move)            Southeast
     */
    private Move[] getLegalMoves(Piece[][] gameBoard, int playerID) {
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
                if (gameBoard[row][col].getPieceType() == playerID || gameBoard[row][col].getPieceType() == playerKingID) {

                    // Check if player can jump Northeast
                    if (isLegalJump(gameBoard, playerID, row, col, row + 1, col + 1, row + 2, col + 2)) {
                        moves.add(new Move(row, col, row + 2, col + 2));
                    }

                    // Check if player can jump Northwest
                    if (isLegalJump(gameBoard, playerID, row, col, row - 1, col + 1, row - 2, col + 2)) {
                        moves.add(new Move(row, col, row - 2, col + 2));
                    }

                    // Check if player can jump Southeast
                    if (isLegalJump(gameBoard, playerID, row, col, row + 1, col - 1, row + 2, col - 2)) {
                        moves.add(new Move(row, col, row + 2, col - 2));
                    }

                    // Check if player can jump Southwest
                    if (isLegalJump(gameBoard, playerID, row, col, row - 1, col - 1, row - 2, col - 2)) {
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
                    if (gameBoard[row][col].getPieceType() == playerID || gameBoard[row][col].getPieceType() == playerKingID) {
                        // Diagonal to the Northeast
                        if (isLegalMove(gameBoard, playerID, row, col, row + 1, col + 1)) {
                            moves.add(new Move(row, col, row + 1, col + 1));
                        }
                        // Diagonal to the Southeast
                        if (isLegalMove(gameBoard, playerID, row, col, row - 1, col + 1)) {
                            moves.add(new Move(row, col, row - 1, col + 1));
                        }
                        // Diagonal to the Northwest
                        if (isLegalMove(gameBoard, playerID, row, col, row + 1, col - 1)) {
                            moves.add(new Move(row, col, row + 1, col - 1));
                        }
                        // Diagonal to the Southwest
                        if (isLegalMove(gameBoard, playerID, row, col, row - 1, col - 1)) {
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
