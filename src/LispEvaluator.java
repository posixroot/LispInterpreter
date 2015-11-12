import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kiran on 10/22/15.
 */
public class LispEvaluator {

    HashMap<String, Node> dlist;

    public LispEvaluator() {
        dlist = new HashMap<>();
    }

    public Node eval(Node root, HashMap<String,ArrayDeque<Node>> alist) {

        if(root.lexToken!=null) {
            return evalAtom(root, alist);
        } else {
            return evalList(root, alist);
        }
    }

    private Node evalList(Node root, HashMap<String,ArrayDeque<Node>> alist) {

        Node funNode = root.left;
        Node n = null;
        if(funNode.lexToken!=null) {
            String funcName = funNode.lexToken.getLiteralValue();
            //System.out.println("DEBUG funcname : " + funcName);
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
                    n=updateDlist(root.right, alist);
                    break;
                default:
                    n = apply(funcName, root.right, alist);
                    if(n==null) {
                        System.out.println("Invalid function name!");
                        System.exit(0);
                    }
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

    private Node apply(String funcName, Node next, HashMap<String, ArrayDeque<Node>> alist) {

        if(!dlist.containsKey(funcName)
                //|| next.lexToken!=null
                ) {
            System.out.println("Apply error: Invalid function call arguments.");
            System.exit(0);
        }

        Node actualList = next;
        Node formalList = evalCarFunction(evalCdrFunction(dlist.get(funcName), alist), alist);

        if(!checkParameterCount(actualList, formalList, alist)) {
            System.out.println("Apply error: " + funcName + " formal/acutal parameters mismatch.");
            System.exit(0);
        }

        ArrayList<String> formalVarsBacktrack = new ArrayList<>();

        Node actualIter = actualList, formalIter = formalList;
        while(actualIter.lexToken==null && formalIter.lexToken==null) {
            //check if there is only one variable on the left in case of formal parameter list.
            Node formalVar = evalCarFunction(formalIter, alist);
            Node actualVar = eval(evalCarFunction(actualIter, alist),alist);
            if (formalVar.lexToken == null
                    //|| actualVar.lexToken == null
                    ) {
                System.out.println("Apply error: Invalid formal/actual parameter list.");
                System.exit(0);
            }
            if (!checkValidName(formalVar) && evalInt(formalVar, alist) != null) {
                System.out.println("Apply error: Invalid formal parameter name");
                System.exit(0);
            }
            // need to check if variable name is in dlist(function-name) ? Nope....
            String formalName = formalVar.lexToken.getLiteralValue();
            formalVarsBacktrack.add(formalName);
            if (!alist.containsKey(formalName)) {
                alist.put(formalName, new ArrayDeque<>());
            }
            alist.get(formalName).addLast(actualVar);

            actualIter = evalCdrFunction(actualIter, alist);
            formalIter = evalCdrFunction(formalIter, alist);
        }
        //System.out.println("Apply Debug: formals/actuals matched and added.");

        Node funcBody = evalCarFunction(evalCdrFunction(evalCdrFunction(dlist.get(funcName), alist), alist), alist);

        Node ret = eval(funcBody, alist);

        for(String s : formalVarsBacktrack) {
            alist.get(s).removeLast();
        }
        return ret;
    }

    private boolean checkParameterCount(Node actualList, Node formalList, HashMap<String, ArrayDeque<Node>> alist) {
        Node actualIter = actualList;
        Node formalIter = formalList;
        int actualCount=0, formalCount=0;
        while(actualIter.lexToken==null && formalIter.lexToken==null) {
            actualCount++;
            formalCount++;
            actualIter = evalCdrFunction(actualIter, alist);
            formalIter = evalCdrFunction(formalIter, alist);
        }
        if(actualIter.lexToken!=null && formalIter.lexToken!=null) {
            if(!actualIter.lexToken.getLiteralValue().equals("NIL") ||
                    !formalIter.lexToken.getLiteralValue().equals("NIL")) {
                return false;
            }
        } else {
            while(actualIter.lexToken==null) {
                /*if(evalCarFunction(actualIter, alist).lexToken!=null
                        //&& !evalCarFunction(actualIter, alist).lexToken.getLiteralValue().equals("NIL")
                        ) {*/
                    actualCount++;
                //}
                actualIter = evalCdrFunction(actualIter, alist);
            }
            if(!actualIter.lexToken.getLiteralValue().equals("NIL")) {
                System.out.println("Apply error: Invalid end of actual Parameter List.");
                System.exit(0);
            }
            while(formalIter.lexToken==null) {
                formalCount++;
                formalIter = evalCdrFunction(formalIter, alist);
            }
        }
        //System.out.println("Debug paramCount: actual: " + actualCount + " formalCount: " + formalCount);
        return actualCount==formalCount;
    }

    private Node updateDlist(Node root, HashMap<String, ArrayDeque<Node>> alist) {

        Node func = evalCarFunction(root, alist);
        if(func.lexToken==null || !checkValidName(func)) {
            System.out.println("Update Dlist error: Expected an atom but got list instead.");
            System.exit(0);
        }

        //check param and body argument counts.
        if(evalCdrFunction(root,alist).lexToken!=null || evalCdrFunction(evalCdrFunction(root, alist), alist).lexToken!=null ||
                evalCdrFunction(evalCdrFunction(evalCdrFunction(root, alist), alist), alist).lexToken==null ||
                !evalCdrFunction(evalCdrFunction(evalCdrFunction(root, alist), alist), alist).lexToken.getLiteralValue().equals("NIL")
                ) {
            System.out.println("Update Dlist error: wrong parameter list and body of new function.");
            System.exit(0);
        }

        //check if param names are duplicates.
        ArrayList<String> formalVarsBacktrace = new ArrayList<>();
        Node paramList = evalCarFunction(evalCdrFunction(root, alist), alist);
        while(paramList.lexToken==null) {
            Node formalVar = evalCarFunction(paramList, alist);
            String formalName = formalVar.lexToken.getLiteralValue();
            if(formalVarsBacktrace.contains(formalName) || !checkValidName(formalVar)) {
                System.out.println("Defun error: Duplicate/invalid parameter name detected.");
                System.exit(0);
            }
            formalVarsBacktrace.add(formalName);
            paramList = evalCdrFunction(paramList, alist);
        }
        //check if paramlist is ending with NIL.
        if(!paramList.lexToken.getLiteralValue().equals("NIL")) {
            System.out.println("Defun error: List end discrepancy.");
            System.exit(0);
        }

        dlist.put(func.lexToken.getLiteralValue(), root);
        System.out.println(func.lexToken.getLiteralValue());
        //return root;
        return null;
    }

    private boolean checkValidName(Node func) {
        switch(func.lexToken.getLiteralValue()) {
            case "CAR":case "CDR": case "CONS":case "ATOM":case "EQ":case "NULL":case "INT":case "PLUS":
            case "MINUS":case "TIMES":case "REMAINDER":case "QUOTIENT":case "LESS":case "GREATER":case "COND":
            case "QUOTE":case "DEFUN":case "T":case "NIL":
                return false;
            default:
                return true;
        }
    }

    private Node evalGreaterFunction(Node root, HashMap<String,ArrayDeque<Node>> alist) {
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

    private Node evalLessFunction(Node root, HashMap<String,ArrayDeque<Node>> alist) {
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

    private Node evalRemainderFunction(Node root, HashMap<String,ArrayDeque<Node>> alist) {
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

    private Node evalQuotientFunction(Node root, HashMap<String,ArrayDeque<Node>> alist) {
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

    private Node evalTimesFunction(Node root, HashMap<String,ArrayDeque<Node>> alist) {
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

    private Node evalMinusFunction(Node root, HashMap<String,ArrayDeque<Node>> alist) {
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

    private Node evalPlusFunction(Node root, HashMap<String,ArrayDeque<Node>> alist) {
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

    private Node evalConsFunction(Node root, HashMap<String,ArrayDeque<Node>> alist) {

        if(root.lexToken!=null) {
            System.out.println("CONS error: Expecting a list, but got atom instead.");
            System.exit(0);
        }

        Node leftTree = eval(evalCarFunction(root, alist), alist);
        Node rightTree = eval(evalCarFunction(evalCdrFunction(root, alist), alist), alist);
        Node nullTree = evalCdrFunction(evalCdrFunction(root, alist), alist);

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

    private Node evalCdrFunction(Node root, HashMap<String,ArrayDeque<Node>> alist) {

        if(root.lexToken!=null) {
            root = eval(root, alist);
            if(root.lexToken!=null) {
                System.out.println("CDR error: Expecting a list, but got atom instead.");
                System.exit(0);
            }
        }
        return root.right;
    }

    private Node evalCarFunction(Node root, HashMap<String,ArrayDeque<Node>> alist) {

        if(root.lexToken!=null) {
            root = eval(root, alist);
            if(root.lexToken!=null) {
                System.out.println("CAR error: Expecting a list, but got atom instead.");
                System.exit(0);
            }
        }
        return root.left;
    }

    private Node evalCondFunction(Node root, HashMap<String,ArrayDeque<Node>> alist) {

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


    private Node evalQuoteFunction(Node root, HashMap<String,ArrayDeque<Node>> alist) {
        if(root.lexToken!=null || root.right.lexToken==null || !root.right.lexToken.getLiteralValue().equals("NIL")) {
            System.out.println("QUOTE error: More than 1 parameter detected.");
            System.exit(0);
        }

        return evalCarFunction(root, alist);
    }

    private Node evalAtomFunction(Node root, HashMap<String,ArrayDeque<Node>> alist) {
        Node ret = new Node(new LexToken(LexTokenID.ATOM, false));
        Node result = eval(evalCarFunction(root, alist), alist);
        if(evalCdrFunction(root, alist).lexToken!=null && evalCdrFunction(root, alist).lexToken.getLiteralValue().equals("NIL")) {
            if (result.left == null && result.right == null && result.lexToken != null) {
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

    private Node evalIntFunction(Node root, HashMap<String,ArrayDeque<Node>> alist) {
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

    private Node evalNullFunction(Node root, HashMap<String,ArrayDeque<Node>> alist) {
        //check if the argument is a list
        Node list = eval(evalCarFunction(root, alist), alist);
        Node ret = new Node(new LexToken(LexTokenID.ATOM, false));
        if(evalCdrFunction(root, alist).lexToken==null || !evalCdrFunction(root, alist).lexToken.getLiteralValue().equals("NIL")) {
            System.out.println("Null error: Too many parameters.");
            System.exit(0);
        }

        if(list.lexToken!=null) {
            list = eval(list, alist);
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
        ret.lexToken.setLiteralValue("NIL");
        ret.isList = true;
        ret.isInnerList = true;

        return ret;
    }

    private Node evalEqFunction(Node root, HashMap<String,ArrayDeque<Node>> alist) {
        //check if it is a list and not an atom
        if(root.left==null || root.right==null || root.lexToken!=null) {
            System.out.println("EQ error: Expecting a list, but got an atom instead.");
            System.exit(0);
        }

        Node lop = eval(evalCarFunction(root, alist), alist);
        Node rop = eval(evalCarFunction(evalCdrFunction(root, alist), alist), alist);
        Node nullop = eval (evalCdrFunction(evalCdrFunction(root, alist), alist) , alist);

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
            lop = evalInt(lop, alist);
            rop = evalInt(rop, alist);
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

    private Node evalInt(Node root, HashMap<String,ArrayDeque<Node>> alist) {
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

    private Node evalAtom(Node root, HashMap<String,ArrayDeque<Node>> alist) {

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
            if(bound(s, alist)) {
                Node ret = getVal(s, alist);
                if(ret.lexToken!=null && ret.lexToken.isNumericAtom) {
                    return evalInt(ret, alist);
                }
                return ret;
            }
            System.out.println("Unidentified Atom!");
            System.exit(0);
            return null;
        }

    }

    private Node getVal(String s, HashMap<String, ArrayDeque<Node>> alist) {
        return alist.get(s).getLast();
    }

    private boolean bound(String s, HashMap<String, ArrayDeque<Node>> alist) {
        if(alist.containsKey(s) && !alist.get(s).isEmpty()) {
            return true;
        }
        return false;
    }
}

