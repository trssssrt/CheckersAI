import java.awt.*;

public final class Constants {

    private Constants() {
        // restrict instantiation
    }

    public static final int defaultWindowHeight = 750,
            defaultWindowWidth = 1000,
            defaultNumRowsAndColumns = 8;
    public static final int defaultinitialX = 100,
            defaultinitialY = 50,
            default_squareSize = 80,
            default_pieceSize = 60;

    public static final boolean default_SingleAI = true;
    public static final Color backgroundColor = Color.decode("#492A1B");



    public static final Color gameBlack = Color.BLACK.brighter(),
            gameRed = Color.RED.darker();
//    public static final Color gameRed = Color.BLACK.brighter(),
//            gameBlack= Color.RED.darker();

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
            RED_KING = 2,
            BLACK = 3,
            BLACK_KING = 4;

    // 0 - Human, 1 - Easy, 2 - Medium, 3 - Intermediate, 4 - Hard
    public static final int defaultGameDifficulty = 3,
            difficulty_ZERO = 0,
            difficulty_Easy = 1,
            difficulty_Medium = 2,
            difficulty_Intermediate = 3,
            difficulty_Hard = 4;

    public static final int default_COMPUTER_MOVE_DELAY_IN_MILLISECONDS = 500,
            default_COMPUTER_JUMP_DELAY_IN_MILLISECONDS = 100;

}