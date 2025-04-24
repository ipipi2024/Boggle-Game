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
    private CompressedTrie validWords;
    // initialize BogglePlayer with a file of English words
    public BogglePlayer(String wordFile) throws IOException
    {
      try {
        dictionary = new BufferedReader(new FileReader(wordFile));
        validWords = new CompressedTrie();

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
    private void dfsCompressed(
      char[][] board,
      boolean[][] visited,
      int row,
      int col,
      CompressedNode node,
      StringBuilder currentWord,
      List<Location> currentPath,
      Set<String> foundWords,
      PriorityQueue<Word> wordHeap
  ) {
      if (row < 0 || row >= 4 || col < 0 || col >= 4 || visited[row][col]) {
          return;
      }
  
      // Handle current position (including Q→QU conversion)
      char c = board[row][col];
      String charStr = (c == 'Q') ? "QU" : String.valueOf(c);
      visited[row][col] = true;
      currentPath.add(new Location(row, col));
  
      // Check all child nodes
      for (CompressedNode child : node.children.values()) {
          if (child.text.startsWith(charStr)) {
              // Save current length for backtracking
              int wordLengthBefore = currentWord.length();
              int pathLengthBefore = currentPath.size();
  
              // Add the matching characters
              currentWord.append(charStr);
  
              if (child.text.length() > charStr.length()) {
                  // Handle multi-character sequences from compressed trie
                  String remaining = child.text.substring(charStr.length());
                  List<Location> extendedPath = new ArrayList<>(currentPath);
                  boolean[][] extendedVisited = copyVisitedArray(visited);
  
                  if (matchRemaining(board, extendedVisited, row, col, remaining, extendedPath)) {
                      currentWord.append(remaining);
  
                      if (child.isWord && currentWord.length() >= 3) {
                          String word = currentWord.toString();
                          if (!foundWords.contains(word)) {
                              Word newWord = new Word(word);
                              newWord.setPath(new ArrayList<>(extendedPath));
                              wordHeap.add(newWord);
                              foundWords.add(word);
                          }
                      }
  
                      // Continue DFS from end of extended path
                      Location last = extendedPath.get(extendedPath.size() - 1);
                      dfsCompressed(board, extendedVisited, last.row, last.col, child,
                                  currentWord, extendedPath, foundWords, wordHeap);
                  }
                  // Backtrack: reset to length before this child was processed
                  currentWord.setLength(wordLengthBefore);
              } else {
                  // Complete match at current node
                  if (child.isWord && currentWord.length() >= 3) {
                      String word = currentWord.toString();
                      if (!foundWords.contains(word)) {
                          Word newWord = new Word(word);
                          newWord.setPath(new ArrayList<>(currentPath));
                          wordHeap.add(newWord);
                          foundWords.add(word);
                      }
                  }
  
                  // Explore all 8 directions
                  for (int dr = -1; dr <= 1; dr++) {
                      for (int dc = -1; dc <= 1; dc++) {
                          if (dr == 0 && dc == 0) continue;
                          dfsCompressed(board, visited, row + dr, col + dc, child,
                                      currentWord, currentPath, foundWords, wordHeap);
                      }
                  }
                  // Backtrack: reset to length before this child was processed
                  currentWord.setLength(wordLengthBefore);
              }
          }
      }
  
      // Backtrack
      visited[row][col] = false;
      currentPath.remove(currentPath.size() - 1);
  }

private boolean matchRemaining(
    char[][] board,
    boolean[][] visited,
    int row,
    int col,
    String remaining,
    List<Location> path
) {
    if (remaining.isEmpty()) {
        return true;
    }

    // Handle Q→QU
    String nextChar = remaining.substring(0, 1);
    if (nextChar.equals("Q") && remaining.length() > 1 && remaining.charAt(1) == 'U') {
        nextChar = "QU";
    }
    int charLength = nextChar.length();

    // Check all 8 directions
    for (int dr = -1; dr <= 1; dr++) {
        for (int dc = -1; dc <= 1; dc++) {
            if (dr == 0 && dc == 0) continue;

            int newRow = row + dr;
            int newCol = col + dc;

            if (newRow < 0 || newRow >= 4 || newCol < 0 || newCol >= 4 || visited[newRow][newCol]) {
                continue;
            }

            char c = board[newRow][newCol];
            String boardChar = (c == 'Q') ? "QU" : String.valueOf(c);

            if (boardChar.equals(nextChar)) {
                visited[newRow][newCol] = true;
                path.add(new Location(newRow, newCol));

                boolean matched = matchRemaining(
                    board, visited, newRow, newCol,
                    remaining.substring(charLength), path
                );

                if (matched) {
                    return true;
                }

                // Backtrack if this path didn't work
                visited[newRow][newCol] = false;
                path.remove(path.size() - 1);
            }
        }
    }
    return false;
}


private boolean[][] copyVisitedArray(boolean[][] original) {
  boolean[][] copy = new boolean[4][4];
  for (int i = 0; i < 4; i++) {
      System.arraycopy(original[i], 0, copy[i], 0, 4);
  }
  return copy;
}

  public Word[] getWords(char[][] board) {
    PriorityQueue<Word> wordHeap = new PriorityQueue<>((w1, w2) -> Integer.compare(w2.getWord().length(), w1.getWord().length()));

    Set<String> foundWords = new HashSet<>();

    for (int row = 0; row < 4; row++) {
      for (int col = 0; col < 4; col++) {
        boolean[][] visited = new boolean[4][4];
        dfsCompressed(board, visited, row, col, validWords.getRoot(), new StringBuilder(), new ArrayList<>(), foundWords, wordHeap);
      }
    }

    // Convert to array in descending order of length
    Word[] result = new Word[Math.min(20, wordHeap.size())];
    for (int i = 0; i < result.length; i++) {
      result[i] = wordHeap.poll();
      System.out.println(result[i].getWord() + " " + result[i].getPath().size());
    }
      return result;
    }

  
    
  


}