/**
 * Created by kiran on 9/26/15.
 */
public class Node {
    boolean isList;
    boolean isInnerList;
    Node left;
    Node right;
    LexToken lexToken;

    //needed for type-checking
    MyType type;

    public Node(LexToken lexToken) {
        this.lexToken = lexToken;
        left = null;
        right = null;
    }

    public Node(Node left, Node right) {
        this.left = left;
        this.right = right;
        lexToken = null;
    }

    //needed for type-checking
    public boolean isInnerNode() {
        return lexToken == null;
    }
}