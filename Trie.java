import java.util.*;
public class Trie {
    private final Node root;

    public Trie() {
        root = new Node(' ');
    }

    public Trie addWord(String word) {
        Node currentNode = root;
        Map<Character,Node> children = root.getChildren();

        char[] sWord = word.toCharArray();
        for (char L : sWord) {
            if (children.containsKey(L)) {
                /* Already exists a path for the current character */
                currentNode = children.get(L);
            }
            else {
                /* New character needs to be created, unique path */
                currentNode = new Node(L);
                children.put(L, currentNode);
            }
            /* Returns empty map if new Node */
            children = currentNode.getChildren();
        }
        /* End of word */
        currentNode.setEnd(true);
        return this;
    }

    /* Kinda redundant as I need the acutal node, this if statement is in BogglePlayer */
    public boolean containsPart(String s) {
        return search(s) != null;
    }

    public Node search(String target) {
        Node currentNode = root;
        Map<Character,Node> children = root.getChildren();
        char[] sWord = target.toCharArray();

        for (char L : sWord) {
            if (!children.containsKey(L)) {
                return null;
            }
            currentNode = children.get(L);
            children = currentNode.getChildren();
        }
        return currentNode;
    }


    /* Not yet implemented, need to figure out way to somehow store string only for the leaf nodes */
    public Trie compress() {
        return this;
    }
}
