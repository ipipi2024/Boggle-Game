/*

  Authors (group members):
  Email addresses of group members:
  Group name:

  Course:
  Section:

  Description of the overall algorithm and key data structures:


*/

import java.io.*;
import java.util.*;


public class BogglePlayer 
{
    private BufferedReader dictionary;
    private Trie validWords;
    public wordComparator wC = new wordComparator();
    // initialize BogglePlayer with a file of English words
    public BogglePlayer(String wordFile) throws IOException
    {
      try {
        dictionary = new BufferedReader(new FileReader(wordFile));
        validWords = new Trie();

        String curr = dictionary.readLine();
        /* If the word is above length 2, conver to upper case and add */
        while (curr != null) {
          if (curr.length() > 2) {
            curr = curr.toUpperCase();
            validWords.addWord(curr);
          }
          curr = dictionary.readLine();
        }
      } catch (FileNotFoundException e) {
        System.out.println(e+" --- Wrong file name");
      }
    }





    /* For debugging */
    public void printBoard(char[][] board) {
      for (int row = 0; row < board.length; row ++) {
        System.out.println();
        for (int col = 0; col < board[0].length; col++) {
          System.out.print(board[row][col]+" ");
        }
      }

    }





    // based on the board, find valid words
    //
    // board: 4x4 board, each element is a letter, 'Q' represents "QU", 
    //    first dimension is row, second dimension is column
    //    ie, board[row][col]     
    //
    // Return at most 20 valid words in UPPERCASE and 
    //    their paths of locations on the board in myWords;
    //
    // See Word.java for details of the Word class and
    //     Location.java for details of the Location class






    /* Gets the surrounding characters in a square of the target
     * Iterates over all of possible, but if out of bounds doesnt add
     */
    private Queue<Word> getSurrounding(Word target, int row, int col, char[][] board) {
      /* Go around in a square of the current indexes and add to queue */
      Queue<Word> adj = new LinkedList<>();
      for (int i = row - 1; i <= row+1 && i < 4; i++) {
        for (int j = col-1; j <= col+1 && j < 4; j++) {
          /* If in bounds and isnt already on the path */
          if ((i >= 0 && j >= 0) && target.isUnique(i, j)) {
            /* New word */
            Word potential = new Word(target);
            /* Set the new word */
            potential.setWord(potential.getWord() + board[i][j]);
            /* Add it to path */
            potential.addLetterRowAndCol(i, j);
            /* Add it to the queue */
            adj.add(potential);
          }
        }
      }
      return adj;
    }

    public Word[] getWords(char[][] board)
    {
      /* Priority queue is to return the longest words, aka the most points */
      PriorityQueue<Word> pQ = new PriorityQueue<>(wC);

	    Word[] myWords = new Word[20];  // assuming 20 words are found
      int wordCounter = 0;

      /* Iterates over the entire board */
      for (int row = 0; row < 4; row ++) {
        for (int col = 0; col < 4; col++) {
          /* Create a new word */
          Word start = new Word(""+board[row][col]);
          /* Add path */
          start.addLetterRowAndCol(row, col);
          /* Get the surrounding characters */
          Queue<Word> potentialWords = getSurrounding(start, row, col, board);
          while (!potentialWords.isEmpty()) {
            /* Will discover and compare all possible word paths from the row col */
            Word current = potentialWords.remove();
            Node res = validWords.search(current.getWord());
            /* If res is null, that means that prefix doesnt exist so that word doesnt exist */
            if (res != null) {
              if (res.getEnd() == true) {
                pQ.add(current);
              }
              /* Its possible that words like different and differentiate exist, so you keep searching with that prefix */
              Location loc = current.getLetterLocation(current.getPathLength()-1);
              Queue<Word> temp = getSurrounding(current, loc.row, loc.col, board);
              potentialWords.addAll(temp);
            }
          }
        }
      }

      /* Add top 20 words to the myWords */
      while (!pQ.isEmpty() && wordCounter < 20) {
        myWords[wordCounter] = pQ.remove();
        //System.out.println(myWords[wordCounter].getWord());
        wordCounter++;
      }


        return myWords;
    }

    /* Comparator for the priority queue */
    static class wordComparator implements Comparator<Word> {
      @Override
      public int compare(Word ob1, Word ob2) {
          return Integer.compare(ob2.getWord().length(), ob1.getWord().length());
      }
  }
}