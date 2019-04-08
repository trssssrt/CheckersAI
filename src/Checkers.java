import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Create Swing Application of Checkers game
 */
class Checkers extends JPanel {
    private JFrame window;

    static private Map<String, Integer> windowDimensions = new HashMap<>() {{
        put("width", Constants.defaultWindowWidth);
        put("height", Constants.defaultWindowHeight);
    }};
    private static int numRowsAndColumns = Constants.defaultNumRowsAndColumns;
    private static Color backgroundColor = Constants.backgroundColor;
    private final Board board = new Board(backgroundColor);


    private static JMenuBar menuBar;
    private static JMenu gameMenu, helpMenu, difficultyMenu;
    private static String humanVsHuman = "Human Vs Human",
            computerVsHuman = "Computer Vs Human",
            computerVsComputer = "Computer Vs Computer",
            computerDifficulty_Text = "Computer Difficulty: ",
            difficultyLevel1 = "Easy",
            difficultyLevel2 = "Medium",
            difficultyLevel3 = "Intermediate",
            difficultyLevel4 = "Hard";
    // Integer difficulty levels
    private static int difficulty_ZERO = Constants.difficulty_ZERO,
            difficulty_Easy = Constants.difficulty_Easy,
            difficulty_Medium = Constants.difficulty_Medium,
            difficulty_Intermediate = Constants.difficulty_Intermediate,
            difficulty_Hard = Constants.difficulty_Hard;
    private String[] playersColorString = {"<html>Colors: ",
            "<font color=\"", "red", "\"><strong>", "Player 1", "</strong></font>",
            ", ",
            "<font color=\"", "black", "\"><strong>", "Player 2", "</strong></font>",
            "</html>"};
    private static JLabel messageToUser;
    private static ButtonGroup difGroup;
    private static JRadioButtonMenuItem easyDifficultyMenuItem, mediumDifficultyMenuItem, intermediateDifficultyMenuItem, hardDifficultyMenuItem;

    /**
     * Main JFrame window created here. Most of the work is passed into Board
     */
    public static void main(String[] args) {
        JFrame window = new JFrame("Checkers");
        window.setSize(windowDimensions.get("width"), windowDimensions.get("height"));
        // Game Menu
        menuBar = new JMenuBar();
//        createMenuBar();
        window.setJMenuBar(menuBar);

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
        createMenuBar();

        board.setPreferredSize(new Dimension(windowDimensions.get("width"), windowDimensions.get("height")));
        board.userMessage.setText(messageToUser.getText());
    }

    private void createMenuBar() {
        // Game Menu
        gameMenu = new JMenu("Game"); // Create a menu with name "Tools"

        JMenu newGameMenu = new JMenu("New");
        JMenuItem HumanVSHuman = new JMenuItem(humanVsHuman);
        JMenuItem ComputerVSHuman = new JMenuItem(computerVsHuman);
        JMenuItem ComputerVsComputer = new JMenuItem(computerVsComputer);
        newGameMenu.add(HumanVSHuman);
        newGameMenu.add(ComputerVSHuman);
        newGameMenu.add(ComputerVsComputer);
        gameMenu.add(newGameMenu);

        // Add New Game Actions
        HumanVSHuman.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.computerDifficulty = 0;
                board.performDoNewGame();

                // update messageToUser & deselect difficulty level
                messageToUser.setText(humanVsHuman);
                board.userMessage.setText(humanVsHuman);
            }
        });
        ComputerVSHuman.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("ADD AI");
                int difficulty = difficulty_ZERO;
                if (easyDifficultyMenuItem.isSelected()) {
                    difficulty = difficulty_Easy;
                    messageToUser.setText(computerDifficulty_Text + difficultyLevel1);
                } else if (mediumDifficultyMenuItem.isSelected()) {
                    difficulty = difficulty_Medium;
                    messageToUser.setText(computerDifficulty_Text + difficultyLevel2);
                } else if (intermediateDifficultyMenuItem.isSelected()) {
                    difficulty = difficulty_Medium;
                    messageToUser.setText(computerDifficulty_Text + difficultyLevel3);
                } else if (hardDifficultyMenuItem.isSelected()) {
                    difficulty = difficulty_Hard;
                    messageToUser.setText(computerDifficulty_Text + difficultyLevel4);
                }
                board.singleAI = true;
                board.computerDifficulty = difficulty;
                board.performDoNewGame();
                board.userMessage.setText(messageToUser.getText());
            }
        });
        ComputerVsComputer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("ADD AI & AI");
                int difficulty = difficulty_ZERO;
                if (easyDifficultyMenuItem.isSelected()) {
                    difficulty = difficulty_Easy;
                    messageToUser.setText(computerDifficulty_Text + difficultyLevel1);
                } else if (mediumDifficultyMenuItem.isSelected()) {
                    difficulty = difficulty_Medium;
                    messageToUser.setText(computerDifficulty_Text + difficultyLevel2);
                } else if (intermediateDifficultyMenuItem.isSelected()) {
                    difficulty = difficulty_Intermediate;
                    messageToUser.setText(computerDifficulty_Text + difficultyLevel3);
                } else if (hardDifficultyMenuItem.isSelected()) {
                    difficulty = difficulty_Hard;
                    messageToUser.setText(computerDifficulty_Text + difficultyLevel4);
                }
                board.singleAI = false;
                board.computerDifficulty = difficulty;
                board.performDoNewGame();
                board.userMessage.setText(messageToUser.getText());
            }
        });

//        gameMenu.addSeparator();

        JMenuItem resignCommand = new JMenuItem("Resign"); // Create a menu item.
        // Add listener to menu item.
        resignCommand.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.performDoResign();
            }
        });
        gameMenu.add(resignCommand); // Add menu item to menu.

        menuBar.add(gameMenu);

        // Help Menu
        helpMenu = new JMenu("Help");
        JMenuItem helpCommand = new JMenuItem("Game Instructions"); // Create a menu item.
        helpCommand.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showGameRulesScreen();
            }
        });

        JRadioButtonMenuItem toggleLegalMoveColorsCommand = new JRadioButtonMenuItem("Color Legal Moves"); // Create a menu item.
        toggleLegalMoveColorsCommand.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleLegalMoveColors();
            }
        });
        toggleLegalMoveColorsCommand.setSelected(false);

        JRadioButtonMenuItem playerColors = new JRadioButtonMenuItem(String.join("", playersColorString)); // Create a menu item.
        playerColors.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playerColors(playerColors.isSelected());
                playerColors.setText(String.join("", playersColorString));
            }
        });
        playerColors.setSelected(true);


        // Add menu items to menu.
        helpMenu.add(helpCommand);
        helpMenu.add(toggleLegalMoveColorsCommand);
        helpMenu.add(playerColors);

        menuBar.add(helpMenu);


        // Difficulty Menu
        messageToUser = new JLabel(computerDifficulty_Text + difficultyLevel2); // Medium is Default Game Difficulty
        difficultyMenu = new JMenu("Difficulty");
        difficultyMenu.setMnemonic(KeyEvent.VK_F);

        difGroup = new ButtonGroup();

        easyDifficultyMenuItem = new JRadioButtonMenuItem(difficultyLevel1);
        easyDifficultyMenuItem.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (!messageToUser.getText().contains(humanVsHuman)) {
                        messageToUser.setText(computerDifficulty_Text + difficultyLevel1);
                        //!@#$%^&*() INCLUDE SIMILAR CODE IF I WANT TO UPDATE ON CLICK
//                        board.userMessage.setText(messageToUser.getText());
                    }
                }
            }
        });

        mediumDifficultyMenuItem = new JRadioButtonMenuItem(difficultyLevel2);
        mediumDifficultyMenuItem.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (!messageToUser.getText().contains(humanVsHuman)) {
                        messageToUser.setText(computerDifficulty_Text + difficultyLevel2);
                        //!@#$%^&*() INCLUDE SIMILAR CODE IF I WANT TO UPDATE ON CLICK
//                        board.userMessage.setText(messageToUser.getText());
                    }
                }
            }
        });
//        mediumDifficultyMenuItem.setSelected(true); //!@#$%^&*() uncomment for production use

        intermediateDifficultyMenuItem = new JRadioButtonMenuItem(difficultyLevel3);
        intermediateDifficultyMenuItem.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (!messageToUser.getText().contains(humanVsHuman)) {
                        messageToUser.setText(computerDifficulty_Text + difficultyLevel3);
                        //!@#$%^&*() INCLUDE SIMILAR CODE IF I WANT TO UPDATE ON CLICK
//                        board.userMessage.setText(messageToUser.getText());
                    }
                }
            }
        });

        hardDifficultyMenuItem = new JRadioButtonMenuItem(difficultyLevel4);
        hardDifficultyMenuItem.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (!messageToUser.getText().contains(humanVsHuman)) {
                        messageToUser.setText(computerDifficulty_Text + difficultyLevel4);
                        //!@#$%^&*() INCLUDE SIMILAR CODE IF I WANT TO UPDATE ON CLICK
//                        board.userMessage.setText(messageToUser.getText());
                    }
                }
            }
        });


//        easyDifficultyMenuItem.setSelected(true); // Remove for production //!@#$%^&*()
//        mediumDifficultyMenuItem.setSelected(true); // Remove for production //!@#$%^&*()
        intermediateDifficultyMenuItem.setSelected(true); // Remove for production //!@#$%^&*()
//        hardDifficultyMenuItem.setSelected(true); // Remove for production //!@#$%^&*()


        difficultyMenu.add(easyDifficultyMenuItem);
        difficultyMenu.add(mediumDifficultyMenuItem);
        difficultyMenu.add(intermediateDifficultyMenuItem);
        difficultyMenu.add(hardDifficultyMenuItem);

        difGroup.add(easyDifficultyMenuItem);
        difGroup.add(mediumDifficultyMenuItem);
        difGroup.add(intermediateDifficultyMenuItem);
        difGroup.add(hardDifficultyMenuItem);

        menuBar.add(difficultyMenu);

        menuBar.add(messageToUser, BorderLayout.LINE_END);

        // Update Message in Board.java
        board.userMessage.setText(messageToUser.getText());
    }

    /**
     * This displays the game rules
     */
    private void showGameRulesScreen() {
        JOptionPane.showMessageDialog(window,
                "<html>" +
                        "<head>" +
                        "<style>" +
                        "p {" +
                        "  width: " + windowDimensions.get("width") / 3 + "px;" +
                        "}" +
                        "ul {" +
                        "  width: " + windowDimensions.get("width") / 3 + "px;" +
                        "}" +
                        "</style>" +
                        "</head>" +
                        "<h2>How To Play:</h2>" +
                        "<p>Choose a player to go first. On your turn, move any one of your checkers by the movement rules described below. " +
                        "After you move one checker, your turn is over. The game continues with players alternating turns.</p>" +
                        "<h2>" +
                        "Movement rules:" +
                        "</h2>" +
                        "<ul>" +
                        "<li>Always move your checker diagonally forward, toward your opponent’s side of the game board</li>" +
                        "<li>After a checker becomes a “King,” it cam one diagonally forward or backward.</li>" +
                        "<li>Move your checker one space diagonally, to an open adjacent square.</li>" +
                        "<li>Jump one or more checkers diagonally to an open square adjacent to the checker you captured.</li>" +
                        "<li>If all squares adjacent to your checker are occupied, your checker is blocked and cannot move.</li>" +
                        "</ul>" +
                        "<h2>" +
                        "Capturing an Opponent’s Checker:" +
                        "</h2>" +
                        "<p>" +
                        "If you can jump an opponent’s checker, you must capture it. Then remove it from the game board and place it in front of you. " +
                        "If another jump is possible, after the first, you must jump again." +
                        "</p>" +
                        "<h2>" +
                        "Becoming a “King”" +
                        "</h2>" +
                        "<p>" +
                        "As soon as one of your checkers reaches the first row on your opponent’s side of the game board, it becomes a King. " +
                        "This is represented by a gold crown. Now this piece can move forward or backward." +
                        "</p>" +
                        "<h2>How to Win:</h2>" +
                        "<p>" +
                        "The first player to capture all opposing checkers from the game board wins the game" +
                        "</p>" +
                        "</html>",
                "Checkers Game Instructions",
                JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * This displays the valid moves for the user
     */
    private void toggleLegalMoveColors() {
        board.toggleLegalMoveColors();
    }

    /**
     * @param isSelected playerColors JRadioButtonMenuItem is selected
     */
    private void playerColors(boolean isSelected) {
        // Player 1 Color
        playersColorString[2] = isSelected ? "red" : "black";
        // Player 2 Color
        playersColorString[8] = isSelected ? "black" : "red";
        board.setPlayerTwoIsBlack(isSelected);
    }
}