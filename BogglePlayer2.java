/*
  Authors : Your name(s)
  Email addresses of group members: Your email(s)
  Group name: Chaining It

  Course: CSE 2010
  Section: E1

  Description of the overall algorithm and key data structures:
  
  This implementation uses a memory-optimized Trie data structure to efficiently store and search the dictionary. 
  The Trie enables quick prefix lookups which helps prune invalid paths early during board traversal.
  
  For finding words on the board, we use depth-first search (DFS) from each position on the board.
  The DFS algorithm recursively explores adjacent cells while tracking visited positions to avoid using
  the same position multiple times for a single word.

  Memory optimizations include:
  - Using array-based Trie nodes instead of HashMap
  - Using primitive arrays instead of objects where possible
  - Pre-sizing collections to avoid dynamic resizing
  - Using character arrays instead of StringBuilder for word construction
  - Special handling for Q-U to reduce object creation
  
  The implementation collects all valid words during board traversal and then selects the top 20
  highest-scoring words (prioritizing longer words) for the final result.
*/

import java.util.*;
import java.io.*;

public class BogglePlayer2 {
  // Trie data structure for dictionary lookup
  private TrieNode root;

  // Direction arrays for the 8 adjacent cells (horizontal, vertical, diagonal)
  private static final int[] ROW_DIR = { -1, -1, -1, 0, 0, 1, 1, 1 };
  private static final int[] COL_DIR = { -1, 0, 1, -1, 1, -1, 0, 1 };
  
  // Max word length on a 4x4 board is 16
  private static final int MAX_WORD_LENGTH = 16;

  // Inner class representing a node in the Trie
  private class TrieNode {
    private final TrieNode[] children;
    private boolean isEndOfWord;

    public TrieNode() {
      // Only uppercase letters (26 characters)
      children = new TrieNode[26]; 
      isEndOfWord = false;
    }
  }

  // Initialize BogglePlayer with a file of English words
  public BogglePlayer2(String wordFile) {
    root = new TrieNode();
    try {
      // Read the dictionary file
      BufferedReader reader = new BufferedReader(new FileReader(wordFile));
      String line;
      while ((line = reader.readLine()) != null) {
        // Convert to uppercase and insert into Trie
        insertWord(line.toUpperCase());
      }
      reader.close();
    } catch (IOException e) {
      System.err.println("Error reading dictionary file: " + e.getMessage());
    }
  }

  // Insert a word into the Trie
  private void insertWord(String word) {
    TrieNode current = root;

    for (int i = 0; i < word.length(); i++) {
      char c = word.charAt(i);
      int index = c - 'A'; // Convert char to array index (0-25)
      
      // Skip invalid characters (non A-Z)
      if (index < 0 || index >= 26) continue;
      
      // If the node doesn't exist, create a new one
      if (current.children[index] == null) {
        current.children[index] = new TrieNode();
      }

      current = current.children[index];

      // Special case: If the character is 'Q', automatically add 'U'
      if (c == 'Q' && i + 1 < word.length() && word.charAt(i + 1) == 'U') {
        int uIndex = 'U' - 'A';
        if (current.children[uIndex] == null) {
          current.children[uIndex] = new TrieNode();
        }
        current = current.children[uIndex];
        i++; // Skip the 'U'
      }
    }

    // Mark the end of a word
    current.isEndOfWord = true;
  }

  // Based on the board, find valid words
  public Word[] getWords(char[][] board) {
    // Use a Set with initial capacity to efficiently track duplicates
    // Assuming a reasonable number of possible words
    Set<String> foundWordStrings = new HashSet<>(500);

    // PriorityQueue to store found words prioritized by score (longer words first)
    // Initial capacity hint helps reduce resizing
    PriorityQueue<Word> foundWords = new PriorityQueue<>(50, (w1, w2) -> {
      int score1 = calculateScore(w1.getWord().length());
      int score2 = calculateScore(w2.getWord().length());
      return score2 - score1; // Higher score (longer words) first
    });

    // Visited cells matrix to track the path
    boolean[][] visited = new boolean[4][4];
    
    // Reusable buffers for DFS to avoid creating objects in recursion
    char[] wordBuffer = new char[MAX_WORD_LENGTH * 2]; // *2 to handle QU case
    int[] pathRows = new int[MAX_WORD_LENGTH];
    int[] pathCols = new int[MAX_WORD_LENGTH];

    // Try starting DFS from each cell on the board
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        // Start DFS from this cell with the root node of the Trie
        dfs(board, i, j, visited, wordBuffer, 0, pathRows, pathCols, 0, 
            foundWords, foundWordStrings, root);
      }
    }

    // Extract the top 20 words (at most) from the priority queue
    int resultSize = Math.min(20, foundWords.size());
    Word[] myWords = new Word[resultSize];
    for (int i = 0; i < resultSize; i++) {
      myWords[i] = foundWords.poll();
    }

    return myWords;
  }

  // Calculate score based on word length: (length - 2)^2
  private int calculateScore(int length) {
    if (length < 3)
      return 0;
    return (length - 2) * (length - 2);
  }

  // Memory-optimized DFS to find words on the board
  private void dfs(char[][] board, int row, int col, boolean[][] visited,
                  char[] wordBuffer, int wordLength, int[] pathRows, int[] pathCols, int pathLength,
                  PriorityQueue<Word> foundWords, Set<String> foundWordStrings, TrieNode currentNode) {

    // Bounds check
    if (row < 0 || row >= 4 || col < 0 || col >= 4 || visited[row][col]) {
      return;
    }

    // Get the current letter
    char letter = board[row][col];
    
    // Convert letter to index
    int index = letter - 'A';
    
    // Skip invalid characters
    if (index < 0 || index >= 26) return;

    // Check if this letter exists in the Trie from our current position
    TrieNode nextNode = currentNode.children[index];

    // If letter doesn't exist in Trie at this point, backtrack immediately
    if (nextNode == null) {
      return;
    }

    // Handle the letter (special case for Q)
    if (letter == 'Q') {
      // Check if U exists after Q
      int uIndex = 'U' - 'A';
      TrieNode uNode = nextNode.children[uIndex];
      if (uNode == null) {
        return; // U doesn't follow Q in the Trie, so this path is invalid
      }
      
      // Add QU to the word buffer
      wordBuffer[wordLength] = 'Q';
      wordBuffer[wordLength + 1] = 'U';
      wordLength += 2;
      
      nextNode = uNode; // Move to the node after U
    } else {
      // Add current letter to word buffer
      wordBuffer[wordLength] = letter;
      wordLength++;
    }

    // Add current position to the path arrays
    pathRows[pathLength] = row;
    pathCols[pathLength] = col;
    pathLength++;

    // Mark current cell as visited
    visited[row][col] = true;

    // Check if we've found a complete word of at least 3 letters
    if (wordLength >= 3 && nextNode.isEndOfWord) {
      // Create the string only when we've found a valid word
      String wordFound = new String(wordBuffer, 0, wordLength);
      
      // Only add if we haven't seen this word before
      if (!foundWordStrings.contains(wordFound)) {
        foundWordStrings.add(wordFound);

        // Create a Word object
        Word word = new Word(wordFound);

        // Convert path arrays to Location objects
        ArrayList<Location> path = new ArrayList<>(pathLength);
        for (int i = 0; i < pathLength; i++) {
          path.add(new Location(pathRows[i], pathCols[i]));
        }
        word.setPath(path);

        // Add to our collection of found words
        foundWords.add(word);
      }
    }

    // Continue DFS in all 8 directions with the next node in the Trie
    for (int i = 0; i < 8; i++) {
      int newRow = row + ROW_DIR[i];
      int newCol = col + COL_DIR[i];

      dfs(board, newRow, newCol, visited, wordBuffer, wordLength, 
          pathRows, pathCols, pathLength, foundWords, foundWordStrings, nextNode);
    }

    // Backtrack: mark cell as unvisited (no need to clean wordBuffer or path arrays)
    visited[row][col] = false;
  }
}