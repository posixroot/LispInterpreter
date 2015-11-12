import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;

/**
 * Created by kiran on 9/26/15.
 */
public class Parser {

    LexAnalyzer lex;
    TreePrinter treePrinter;
    LispEvaluator lispEvaluator;

    public Parser(BufferedReader br) {
        lex = new LexAnalyzer(br);
        treePrinter = new TreePrinter();
        lispEvaluator = new LispEvaluator();
    }

    public void parseStart() {

        Node ret = parseSexp();

        //treePrinter.printExp(ret);

        //System.out.println("DEBUG eval print follows: ");
        treePrinter.printExp(lispEvaluator.eval(ret, new HashMap<String,ArrayDeque<Node>>()));

        while(lex.getPeek()==' ' || lex.getPeek()=='\n' || lex.getPeek()=='\t' || lex.getPeek()=='\0')
            lex.peekAhead();
        if(lex.getPeek()!=65535) {
            parseStart();
        }
    }

    public Node parseSexp() {
        LexToken lexToken = lex.getNextToken();

        //error handling
        if(lexToken==null) {
            System.out.println("ERROR: Token ATOM or Token OPEN_PAR expected, but got null token instead!");
            System.exit(0);
            return null;
        }

        if(lexToken.tokenID==LexTokenID.ATOM) {
            Node node = new Node(lexToken);
            if(lexToken.getLiteralValue().equals("NIL")) {
                node.isList = true;
                node.isInnerList = true;
            } else {
                node.isList = false;
                node.isInnerList = false;
            }
            return node;
        } else if(lexToken.tokenID==LexTokenID.OPEN_PAR) {

            Node left = parseSexp();

            if(lex.getNextToken().tokenID!=LexTokenID.DOT) {
                System.out.println("ERROR: Token DOT expected, but got " + lexToken.tokenID + " instead!");
                System.exit(0);
            }

            Node right = parseSexp();

            if(lex.getNextToken().tokenID!=LexTokenID.CLOSE_PAR) {
                System.out.println("ERROR: Token CLOSE_PAR expected, but got " + lexToken.tokenID + " instead!");
                System.exit(0);
            }

            Node node = new Node(left, right);
            node.isList = right.isList;
            node.isInnerList = (right.lexToken==null? right.isInnerList : right.lexToken.getLiteralValue().equals("NIL"))&&(left.lexToken==null? left.isInnerList : true);
            return node;
        } else {
            System.out.println("ERROR: Token ATOM or Token OPEN_PAR expected, but got " + lexToken.tokenID + " instead!");
            System.exit(0);
            return null;
        }
    }

}