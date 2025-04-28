public class CompressedTrie {
    private final Node root;
    public CompressedTrie() {
            root = new Node();
        }

    // Insert a word into the Trie
    public void insert(String word) {
        Node current = root;
        for (char c : word.toCharArray()) {
            int idx = c - 'A';
            if (current.children[idx] == null) {
                current.children[idx] = new Node();
            }
            current = current.children[idx];
        }
        current.isWord = true;
    }

    public Node getRoot() {
        return root;
    }
}