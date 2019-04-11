import java.awt.*;
import java.util.HashMap;

/**
 * All Program (Multi-Used) Constants are Stored HERE
 */
public final class Constants {

    private Constants() {
        // restrict instantiation
    }

    public static final int defaultWindowHeight = 750,
            defaultWindowWidth = 850,//1000,
            defaultNumRowsAndColumns = 8;
    public static final int defaultinitialX = 100,
            defaultinitialY = 50,
            default_squareSize = 80,
            default_pieceSize = 60;

    public static final boolean default_SingleAI = true;
    public static final Color backgroundColor = Color.decode("#492A1B");

    public static final HashMap<String, String> colorStringMap = new HashMap<>() {{
        put("RED", "RED");
        put("BLACK", "BLACK");
    }};
    public static final Color gameBlack = Color.BLACK.brighter(),
            gameRed = Color.RED.darker();

    public static final Color darkColor = Color.decode("#9D7D5C");
    public static final Color lightColor = Color.decode("#F1EBDE");
    public static final Color emptyPieceColor = new Color(0, 0, 0, 0),
            legalMovePieceColor = Color.ORANGE,
            legalMoveColor = Color.MAGENTA,
            selectedPiece = Color.WHITE,
            selectedPieceLegalMove = Color.GREEN;
    public static final float legalMoveBorder = 1.9f,
            selectedLegalMoveBorder = 1.9f,
            selectedPieceBorder = 2f;

    public static final int
            EMPTY = 0,
            RED = 1,
            BLACK = 3;

    public static final String computerDifficulty_Text = "Computer Difficulty: ";
    public static final String computerVsHuman = "Computer Vs Human",
            computerVsComputer = "Computer Vs Computer";
    public static final String[] difficultyLevels = {"Human Vs Human", "Easy", "Medium", "Intermediate", "Hard"};

    // 0 - Human, 1 - Easy, 2 - Medium, 3 - Intermediate, 4 - Hard
    public static final int defaultGameDifficulty = 2,
            difficulty_ZERO = 0,
            difficulty_Easy = 1,
            difficulty_Medium = 2,
            difficulty_Intermediate = 3,
            difficulty_Hard = 4;

    public static final int default_COMPUTER_MOVE_DELAY_IN_MILLISECONDS = 1000,
            default_COMPUTER_JUMP_DELAY_IN_MILLISECONDS = 1000;


    // Variables determining Random changes in Heuristic
    public static final int RMIN = -10, RMAX = 10;

}