import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/*
  Modified by: Claude
  Original Author: Taher Patanwala
  Pseudocode: Philip Chan

  Usage: EvalBogglePlayer wordFile [numSeeds] [startSeed]

  Input:
  wordFile has valid words, one on each line
  numSeeds is the number of different seeds to use for testing [optional, default = 5]
  startSeed is the first seed value [optional, default = 123456789]

  Description:
  The goal is to evaluate BogglePlayer across multiple random boards
  Validity and points for words are in the assignment.

  The performance of BogglePlayer is measured by:
  a.  totalPoints: total points of found words
  b.  speed: time in second for finding words
  c.  space consumption: memory consumption
  d.  overall score--(totalPoints^2)/sqrt(time * memory)  
*/

public class test {
    private static ArrayList<String> dictionary = new ArrayList<>();
    private static final ArrayList<String> boggleDices = new ArrayList<>(Arrays.asList(
            "AAEEGN", "ABBJOO", "ACHOPS", "AFFKPS", "AOOTTW", "CIMOTU", "DEILRX", "DELRVY",
            "DISTTY", "EEGHNW", "EEINSU", "EHRTVW", "EIOSST", "ELRTTY", "HIMNUQ", "HLNNRZ"));

    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 3) {
            System.err.println("Usage: EvalBogglePlayer wordFile [numSeeds] [startSeed]");
            System.exit(-1);
        }

        // for getting cpu time
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        if (!bean.isCurrentThreadCpuTimeSupported()) {
            System.err.println("cpu time not supported, use wall-clock time:");
            System.err.println("Use System.nanoTime() instead of bean.getCurrentThreadCpuTime()");
            System.exit(-1);
        }

        // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
        DecimalFormat df = new DecimalFormat("0.####E0");
        
        //Preprocessing in BogglePlayer
        System.out.println("Preprocessing in BogglePlayer...");
        long startPreProcTime = bean.getCurrentThreadCpuTime();
        BogglePlayer player = new BogglePlayer(args[0]);
        long endPreProcTime = bean.getCurrentThreadCpuTime();

        //Stop if pre-processing runs for more than 3 minutes.
        double processingTimeInSec = (endPreProcTime - startPreProcTime) / 1E9;
        if (processingTimeInSec > 180) {
            System.err.println("Preprocessing time \"" + processingTimeInSec + " sec\" is too long--more than 3 minutes");
            System.exit(-1);
        }

        // report time and memory spent on preprocessing
        System.out.println("Pre-processing in seconds (not part of score): " + df.format(processingTimeInSec));
        runtime.gc();
        System.out.println("memory in bytes (not part of score): " + df.format((double)peakMemoryUsage()));

        // Number of seeds to use for evaluation
        int numSeeds = 5;  // default
        if (args.length >= 2) {
            numSeeds = Integer.parseInt(args[1]);
        }
        
        // Starting seed value
        long startSeed = 123456789;  // default
        if (args.length == 3) {
            startSeed = Long.parseLong(args[2]);
        }
        
        System.out.println("Playing Boggle with " + numSeeds + " different seeds...");
        System.out.println("------------------------------------------------------");

        // Variables to track aggregate statistics
        double totalPoints = 0;
        double totalTime = 0;
        double totalMemory = 0;
        double totalScore = 0;
        
        // Run evaluation for each seed
        for (int seedIdx = 0; seedIdx < numSeeds; seedIdx++) {
            long currentSeed = startSeed + seedIdx;
            System.out.println("Seed #" + (seedIdx + 1) + " (" + currentSeed + "):");
            
            Random rnd = new Random(currentSeed);

            // Generate random board, and pass it to the BogglePlayer to play Boggle
            char[][] board = new char[4][4],
                     boardCopy = new char[4][4];
            
            // Create a deep copy of the dice list for each seed
            ArrayList<String> seedDices = new ArrayList<>(boggleDices);
            int length = seedDices.size();

            // Create random board
            // make a copy of the board for BogglePlayer
            // so BogglePlayer can't change the provided board
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    int diceIndex = rnd.nextInt(length);
                    String dice = seedDices.get(diceIndex);
                    board[i][j] = dice.charAt(rnd.nextInt(6));
                    boardCopy[i][j] = board[i][j];
                    seedDices.set(diceIndex, seedDices.get(length - 1));
                    seedDices.set(length - 1, dice);
                    length--;
                }
            }
            
            // Print the current board
            System.out.println("  Board:");
            for (int i = 0; i < 4; i++) {
                System.out.print("  ");
                for (int j = 0; j < 4; j++) {
                    System.out.print(board[i][j] + " ");
                }
                System.out.println();
            }

            // Calculate the time taken to find the words on the board
            long startTime = bean.getCurrentThreadCpuTime();
            // Play the game of Boggle and find the words
            Word[] words = player.getWords(boardCopy);
            long endTime = bean.getCurrentThreadCpuTime();
            
            // Calculate the used memory
            runtime.gc();
            long memory = peakMemoryUsage();

            double elapsedTime = (endTime - startTime) / 1.0E9;
            if (elapsedTime > 180) { // longer than 3 minutes
                System.err.println("player.getWords() exceeded 3 minutes");
                System.exit(-1);
            }

            if (elapsedTime <= 0) { // too small to measure, unlikely
                System.err.println("Zero time usage was reported; please rerun");
                System.exit(-1);
            }
            
            if (memory <= 0) { // too small to measure, highly unlikely
                System.err.println("Zero memory usage was reported; please rerun");
                System.exit(-1);
            }

            // Read the dictionary if this is the first iteration
            if (seedIdx == 0) {
                BufferedReader file = new BufferedReader(new FileReader(args[0]));
                String line;
                while ((line = file.readLine()) != null) {
                    dictionary.add(line.toUpperCase());
                }
                file.close();
            }

            // Calculate points for the words found
            int points = calculatePoints(words, board);
            double score = (points * points) / Math.sqrt(elapsedTime * memory);
            
            // Print results for this seed
            System.out.printf("  Points: %d\n", points);
            System.out.println("  Time in seconds: " + df.format(elapsedTime));
            System.out.println("  Used memory in bytes: " + df.format((double)memory));
            System.out.printf("  Score: %.4f\n", score);
            System.out.println("------------------------------------------------------");
            
            // Accumulate totals
            totalPoints += points;
            totalTime += elapsedTime;
            totalMemory += memory;
            totalScore += score;
        }
        
        // Calculate and display average results
        double avgPoints = totalPoints / numSeeds;
        double avgTime = totalTime / numSeeds;
        double avgMemory = totalMemory / numSeeds;
        double avgScore = totalScore / numSeeds;
        
        System.out.println("\nAVERAGE RESULTS ACROSS " + numSeeds + " SEEDS:");
        System.out.println("------------------------------------------------------");
        System.out.printf("Average Points: %.2f\n", avgPoints);
        System.out.println("Average Time in seconds: " + df.format(avgTime));
        System.out.println("Average Memory in bytes: " + df.format(avgMemory));
        System.out.printf("Average Score: %.4f\n", avgScore);
        
        // Keep player used to avoid garbage collection of player
        BogglePlayer player2 = player;
    }

    /**
     * Calculates the points for the words found on the board
     *
     * @param words The list of words whose points are to be calculated
     * @param board The board on which the words were found
     * @return Returns the number of points
     */
    private static int calculatePoints(Word[] words, char[][] board) {
        int points = 0;

        if (words == null) {
            System.out.println("Your word list is null");
            return(0);
        }
            
        int wordListLen = words.length;    
        if (wordListLen < 0) {
            System.out.printf("Your word list has negative length: %d\n", wordListLen);
            return 0;  
        } else if (wordListLen > 20) {
            points -= (wordListLen - 20);  // Penalty if more than 20 words were returned
            wordListLen = 20;              // only the first 20 are counted
        }

        // Calculate points for the first 20 words, or fewer
        for (int index = 0; index < wordListLen; index++) {
            Word w = words[index];
            if (w != null) {
                // Check if the word is unique
                boolean duplicate = false;
                for (int i = 0; i < index; i++) {
                    if (w.getWord().equals(words[i].getWord())) {
                        duplicate = true;
                        break;
                    }
                }

                // If the word is duplicate then give penalty, else check if word is valid
                if (duplicate) {
                    points -= (w.getPathLength() - 2) * (w.getPathLength() - 2);
                } else {
                    points += checkForWordValidity(w, board);
                }
            } else { // word is null
                points--;
            }
        }

        if (points < 0)
            points = 0;

        return points;
    }

    /**
     * Checks if the word is valid and assigns positive points for valid word and negative points for invalid word
     *
     * @param word  The word that is to be evaluated
     * @param board The board on which the word was found
     * @return Positive or negative points for the word
     */
    private static int checkForWordValidity(Word word, char[][] board) {
        int length = word.getWord().length();

        // Check if path length is same as word length
        int numberOfQs = 0;
        for (int i = 0; i < word.getWord().length(); i++) {
            if(word.getWord().charAt(i) == 'Q'){
                numberOfQs++;
            }
        }
        if(length != word.getPathLength() + numberOfQs){
            return -((length - 2) * (length - 2));
        }

        // Check if word has at least 3 letters and at most 16 characters
        if (length < 3)
            return -1;
        if (length > 16)
            return -((length - 2) * (length - 2));
    
        // Check if the letters of the word are adjacent on the board
        for (int i = 1; i < word.getPathLength(); i++) {
            if (squareDistance(word.getLetterLocation(i - 1), word.getLetterLocation(i)) > 2) {
                return -((length - 2) * (length - 2));
            }
        }

        // Check each letter on the board is used at most once and if letter are are the board or not
        boolean[][] used = new boolean[4][4];
        for (int i = 0, letterIndex = 0; i < word.getPathLength(); i++, letterIndex++) {
            int row = word.getLetterRow(i);
            int col = word.getLetterCol(i);

            if (used[row][col] || board[row][col] != word.getWord().charAt(letterIndex))
                return -((length - 2) * (length - 2));
            else
                used[row][col] = true;
            if (word.getWord().charAt(letterIndex) == 'Q')
                letterIndex++;
        }

        // Check if word exists in the dictionary
        if (!dictionary.contains(word.getWord().toUpperCase())) {
            return -((length - 2) * (length - 2));
        }

        return (length - 2) * (length - 2);
    }

    /**
     * Calculates the square distance between the two location objects
     *
     * @param l1 The first Location
     * @param l2 The second Location
     * @return Returns the distance square the two locations
     */
    private static int squareDistance(Location l1, Location l2) {
        return (l1.row - l2.row) * (l1.row - l2.row) + (l1.col - l2.col) * (l1.col - l2.col);
    }

    /**
     * Return peak memory usage in bytes
     *
     * adapted from:
     * https://stackoverflow.com/questions/34624892/how-to-measure-peak-heap-memory-usage-in-java 
     * https://docs.oracle.com/javase/8/docs/api/java/lang/management/MemoryType.html
     * only two memory types: HEAP and NON_HEAP (including JVM)
     * measuring only HEAP
     */
    private static long peakMemoryUsage() {
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        long total = 0;
        for (MemoryPoolMXBean memoryPoolMXBean : pools) {
            if (memoryPoolMXBean.getType() == MemoryType.HEAP) {
                long peakUsage = memoryPoolMXBean.getPeakUsage().getUsed();
                // System.out.println("Peak used for: " + memoryPoolMXBean.getName() + " is: " + peakUsage);
                total = total + peakUsage;
            }
        }
        return total;
    }
}