import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * Create and Paint checker board
 */
class Board extends JPanel implements ActionListener, MouseListener {

    private final CheckersData board = new CheckersData();
    private boolean gameInProgress;
    private int currentPlayer; // Holds Piece Type (RED or BLACK)

    private int selectedRow, selectedCol;  // -1 if Player has not selected Row & Col

    private static Move[] legalMoves;  // Current Player's legal moves
    private static int numRowsAndColumns = Constants.defaultNumRowsAndColumns;
    private int COMPUTER_MOVE_DELAY_IN_MILLISECONDS = Constants.default_COMPUTER_MOVE_DELAY_IN_MILLISECONDS,
            COMPUTER_JUMP_DELAY_IN_MILLISECONDS = Constants.default_COMPUTER_JUMP_DELAY_IN_MILLISECONDS;


    // Variables dealing with game Paints & Graphics
    private static final Color darkColor = Constants.darkColor;
    private static final Color lightColor = Constants.lightColor;
    private static Color gameBlack = Constants.gameBlack,
            gameRed = Constants.gameRed;
    private boolean playerTwoIsBlack = true;
    private static final Color emptyPieceColor = Constants.emptyPieceColor,
            legalMovePieceColor = Constants.legalMovePieceColor,
            legalMoveColor = Constants.legalMoveColor,
            selectedPiece = Constants.selectedPiece,
            selectedPieceLegalMove = Constants.selectedPieceLegalMove;
    private static final float legalMoveBorder = Constants.legalMoveBorder,
            selectedLegalMoveBorder = Constants.selectedLegalMoveBorder,
            selectedPieceBorder = Constants.selectedPieceBorder;
    private static Graphics[][] gameBoardGraphics = new Graphics[numRowsAndColumns][numRowsAndColumns];
    private static final int initialX = Constants.defaultinitialX, initialY = Constants.defaultinitialY;
    private static final int squareSize = Constants.default_squareSize, pieceSize = Constants.default_pieceSize;

    private JLabel message;

    public int computerDifficulty = Constants.defaultGameDifficulty;
    private boolean displayLegalMoveColors; // If True, highlight legal moves for player
    private boolean showGameOverPopUp = false;
    private boolean singleAI = Constants.default_SingleAI;
    private AI_Heuristic computerPlayer, computerPlayer2;


    Board(Color backgroundColor) {
        setBackground(backgroundColor);
        addMouseListener(this);

        message = new JLabel("", JLabel.CENTER);
        message.setFont(new Font("Serif", Font.BOLD, 14));
        message.setForeground(Color.decode("#C8D2C6"));
        add(message, BorderLayout.LINE_START);

        displayLegalMoveColors = false;
        doNewGame();
    }

    /**
     * Respond to user's click on one of the two buttons.
     */
    public void actionPerformed(ActionEvent evt) {
    }

    /**
     * Start a new game
     */
    void performDoNewGame() {
        doNewGame();
    }

    private void doNewGame() {
        if (gameInProgress) {
            message.setText("Cannot start new game if there is one currently in progress!");
            return;
        }
        playerTwoIsBlack();

        board.setUpCheckerBoard(numRowsAndColumns);
        currentPlayer = CheckersData.RED;   // RED moves first.
        legalMoves = board.getLegalMoves(CheckersData.RED);  // Get RED's legal moves.

        selectedRow = -1;   // No pieces are selected at start of game
        if (computerDifficulty != Constants.difficulty_ZERO) {
            String playerText = singleAI ? Constants.computerVsHuman : Constants.computerVsComputer;
            message.setText("<html>" + playerText + "<br/>" + Constants.computerDifficulty_Text + Constants.difficultyLevels[computerDifficulty] + "<br/>" + getCurrentPlayerColor() + ":  Make your move." + "</html>");
        } else {
            message.setText("<html>" + Constants.difficultyLevels[computerDifficulty] + "<br/>" + getCurrentPlayerColor() + ":  Make your move." + "</html>");
        }
        gameInProgress = true;


        if (computerDifficulty != Constants.difficulty_ZERO) {
            if (computerDifficulty == Constants.difficulty_Easy) {
                setDELAYS_TO_DEFAULTS();
            } else {
                setCOMPUTER_MOVE_DELAY_IN_MILLISECONDS(COMPUTER_MOVE_DELAY_IN_MILLISECONDS * computerDifficulty * 4 / 5);
                setCOMPUTER_JUMP_DELAY_IN_MILLISECONDS(COMPUTER_JUMP_DELAY_IN_MILLISECONDS * computerDifficulty * 4 / 5);
            }
            computerPlayer = new AI_Heuristic(
                    CheckersData.BLACK,
                    computerDifficulty,
                    board.gamePieces,
                    numRowsAndColumns);

            if (!singleAI) {
                computerPlayer2 = new AI_Heuristic(
                        CheckersData.RED,
                        computerDifficulty,
                        board.gamePieces,
                        numRowsAndColumns);
                // Delay move to allow user to see computer 'think' by delaying time to computation
                repaint();
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                computerPlayer2.updateGameBoard(board.gamePieces);
                                doMakeMove(computerPlayer2.getBestMove());
                            }
                        },
                        // The First Move Should start being calculated quickly
                        Constants.default_COMPUTER_JUMP_DELAY_IN_MILLISECONDS / 100
                );
            }
        }

        // Update screen
        repaint();
    }

    public void setPlayerTwoIsBlack(boolean playerOneIsBlack) {
        this.playerTwoIsBlack = playerOneIsBlack;
    }

    void playerTwoIsBlack() {
        if (this.playerTwoIsBlack) {
            gameBlack = Constants.gameBlack;
            gameRed = Constants.gameRed;
        } else {
            gameBlack = Constants.gameRed;
            gameRed = Constants.gameBlack;
        }
    }

    /**
     * @return Current Player Color
     */
    private String getCurrentPlayerColor() {
        if (this.playerTwoIsBlack && currentPlayer == CheckersData.RED) {
            return Constants.colorStringMap.get("RED");
        } else if (this.playerTwoIsBlack && currentPlayer == CheckersData.BLACK) {
            return Constants.colorStringMap.get("BLACK");
        } else if (!this.playerTwoIsBlack && currentPlayer == CheckersData.RED) {
            return Constants.colorStringMap.get("BLACK");
        } else {//if (!this.playerTwoIsBlack && currentPlayer == CheckersData.BLACK) {
            return Constants.colorStringMap.get("RED");
        }
    }

    /**
     * If the current player resigns, then the game ends and opponent wins
     */
    void performDoResign() {
        doResign();
    }

    private void doResign() {
        if (!gameInProgress) {
            message.setText("There is no game in progress!");
            return;
        }
        String currentOpponent = getCurrentPlayerColor().equals(Constants.colorStringMap.get("RED")) ? Constants.colorStringMap.get("BLACK") : Constants.colorStringMap.get("RED");
        gameOver(getCurrentPlayerColor() + " resigns.  " + currentOpponent + " wins.");
    }


    /**
     * @param str Message sent to players at the end of the game
     */
    private void gameOver(String str) {
        if (showGameOverPopUp) {
            gameOverPopUp(str);
        }

        message.setText(str);
        gameInProgress = !gameInProgress;
    }


    /**
     * If user selects valid tile, update screen with moves
     * (This is called by mousePressed())
     *
     * @param clickedRow The row the player selects
     * @param clickedCol The column the player selects
     */
    private void doClickTile(int clickedRow, int clickedCol) {
        // If player clicks a row and column that are valid, update selectedRow & selectedCol and update screen
        for (Move legalMove : legalMoves) {
            if (legalMove.fromRow == clickedRow && legalMove.fromCol == clickedCol) {
                selectedRow = clickedRow;
                selectedCol = clickedCol;
                // Update Screen
                repaint();
                return;
            }
        }

        // Inform player that they can only select their pieces
        if (selectedRow < 0) {
            message.setText(
                    getCurrentPlayerColor() + ": " + "Please select piece to move."
            );
            return;
        }

        // When player chooses where to move, move the piece
        for (Move legalMove : legalMoves) {
            if (legalMove.fromRow == selectedRow && legalMove.fromCol == selectedCol
                    && legalMove.toRow == clickedRow && legalMove.toCol == clickedCol) {
                doMakeMove(legalMove);
                return;
            }
        }

        // Inform player that they can only select the highlighted
        message.setText(
                getCurrentPlayerColor() + ": " + "Invalid move. Please select legal move.."
        );
    }


    /**
     * Moves players piece from old Row and Column to new ones.
     * If move is a jump, look for more jumps. If there are
     * more jumps, the player continues his turn.
     *
     * @param move The player's selected move
     */
    private void doMakeMove(Move move) {
        // Only allow AI to Make Moves IF a current game is in Progress (otherwise it will loop until game end)
        if (!gameInProgress) {
            return;
        }

//        if (computerDifficulty != Constants.difficulty_ZERO && currentPlayer == CheckersData.BLACK) {
        if (computerDifficulty != Constants.difficulty_ZERO && isComputerPlayingAndIsItComputersTurn()) {
            repaint();
        }
        board.makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol);

        /* If there is a legal jump, look for more possible jumps
         * starting from the tile onto which the player jumps.
         * Force multiple jumps if possible.
         */

        if (move.isJump()) {
            // Check for double jump (this will continue to get called until there are no more successive jumps)
            legalMoves = board.getLegalJumpsFromPosition(currentPlayer, move.toRow, move.toCol);
            if (legalMoves != null) {

                // AI turn (If there is one)
                if (isComputerPlayingAndIsItComputersTurn()) {
                    // Delay move to allow user to see computer 'think' by delaying time to computation
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    // AI turn (If there is one)
                                    if (singleAI || currentPlayer == computerPlayer.getComputerPlayerID()) {
                                        computerPlayer.updateGameBoard(board.gamePieces);
                                        doMakeMove(computerPlayer.getBestMove());
                                    } else if (currentPlayer == computerPlayer2.getComputerPlayerID()) {
                                        computerPlayer2.updateGameBoard(board.gamePieces);
                                        doMakeMove(computerPlayer2.getBestMove());
                                    }
                                    return;
                                }
                            },
                            COMPUTER_JUMP_DELAY_IN_MILLISECONDS
                    );
                }

                // Enforce Jump Rule
                selectedRow = move.toRow;
                selectedCol = move.toCol;
                // Update board
                repaint();
                return;
            }
        }

        /*
         * When turn ends, change player. (Switch players)
         * End game if there are no more legal moves.
         */
        if (currentPlayer == CheckersData.RED) {
            currentPlayer = CheckersData.BLACK;
            legalMoves = board.getLegalMoves(currentPlayer);
            if (legalMoves == null) {
                // Player Wins Text (With adjustment for player changing color)
                String lastPlayer = !getCurrentPlayerColor().equals(Constants.colorStringMap.get("RED")) ? Constants.colorStringMap.get("RED") : Constants.colorStringMap.get("BLACK");
                gameOver(lastPlayer + " WINS!!!");

            } else if (legalMoves[0].isJump()) {
                message.setText(getCurrentPlayerColor() + ":  You must jump.");
            } else {
                message.setText(getCurrentPlayerColor() + ":  Make your move.");
            }
        } else {
            currentPlayer = CheckersData.RED;
            legalMoves = board.getLegalMoves(currentPlayer);
            if (legalMoves == null) {
                // Player Wins Text (With adjustment for player changing color)
                String lastPlayer = !getCurrentPlayerColor().equals(Constants.colorStringMap.get("RED")) ? Constants.colorStringMap.get("RED") : Constants.colorStringMap.get("BLACK");
                gameOver(lastPlayer + " WINS!!!");

            } else if (legalMoves[0].isJump()) {
                message.setText(getCurrentPlayerColor() + ":   You must jump.");
            } else {
                message.setText(getCurrentPlayerColor() + ":  Make your move.");
            }
        }

        // Player has not selected piece: selectedRow = -1
        selectedRow = -1;

        // Auto select piece if it is the only legal piece to move
        if (legalMoves != null) {
            /* Make sure the board is redrawn in its new state. */
            repaint();

            // AI turn (If there is one)
            if (isComputerPlayingAndIsItComputersTurn()) {
                // Delay move to allow user to see computer 'think' by delaying time to computation
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                if (singleAI || currentPlayer == computerPlayer.getComputerPlayerID()) {
                                    computerPlayer.updateGameBoard(board.gamePieces);
                                    doMakeMove(computerPlayer.getBestMove());
                                } else if (currentPlayer == computerPlayer2.getComputerPlayerID()) {
                                    computerPlayer2.updateGameBoard(board.gamePieces);
                                    doMakeMove(computerPlayer2.getBestMove());
                                }
                                return;
                            }
                        },
                        COMPUTER_MOVE_DELAY_IN_MILLISECONDS
                );
            }
            boolean isOnlyOneLegalPieceToMove = true;
            for (Move legalMove : legalMoves) {
                if (legalMove.fromRow != legalMoves[0].fromRow
                        || legalMove.fromCol != legalMoves[0].fromCol) {
                    isOnlyOneLegalPieceToMove = false;
                    break;
                }
            }
            if (isOnlyOneLegalPieceToMove) {
                selectedRow = legalMoves[0].fromRow;
                selectedCol = legalMoves[0].fromCol;
            }
        }

        /* Make sure the board is redrawn in its new state. */
        repaint();
    }

    /**
     * Determines if we have an AI and if it is the AI's turn
     *
     * @return True if is the computer's turn, False otherwise
     */
    private boolean isComputerPlayingAndIsItComputersTurn() {
        if (computerDifficulty != Constants.difficulty_ZERO) {
            if (singleAI) {
                return currentPlayer == computerPlayer.getComputerPlayerID();
            } else
                return currentPlayer == computerPlayer.getComputerPlayerID() || currentPlayer == computerPlayer2.getComputerPlayerID();
        } else {
            return false;
        }
    }


    /**
     * Draw checkerboard pattern, then the checkers pieces.
     * If we have an active game, highlight legal moves for
     * current player
     * Every time this is called it repaints EVERY part
     * of the game
     */
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);

        int currentX = initialX;
        int currentY = initialY;
        for (int row = 0; row < numRowsAndColumns; row++) {
            for (int col = 0; col < numRowsAndColumns; col++) {

                //Paints the game board
                if (row % 2 == col % 2) {
                    g.setColor(darkColor);
                } else {
                    g.setColor(lightColor);
                }
                g2d.fill(new Rectangle2D.Double(currentX, currentY, squareSize, squareSize));
                gameBoardGraphics[row][col] = g;

                // Check piece type and color it appropriately
                if (board.gamePieces[row][col].getPieceType() == CheckersData.RED
                        || board.gamePieces[row][col].getPieceType() == CheckersData.RED_KING) {
                    g2d.setColor(gameRed);
                } else if (board.gamePieces[row][col].getPieceType() == CheckersData.BLACK
                        || board.gamePieces[row][col].getPieceType() == CheckersData.BLACK_KING) {
                    g2d.setColor(gameBlack);

                } else if (board.gamePieces[row][col].getPieceType() == CheckersData.EMPTY) {
                    g2d.setColor(emptyPieceColor);
                }

                // Draw Ellipse around gamePieces
                if (board.gamePieces[row][col].getPieceType() != CheckersData.EMPTY) {
                    Ellipse2D pieceShape = new Ellipse2D.Double(currentX + initialX / 10.0,
                            currentY + initialX / 10.0, pieceSize, pieceSize);
                    board.gamePieces[row][col].setOval(pieceShape);
                    g2d.fill(pieceShape);
                }

                // If piece is a king, add draw a crown
                if (board.gamePieces[row][col].isKing()) {
                    drawCrown(currentX + squareSize / 2, currentY + squareSize / 2, g2d);
                }
                currentX += squareSize;
            }
            currentY += squareSize;
            currentX = initialX;
        }


        if (gameInProgress && displayLegalMoveColors && !isComputerPlayingAndIsItComputersTurn()) {
            for (Move legalMove : legalMoves) {
                // Add border around tiles to which a player can legally move
                gameBoardGraphics[legalMove.fromRow][legalMove.fromCol].setColor(legalMoveColor);
                gameBoardGraphics[legalMove.fromRow][legalMove.fromCol].drawRect(
                        initialX + squareSize * legalMove.toCol,
                        initialY + squareSize * legalMove.toRow,
                        squareSize,
                        squareSize);

                // Add border around tiles that indicate pieces which can legally move
                gameBoardGraphics[legalMove.fromRow][legalMove.fromCol].setColor(legalMovePieceColor);
                gameBoardGraphics[legalMove.fromRow][legalMove.fromCol].drawRect(
                        initialX + squareSize * legalMove.fromCol,
                        initialY + squareSize * legalMove.fromRow,
                        squareSize,
                        squareSize);

                // Set border on changed tiles
                g2d.setStroke(new BasicStroke(legalMoveBorder));
            }

            // If a piece is selected add add border to piece's tile and another border to legal moves
            if (selectedRow >= 0) {
                // Draw border around selected piece
                gameBoardGraphics[selectedRow][selectedCol].setColor(selectedPiece);
                gameBoardGraphics[selectedRow][selectedCol].drawRect(
                        initialX + squareSize * selectedCol,
                        initialY + squareSize * selectedRow,
                        squareSize,
                        squareSize);

                g2d.setStroke(new BasicStroke(selectedPieceBorder));

                // Add border around the legal moves of selected piece
                for (Move legalMove : legalMoves) {
                    if (legalMove.fromCol == selectedCol && legalMove.fromRow == selectedRow) {
                        gameBoardGraphics[legalMove.fromRow][legalMove.fromCol].setColor(selectedPieceLegalMove);
                        gameBoardGraphics[legalMove.fromRow][legalMove.fromCol].drawRect(
                                initialX + squareSize * legalMove.toCol,
                                initialY + squareSize * legalMove.toRow,
                                squareSize,
                                squareSize);
                        g2d.setStroke(new BasicStroke(selectedLegalMoveBorder));
                    }
                }
            }
        }
    }

    /**
     * Draws a crown for a piece at the specified position.
     *
     * @param row The location of the center of the piece at the appropriate row
     * @param col The location of the center of the piece at the appropriate column
     * @param g   The Graphics2D with which to draw.
     */
    private void drawCrown(int row, int col, Graphics2D g, Color c) {
        g.setColor(c);
        Polygon crown = new Polygon(
                new int[]{-pieceSize / 3, -pieceSize / 6, 0, pieceSize / 6, pieceSize / 3, pieceSize * 4 / 15, -pieceSize * 4 / 15},
                new int[]{-pieceSize * 7 / 30, 0, -pieceSize * 7 / 30, 0, -pieceSize * 7 / 30, pieceSize / 5, pieceSize / 5},
                7); // There are 7 corners in the polygon
        crown.translate(row, col);
        g.fill(crown);
    }

    private void drawCrown(int row, int col, Graphics2D g) {
        drawCrown(row, col, g, Color.YELLOW);
    }

    /**
     * mousePressed
     * <p>
     * Responds when the user clicks the game board.
     * Calculate row and column that the user clicks,
     * then send calculated data to handler.
     *
     * @param evt The Java generated MouseEvent
     */
    public void mousePressed(MouseEvent evt) {
        if (gameInProgress) {
            // Approximate value is floored by int to give data
            int col = (evt.getX() - initialX) / squareSize;
            int row = (evt.getY() - initialY) / squareSize;
            if (col >= 0 && col < numRowsAndColumns && row >= 0 && row < numRowsAndColumns) {
                // If we are playing with a computer And it's the computer's turn,
                // then don't respond to mouse clicks within the game board
                if (!isComputerPlayingAndIsItComputersTurn()) {
                    doClickTile(row, col);
                }
            }
        } else {
            message.setText("Click \"New Game\" to start a new game.");
        }
    }

    public void mouseReleased(MouseEvent evt) {
    }

    public void mouseClicked(MouseEvent evt) {
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }

    /**
     * Displays a game over screen and asks player if they'd like to play again
     * Resets game configuration if player does want another game
     *
     * @param message Message to User(s)
     */
    private void gameOverPopUp(String message) {
        JOptionPane.showMessageDialog(null,
                "<html>" +
                        "<head>" +
                        "</head>" +
                        "<h1>" + message + "</h1>" +
                        "</html>",
                "GAME OVER",
                JOptionPane.PLAIN_MESSAGE);
    }

    public void toggleLegalMoveColors() {
        displayLegalMoveColors = !displayLegalMoveColors;
        repaint();
    }

    public void gameEndWindowToggle() {
        this.showGameOverPopUp = !this.showGameOverPopUp;
    }

    public void setSingleAI(boolean single_AI) {
        this.singleAI = single_AI;
    }

    public void setComputerDifficulty(int computer_Difficulty) {
        this.computerDifficulty = computer_Difficulty;
    }

    private void setCOMPUTER_MOVE_DELAY_IN_MILLISECONDS(int newTime) {
        this.COMPUTER_MOVE_DELAY_IN_MILLISECONDS = newTime;
    }

    private void setCOMPUTER_JUMP_DELAY_IN_MILLISECONDS(int newTime) {
        this.COMPUTER_JUMP_DELAY_IN_MILLISECONDS = newTime;
    }

    private void setDELAYS_TO_DEFAULTS() {
        this.COMPUTER_MOVE_DELAY_IN_MILLISECONDS = Constants.default_COMPUTER_MOVE_DELAY_IN_MILLISECONDS;
        this.COMPUTER_JUMP_DELAY_IN_MILLISECONDS = Constants.default_COMPUTER_JUMP_DELAY_IN_MILLISECONDS;
    }
}