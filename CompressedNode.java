import java.util.HashMap;
import java.util.Map;

public class CompressedNode {
    String text;
    boolean isWord;
    Map<Character, CompressedNode> children;

    public CompressedNode(String text) {
        this.text = text;
        this.children = new HashMap<>();
    }

    public boolean isEnd() {
        return isWord;
    }

    public CompressedNode getChild(char letter) {
        return children.get(letter);
    }

    public Map<Character, CompressedNode> getChildren() {
        return children;
    }

    public String getText() {
        return text;
    }
}
