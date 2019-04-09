import java.util.ArrayList;
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
            DEPTH_DIFFICULTY_FACTOR = 4;

    private Move bestMove;
    private int bestMoveCosts = 0;

    private int RMIN = Constants.RMIN, RMAX = Constants.RMAX;// Variables determining Random changes in Heuristic

    private int PAWN_PIECE = 500,
            KING = PAWN_PIECE * 5 / 3,
            MOVABLE_PAWN = PAWN_PIECE * 5 / 6,
            MOVABLE_KING = KING * 5 / 6,
            TRAPPED_CORNER_KING_VALUE = -KING * 2 / 3,// Encourages Pieces to move forward
            PROTECTED_PIECE_VALUE = 40,// Encourages Pieces to be protected
            POSSIBLE_JUMP_VALUE = 0,// Encourages pieces to move to jump locations
            SIMPLE_DISTANCE_VALUE = 0,
            ADVANCED_DISTANCE_VALUE = 5;
    private int PAWN_PIECE_ROW_VALUE = PAWN_PIECE / 100,
            KING_PIECE_ROW_VALUE = KING / 100;

    private final int[] stage = {0, 1, 3}; // 3 Stages: 0 - Beginning, 1 - Middle, 2 - End


    AI_Heuristic(int computerPlayerID, int difficulty, Piece[][] board, int numRowsAndColumns) {
        this.computerPlayerID = computerPlayerID;
        this.difficulty = difficulty;
        this.gameBoard = deepCopy(board);
        this.numRowsAndColumns = numRowsAndColumns;

        this.DEPTH = difficulty * DEPTH_DIFFICULTY_FACTOR;
    }

    public int getComputerPlayerID() {
        return computerPlayerID;
    }

    Move getBestMove() {
//        System.out.println("DIFFICULTY: " + difficulty);
        if (difficulty > 1) {
//            System.out.println("--Player ID: " + computerPlayerID);
            int a = this.negamaxAB(gameBoard, DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, computerPlayerID);
//            System.out.println("BEST MOVE: " + bestMove);
            bestMoveCosts += a;
            System.out.println("Best Move SCORE For " + computerPlayerID + ": " + a
                    + " (" + bestMove.fromRow + ", " + bestMove.fromCol +
                    ") -> (" + bestMove.toRow + ", " + bestMove.toCol + ")" +
                    " Depth: " + DEPTH +
                    " BMC: " + bestMoveCosts);
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
        if (depth == 0) {
            // If there are equal scores, then the game is completely deterministic
            // So, to prevent this, we add a pseudo-random number.
            return evaluateHeuristic(board, playerID) + randomInt(RMIN, RMAX);
        }

        int bestValue = Integer.MIN_VALUE;

        if (legalMoveList != null) {
            for (Move move : legalMoveList) {
                // Create deep copy of board
                Piece[][] newBoard = deepCopy(board);
                makeMove(newBoard, move.fromRow, move.fromCol, move.toRow, move.toCol);

                int val = -negamaxAB(newBoard, depth - 1, -beta, -alpha, playerID);

                // Delete created board to stop excess storage
                // (indicates to garbage collector that this can be removed)
                newBoard = null;

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

    /**
     * @param board    The Current State of the Game Board
     * @param playerID Current Player's Game ID
     * @return Relative Game Board Value
     */
    private int evaluateHeuristic(Piece[][] board, int playerID) {
        if (difficulty == Constants.difficulty_Medium) {
            RMIN = Constants.RMIN;
            RMAX = Constants.RMAX;
            return simpleScore(board, playerID);
        } else if (difficulty == Constants.difficulty_Intermediate) {
            RMIN = Constants.RMIN;
            RMAX = Constants.RMAX;
            int[] tPS = trappedPieceScore(board);
            int isRed = RED == playerID ? 1 : -1;
            return simpleScore(board, playerID)
                    + simpleDistanceScore(board, playerID)
                    + advancedDistanceScore2(board, playerID, 2) * ADVANCED_DISTANCE_VALUE // Causes Issues, Most likely need SMALL scaling factor//!@#$%^&*()
                    + isRed * (tPS[0] - tPS[2]) * PAWN_PIECE_ROW_VALUE
                    + isRed * (tPS[1] - tPS[3]) * KING_PIECE_ROW_VALUE
                    + protectedPieceScore(board, playerID) * PROTECTED_PIECE_VALUE
                    + possibleJumpsScore(board, playerID) * POSSIBLE_JUMP_VALUE;
        } else  /*(difficulty > Constants.difficulty_Intermediate)*/ {
            /**
             * C1-C8 Focus on the difference between player pieces
             * C9-C10 Focus on the current player's value
             */
            int C1 = 0,// Difference Value of Pawn Pieces
                    C2 = 0, // Difference Value of KING Pieces
                    C3 = 0, // Value of Protected Pawn Pieces
                    C4 = 0, // Value of Protected Kings
                    C5 = 0, // Value of movable Pawn Pieces
                    C6 = 0, // Value of movable Kings
                    C7 = 0, // Average Pawn Piece distance to promotion
                    C8 = 0, // Value of opened Promotion Row
                    C9 = 0,  // Piece Value
                    C10 = 0,// King Value
                    C11 = 0,// Mean Distance to Other Pieces
                    C12 = 0,// Difference of pawns 'hiding' on the edge of the board
                    C13 = 0,// Difference of Kings 'hiding' on the edge of the board
                    C14 = -PAWN_PIECE,// Pawn 'hiding' on the edge of the board,
                    C15 = -KING // King 'hiding' on the edge of the board
                            ;


            int[] pieceCount = pieceCount(board);
            int currentStage;
            int beginningPhaseKingCount = 0,
                    beginningPhasePawnCount = 3,
                    middlePhasePawnCount = 3;
            int middlePhaseKingCount = 1;

        /*
          Stage 1: Beginning - Each player has more than 3 pawns & No kings
          Stage 2: Kings - Each player has more than 3 pieces there is at least 1 king
          Stage 3: Ending - One player has at least 3 pieces left
         */
            if (pieceCount[0] > beginningPhasePawnCount
                    && pieceCount[2] > beginningPhasePawnCount
                    && Math.abs(pieceCount[1] + pieceCount[3]) == beginningPhaseKingCount) {

                // Stage 1
                currentStage = stage[0];
                RMIN = -PAWN_PIECE * 1 / 10;
                RMAX = PAWN_PIECE * 1 / 10;
                C1 = PAWN_PIECE * 2 / 7; // Value of difference of Pawn Pieces
                C2 = KING * 2 / 7; // Value of difference of King Pieces
                C3 = PAWN_PIECE * 3 / 4; // Value of difference of Protected Pawns
                C4 = KING * 3 / 4; // Value of difference of Protected Kings
                C5 = PAWN_PIECE * 10 / 13; // Value of difference of movable Pawn Pieces
                C6 = KING * 10 / 13; // Value of difference of movable Kings
                C7 = PAWN_PIECE * 3 / 11; // Value of difference of Average Pawn Piece distance to promotion
                C8 = -PAWN_PIECE * 2 / 19; // Value of difference of opened Promotion Row
                C9 = PAWN_PIECE;  // Piece Value
                C10 = KING; // King Value

            } else if ((pieceCount[0] + pieceCount[1]) > middlePhasePawnCount
                    && (pieceCount[2] + pieceCount[3]) > middlePhasePawnCount
                    && Math.abs(pieceCount[1] + pieceCount[3]) >= middlePhaseKingCount) {

                // Stage 2
                currentStage = stage[1];
                RMIN = -PAWN_PIECE * 2 / 10;
                RMAX = PAWN_PIECE * 2 / 10;
                C1 = PAWN_PIECE * 2 / 5; // Value of difference of Pawn Pieces
                C2 = KING * 2 / 5; // Value of difference of King Pieces
                C3 = PAWN_PIECE * 5 / 8; // Value of difference of Protected Pawns
                C4 = KING * 3 / 8; // Value of difference of Protected Kings
                C5 = 0; // Value of difference of movable Pawn Pieces
                C6 = KING * 2 / 5; // Value of difference of movable Kings
                C7 = PAWN_PIECE * 5 / 7; // Value of difference of Average Pawn Piece distance to promotion
                C8 = PAWN_PIECE * 3 / 7; // Value of difference of opened Promotion Row
                C9 = PAWN_PIECE;  // Piece Value
                C10 = KING; // King Value
                C11 = -KING / 2; // Mean Distance to Other Pieces
                C12 = PAWN_PIECE / 2; // Difference of pawns 'hiding' on the edge of the board
                C13 = -KING / 2; // Difference of Kings 'hiding' on the edge of the board
                C14 = -PAWN_PIECE; // Pawn 'hiding' on the edge of the board,
                C15 = -KING; // King 'hiding' on the edge of the board

            } else {
                // Stage 3
                currentStage = stage[2];
                RMIN = -PAWN_PIECE * 3 / 10;
                RMAX = PAWN_PIECE * 3 / 10;
                C1 = PAWN_PIECE * 2 / 31; // Value of difference of Pawn Pieces
                C2 = KING * 2 / 33; // Value of difference of King Pieces
                C3 = PAWN_PIECE * 1 / 4; // Value of difference of Protected Pawns
                C4 = KING * 1 / 4; // Value of difference of Protected Kings
                C5 = PAWN_PIECE * 2 / 19; // Value of difference of movable Pawn Pieces
                C6 = KING / 3; // Value of difference of movable Kings
                C7 = PAWN_PIECE / 3; // Value of difference of Average Pawn Piece distance to promotion
                C8 = 0; // Value of difference of opened Promotion Row
                C9 = PAWN_PIECE;  // Piece Value
                C10 = KING;// King Value
            }

            // STAGES
            if (currentStage == stage[0]) {
                int[] protectedPieceCount = protectedPiecesScore(board);
                int[] movablePieceCount = movablePieceScore(board);
                int[] promotionRowCount = promotionRowScore(board);
                int[] meanDistanceToPromotionCount = meanDistanceToPromotionScore(board);
                int isRed = playerID == RED ? 1 : -1;
                return isRed * ( // If player is black, multiplying by -1 will reverse each subtraction
                        C1 * (pieceCount[0] - pieceCount[2]) // Difference in Pawn Pieces
                                + C2 * (pieceCount[1] - pieceCount[3])// Difference in King Pieces
                                + C3 * (protectedPieceCount[0] - protectedPieceCount[2]) // Difference in Protected Pawn Pieces
                                + C4 * (protectedPieceCount[1] - protectedPieceCount[3]) // Difference in Protected King Pieces
                                + C5 * (movablePieceCount[0] - movablePieceCount[2]) // Difference in movable Pawn Pieces
                                + C6 * (movablePieceCount[1] - movablePieceCount[3]) // Difference in movable Pawn Pieces
                                + C7 * (meanDistanceToPromotionCount[0] - meanDistanceToPromotionCount[1]) // Difference in 'closeness' to promotional row
                                + C8 * (promotionRowCount[0] - promotionRowCount[1]) // Difference in Respective Open Promotional Row Tiles
                )
                        // if player is Red count Red pieces, otherwise count Black
                        + isRed == 1 ?
                        (
                                C9 * pieceCount[0]
                                        + C10 * pieceCount[1]
                        )
                        :
                        (
                                C9 * pieceCount[2]
                                        + C10 * pieceCount[3]
                        );
            } else if (currentStage == stage[1]) {

                int[] protectedPieceCount = protectedPiecesScore(board);
                int[] movablePieceCount = movablePieceScore(board);
                int[] promotionRowCount = promotionRowScore(board);
                int[] meanDistanceToPromotionCount = meanDistanceToPromotionScore(board);
                int[] meanDistanceCount = meanDistanceScore(board, 2);
                int[] trappedPieceCount = trappedPieceScore(board);
                int isRed = playerID == RED ? 1 : -1;
                return isRed * ( // If player is black, multiplying by -1 will reverse each subtraction
                        C1 * (pieceCount[0] - pieceCount[2]) // Difference in Pawn Pieces
                                + C2 * (pieceCount[1] - pieceCount[3])// Difference in King Pieces
                                + C3 * (protectedPieceCount[0] - protectedPieceCount[2]) // Difference in Protected Pawn Pieces
                                + C4 * (protectedPieceCount[1] - protectedPieceCount[3]) // Difference in Protected King Pieces
                                + C5 * (movablePieceCount[0] - movablePieceCount[2]) // Difference in movable Pawn Pieces
                                + C6 * (movablePieceCount[1] - movablePieceCount[3]) // Difference in movable Pawn Pieces
                                + C7 * (meanDistanceToPromotionCount[0] - meanDistanceToPromotionCount[1]) // Difference in 'closeness' to promotional row
                                + C8 * (promotionRowCount[0] - promotionRowCount[1]) // Difference in Respective Open Promotional Row Tiles
//                            + C11 * (meanDistanceCount[0] - meanDistanceCount[1])
                                + C12 * (trappedPieceCount[0] - trappedPieceCount[2])// Difference in Edge Pawns
                                + C13 * (trappedPieceCount[1] - trappedPieceCount[3]) // Difference in Edge Kings
                )
                        // if player is Red count Red pieces, otherwise count Black
                        + isRed == 1 ?
                        (
                                C9 * pieceCount[0]
                                        + C10 * pieceCount[1]
                                        + C11 * meanDistanceCount[0]
                                        + C14 * trappedPieceCount[0]
                                        + C15 * trappedPieceCount[1]
                        )
                        :
                        (
                                C9 * pieceCount[2]
                                        + C10 * pieceCount[3]
                                        + C11 * meanDistanceCount[1]
                                        + C14 * trappedPieceCount[2]
                                        + C15 * trappedPieceCount[3]
                        );

            } else {

                int[] protectedPieceCount = protectedPiecesScore(board);
                int[] movablePieceCount = movablePieceScore(board);
                int[] promotionRowCount = promotionRowScore(board);
                int[] meanDistanceToPromotionCount = meanDistanceToPromotionScore(board);
                int[] meanDistanceCount = meanDistanceScore(board, 2);
                int[] trappedPieceCount = trappedPieceScore(board);
                int isRed = playerID == RED ? 1 : -1;
                return isRed * ( // If player is black, multiplying by -1 will reverse each subtraction
                        C1 * (pieceCount[0] - pieceCount[2]) // Difference in Pawn Pieces
                                + C2 * (pieceCount[1] - pieceCount[3])// Difference in King Pieces
                                + C3 * (protectedPieceCount[0] - protectedPieceCount[2]) // Difference in Protected Pawn Pieces
                                + C4 * (protectedPieceCount[1] - protectedPieceCount[3]) // Difference in Protected King Pieces
                                + C5 * (movablePieceCount[0] - movablePieceCount[2]) // Difference in movable Pawn Pieces
                                + C6 * (movablePieceCount[1] - movablePieceCount[3]) // Difference in movable Pawn Pieces
                                + C7 * (meanDistanceToPromotionCount[0] - meanDistanceToPromotionCount[1]) // Difference in 'closeness' to promotional row
                                + C8 * (promotionRowCount[0] - promotionRowCount[1]) // Difference in Respective Open Promotional Row Tiles
//                            + C11 * (meanDistanceCount[0] - meanDistanceCount[1])
                                + C12 * (trappedPieceCount[0] - trappedPieceCount[2])// Difference in Edge Pawns
                                + C13 * (trappedPieceCount[1] - trappedPieceCount[3]) // Difference in Edge Kings
                )
                        // if player is Red count Red pieces, otherwise count Black
                        + isRed == 1 ?
                        (
                                C9 * pieceCount[0]
                                        + C10 * pieceCount[1]
                                        + C11 * meanDistanceCount[0]
                                        + C14 * trappedPieceCount[0]
                                        + C15 * trappedPieceCount[1]
                        )
                        :
                        (
                                C9 * pieceCount[2]
                                        + C10 * pieceCount[3]
                                        + C11 * meanDistanceCount[1]
                                        + C14 * trappedPieceCount[2]
                                        + C15 * trappedPieceCount[3]
                        );
            }
        }
    }

    /**
     * @param board Current Board State
     * @return Number of each piece type
     */
    private int[] pieceCount(Piece[][] board) {
        int[] pC = {0, 0, 0, 0}; // Red, RED_KING, BLACK, BLACK_KING

        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
                if (board[row][col].getPieceType() == RED) {
                    pC[0]++;
                } else if (board[row][col].getPieceType() == RED_KING && board[row][col].isKing()) {
                    pC[1]++;
                } else if (board[row][col].getPieceType() == BLACK) {
                    pC[2]++;
                } else if (board[row][col].getPieceType() == BLACK_KING && board[row][col].isKing()) {
                    pC[3]++;
                }
            }
        }
        return pC;
    }

    /**
     * @param board Current Board State
     * @return Number of each protected piece type
     */
    private int[] protectedPiecesScore(Piece[][] board) {
        int[] pieces = {0, 0, 0, 0};// Red, RED_KING, BLACK, BLACK_KING

        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
                if (board[row][col].getPieceType() == RED && !board[row][col].isKing()) {
                    if (isOnBoard(row + 1, col + 1)) {
                        if (board[row + 1][col + 1].getPieceType() == RED && !board[row + 1][col + 1].isKing()) {
                            pieces[0]++;
                        }
                    }

                    if (isOnBoard(row + 1, col - 1)) {
                        if (board[row + 1][col - 1].getPieceType() == RED && !board[row + 1][col - 1].isKing()) {
                            pieces[0]++;
                        }
                    }

                } else if (board[row][col].getPieceType() == BLACK && !board[row][col].isKing()) {
                    if (isOnBoard(row - 1, col + 1)) {
                        if (board[row - 1][col + 1].getPieceType() == BLACK && !board[row - 1][col + 1].isKing()) {
                            pieces[2]++;
                        }
                    }


                    if (isOnBoard(row - 1, col - 1)) {
                        if (board[row - 1][col - 1].getPieceType() == BLACK && !board[row - 1][col - 1].isKing()) {
                            pieces[2]++;
                        }
                    }
                } else if (board[row][col].getPieceType() == RED_KING && board[row][col].isKing()) {
                    if (isOnBoard(row + 1, col + 1)) {
                        if (board[row + 1][col + 1].getPieceType() == RED || board[row + 1][col + 1].getPieceType() == RED_KING) {
                            pieces[1]++;
                        }
                    }


                    if (isOnBoard(row + 1, col - 1)) {
                        if (board[row + 1][col - 1].getPieceType() == RED || board[row + 1][col - 1].getPieceType() == RED_KING) {
                            pieces[1]++;
                        }
                    }

                } else if (board[row][col].getPieceType() == BLACK_KING && board[row][col].isKing()) {

                    if (isOnBoard(row - 1, col + 1)) {
                        if (board[row - 1][col + 1].getPieceType() == BLACK || board[row - 1][col + 1].getPieceType() == BLACK_KING) {
                            pieces[2]++;
                        }
                    }


                    if (isOnBoard(row - 1, col - 1)) {
                        if (board[row - 1][col - 1].getPieceType() == BLACK || board[row - 1][col - 1].getPieceType() == BLACK_KING) {
                            pieces[2]++;
                        }
                    }
                }
            }
        }
        return pieces;
    }


    /**
     * @param board Current Board State
     * @return Number of pieces that can move
     */
    private int[] movablePieceScore(Piece[][] board) {
        int[] pieces = {0, 0, 0, 0};// Red, RED_KING, BLACK, BLACK_KING

        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
                if (board[row][col].getPieceType() == RED && !board[row][col].isKing()) {

                    if (isOnBoard(row - 1, col + 1)) {
                        if (board[row - 1][col + 1].getPieceType() == EMPTY) {
                            pieces[0]++;
                        }
                    }

                    if (isOnBoard(row - 1, col - 1)) {
                        if (board[row - 1][col - 1].getPieceType() == EMPTY) {
                            pieces[0]++;
                        }
                    }
                } else if (board[row][col].getPieceType() == BLACK && !board[row][col].isKing()) {
                    if (isOnBoard(row + 1, col + 1)) {
                        if (board[row + 1][col + 1].getPieceType() == EMPTY) {
                            pieces[2]++;
                        }
                    }

                    if (isOnBoard(row + 1, col - 1)) {
                        if (board[row + 1][col - 1].getPieceType() == EMPTY) {
                            pieces[2]++;
                        }
                    }

                } else if (board[row][col].getPieceType() == RED_KING && board[row][col].isKing()) {
                    if (isOnBoard(row + 1, col + 1)) {
                        if (board[row + 1][col + 1].getPieceType() == EMPTY) {
                            pieces[1]++;
                        }
                    }

                    if (isOnBoard(row - 1, col + 1)) {
                        if (board[row - 1][col + 1].getPieceType() == EMPTY) {
                            pieces[1]++;
                        }
                    }

                    if (isOnBoard(row + 1, col - 1)) {
                        if (board[row + 1][col - 1].getPieceType() == EMPTY) {
                            pieces[1]++;
                        }
                    }

                    if (isOnBoard(row - 1, col - 1)) {
                        if (board[row - 1][col - 1].getPieceType() == EMPTY) {
                            pieces[1]++;
                        }
                    }
                } else if (board[row][col].getPieceType() == BLACK_KING && board[row][col].isKing()) {
                    if (isOnBoard(row + 1, col + 1)) {
                        if (board[row + 1][col + 1].getPieceType() == EMPTY) {
                            pieces[2]++;
                        }
                    }

                    if (isOnBoard(row - 1, col + 1)) {
                        if (board[row - 1][col + 1].getPieceType() == EMPTY) {
                            pieces[2]++;
                        }
                    }

                    if (isOnBoard(row + 1, col - 1)) {
                        if (board[row + 1][col - 1].getPieceType() == EMPTY) {
                            pieces[2]++;
                        }
                    }

                    if (isOnBoard(row - 1, col - 1)) {
                        if (board[row - 1][col - 1].getPieceType() == EMPTY) {
                            pieces[2]++;
                        }
                    }
                }
            }
        }
        return pieces;
    }

    /**
     * @param board Current Board State
     * @return Total Open Promotional Row Tiles
     */
    private int[] promotionRowScore(Piece[][] board) {
        int[] pieces = {0, 0};// Red, BLACK
        for (Piece p : board[numRowsAndColumns - 1]) {
            if (p.getPieceType() == EMPTY) {
                pieces[0]++;
            }
        }
        for (Piece p : board[0]) {
            if (p.getPieceType() == EMPTY) {
                pieces[1]++;
            }
        }

        return pieces;
    }


    /**
     * meanDistanceToPromotionScore
     * calculates the mean distance
     * for Pawn Pieces to
     * promotion line
     *
     * @param board Current Board State
     * @return Mean Piece distance to player's respective promotional row
     */
    private int[] meanDistanceToPromotionScore(Piece[][] board) {
        int[] pieces = {0, 0}; // RED, BLACK
        int black = 0, red = 0;

        // Find Piece locations
        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
                if (board[row][col].getPieceType() == RED && !board[row][col].isKing()) {
                    red++;
                    pieces[0] += (numRowsAndColumns - 1) - row;
                } else if (board[row][col].getPieceType() == BLACK && !board[row][col].isKing()) {
                    black++;
                    pieces[1] += row + 1;
                }
            }
        }

        if (red != 0) {
            pieces[0] /= red;
        }
        if (black != 0) {
            pieces[1] /= black;
        }
        return pieces;
    }

    /**
     * @param board Current Board State
     * @return The measured and averaged distance between kings and other pieces
     */
    private int[] meanDistanceScore(Piece[][] board, int norm) {
        int[] pieces = {0, 0}; // RED_KING, BLACK_KING

        Vector<String> redKingsCoord = new Vector<>(),
                blackKingsCoord = new Vector<>(),
                redPawnCoord = new Vector<>(),
                blackPawnCoord = new Vector<>();
        Vector<Integer> redNorm = new Vector<>(),
                blackNorm = new Vector<>();

        // Find Piece locations
        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {
                if (board[row][col].getPieceType() == RED_KING && board[row][col].isKing()) {
                    redKingsCoord.add("" + row + "," + col);
                }
                if (board[row][col].getPieceType() == BLACK_KING && board[row][col].isKing()) {
                    blackKingsCoord.add("" + row + "," + col);
                }
                if ((board[row][col].getPieceType() == RED && !board[row][col].isKing())
                        ||
                        (board[row][col].getPieceType() == RED_KING && board[row][col].isKing())
                ) {
                    redPawnCoord.add("" + row + "," + col);
                }
                if ((board[row][col].getPieceType() == BLACK && !board[row][col].isKing())
                        ||
                        board[row][col].getPieceType() == BLACK_KING && board[row][col].isKing()) {
                    blackPawnCoord.add("" + row + "," + col);
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
            for (String pawn : blackPawnCoord) {
                String sN[] = pawn.split(",");
                for (int i = 0; i < sN.length; i += 2) {
                    int rowN = Integer.parseInt(sN[i + 1]);
                    int colN = Integer.parseInt((sN[i]));
                    sum += Math.pow(row - rowN, norm);
                    sum += Math.pow(col - colN, norm);
                }
                redNorm.add((int) ((double) Math.pow((double) sum, (double) 1 / norm)));
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
            for (String pawn : redPawnCoord) {
                String sN[] = pawn.split(",");
                for (int i = 0; i < sN.length; i += 2) {
                    int rowN = Integer.parseInt(sN[i + 1]);
                    int colN = Integer.parseInt((sN[i]));
                    sum += Math.pow(row - rowN, norm);
                    sum += Math.pow(col - colN, norm);
                }
                blackNorm.add((int) ((double) Math.pow((double) sum, (double) 1 / norm)));
            }
        }

        // Get average distance
        if (redNorm.size() != 0) {
            for (Integer integer : redNorm) {
                pieces[0] += integer;
            }
            pieces[0] /= redNorm.size();
        }
        // Get average distance
        if (blackNorm.size() != 0) {
            for (Integer integer : blackNorm) {
                pieces[1] += integer;
            }
            pieces[1] /= blackNorm.size();
        }

        return pieces;
    }

    /**
     * @param board Current Board State
     * @return Number of trapped Kings on the Sides
     */
    private int[] trappedPieceScore(Piece[][] board) {
        int[] pieces = {0, 0, 0, 0};//RED, RED_KING, BLACK, BLACK_KING
        for (int i = 0; i < numRowsAndColumns; i++) {
            if (board[i][0].getPieceType() != EMPTY) {
                if (board[i][0].getPieceType() == RED) {
                    pieces[0]++;
                } else if (board[i][0].getPieceType() == RED_KING) {
                    pieces[1]++;
                } else if (board[i][0].getPieceType() == BLACK) {
                    pieces[2]++;
                } else if (board[i][0].getPieceType() == BLACK_KING) {
                    pieces[3]++;
                }
            } else if (board[i][numRowsAndColumns - 1].getPieceType() != EMPTY) {
                if (board[i][numRowsAndColumns - 1].getPieceType() == RED) {
                    pieces[0]++;
                } else if (board[i][numRowsAndColumns - 1].getPieceType() == RED_KING) {
                    pieces[1]++;
                } else if (board[i][numRowsAndColumns - 1].getPieceType() == BLACK) {
                    pieces[2]++;
                } else if (board[i][numRowsAndColumns - 1].getPieceType() == BLACK_KING) {
                    pieces[3]++;
                }
            } else if (board[0][i].getPieceType() != EMPTY) {
                if (board[0][i].getPieceType() == RED) {
                    pieces[0]++;
                } else if (board[0][i].getPieceType() == RED_KING) {
                    pieces[1]++;
                } else if (board[0][i].getPieceType() == BLACK) {
                    pieces[2]++;
                } else if (board[0][i].getPieceType() == BLACK_KING) {
                    pieces[3]++;
                }
            } else if (board[numRowsAndColumns - 1][i].getPieceType() != EMPTY) {
                if (board[numRowsAndColumns - 1][i].getPieceType() == RED) {
                    pieces[0]++;
                } else if (board[numRowsAndColumns - 1][i].getPieceType() == RED_KING) {
                    pieces[1]++;
                } else if (board[numRowsAndColumns - 1][i].getPieceType() == BLACK) {
                    pieces[2]++;
                } else if (board[numRowsAndColumns - 1][i].getPieceType() == BLACK_KING) {
                    pieces[3]++;
                }
            }
        }
        return pieces;
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
        int[] pC = pieceCount(board);
        int black = pC[2] * PAWN_PIECE + pC[3] * KING,
                red = pC[0] * PAWN_PIECE + pC[2] * KING;
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
                newBoard[row][col] = new Piece(
                        localBoard[row][col].getPieceType(),
                        null,
                        localBoard[row][col].isKing());
            }
        }
        return newBoard;
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
    private int advancedDistanceScore2(Piece[][] board, int player, int norm) {
        int black = 0, red = 0;

        Vector<String> redKingsCoord = new Vector<>(),
                blackKingsCoord = new Vector<>(),
                redNormalCoord = new Vector<>(),
                blackNormalCoord = new Vector<>();
        Vector<Double> redNorm = new Vector<>(),
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
                redNorm.add(Math.pow(sum , (double) (1 / norm)));
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
                blackNorm.add(Math.pow(sum , (double) (1 / norm)));
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
     * @param min Minimum Integer
     * @param max Maximum Integer
     * @return Random Integer between min and max
     */
    private int randomInt(int min, int max) {
        return (int) (Math.random() * ((max - min) + 1)) + min;
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
            gameBoard[jumpRow][jumpCol].resetPiece(EMPTY);
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

        ArrayList<Move> moves = new ArrayList<>();  // Moves will be stored in this list (since arrays are immutable).

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
