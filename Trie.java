public class Trie {
    private final Node root;

    public Trie() {
        root = new Node(' ');
    }

    public Trie addWord(String word) {
        Node currentNode = root;
    
        for (char c : word.toCharArray()) {
            int idx = c - 'A';
            if (currentNode.getChildren()[idx] == null) {
                currentNode.setChild(c, new Node(c));
            }
            currentNode = currentNode.getChild(c);
        }
        currentNode.setEnd(true);
        return this;
    }
    

    /* Kinda redundant right now as I need the acutal node, this if statement is in BogglePlayer */
    public boolean containsPart(String s) {
        return search(s) != null;
    }

    public Node search(String word) {
        Node currentNode = root;
    
        for (char c : word.toCharArray()) {
            currentNode = currentNode.getChild(c);
            if (currentNode == null) return null;
        }
        return currentNode;
    }
    


    /* Not yet implemented, need to figure out way to somehow store string only for the leaf nodes */
    public Trie compress() {
        return this;
    }
}
