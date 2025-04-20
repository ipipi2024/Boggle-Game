/*
  Authors : Your name(s)
  Email addresses of group members: Your email(s)
  Group name: Chaining It

  Course: CSE 2010
  Section: E1

  Description of the overall algorithm and key data structures:
  
  This implementation uses a Trie data structure to efficiently store and search the dictionary. 
  The Trie enables quick prefix lookups which helps prune invalid paths early during board traversal.
  
  For finding words on the board, we use depth-first search (DFS) from each position on the board.
  The DFS algorithm recursively explores adjacent cells while tracking visited positions to avoid using
  the same position multiple times for a single word.

  Hashset is used to store found words to do O(1) lookup for duplicate checking
  
  The implementation collects all valid words during board traversal and then selects the top 20
  highest-scoring words (prioritizing longer words) for the final result.
*/

import java.util.*;
import java.io.*;

public class BogglePlayer {
  // Trie data structure for dictionary lookup
  private TrieNode root;

  // Direction arrays for the 8 adjacent cells (horizontal, vertical, diagonal)
  private static final int[] ROW_DIR = { -1, -1, -1, 0, 0, 1, 1, 1 };
  private static final int[] COL_DIR = { -1, 0, 1, -1, 1, -1, 0, 1 };

  // Inner class representing a node in the Trie
  private class TrieNode {
    private final Map<Character, TrieNode> children;
    private boolean isEndOfWord;

    public TrieNode() {
      children = new HashMap<>();
      isEndOfWord = false;
    }
  }

  // Initialize BogglePlayer with a file of English words
  public BogglePlayer(String wordFile) {
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
  // Insert a word into the Trie
  private void insertWord(String word) {
    TrieNode current = root;

    for (int i = 0; i < word.length(); i++) {
      char c = word.charAt(i);
      TrieNode node = current.children.get(c);

      // If the node doesn't exist, create a new one
      if (node == null) {
        node = new TrieNode();
        current.children.put(c, node);

        // Special case: If the character is 'Q', automatically add 'U' as a child
        if (c == 'Q') {
          TrieNode uNode = new TrieNode();
          node.children.put('U', uNode);
          current = uNode; // Set current to uNode to continue from 'U'
          continue; // Skip the current = node at the end since we've set current = uNode
        }
      }

      current = node;
    }

    // Mark the end of a word
    current.isEndOfWord = true;
  }

  // Modified method that returns the node at the end of prefix path
  private TrieNode prefixNode(String prefix) {
    TrieNode current = root;

    for (int i = 0; i < prefix.length(); i++) {
      char c = prefix.charAt(i);
      TrieNode node = current.children.get(c);

      if (node == null) {
        return null;
      }

      current = node;
    }

    return current; // Return the node at the end of the prefix
  }

  // Check if a prefix exists in the Trie
  private boolean prefixExists(String prefix) {
    return prefixNode(prefix) != null;
  }

  // Check if a word exists in the Trie starting from a end of prefixNode
  private boolean wordExists(TrieNode startNode, String word) {
    return startNode != null && startNode.isEndOfWord;
  }

  // Based on the board, find valid words
  public Word[] getWords(char[][] board) {
    // Use a Set to efficiently track duplicates
    Set<String> foundWordStrings = new HashSet<>();

    // PriorityQueue to store found words prioritized by score (longer words first)
    PriorityQueue<Word> foundWords = new PriorityQueue<>((w1, w2) -> {
      int score1 = calculateScore(w1.getWord().length());
      int score2 = calculateScore(w2.getWord().length());
      return score2 - score1; // Higher score (longer words) first
    });

    // Visited cells matrix to track the path
    boolean[][] visited = new boolean[4][4];

    // Try starting DFS from each cell on the board
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        StringBuilder currentWord = new StringBuilder();
        ArrayList<Location> currentPath = new ArrayList<>();

        // Start DFS from this cell
        dfs(board, i, j, visited, currentWord, currentPath, foundWords, foundWordStrings);
      }
    }

    // Extract the top 20 words (at most) from the priority queue
    Word[] myWords = new Word[Math.min(20, foundWords.size())];
    for (int i = 0; i < myWords.length; i++) {
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

  // DFS to find words on the board
  private void dfs(char[][] board, int row, int col, boolean[][] visited,
      StringBuilder currentWord, ArrayList<Location> currentPath,
      PriorityQueue<Word> foundWords, Set<String> foundWordStrings) {

    // Bounds check
    if (row < 0 || row >= 4 || col < 0 || col >= 4 || visited[row][col]) {
      return;
    }

    // Get the current letter and handle Q->QU case
    char letter = board[row][col];

    // Add current letter to the word being built
    currentWord.append(letter);
    if (letter == 'Q') {
      currentWord.append('U');
    }

    // Add current position to the path
    currentPath.add(new Location(row, col));

    // Mark current cell as visited
    visited[row][col] = true;

    String wordSoFar = currentWord.toString();
    // Check if the current prefix exists in the dictionary
    TrieNode prefixNode = prefixNode(wordSoFar);

    // Check if the current prefix exists in the dictionary
    if (prefixNode != null) {
      // If it's a complete word with at least 3 letters, add it to our found words
      // optimized to start checking from word exist from end of prefixNode
      if (wordSoFar.length() >= 3 && prefixNode.isEndOfWord) {
        // Only add if we haven't seen this word before
        if (!foundWordStrings.contains(wordSoFar)) {
          foundWordStrings.add(wordSoFar);

          // Create a Word object
          Word word = new Word(wordSoFar);

          // Deep copy of the current path to store in the Word object
          ArrayList<Location> pathCopy = new ArrayList<>();
          for (Location loc : currentPath) {
            pathCopy.add(new Location(loc.row, loc.col));
          }

          word.setPath(pathCopy);

          // Add to our collection of found words
          foundWords.add(word);
        }
      }

      // Continue DFS in all 8 directions
      for (int i = 0; i < 8; i++) {
        int newRow = row + ROW_DIR[i];
        int newCol = col + COL_DIR[i];

        dfs(board, newRow, newCol, visited, currentWord, currentPath, foundWords, foundWordStrings);
      }
    }

    // Backtrack: remove the current letter and mark cell as unvisited
    if (letter == 'Q') {
      currentWord.setLength(currentWord.length() - 2); // Remove QU
    } else {
      currentWord.setLength(currentWord.length() - 1); // Remove the letter
    }

    currentPath.remove(currentPath.size() - 1);
    visited[row][col] = false;
  }
}