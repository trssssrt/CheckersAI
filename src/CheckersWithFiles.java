import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
 * This panel lets two users play checkers against each other.
 * Red always starts the game.  If a player can jump an opponent's
 * piece, then the player must jump.  When a player can make no more
 * moves, the game ends.
 * <p>
 * The class has a main() routine that lets it be run as a stand-alone
 * application.  The application just opens a window that uses an object
 * of type Checkers as its content pane.
 */
class Checkers extends JPanel {


    private JButton newGameButton;  // Button for starting a new game.
    private JButton resignButton;   // Button that a player can use to end
    //    the game by resigning.

    private JLabel message;  // Label for displaying messages to the user.
    static private Map<String, Integer> windowDimensions = new HashMap<>() {{
        put("width", 1000);
        put("height", 750);
    }};
    private static int numRowsAndColumns = 8;
    final Board board = new Board();

    /**
     * Main routine makes it possible to run Checkers as a stand-alone
     * application.  Opens a window showing a Checkers panel; the program
     * ends when the user closes the window.
     */
    public static void main(String[] args) {
        JFrame window = new JFrame("Checkers");
        window.setSize(windowDimensions.get("width"), windowDimensions.get("height"));
        Checkers content = new Checkers();
        window.setContentPane(content);
        window.pack();
        // Put Window in Center of Screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation((screenSize.width - window.getWidth()) / 2,
                (screenSize.height - window.getHeight()) / 2);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setVisible(true);
    }

    /**
     * The constructor creates the Board (which in turn creates and manages
     * the buttons and message label), adds all the components, and sets
     * the bounds of the components.  A null layout is used.  (This is
     * the only thing that is done in the main Checkers class.)
     */
    private Checkers() {

//        setLayout(null);  // This is the cause of the sizing issues
        setPreferredSize(new Dimension(windowDimensions.get("width"), windowDimensions.get("height")));

        setBackground(new Color(0, 150, 0));  // Dark green background.//!@#$%^&*()

        /* Create the components and add them to the panel. */

//        Board board = new Board();  // Note: The constructor for the
//        //   board also creates the buttons
//        //   and label.
        add(board);
        //!@#$%^&*() Buttons No Longer Work
//        add(newGameButton);
//        add(resignButton);
//        add(message);
      
      /* Set the position and size of each component by calling
       its setBounds() method. */

        board.setPreferredSize(new Dimension(windowDimensions.get("width"), windowDimensions.get("height")));
//        board.setBounds(20, 20, 164, 164); // Note:  size MUST be 164-by-164 ! // This is the cause of the sizing issue
//        newGameButton. //!@#$%^&*()
//        newGameButton.setBounds(210, 20, 120, 30);
//        resignButton.setBounds(210, 155, 120, 30);
//        message.setBounds(0, 200, 350, 30);


        // ADDED FROM BOARD.JAVA //!@#$%^&*()
//        board.newGameButton = newGameButton;
//        board.resignButton = resignButton;
//        board.message = message;
//        resignButton = new JButton("Resign");
//        resignButton.addActionListener(board);
//        newGameButton = new JButton("New Game");
//        newGameButton.addActionListener(board);
//        message = new JLabel("", JLabel.CENTER);
//        message.setFont(new Font("Serif", Font.BOLD, 14));
//        message.setForeground(Color.green);

    } // end constructor
} // end class Checkers