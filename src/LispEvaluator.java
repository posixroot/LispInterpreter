import java.util.ArrayDeque;
import java.util.HashMap;

/**
 * Created by kiran on 10/22/15.
 */
public class LispEvaluator {

    public Node eval(Node root, HashMap<String,String> alist) {

        if(root.lexToken!=null) {
            return evalAtom(root, alist);
        } else {
            return evalList(root, alist);
        }
    }

    private Node evalList(Node root, HashMap<String,String> alist) {

        Node funNode = root.left;
        Node n = null;
        if(funNode.lexToken!=null) {
            String funcName = funNode.lexToken.getLiteralValue();
            switch(funcName) {
                case "QUOTE":
                    n=evalQuoteFunction(root.right, alist);
                    break;
                case "COND":
                    n=evalCondFunction(root.right, alist);
                    break;
                case "CAR":
                    if(evalCdrFunction(root.right, alist).lexToken==null || !evalCdrFunction(root.right, alist).lexToken.getLiteralValue().equals("NIL")) {
                        System.out.println("CAR error: Too many parameters.");
                        System.exit(0);
                    }
                    n=evalCarFunction(eval(evalCarFunction(root.right, alist), alist), alist);
                    break;
                case "CDR":
                    if(evalCdrFunction(root.right, alist).lexToken==null || !evalCdrFunction(root.right, alist).lexToken.getLiteralValue().equals("NIL")) {
                        System.out.println("CDR error: Too many parameters.");
                        System.exit(0);
                    }
                    n=evalCdrFunction(eval(evalCarFunction(root.right, alist), alist), alist);
                    break;
                case "CONS":
                    n=evalConsFunction(root.right, alist);
                    break;
                case "ATOM":
                    n=evalAtomFunction(root.right, alist);
                    break;
                case "EQ":
                    n=evalEqFunction(root.right, alist);
                    break;
                case "INT":
                    n=evalIntFunction(root.right, alist);
                    break;
                case "NULL":
                    n=evalNullFunction(root.right, alist);
                    break;
                case "PLUS":
                    n=evalPlusFunction(root.right, alist);
                    break;
                case "MINUS":
                    n=evalMinusFunction(root.right, alist);
                    break;
                case "TIMES":
                    n=evalTimesFunction(root.right, alist);
                    break;
                case "QUOTIENT":
                    n=evalQuotientFunction(root.right, alist);
                    break;
                case "REMAINDER":
                    n=evalRemainderFunction(root.right, alist);
                    break;
                case "LESS":
                    n=evalLessFunction(root.right, alist);
                    break;
                case "GREATER":
                    n=evalGreaterFunction(root.right, alist);
                    break;
                case "DEFUN":
                    //n=updateDlist(root.right, alist);
                    break;
                default:
                    //apply(funcName, root.right, alist);
                    System.out.println("Invalid function name!");
                    System.exit(0);
                    break;
            }
        } else {
            root.left = eval(root.left, alist);
            root.right = eval(root.right, alist);
            n = root;
            n.isList = n.right.isList;
            setInnerList(n);
        }
        return n;
    }

    private Node evalGreaterFunction(Node root, HashMap<String,String> alist) {
        Node result = evalMinusFunction(root, alist);
        Node ret = new Node(new LexToken(LexTokenID.ATOM, false));
        if(result.lexToken.getNumericValue()>0) {
            ret.lexToken.setLiteralValue("T");
            ret.isList = false;
            ret.isInnerList = false;
        } else {
            ret.lexToken.setLiteralValue("NIL");
            ret.isList = true;
            ret.isInnerList = true;
        }
        return ret;
    }

    private Node evalLessFunction(Node root, HashMap<String,String> alist) {
        Node result = evalMinusFunction(root, alist);
        Node ret = new Node(new LexToken(LexTokenID.ATOM, false));
        if(result.lexToken.getNumericValue()<0) {
            ret.lexToken.setLiteralValue("T");
            ret.isList = false;
            ret.isInnerList = false;
        } else {
            ret.lexToken.setLiteralValue("NIL");
            ret.isList = true;
            ret.isInnerList = true;
        }
        return ret;
    }

    private Node evalRemainderFunction(Node root, HashMap<String,String> alist) {
        if(root.lexToken!=null) {
            System.out.println("REMAINDER error: Expecting a list, but got atom instead.");
            System.exit(0);
        }

        Node op1 = evalInt(eval(evalCarFunction(root, alist), alist), alist);
        Node op2 = evalInt(eval(evalCarFunction(evalCdrFunction(root, alist), alist), alist), alist);
        Node op3 = evalCdrFunction(evalCdrFunction(root, alist), alist);

        if(op3.lexToken==null || !op3.lexToken.getLiteralValue().equals("NIL")) {
            System.out.println("PLUS error: Illegal parameters");
            System.exit(0);
        }

        Node ret = new Node(new LexToken(LexTokenID.ATOM, true));
        int rem = op1.lexToken.getNumericValue() % op2.lexToken.getNumericValue();
        if((rem>=0 && op1.lexToken.getNumericValue()<0) || (rem<0 && op1.lexToken.getNumericValue()>=0))
            rem *= -1;
        ret.lexToken.setNumericValue(rem);
        ret.lexToken.setLiteralValue(String.valueOf(ret.lexToken.getNumericValue()));
        ret.isList = false;
        ret.isInnerList = false;
        return ret;
    }

    private Node evalQuotientFunction(Node root, HashMap<String,String> alist) {
        if(root.lexToken!=null) {
            System.out.println("QUOTIENT error: Expecting a list, but got atom instead.");
            System.exit(0);
        }

        Node op1 = evalInt(eval(evalCarFunction(root, alist), alist), alist);
        Node op2 = evalInt(eval(evalCarFunction(evalCdrFunction(root, alist), alist), alist), alist);
        Node op3 = evalCdrFunction(evalCdrFunction(root, alist), alist);

        if(op3.lexToken==null || !op3.lexToken.getLiteralValue().equals("NIL")) {
            System.out.println("PLUS error: Illegal parameters");
            System.exit(0);
        }

        Node ret = new Node(new LexToken(LexTokenID.ATOM, true));
        int quotient = op1.lexToken.getNumericValue() / op2.lexToken.getNumericValue();
        ret.lexToken.setNumericValue(quotient);
        ret.lexToken.setLiteralValue(String.valueOf(ret.lexToken.getNumericValue()));
        ret.isList = false;
        ret.isInnerList = false;
        return ret;
    }

    private Node evalTimesFunction(Node root, HashMap<String,String> alist) {
        if(root.lexToken!=null) {
            System.out.println("TIMES error: Expecting a list, but got atom instead.");
            System.exit(0);
        }

        Node op1 = evalInt(eval(evalCarFunction(root, alist), alist), alist);
        Node op2 = evalInt(eval(evalCarFunction(evalCdrFunction(root, alist), alist), alist), alist);
        Node op3 = evalCdrFunction(evalCdrFunction(root, alist), alist);

        if(op3.lexToken==null || !op3.lexToken.getLiteralValue().equals("NIL")) {
            System.out.println("PLUS error: Illegal parameters");
            System.exit(0);
        }

        Node ret = new Node(new LexToken(LexTokenID.ATOM, true));
        int mult = op1.lexToken.getNumericValue() * op2.lexToken.getNumericValue();
        ret.lexToken.setNumericValue(mult);
        ret.lexToken.setLiteralValue(String.valueOf(ret.lexToken.getNumericValue()));
        ret.isList = false;
        ret.isInnerList = false;
        return ret;
    }

    private Node evalMinusFunction(Node root, HashMap<String,String> alist) {
        if(root.lexToken!=null) {
            System.out.println("MINUS error: Expecting a list, but got atom instead.");
            System.exit(0);
        }

        Node op1 = evalInt(eval(evalCarFunction(root, alist), alist), alist);
        Node op2 = evalInt(eval(evalCarFunction(evalCdrFunction(root, alist), alist), alist), alist);
        Node op3 = evalCdrFunction(evalCdrFunction(root, alist), alist);

        if(op3.lexToken==null || !op3.lexToken.getLiteralValue().equals("NIL")) {
            System.out.println("PLUS error: Illegal parameters");
            System.exit(0);
        }

        Node ret = new Node(new LexToken(LexTokenID.ATOM, true));
        int diff = op1.lexToken.getNumericValue() - op2.lexToken.getNumericValue();
        ret.lexToken.setNumericValue(diff);
        ret.lexToken.setLiteralValue(String.valueOf(ret.lexToken.getNumericValue()));
        ret.isList = false;
        ret.isInnerList = false;
        return ret;
    }

    private Node evalPlusFunction(Node root, HashMap<String,String> alist) {
        if(root.lexToken!=null) {
            System.out.println("PLUS error: Expecting a list, but got atom instead.");
            System.exit(0);
        }

        Node op1 = evalInt(eval(evalCarFunction(root, alist), alist), alist);
        Node op2 = evalInt(eval(evalCarFunction(evalCdrFunction(root, alist), alist), alist), alist);
        Node op3 = evalCdrFunction(evalCdrFunction(root, alist), alist);

        if(op3.lexToken==null || !op3.lexToken.getLiteralValue().equals("NIL")) {
            System.out.println("PLUS error: Illegal parameters");
            System.exit(0);
        }

        Node ret = new Node(new LexToken(LexTokenID.ATOM, true));
        int sum = op1.lexToken.getNumericValue() + op2.lexToken.getNumericValue();
        ret.lexToken.setNumericValue(sum);
        ret.lexToken.setLiteralValue(String.valueOf(ret.lexToken.getNumericValue()));
        ret.isList = false;
        ret.isInnerList = false;
        return ret;
    }

    private Node evalConsFunction(Node root, HashMap<String,String> alist) {

        if(root.lexToken!=null) {
            System.out.println("CONS error: Expecting a list, but got atom instead.");
            System.exit(0);
        }

        Node leftTree = eval(evalCarFunction(root, alist), alist);
        Node rightTree = eval(evalCarFunction(evalCdrFunction(root, alist), alist), alist);
        Node nullTree = evalCdrFunction(evalCdrFunction(root, alist), alist);

        /*if(rightTree.lexToken!=null) {
            System.out.println("CONS error: Expecting a list, but got atom instead.");
            System.exit(0);
        }*/
        if(nullTree.lexToken==null || !nullTree.lexToken.getLiteralValue().equals("NIL")) {
            System.out.println("CONS error: More than 2 parameters detected.");
            System.exit(0);
        }

        Node ret = new Node(leftTree, rightTree);
        ret.isList = ret.right.isList;
        setInnerList(ret);
        return ret;

    }

    private void setInnerList(Node node) {
        Node right = node.right;
        Node left = node.left;
        node.isInnerList = (right.lexToken==null? right.isInnerList : right.lexToken.getLiteralValue().equals("NIL"))&&((left.lexToken==null)? left.isInnerList : true);
    }

    private Node evalCdrFunction(Node root, HashMap<String,String> alist) {

        if(root.lexToken!=null) {
            System.out.println("CDR error: Expecting a list, but got atom instead.");
            System.exit(0);
        }
        return root.right;
    }

    private Node evalCarFunction(Node root, HashMap<String,String> alist) {

        if(root.lexToken!=null) {
            System.out.println("CAR error: Expecting a list, but got atom instead.");
            System.exit(0);
        }
        return root.left;
    }

    private Node evalCondFunction(Node root, HashMap<String,String> alist) {

        if(root.lexToken!=null) {
            System.out.println("COND error: Expecting a list, but got atom instead.");
            System.out.println("DEBUG COND: Failed at 1");
            System.exit(0);
        }

        Node pair = evalCarFunction(root, alist);

        if(pair.lexToken!=null) {
            //error because we are expecting a pair(list)
            System.out.println("COND error: Expecting a list, but got atom instead.");
            System.out.println("DEBUG COND: Failed at 2");
            //(COND.( (NIL.6).(T.4) )) fails here but not (COND.( (NIL.6).((T.4).NIL) ))
            System.exit(0);
        }

        Node conditionResult = eval(evalCarFunction(pair, alist), alist);

        if(conditionResult.lexToken==null){
            //error expcting a T or NIL
            System.out.println("COND error: Expecting T or NIL, but got a list instead.");
            System.out.println("DEBUG COND: Failed at 3");
            System.exit(0);
        }

        if(conditionResult.lexToken.getLiteralValue().equals("T")) {
            //check for more elements
            Node check = eval(evalCdrFunction(evalCdrFunction(pair, alist), alist), alist);
            if(check.lexToken==null || !check.lexToken.getLiteralValue().equals("NIL")) {
                System.out.println("COND error: Too many arguments");
                System.exit(0);
            }
            return eval(evalCarFunction(evalCdrFunction(pair, alist), alist), alist);
        } else if(conditionResult.lexToken.getLiteralValue().equals("NIL")) {
            return evalCondFunction(evalCdrFunction(root, alist), alist);
        } else {
            //error only expecting T or NIL
            System.out.println("COND error: Expecting T or NIL.");
            System.out.println("DEBUG COND: Failed at 4");
            System.exit(0);
            return null;
        }
    }

    private Node evalQuoteFunction(Node root, HashMap<String,String> alist) {
        if(root.right.lexToken==null || !root.right.lexToken.getLiteralValue().equals("NIL")) {
            System.out.println("QUOTE error: More than 1 parameter detected.");
            System.exit(0);
        }

        return evalCarFunction(root, alist);
    }

    private Node evalAtomFunction(Node root, HashMap<String,String> alist) {
        Node ret = new Node(new LexToken(LexTokenID.ATOM, false));
        Node result = eval(evalCarFunction(root, alist), alist);
        if(evalCdrFunction(root, alist).lexToken!=null && evalCdrFunction(root, alist).lexToken.getLiteralValue().equals("NIL")) {
            if (result.left == null && result.right == null && result.lexToken != null) {
                //result = evalAtom(result);
                ret.lexToken.setLiteralValue("T");
                ret.isList = false;
                ret.isInnerList = false;
                return ret;
            }
        }
        ret.lexToken.setLiteralValue("NIL");
        ret.isList = true;
        ret.isInnerList = true;
        return ret;
    }

    private Node evalIntFunction(Node root, HashMap<String,String> alist) {
        Node ret = new Node(new LexToken(LexTokenID.ATOM, false));

        Node result = eval(evalCarFunction(root, alist), alist);

        if(evalCdrFunction(root, alist).lexToken==null || !evalCdrFunction(root, alist).lexToken.getLiteralValue().equals("NIL")) {
            System.out.println("INT error: Too many parameters.");
            System.exit(0);
        }

        if(result.lexToken==null) {
            ret.lexToken.setLiteralValue("NIL");
            ret.isList = true;
            ret.isInnerList = true;
            return ret;
        }
        if(!result.lexToken.isNumericAtom) {
            ret.lexToken.setLiteralValue("NIL");
            ret.isList = true;
            ret.isInnerList = true;
            return ret;
        }
        //evalInt(root);
        int numval = -1;
        try {
            numval = Integer.parseInt(result.lexToken.getLiteralValue());
        } catch (Exception e) {
            System.out.println("Int error: Invalid numeric Atom!");
            System.exit(0);
        }
        ret.lexToken.setLiteralValue("T");
        ret.isList = false;
        ret.isInnerList = false;
        return ret;
    }

    private Node evalNullFunction(Node root, HashMap<String,String> alist) {
        //check if the argument is a list
        Node list = eval(evalCarFunction(root, alist), alist);
        Node ret = new Node(new LexToken(LexTokenID.ATOM, false));
        if(evalCdrFunction(root, alist).lexToken==null || !evalCdrFunction(root, alist).lexToken.getLiteralValue().equals("NIL")) {
            System.out.println("Null error: Too many parameters.");
            System.exit(0);
        }

        if(list.lexToken!=null) {
            if(list.lexToken.getLiteralValue().equals("NIL")) {
                ret.lexToken.setLiteralValue("T");
                ret.isList = false;
                ret.isInnerList = false;
                return ret;
            } else {
                System.out.println("NULL error: Expecting a list, but got an atom instead.");
                System.exit(0);
            }
        }
        /*Node leftNode = list.left;
        Node rightNode = list.right;

        if(isSubtreeNull(leftNode) && isSubtreeNull(rightNode)) {
            ret.lexToken.setLiteralValue("T");
            ret.isList = false;
            ret.isInnerList = false;
        } else {*/
            ret.lexToken.setLiteralValue("NIL");
            ret.isList = true;
            ret.isInnerList = true;
        //}
        return ret;
    }

    private boolean isSubtreeNull(Node node, HashMap<String,String> alist) {

        if(node.lexToken!=null) {
            if(node.lexToken.getLiteralValue().equals("NIL")) {
                return true;
            } else {
                return false;
            }
        } else {
            //return isSubtreeNull(node.left) && isSubtreeNull(node.right);
            return false;
        }
    }

    private Node evalEqFunction(Node root, HashMap<String,String> alist) {
        //check if it is a list and not an atom
        if(root.left==null || root.right==null || root.lexToken!=null) {
            System.out.println("EQ error: Expecting a list, but got an atom instead.");
            System.exit(0);
        }

        Node lop = eval(evalCarFunction(root, alist), alist);
        Node rop = eval(evalCarFunction(evalCdrFunction(root, alist), alist), alist);
        Node nullop = evalCdrFunction(evalCdrFunction(root, alist), alist);

        if(nullop.lexToken==null || !nullop.lexToken.getLiteralValue().equals("NIL")) {
            System.out.println("EQ error: Too many parameters.");
            System.exit(0);
        }

        Node ret = new Node(new LexToken(LexTokenID.ATOM, false));
        if(lop.lexToken==null || rop.lexToken==null) {
            System.out.println("EQ error: atom expected but got a list.");
            System.exit(0);
        }
        if(lop.lexToken.isNumericAtom ^ rop.lexToken.isNumericAtom) {
            System.out.println("EQ error: Numeric Atom cannot be compared to Literal Atom");
            System.exit(0);
        }
        if(lop.lexToken.isNumericAtom) {
            if (lop.lexToken.getNumericValue() == rop.lexToken.getNumericValue()) {
                ret.lexToken.setLiteralValue("T");
                ret.isList = false;
                ret.isInnerList = false;
            } else {
                ret.lexToken.setLiteralValue("NIL");
                ret.isList = true;
                ret.isInnerList = true;
            }
        } else {
            if (lop.lexToken.getLiteralValue().equals(rop.lexToken.getLiteralValue())) {
                ret.lexToken.setLiteralValue("T");
                ret.isList = false;
                ret.isInnerList = false;
            } else {
                ret.lexToken.setLiteralValue("NIL");
                ret.isList = true;
                ret.isInnerList = true;
            }

        }

        return ret;
    }

    private Node evalInt(Node root, HashMap<String,String> alist) {
        //check that there is only one element
        if(root.lexToken==null) {
            System.out.println("Illegal List - Expected an atom!");
            System.exit(0);
        }

        String s = root.lexToken.getLiteralValue();
        int numval = -1;
        try {
            numval = Integer.parseInt(s);
        } catch (Exception e) {
            System.out.println("Invalid numeric Atom!");
            System.exit(0);
        }
        root.lexToken.setNumericValue(numval);
        return root;
    }

    private Node evalAtom(Node root, HashMap<String,String> alist) {

        if(root.lexToken==null) {
            System.out.println("Expecting an atom, but got a list instead.");
            System.exit(0);
        }

        String s = root.lexToken.getLiteralValue();

        if(s.equals("T")) {
            return root;
        } else if(s.equals("NIL")) {
            return root;
        } else if(root.lexToken.isNumericAtom){
            return evalInt(root, alist);
        } else {

            //implement bound and getval

            System.out.println("Unidentified Atom!");
            System.exit(0);
            return null;
        }

    }
}