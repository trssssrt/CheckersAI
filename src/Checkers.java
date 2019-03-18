import java.awt.*;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Create Swing Application of Checkers game
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
    private static Color backgroundColor = Color.decode("#492A1B");
    private final Board board = new Board(backgroundColor);

    /**
     * Main JFrame window created here. Most of the work is passed into Board
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

    private Checkers() {
        setPreferredSize(new Dimension(windowDimensions.get("width"), windowDimensions.get("height")));

        setBackground(backgroundColor);

        /* Create the components and add them to the panel. */
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

    }
}