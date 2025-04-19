import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;

public class test {
    // The dice configuration used in Boggle
    private static final ArrayList<String> boggleDices = new ArrayList<>(Arrays.asList(
            "AAEEGN", "ABBJOO", "ACHOPS", "AFFKPS", "AOOTTW", "CIMOTU", "DEILRX", "DELRVY",
            "DISTTY", "EEGHNW", "EEINSU", "EHRTVW", "EIOSST", "ELRTTY", "HIMNUQ", "HLNNRZ"));

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java test <dictionary_file> [seed]");
            System.exit(1);
        }

        String dictionaryFile = args[0];
        long seed = 123456789; // Default seed
        
        // Allow custom seed if provided
        if (args.length > 1) {
            try {
                seed = Long.parseLong(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid seed. Using default seed.");
            }
        }
        
        // Initialize the player with the dictionary
        System.out.println("Loading dictionary from " + dictionaryFile + "...");
        BogglePlayer player = new BogglePlayer(dictionaryFile);
        
        // Generate a random board using the same algorithm as in EvalBogglePlayer
        char[][] board = generateBoard(seed);
        
        // Print the board
        System.out.println("\nBoggle Board:");
        printBoard(board);
        
        // Find words on the board
        System.out.println("\nFinding words...");
        Word[] words = player.getWords(board);
        
        // Display results
        System.out.println("\nFound " + words.length + " words:");
        
        // Sort words by length (longest first) to match the expected output order
        Arrays.sort(words, (w1, w2) -> {
            int score1 = calculateScore(w1.getWord().length());
            int score2 = calculateScore(w2.getWord().length());
            return score2 - score1;
        });
        
        // Print each word with its score and path
        for (int i = 0; i < words.length; i++) {
            Word word = words[i];
            int score = calculateScore(word.getWord().length());
            
            System.out.println((i+1) + ". " + word.getWord() + " (Score: " + score + ")");
            
            // Print the path
            System.out.print("   Path: ");
            for (int j = 0; j < word.getPathLength(); j++) {
                System.out.print("(" + word.getLetterRow(j) + "," + word.getLetterCol(j) + ")");
                if (j < word.getPathLength() - 1) {
                    System.out.print(" -> ");
                }
            }
            System.out.println();
        }
        
        // Calculate total score
        int totalScore = 0;
        for (Word word : words) {
            totalScore += calculateScore(word.getWord().length());
        }
        
        System.out.println("\nTotal Score: " + totalScore);
    }
    
    // Generate a random Boggle board
    private static char[][] generateBoard(long seed) {
        Random rnd = new Random(seed);
        char[][] board = new char[4][4];
        ArrayList<String> dicesCopy = new ArrayList<>(boggleDices); // Make a copy to modify
        
        int length = dicesCopy.size();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int diceIndex = rnd.nextInt(length);
                String dice = dicesCopy.get(diceIndex);
                board[i][j] = dice.charAt(rnd.nextInt(6));
                
                // Swap the used dice with the last unused dice
                dicesCopy.set(diceIndex, dicesCopy.get(length - 1));
                dicesCopy.set(length - 1, dice);
                length--;
            }
        }
        
        return board;
    }
    
    // Print the Boggle board in a nice format using ASCII characters
    private static void printBoard(char[][] board) {
        System.out.println("+---+---+---+---+");
        for (int i = 0; i < 4; i++) {
            System.out.print("|");
            for (int j = 0; j < 4; j++) {
                char letter = board[i][j];
                // Handle 'Q' specially to display "QU"
                if (letter == 'Q') {
                    System.out.print(" QU");
                } else {
                    System.out.print(" " + letter + " ");
                }
                System.out.print("|");
            }
            if (i < 3) {
                System.out.println("\n+---+---+---+---+");
            }
        }
        System.out.println("\n+---+---+---+---+");
    }
    
    // Calculate the score for a word based on its length
    private static int calculateScore(int length) {
        if (length < 3) return 0;
        return (length - 2) * (length - 2);
    }
}