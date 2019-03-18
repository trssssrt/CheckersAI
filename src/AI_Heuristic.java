public class AI_Heuristic {
    private int difficulty;

    AI_Heuristic(int difficulty) {
        this.difficulty = difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public Move selectMove(Move[] legalMoves) {
        if (difficulty > 1) {
            System.out.println("CURRENT DIFFICULTY: " + difficulty);
            return legalMoves[1];
        } else {
//            double r = Math.random() * legalMoves.length;
//            System.out.println(r + " - " + (int) r +" - " + legalMoves.length + " - " + difficulty);
//            return legalMoves[0];
//            return legalMoves[(int) r];
            return legalMoves[(int) Math.random() * legalMoves.length];
        }
    }
}
