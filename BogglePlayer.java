/*
  Authors : Your name(s)
  Email addresses of group members: Your email(s)
  Group name: Chaining It

  Course: CSE 2010
  Section: E1

  Description of the overall algorithm and key data structures:
  
  This implementation uses a Compressed Trie (also known as Patricia Trie or Radix Trie) data structure 
  to efficiently store and search the dictionary. The Compressed Trie optimizes storage by collapsing
  nodes with single children into a single node with a string label, reducing the overall memory footprint
  and potentially improving search performance.
  
  For finding words on the board, we use depth-first search (DFS) from each position on the board.
  The DFS algorithm recursively explores adjacent cells while tracking visited positions to avoid using
  the same position multiple times for a single word.

  Hashset is used to store found words to provide O(1) lookup for duplicate checking
  
  The implementation collects all valid words during board traversal and then selects the top 20
  highest-scoring words (prioritizing longer words) for the final result.
  
  Optimization: 
  1. Compressed Trie reduces memory usage by collapsing single-child node chains
  2. DFS passes the current TrieNode to avoid rechecking the entire prefix from the root
  3. Special handling for 'Q' is integrated into the trie structure to automatically handle 'QU' pairs
*/

import java.util.*;
import java.io.*;

public class BogglePlayer {
  // Compressed Trie root for dictionary lookup
  private TrieNode root;

  // Direction arrays for the 8 adjacent cells (horizontal, vertical, diagonal)
  private static final int[] ROW_DIR = { -1, -1, -1, 0, 0, 1, 1, 1 };
  private static final int[] COL_DIR = { -1, 0, 1, -1, 1, -1, 0, 1 };

  // Inner class representing a node in the Compressed Trie
  private class TrieNode {
    private final Map<Character, TrieNode> children;
    private String prefix; // The string segment stored at this node
    private boolean isEndOfWord;

    public TrieNode() {
      this.children = new HashMap<>();
      this.prefix = "";
      this.isEndOfWord = false;
    }

    public TrieNode(String prefix) {
      this.children = new HashMap<>();
      this.prefix = prefix;
      this.isEndOfWord = false;
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
        // Convert to uppercase and insert into Compressed Trie
        insertWord(line.toUpperCase());
      }
      reader.close();
    } catch (IOException e) {
      System.err.println("Error reading dictionary file: " + e.getMessage());
    }
  }

  // Insert a word into the Compressed Trie
  private void insertWord(String word) {
    // Handle empty strings
    if (word.isEmpty()) return;
    
    // Start at the root
    TrieNode current = root;
    int i = 0;
    
    // Traverse through existing nodes as much as possible
    while (i < word.length()) {
        char firstChar = word.charAt(i);
        
        // Special handling for 'Q' - we'll always assume 'U' follows
        if (firstChar == 'Q' && i + 1 < word.length() && word.charAt(i + 1) == 'U') {
            firstChar = 'Q'; // We'll use 'Q' to represent "QU" in our trie
            // We'll handle the special case in our search
        }
        
        // Check if there's a child starting with the current character
        if (!current.children.containsKey(firstChar)) {
            // No matching child, create a new node with remaining substring
            String remaining = word.substring(i);
            TrieNode newNode = new TrieNode(remaining);
            newNode.isEndOfWord = true;
            current.children.put(firstChar, newNode);
            return;
        }
        
        // Get the matching child node
        TrieNode child = current.children.get(firstChar);
        String childPrefix = child.prefix;
        
        // Find the point of mismatch between child's prefix and remaining word
        int j = 0;
        while (j < childPrefix.length() && i + j < word.length() && 
               childPrefix.charAt(j) == word.charAt(i + j)) {
            j++;
        }
        
        if (j == childPrefix.length()) {
            // Child's prefix is completely matched
            i += j;
            current = child;
        } else {
            // Split the node at the point of mismatch
            String matchedPart = childPrefix.substring(0, j);
            String childRemaining = childPrefix.substring(j);
            
            // Create a new intermediate node
            TrieNode splitNode = new TrieNode(matchedPart);
            
            // Adjust the existing child node
            child.prefix = childRemaining;
            
            // Update parent to point to the split node
            current.children.put(firstChar, splitNode);
            
            // Add the existing child as a child of the split node
            splitNode.children.put(childRemaining.charAt(0), child);
            
            // If there's more of the word to add
            if (i + j < word.length()) {
                String wordRemaining = word.substring(i + j);
                TrieNode newChild = new TrieNode(wordRemaining);
                newChild.isEndOfWord = true;
                splitNode.children.put(wordRemaining.charAt(0), newChild);
            } else {
                // The word ends exactly at the split point
                splitNode.isEndOfWord = true;
            }
            
            return;
        }
    }
    
    // If we've gotten here, the word is a prefix of an existing entry
    current.isEndOfWord = true;
  }

  // Check if a string exists in the trie as a complete word
  private boolean search(String word) {
    TrieNodeSearchResult result = findNode(word);
    return result != null && result.node.isEndOfWord && result.fullMatch;
  }
  
  // Check if a string has any words in the trie that start with it
  private boolean startsWith(String prefix) {
    return findNode(prefix) != null;
  }
  
  // Helper class to return search results with additional information
  private class TrieNodeSearchResult {
    TrieNode node;
    boolean fullMatch;
    int matchedLength;
    
    TrieNodeSearchResult(TrieNode node, boolean fullMatch, int matchedLength) {
      this.node = node;
      this.fullMatch = fullMatch;
      this.matchedLength = matchedLength;
    }
  }
  
  // Find a node that matches the given prefix
  private TrieNodeSearchResult findNode(String prefix) {
    if (prefix.isEmpty()) {
      return new TrieNodeSearchResult(root, true, 0);
    }
    
    TrieNode current = root;
    int i = 0;
    
    while (i < prefix.length()) {
      char firstChar = prefix.charAt(i);
      
      // Special handling for Q
      if (firstChar == 'Q' && i + 1 < prefix.length() && prefix.charAt(i + 1) == 'U') {
        // Skip the 'U' in the prefix
        i++;
      }
      
      if (!current.children.containsKey(firstChar)) {
        return null; // No match found
      }
      
      TrieNode child = current.children.get(firstChar);
      String childPrefix = child.prefix;
      
      // Check if childPrefix is a prefix of the remaining search string
      int j = 0;
      while (j < childPrefix.length() && i + j < prefix.length()) {
        // Special case handling for 'Q' in the node prefix
        if (childPrefix.charAt(j) == 'Q' && j + 1 < childPrefix.length() && 
            childPrefix.charAt(j + 1) == 'U' && i + j < prefix.length() && 
            prefix.charAt(i + j) == 'Q' && i + j + 1 < prefix.length() && 
            prefix.charAt(i + j + 1) == 'U') {
          j += 2; // Skip "QU" in node prefix
          continue;
        }
        
        if (childPrefix.charAt(j) != prefix.charAt(i + j)) {
          return null; // Mismatch
        }
        j++;
      }
      
      if (j == childPrefix.length()) {
        // Consumed the entire child prefix
        i += j;
        current = child;
        
        // If we've consumed the entire search prefix, return success
        if (i == prefix.length()) {
          return new TrieNodeSearchResult(current, true, prefix.length());
        }
      } else {
        // The search prefix is shorter than the child prefix
        // but matches it so far - partial match
        return new TrieNodeSearchResult(child, false, i + j);
      }
    }
    
    // Reached the end of the prefix
    return new TrieNodeSearchResult(current, true, prefix.length());
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

        // Start DFS from this cell with the root node of the Trie
        dfs(board, i, j, visited, currentWord, currentPath, foundWords, foundWordStrings, root, 0);
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

  // DFS to find words on the board - Optimized for Compressed Trie
  private void dfs(char[][] board, int row, int col, boolean[][] visited,
      StringBuilder currentWord, ArrayList<Location> currentPath,
      PriorityQueue<Word> foundWords, Set<String> foundWordStrings, 
      TrieNode currentNode, int prefixMatchPos) {

    // Bounds check
    if (row < 0 || row >= 4 || col < 0 || col >= 4 || visited[row][col]) {
      return;
    }

    // Get the current letter
    char letter = board[row][col];
    
    // Mark current cell as visited before we begin exploration
    visited[row][col] = true;
    
    // Add current position to the path
    currentPath.add(new Location(row, col));
    
    // Special handling for 'Q' on the board - we automatically add 'U'
    if (letter == 'Q') {
      currentWord.append("QU");
    } else {
      currentWord.append(letter);
    }

    // Check if the current path forms a valid prefix in our trie
    String wordSoFar = currentWord.toString();
    TrieNodeSearchResult searchResult = findNode(wordSoFar);
    
    if (searchResult != null) {
      // We have a valid prefix
      TrieNode matchedNode = searchResult.node;
      
      // Check if we've found a complete word of at least 3 letters
      if (wordSoFar.length() >= 3 && matchedNode.isEndOfWord && searchResult.fullMatch) {
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

        dfs(board, newRow, newCol, visited, currentWord, currentPath, 
            foundWords, foundWordStrings, matchedNode, searchResult.matchedLength);
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