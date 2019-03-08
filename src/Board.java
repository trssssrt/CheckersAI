import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;


import java.awt.geom.GeneralPath;

/**
 * This panel displays a 160-by-160 checkerboard pattern with
 * a 2-pixel black border.  It is assumed that the size of the
 * canvas is set to exactly 164-by-164 pixels.  This class does
 * the work of letting the users play checkers, and it displays
 * the checkerboard.
 */
class Board extends JPanel implements ActionListener, MouseListener {

    final CheckersData board = new CheckersData();
    ;  // The data for the checkers board is kept here.
    //    This board is also responsible for generating
    //    lists of legal moves.

    boolean gameInProgress; // Is a game currently in progress?

    /* The next three variables are valid only when the game is in progress. */

    int currentPlayer;      // Whose turn is it now?  The possible values
    //    are CheckersData.RED and CheckersData.BLACK.

    int selectedRow, selectedCol;  // If the current player has selected a piece to
    //     move, these give the row and column
    //     containing that piece.  If no piece is
    //     yet selected, then selectedRow is -1.

    static Move[] legalMoves;  // An array containing the legal moves for the
    //   current player.
    private static int numRowsAndColumns = 8;

    final Piece[][] gamePieces = new Piece[numRowsAndColumns][numRowsAndColumns];
    private static final Color gameBlack = Color.BLACK.brighter(),
            gameRed = Color.RED.darker(),
            legalMoveColor = Color.MAGENTA,
            selectedPiece = Color.WHITE,
            selectedPieceLegalMove = Color.GREEN;

    static Graphics[][] gameBoardGraphics = new Graphics[numRowsAndColumns][numRowsAndColumns];

    private static final int initialX = 100, initialY = 50;
    private static final int squareSize = 80, pieceSize = 60;//!@#$%^&*()
    //        private static final int squareSize = 20, pieceSize = 15;//!@#$%^&*()
    // THIS SHOULD UPDATE the buttons on Main Screen
    //!@#$%^&*()
    public JButton resignButton, newGameButton;
    public JLabel message;


    //!@#$%^&*()

    /**
     * Constructor.  Create the buttons and label.  Listens for mouse
     * clicks and for clicks on the buttons.  Create the board and
     * start the first game.
     */
    Board() {
        setBackground(Color.BLACK);
        addMouseListener(this);
        resignButton = new JButton("Resign");
        resignButton.addActionListener(this);
        newGameButton = new JButton("New Game");
        newGameButton.addActionListener(this);
        message = new JLabel("", JLabel.CENTER);
        message.setFont(new Font("Serif", Font.BOLD, 14));
        message.setForeground(Color.green);
        doNewGame();
    }

    //!@#$%^&*()

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
    void doNewGame() {
        if (gameInProgress) {
            // This should not be possible, but it doesn't hurt to check.
            message.setText("Finish the current game first!");
            return;
        }
        board.setUpGame();   // Set up the pieces.
        board.setUpCheckerBoard(numRowsAndColumns);
        // My Changes, my changes SHOULD include gamePieces... CHECK THAT
//        currentPlayer = CheckersData.BLACK;   // BLACK moves first.
//        legalMoves = board.getLegalMoves(CheckersData.BLACK);  // Get First Player's legal moves.
        // ORIGINAL
        currentPlayer = CheckersData.RED;   // RED moves first.
        legalMoves = board.getLegalMoves(CheckersData.RED);  // Get RED's legal moves.


        selectedRow = -1;   // RED has not yet selected a piece to move.
        message.setText("Black:  Make your move.");
        gameInProgress = true;
        newGameButton.setEnabled(false);
        resignButton.setEnabled(true);
        repaint();
    }


    //!@#$%^&*()

    /**
     * Current player resigns.  Game ends.  Opponent wins.
     */
    void doResign() {
        if (!gameInProgress) {
            message.setText("There is no game in progress!");
            return;
        }
        if (currentPlayer == CheckersData.RED) {
            gameOver("RED resigns.  BLACK wins.");
        } else {
            gameOver("BLACK resigns.  RED wins.");
        }
    }


    //!@#$%^&*()

    /**
     * The game ends.  The parameter, str, is displayed as a message
     * to the user.  The states of the buttons are adjusted so the players
     * can start a new game.  This method is called when the game
     * ends at any point in this class.
     */
    void gameOver(String str) {
        message.setText(str);
        newGameButton.setEnabled(true);
        resignButton.setEnabled(false);
        gameInProgress = false;
    }


    //!@#$%^&*()

    /**
     * This is called by mousePressed() when a player clicks on the
     * square in the specified row and col.  It has already been checked
     * that a game is, in fact, in progress.
     */
    void doClickSquare(int row, int col) {

         /* If the player clicked on one of the pieces that the player
          can move, mark this row and col as selected and return.  (This
          might change a previous selection.)  Reset the message, in
          case it was previously displaying an error message. */

        for (Move legalMove : legalMoves) {
            if (legalMove.fromRow == row && legalMove.fromCol == col) {
                selectedRow = row;
                selectedCol = col;
                if (currentPlayer == CheckersData.RED) {
                    message.setText("RED:  Make your move.");
                } else {
                    message.setText("BLACK:  Make your move.");
                }
                repaint();
                return;
            }
        }

         /* If no piece has been selected to be moved, the user must first
          select a piece.  Show an error message and return. */

        if (selectedRow < 0) {
            message.setText("Click the piece you want to move.");
            return;
        }

         /* If the user clicked on a square where the selected piece can be
          legally moved, then make the move and return. */

        for (Move legalMove : legalMoves) {
            if (legalMove.fromRow == selectedRow && legalMove.fromCol == selectedCol
                    && legalMove.toRow == row && legalMove.toCol == col) {
                doMakeMove(legalMove);
                return;
            }
        }

         /* If we get to this point, there is a piece selected, and the square where
          the user just clicked is not one where that piece can be legally moved.
          Show an error message. */

        message.setText("Click the square you want to move to.");

    }  // end doClickSquare()


    //!@#$%^&*()

    /**
     * This is called when the current player has chosen the specified
     * move.  Make the move, and then either end or continue the game
     * appropriately.
     */
    void doMakeMove(Move move) {

        board.makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol);

         /* If the move was a jump, it's possible that the player has another
          jump.  Check for legal jumps starting from the square that the player
          just moved to.  If there are any, the player must jump.  The same
          player continues moving.
          */

        if (move.isJump()) {
            legalMoves = board.getLegalJumpsFrom(currentPlayer, move.toRow, move.toCol);
            if (legalMoves != null) {
                if (currentPlayer == CheckersData.RED) {
                    message.setText("RED:  You must continue jumping.");
                } else {
                    message.setText("BLACK:  You must continue jumping.");
                }
                selectedRow = move.toRow;  // Since only one piece can be moved, select it.
                selectedCol = move.toCol;
                repaint();
                return;
            }
        }

         /* The current player's turn is ended, so change to the other player.
          Get that player's legal moves.  If the player has no legal moves,
          then the game ends. */

        if (currentPlayer == CheckersData.RED) {
            currentPlayer = CheckersData.BLACK;
            legalMoves = board.getLegalMoves(currentPlayer);
            if (legalMoves == null) {
                gameOver("BLACK has no moves.  RED wins.");
            } else if (legalMoves[0].isJump()) {
                message.setText("BLACK:  Make your move.  You must jump.");
            } else {
                message.setText("BLACK:  Make your move.");
            }
        } else {
            currentPlayer = CheckersData.RED;
            legalMoves = board.getLegalMoves(currentPlayer);
            if (legalMoves == null) {
                gameOver("RED has no moves.  BLACK wins.");
            } else if (legalMoves[0].isJump()) {
                message.setText("RED:  Make your move.  You must jump.");
            } else {
                message.setText("RED:  Make your move.");
            }
        }

         /* Set selectedRow = -1 to record that the player has not yet selected
          a piece to move. */

        selectedRow = -1;

         /* As a courtesy to the user, if all legal moves use the same piece, then
          select that piece automatically so the user won't have to click on it
          to select it. */

        if (legalMoves != null) {
            boolean sameStartSquare = true;
            //!@#$%^&*() Foreach?
            for (int i = 1; i < legalMoves.length; i++)
                if (legalMoves[i].fromRow != legalMoves[0].fromRow
                        || legalMoves[i].fromCol != legalMoves[0].fromCol) {
                    sameStartSquare = false;
                    break;
                }
            if (sameStartSquare) {
                selectedRow = legalMoves[0].fromRow;
                selectedCol = legalMoves[0].fromCol;
            }
        }

        /* Make sure the board is redrawn in its new state. */

        repaint();

    }  // end doMakeMove();


    /**
     * Draw checkerboard pattern in gray and lightGray.  Draw the
     * checkers.  If a game is in progress, highlight the legal moves.
     */
//    @Override
//    public void paintComponent(Graphics g) {
//        Color darkColor = Color.decode("#9D7D5C");
//        Color lightColor = Color.decode("#F1EBDE");
//
//        /* Draw a two-pixel black border around the edges of the canvas. */
//
//        g.setColor(Color.black);
//        g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
//        g.drawRect(1, 1, getSize().width - 3, getSize().height - 3);
//
//        /* Draw the squares of the checkerboard and the checkers. */
//        int currentX = 100;
//        int currentY = 50;
//        for (int row = 0; row < numRowsAndColumns; row++) {
//            for (int col = 0; col < numRowsAndColumns; col++) {
//                if (row % 2 == col % 2)
//                    g.setColor(Color.LIGHT_GRAY);
//                else
//                    g.setColor(Color.GRAY);
//                g.fillRect(2 + col * squareSize, 2 + row * squareSize, squareSize, squareSize);
//                switch (board.pieceAt(row, col)) {
//                    case CheckersData.RED:
//                        g.setColor(Color.RED);
//                        g.fillOval(4 + col * squareSize, 4 + row * squareSize, pieceSize, pieceSize);
//                        break;
//                    case CheckersData.BLACK:
//                        g.setColor(Color.BLACK);
//                        g.fillOval(4 + col * squareSize, 4 + row * squareSize, pieceSize, pieceSize);
//                        break;
//                    case CheckersData.RED_KING:
//                        g.setColor(Color.RED);
//                        g.fillOval(4 + col * squareSize, 4 + row * squareSize, pieceSize, pieceSize);
//                        g.setColor(Color.WHITE);
//                        g.drawString("K", 7 + col * squareSize, 16 + row * squareSize);
//                        break;
//                    case CheckersData.BLACK_KING:
//                        g.setColor(Color.BLACK);
//                        g.fillOval(4 + col * squareSize, 4 + row * squareSize, pieceSize, pieceSize);
//                        g.setColor(Color.WHITE);
//                        g.drawString("K", 7 + col * squareSize, 16 + row * squareSize);
//                        break;
//                }
//
//                currentX += squareSize;
//            }
//            currentY += squareSize;
//            currentX = 100;
//        }
//
//         /* If a game is in progress, highlight the legal moves.   Note that legalMoves
//          is never null while a game is in progress. */
//
//        if (gameInProgress) {
//            /* First, draw a 2-pixel cyan border around the pieces that can be moved. */
//            g.setColor(Color.cyan);
//            for (Move legalMove : legalMoves) {
//                g.drawRect(2 + legalMove.fromCol * squareSize, 2 + legalMove.fromRow * squareSize, squareSize - 1, squareSize - 1);
//                g.drawRect(3 + legalMove.fromCol * squareSize, 3 + legalMove.fromRow * squareSize, squareSize - 3, squareSize - 3);
//            }
//               /* If a piece is selected for moving (i.e. if selectedRow >= 0), then
//                draw a 2-pixel white border around that piece and draw green borders
//                around each square that that piece can be moved to. */
//            if (selectedRow >= 0) {
//                g.setColor(Color.white);
//                g.drawRect(2 + selectedCol * squareSize, 2 + selectedRow * squareSize, squareSize - 1, squareSize - 1);
//                g.drawRect(3 + selectedCol * squareSize, 3 + selectedRow * squareSize, squareSize - 3, squareSize - 3);
//                g.setColor(Color.green);
//                for (Move legalMove : legalMoves) {
//                    if (legalMove.fromCol == selectedCol && legalMove.fromRow == selectedRow) {
//                        g.drawRect(2 + legalMove.toCol * squareSize, 2 + legalMove.toRow * squareSize, squareSize - 1, squareSize - 1);
//                        g.drawRect(3 + legalMove.toCol * squareSize, 3 + legalMove.toRow * squareSize, squareSize - 3, squareSize - 3);
//                    }
//                }
//            }
//        }
//
//    }  // end paintComponent()
    @Override
    public void paintComponent(Graphics g) {

        Color darkColor = Color.decode("#9D7D5C");
        Color lightColor = Color.decode("#F1EBDE");

        Graphics2D g2d = (Graphics2D) g;
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);
        int currentX = initialX;
        int currentY = initialY;
        //Every repaint the board and all the pieces on it are repainted from scratch
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
                //Checks what piece is to be drawn and sets the colour that is appropriate
                if (board.gamePieces[row][col].getPieceVal() == 1 || board.gamePieces[row][col].getPieceVal() == 2) {
                    g2d.setColor(gameBlack);
                } else if (board.gamePieces[row][col].getPieceVal() == 3 || board.gamePieces[row][col].getPieceVal() == 4) {
                    g2d.setColor(gameRed);
                } else if (board.gamePieces[row][col].getPieceVal() == 0) {
                    g2d.setColor(new Color(0, 0, 0, 0));
                }

                // Draw Ellipse around gamePieces
                if (board.gamePieces[row][col].getPieceVal() != 0) {
                    Ellipse2D pieceShape = new Ellipse2D.Double(currentX + initialX / 10, currentY + initialX / 10, pieceSize, pieceSize);
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

        if (gameInProgress) {
            /* First, draw a 2-pixel cyan border around the pieces that can be moved. */
            g.setColor(Color.BLUE);
//            System.out.println("Valid Moves");
            for (Move legalMove : legalMoves) {
                // TEST WITH gameBoardGraphics
                gameBoardGraphics[legalMove.fromRow][legalMove.fromCol].setColor(legalMoveColor);
                gameBoardGraphics[legalMove.fromRow][legalMove.fromCol].drawRect(
                        initialX + squareSize * legalMove.toCol,
                        initialY + squareSize * legalMove.toRow,
                        squareSize,
                        squareSize);
                g2d.setStroke(new BasicStroke(1.5f));
                // END TEST
//
//
////                System.out.printf("Move -> (%s,%s) TO (%s,%s)\n", legalMove.fromRow, legalMove.fromCol, legalMove.toRow, legalMove.toCol);
//                // If Piece has a legal move, turn it white
//                g2d.setColor(Color.WHITE);
//                Ellipse2D oval = board.gamePieces[legalMove.fromRow][legalMove.fromCol].getOval();
//                g2d.draw(oval);
//                g2d.setStroke(new BasicStroke(1.5f)); //!@#$%^setStroke&*() Something is wrong with the thickness...
//                // Draw a rectangle around the tiles that are legal moves
////                g.setColor(Color.black);
//                g.fillRect(initialX + squareSize * legalMove.toCol, initialY + squareSize * legalMove.toRow, squareSize, squareSize);
////                g.fillRect(initialX + squareSize * legalMove.toRow, initialY + squareSize * legalMove.toCol, squareSize, squareSize);
////                ((Graphics2D) g).setStroke(new BasicStroke(4));
////                g.drawRect(2 + legalMove.fromCol * squareSize, 2 + legalMove.fromRow * squareSize, squareSize - 1, squareSize - 1);
////                g.drawRect(3 + legalMove.fromCol * squareSize, 3 + legalMove.fromRow * squareSize, squareSize - 3, squareSize - 3);
            }
               /* If a piece is selected for moving (i.e. if selectedRow >= 0), then
                draw a 2-pixel white border around that piece and draw green borders
                around each square that that piece can be moved to. */
            if (selectedRow >= 0) {
                // TEST WITH gameBoardGraphics
                gameBoardGraphics[selectedRow][selectedCol].setColor(selectedPiece);
                gameBoardGraphics[selectedRow][selectedCol].drawRect(
                        initialX + squareSize * selectedCol,
                        initialY + squareSize * selectedRow,
                        squareSize,
                        squareSize);
                Ellipse2D pieceShape = new Ellipse2D.Double(initialX + squareSize * selectedCol + initialX / 10,
                        initialY + squareSize * selectedRow + initialX / 10,
                        pieceSize,
                        pieceSize);
                g2d.setColor(selectedPiece);
                g2d.setStroke(new BasicStroke(2f));
//                g2d.
                board.gamePieces[selectedRow][selectedCol].setOval(pieceShape);
//                g2d.fill(pieceShape);
                float thickness = 2;
                Stroke oldStroke = g2d.getStroke();
                g2d.setStroke(new BasicStroke(thickness));
                g2d.drawRect(initialX + squareSize * selectedCol,
                        initialY + squareSize * selectedRow,
                        squareSize,
                        squareSize);
                g2d.setStroke(oldStroke);
//                float dash1[] = {10.0f};
//                g2d.setStroke(new BasicStroke(1.0f,
//                        BasicStroke.CAP_BUTT,
//                        BasicStroke.JOIN_MITER,
//                        10.0f, dash1, 0.0f));
//                g2d.setStroke(new BasicStroke(2f));
                // END TEST
//                g.setColor(Color.WHITE);
//                Ellipse2D oval = board.gamePieces[selectedRow][selectedCol].getOval();
//                g2d.draw(oval);
//                g2d.setColor(Color.WHITE);
//                g2d.setStroke(new BasicStroke(3)); //!@#$%^&*() Something is wrong with the thickness...
//                g.fillRect(initialX + squareSize * selectedCol, initialY + squareSize * selectedRow, squareSize, squareSize);
//                g.drawRect(2 + selectedCol * squareSize, 2 + selectedRow * squareSize, squareSize - 1, squareSize - 1);
//                g.drawRect(3 + selectedCol * squareSize, 3 + selectedRow * squareSize, squareSize - 3, squareSize - 3);
//                g.setColor(Color.GREEN);
                for (Move legalMove : legalMoves) {
                    if (legalMove.fromCol == selectedCol && legalMove.fromRow == selectedRow) {
                        // TEST WITH gameBoardGraphics
                        gameBoardGraphics[legalMove.fromRow][legalMove.fromCol].setColor(selectedPieceLegalMove);
                        gameBoardGraphics[legalMove.fromRow][legalMove.fromCol].drawRect(
                                initialX + squareSize * legalMove.toCol,
                                initialY + squareSize * legalMove.toRow,
                                squareSize,
                                squareSize);
                        g2d.setStroke(new BasicStroke(1.5f));
                        // END TEST
                        // TEST WITH gameBoardGraphics
//                        gameBoardGraphics[legalMove.fromRow][legalMove.fromCol].setColor(Color.MAGENTA);
//                        gameBoardGraphics[legalMove.fromRow][legalMove.fromCol].drawRect(initialX + squareSize * selectedCol, initialY + squareSize * selectedRow, squareSize, squareSize);
                        // END TEST


//                        g.fillRect(initialX + squareSize * legalMove.toCol, initialY + squareSize * legalMove.toRow, squareSize, squareSize);
//                        g.drawRect(2 + legalMove.toCol * squareSize, 2 + legalMove.toRow * squareSize, squareSize - 1, squareSize - 1);
//                        g.drawRect(3 + legalMove.toCol * squareSize, 3 + legalMove.toRow * squareSize, squareSize - 3, squareSize - 3);
                    }
                }
            }
        }
    }
    //!@#$%^&*()

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
     * Respond to a user click on the board.  If no game is in progress, show
     * an error message.  Otherwise, find the row and column that the user
     * clicked and call doClickSquare() to handle it.
     */
//    public void mousePressed(MouseEvent evt) {
//        if (!gameInProgress)
//            message.setText("Click \"New Game\" to start a new game.");
//        else {
//            int col = (evt.getX() - 2) / squareSize;
//            int row = (evt.getY() - 2) / squareSize;
//            if (col >= 0 && col < numRowsAndColumns && row >= 0 && row < numRowsAndColumns)
//                doClickSquare(row, col);
//        }
//    }
    public void mousePressed(MouseEvent evt) {
        if (!gameInProgress) {
            message.setText("Click \"New Game\" to start a new game.");
        } else {
            int col = (evt.getX() - initialX) / squareSize;
            int row = (evt.getY() - initialY) / squareSize;
            if (col >= 0 && col < numRowsAndColumns && row >= 0 && row < numRowsAndColumns)
                doClickSquare(row, col);
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


}  // end class Board