import java.util.HashMap;
import java.util.Map;


public class Node {
    private final char data;
    private String string;
    private boolean end;
    private Map<Character,Node> children = new HashMap<>();

    public Node(char character) {
        this.string = null;
        this.data = character;
        this.end = false;
    }
    
    public String getString(){
        return string;
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
    
    public void compress(){
        for(Node node : children.values()){
            if (!node.getEnd() ){
                Node current = node;
                StringBuilder compressed = new StringBuilder();
                while( !current.getEnd() &&current.children.size() == 1){
                    Map.Entry<Character,Node> entry = current.children.entrySet().iterator().next();
                    compressed.append(entry.getKey());
                    current = entry.getValue();
                }
                if(compressed.length() > 0){
                    node.string = compressed.toString();
                    node.children = current.children;
                    node.end = current.end;
                }
            }
            node.compress();
        }
        
        
    }
}
