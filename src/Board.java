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
    //    private static Color backgroundColor;
    private int currentPlayer; // Holds Piece Type (RED or BLACK)

    private int selectedRow, selectedCol;  // -1 if Player has not selected Row & Col

    private static Move[] legalMoves;  // Current Player's legal moves
    private static int numRowsAndColumns = 8;
    private int COMPUTER_MOVE_DELAY_IN_MILLISECONDS = 0,//500,//!@#$%^&*()
            COMPUTER_JUMP_DELAY_IN_MILLISECONDS = 0;//100;


    // Variables dealing with game Paints & Graphics
    private static final Color darkColor = Color.decode("#9D7D5C");
    private static final Color lightColor = Color.decode("#F1EBDE");
    private static final Color gameBlack = Color.BLACK.brighter(),
            gameRed = Color.RED.darker(),
            emptyPieceColor = new Color(0, 0, 0, 0),
            legalMovePieceColor = Color.ORANGE,
            legalMoveColor = Color.MAGENTA,
            selectedPiece = Color.WHITE,
            selectedPieceLegalMove = Color.GREEN;
    private static final float legalMoveBorder = 1.9f,
            selectedLegalMoveBorder = 1.9f,
            selectedPieceBorder = 2f;
    private static Graphics[][] gameBoardGraphics = new Graphics[numRowsAndColumns][numRowsAndColumns];
    private static final int initialX = 100, initialY = 50;
    private static final int squareSize = 80, pieceSize = 60;

    // THIS SHOULD UPDATE the buttons on Main Screen
    //!@#$%^&*()
    private JButton resignButton, newGameButton;
    private JLabel message;
    public JLabel userMessage;

    //    public int computerDifficulty = 2; // 0 - Human, 1 - Easy, 2 - Medium, 3 - Intermediate, 4 - Hard
    public int computerDifficulty = 3; //!@#$%^&*() Remove after testing
    private boolean displayLegalMoveColors; // If True, highlight legal moves for player
        public boolean singleAI = true; //!@#$%^&*() Remove after testing
//    public boolean singleAI = !true;
    private AI_Heuristic computerPlayer, computerPlayer2;


    //!@#$%^&*()
    Board(Color backgroundColor) {
//        this.numRowsAndColumns = numRowsAndColumns;
        setBackground(backgroundColor);
//        this.backgroundColor = backgroundColor;
        addMouseListener(this);
        resignButton = new JButton("Resign");
        resignButton.addActionListener(this);
        newGameButton = new JButton("New Game");
        newGameButton.addActionListener(this);
//        add(newGameButton);
//        add(resignButton);
        message = new JLabel("", JLabel.CENTER);
        message.setFont(new Font("Serif", Font.BOLD, 14));
        message.setForeground(Color.green);
        add(message, BorderLayout.LINE_START);

        userMessage = new JLabel("", JLabel.RIGHT);
        userMessage.setFont(new Font("Serif", Font.BOLD, 14));
        userMessage.setForeground(Color.YELLOW);
        add(userMessage, BorderLayout.LINE_END);

        displayLegalMoveColors = false;
        doNewGame();
    }

    //!@#$%^&*() WILL remove if buttons don't exist

    /**
     * Respond to user's click on one of the two buttons.
     */
    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();
        if (src == newGameButton)
            doNewGame();
        else if (src == resignButton)
            doResign();
    }

    //!@#$%^&*()

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
        board.setUpCheckerBoard(numRowsAndColumns);
        currentPlayer = CheckersData.RED;   // RED moves first.
        legalMoves = board.getLegalMoves(CheckersData.RED);  // Get RED's legal moves.

        selectedRow = -1;   // No pieces are selected at start of game
        message.setText("Black:  Make your move.");
        gameInProgress = true;
        newGameButton.setEnabled(false);
        resignButton.setEnabled(true);


        // We can later add implementation for Computer VS Computer HERE too
        if (computerDifficulty != 0) {
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
                // Delay move to allow user to see computer 'think'
                repaint();
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                computerPlayer2.updateGameBoard(board.gamePieces);
                                doMakeMove(computerPlayer2.getBestMove());
                            }
                        },
                        COMPUTER_JUMP_DELAY_IN_MILLISECONDS
                );
            }
        }

        // Update screen
        repaint();
    }


    //!@#$%^&*() I may cut this out entirely. I have not modified it from original

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
        if (currentPlayer == CheckersData.RED) {
            gameOver("RED resigns.  BLACK wins.");
//            gameOverPopUp("RED");
        } else {
            gameOver("BLACK resigns.  RED wins.");
//            gameOverPopUp("BLACK");
        }
    }


    //!@#$%^&*()

    /**
     * @param str Message sent to players at the end of the game
     */
    private void gameOver(String str) {
//        gameOverPopUp(str);
        message.setText(str);
        newGameButton.setEnabled(true);
        resignButton.setEnabled(false);
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
                    (currentPlayer == CheckersData.RED ? "BLACK: " : "RED: ")
                            + "Please select piece to move."
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
        if (currentPlayer == CheckersData.RED) {
            message.setText("BLACK: Invalid move. Please select legal move.");
        } else if (currentPlayer == CheckersData.BLACK) {
            message.setText("RED: Invalid move. Please select legal move.");
        }

    }


    /**
     * Moves players piece from old Row and Column to new ones.
     * If move is a jump, look for more jumps. If there are
     * more jumps, the player continues his turn.
     *
     * @param move The player's selected move
     */
    //!@#$%^&*()
    private void doMakeMove(Move move) {
        if (computerDifficulty > 0 && currentPlayer == CheckersData.BLACK) {
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
                // Delay move to allow user to see computer 'think'
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                if (isComputerPlayingAndIsItComputersTurn()) {
                                    if (singleAI || currentPlayer == computerPlayer.getComputerPlayerID()) {
                                        computerPlayer.updateGameBoard(board.gamePieces);
                                        doMakeMove(computerPlayer.getBestMove());
                                    } else if (currentPlayer == computerPlayer2.getComputerPlayerID()) {
                                        computerPlayer2.updateGameBoard(board.gamePieces);
                                        doMakeMove(computerPlayer2.getBestMove());
                                    }
                                }
                            }
                        },
                        COMPUTER_JUMP_DELAY_IN_MILLISECONDS
                );
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
        //!@#$%^&*()
        if (currentPlayer == CheckersData.RED) {
            currentPlayer = CheckersData.BLACK;
            legalMoves = board.getLegalMoves(currentPlayer);
            if (legalMoves == null) {
                gameOver("RED"); //!@#$%^&*()
            }
//            // AI's turn
//            else if (computerDifficulty > 0) {
//                repaint();
//                computerPlayer.updateGameBoard(board.gamePieces);
//                doMakeMove(computerPlayer.getBestMove());
//            }
            // These only appear in Human V. Human
            else if (legalMoves[0].isJump()) {
                message.setText("RED:  You must jump.");
            } else {
                message.setText("RED:  Make your move.");
            }
        } else {
            currentPlayer = CheckersData.RED;
            legalMoves = board.getLegalMoves(currentPlayer);
            if (legalMoves == null) {
                gameOver("BLACK");//!@#$%^&*()
            } else if (legalMoves[0].isJump()) {
                message.setText("BLACK:   You must jump.");
            } else {
                message.setText("BLACK:  Make your move.");
            }
        }

        // Player has not selected piece: selectedRow = -1
        selectedRow = -1;

        // Auto select piece if it is the only legal piece to move
        if (legalMoves != null) {
            boolean isOnlyOneLegalPieceToMove = true;
            //!@#$%^&*() Foreach?
            for (Move legalMove : legalMoves) {
                if (legalMove.fromRow != legalMoves[0].fromRow
                        || legalMove.fromCol != legalMoves[0].fromCol) {
                    isOnlyOneLegalPieceToMove = false;
                    break;
                }
            }
//            for (int i = 1; i < legalMoves.length; i++)
//                if (legalMoves[i].fromRow != legalMoves[0].fromRow
//                        || legalMoves[i].fromCol != legalMoves[0].fromCol) {
//                    sameStartSquare = false;
//                    break;
//                }
            if (isOnlyOneLegalPieceToMove) {
                selectedRow = legalMoves[0].fromRow;
                selectedCol = legalMoves[0].fromCol;
            }
            /* Make sure the board is redrawn in its new state. */
            repaint();

            // Delay move to allow user to see computer 'think'
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            if (isComputerPlayingAndIsItComputersTurn()) {
                                if (singleAI || currentPlayer == computerPlayer.getComputerPlayerID()) {
                                    computerPlayer.updateGameBoard(board.gamePieces);
                                    doMakeMove(computerPlayer.getBestMove());
                                } else if (currentPlayer == computerPlayer2.getComputerPlayerID()) {
                                    computerPlayer2.updateGameBoard(board.gamePieces);
                                    doMakeMove(computerPlayer2.getBestMove());
                                }
                            }
                        }
                    },
                    COMPUTER_MOVE_DELAY_IN_MILLISECONDS
            );
        }

        /* Make sure the board is redrawn in its new state. */
        repaint();

//        // Delay move to allow user to see computer 'think'
//        new java.util.Timer().schedule(
//                new java.util.TimerTask() {
//                    @Override
//                    public void run() {
//                        if (isComputerPlayingAndIsItComputersTurn()) {
//                            if (singleAI || currentPlayer == computerPlayer.getComputerPlayerID()) {
//                                computerPlayer.updateGameBoard(board.gamePieces);
//                                doMakeMove(computerPlayer.getBestMove());
//                            } else if (currentPlayer == computerPlayer2.getComputerPlayerID()) {
//                                computerPlayer2.updateGameBoard(board.gamePieces);
//                                doMakeMove(computerPlayer2.getBestMove());
//                            }
//                        }
//                    }
//                },
//                COMPUTER_MOVE_DELAY_IN_MILLISECONDS
//        );
    }

    /**
     * Determines if we have an AI and if it is the AI's turn
     *
     * @return True if is the computer's turn, False otherwise
     */
    private boolean isComputerPlayingAndIsItComputersTurn() {
        if (computerDifficulty != 0) {
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
                //!@#$%^&*() I SWITCHED THE COLORS. CHANGE BACK!!!
                //!@#$%^&*() HOWEVER, IT DOESN'T EFFECT GAME PLAY
                if (board.gamePieces[row][col].getPieceType() == CheckersData.RED
                        || board.gamePieces[row][col].getPieceType() == CheckersData.RED_KING) {
//                    g2d.setColor(gameBlack);//!@#$%^&*() Black First For production
                    g2d.setColor(gameRed);
                } else if (board.gamePieces[row][col].getPieceType() == CheckersData.BLACK
                        || board.gamePieces[row][col].getPieceType() == CheckersData.BLACK_KING) {
//                    g2d.setColor(gameRed);//!@#$%^&*() Black First For production
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
                new int[]{-pieceSize / 3, -pieceSize / 6, 0, pieceSize / 6, pieceSize / 3, pieceSize * 4 / 15, -pieceSize * 4 / 15}, //new int[]{-20, -10, 0, 10, 20, 16, -16},
                new int[]{-pieceSize * 7 / 30, 0, -pieceSize * 7 / 30, 0, -pieceSize * 7 / 30, pieceSize / 5, pieceSize / 5}, //new int[]{-14, 0, -14, 0, -14, 12, 12}, 7);
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

    public void toggleLegalMoveColors() {
        displayLegalMoveColors = !displayLegalMoveColors;
        repaint();
    }

    /**
     * Displays a game over screen and asks player if they'd like to play again
     * Resets game configuration if player does want another game
     *
     * @param playerColor The Player's Color for the dialogue box
     */
    private void gameOverPopUp(String playerColor) {
        JOptionPane gameOverScreen = new JOptionPane();
        int confirm = gameOverScreen.showConfirmDialog(null,
                "<html>" +
                        "<head>" +
//                        "<style>" +
//                        "p {" +
//                        "  width: " + windowDimensions.get("width") / 3 + "px;" +
//                        "}" +
//                        "ul {" +
//                        "  width: " + windowDimensions.get("width") / 3 + "px;" +
//                        "}" +
//                        "</style>" +
                        "</head>" +
                        "<h1>" + playerColor + " Wins!!</h1>" +
                        "<h2>Play Again?</h2>" +
                        "</html>",
                "GAME OVER",
                JOptionPane.YES_NO_OPTION);
        gameInProgress = false;
        if (confirm == JOptionPane.YES_OPTION) {
            doNewGame();
        }
    }
}