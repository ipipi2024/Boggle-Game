class Node {
    private char value;
    private Node[] children;
    private boolean isEnd;

    public Node(char value) {
        this.value = value;
        this.children = new Node[26]; // For A-Z
    }

    public Node getChild(char c) {
        return children[c - 'A'];
    }

    public void setChild(char c, Node child) {
        children[c - 'A'] = child;
    }

    public Node[] getChildren() {
        return children;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        this.isEnd = end;
    }
}
