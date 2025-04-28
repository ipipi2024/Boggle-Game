public class Node {
    public Node[] children;
    public String suffix; // Store extra compressed characters
    public boolean isWord;

    Node() {
        children = new Node[26]; // ASCII, A is 65 and Z is 90
        suffix = "";
        isWord = false;
    }
}
