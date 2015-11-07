/**
 * Created by kiran on 10/22/15.
 */
public class TreePrinter {

    public void printExp(Node node) {
        if(node != null) {
            if(node.isInnerList) {
                printList(node);
            } else {
                printNode(node);
            }
        }
        System.out.println();
    }

    public void printNode(Node node) {
        if(node.lexToken!=null) {
            System.out.print(node.lexToken.getLiteralValue());
        } else {
            System.out.print("(");
            printNode(node.left);
            System.out.print(" . ");
            printNode(node.right);
            System.out.print(")");
        }
    }

    public void printList(Node node) {
        if(node.lexToken!=null) {
            //newly added - check for failure
            //if(!node.lexToken.getLiteralValue().equals("NIL"))
                System.out.print(node.lexToken.getLiteralValue());
        } else {
            System.out.print("(");
            printList(node.left);
            while(node.right.lexToken==null) {
                node =  node.right;
                System.out.print(" ");
                printList(node.left);
            }
            System.out.print(")");
        }

    }
}
