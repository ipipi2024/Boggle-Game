import java.util.HashMap;
import java.util.Map;

public class Node {
    private final char data;
    private boolean end;
    private Map<Character,Node> children = new HashMap<>();

    public Node(char character) {
        this.data = character;
        this.end = false;
    }

    public boolean getEnd() {
        return end;
    }

    public void setEnd(boolean b) {
        end = b;
    }

    public char getChar() {
        return data;
    }

    public Map<Character,Node> getChildren() {
        return children;
    }
}
