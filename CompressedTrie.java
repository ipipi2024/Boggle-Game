import java.util.*;

public class CompressedTrie {
    private final CompressedNode root;

    public CompressedTrie() {
        this.root = new CompressedNode("");
    }

    public void addWord(String word) {
        if (word == null || word.isEmpty()) return;
        addWord(root, word);
    }

    private void addWord(CompressedNode node, String word) {
        if (word == null || word.isEmpty()) return;
    
        for (Map.Entry<Character, CompressedNode> entry : node.children.entrySet()) {
            CompressedNode child = entry.getValue();
            String label = child.text;
            int commonPrefixLen = getCommonPrefixLength(word, label);
    
            if (commonPrefixLen == 0) continue;
    
            if (commonPrefixLen < label.length()) {
                CompressedNode split = new CompressedNode(label.substring(commonPrefixLen));
                split.children = child.children;
                split.isWord = child.isWord;
    
                child.text = label.substring(0, commonPrefixLen);
                child.children = new HashMap<>();
                child.children.put(split.text.charAt(0), split);
                child.isWord = false;
            }
    
            String remaining = word.substring(commonPrefixLen);
            if (remaining.isEmpty()) {
                child.isWord = true;
            } else {
                addWord(child, remaining);
            }
            return;
        }
    
        // No matching child, so create a new leaf
        CompressedNode newNode = new CompressedNode(word);
        newNode.isWord = true;
        node.children.put(word.charAt(0), newNode);
    }
    

    public CompressedNode searchPrefix(String word) {
        return search(root, word);
    }

    private CompressedNode search(CompressedNode node, String word) {
        for (CompressedNode child : node.children.values()) {
            int commonLen = commonPrefixLength(word, child.text);
            if (commonLen == child.text.length()) {
                if (word.length() == commonLen) return child;
                return search(child, word.substring(commonLen));
            } else if (commonLen > 0) {
                return null; // partial match, not a valid path
            }
        }
        return null;
    }

    private int commonPrefixLength(String a, String b) {
        int len = Math.min(a.length(), b.length());
        for (int i = 0; i < len; i++) {
            if (a.charAt(i) != b.charAt(i)) return i;
        }
        return len;
    }

    public CompressedNode getRoot() {
        return root;
    }

    private int getCommonPrefixLength(String a, String b) {
        int len = Math.min(a.length(), b.length());
        for (int i = 0; i < len; i++) {
            if (a.charAt(i) != b.charAt(i)) return i;
        }
        return len;
    }
    
}
