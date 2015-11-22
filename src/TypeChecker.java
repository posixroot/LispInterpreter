/**
 * Created by kiran on 11/21/15.
 */
public class TypeChecker {

    public MyType getType(Node node) {

        if(!node.isInnerList) {
            return identifyType(node);
        }

        Node left = node.left;
        Node right = node.right;
        MyType leftType, rightType;

        if(!left.isInnerNode() && isFunction(left)) {
            return getFunctionReturnType(left.lexToken.getLiteralValue(), right);
        }

        if(right.isInnerNode()) {
            rightType = getType(right);
        } else {
            rightType = identifyType(right);
        }

        if(left.isInnerNode()) {
            leftType = getType(left);
        } else {
            leftType = identifyType(left);
        }

        if(leftType==MyType.NAT && rightType==MyType.LIST) {
            return MyType.LIST;
        }

        typeError("Invalid list types");
        return null;
    }

    private MyType getFunctionReturnType(String funcName, Node right) {
        MyType ret = null;
        switch(funcName) {
            case "CAR":
                ret = getCarType(right);
                break;
            case "CDR":
                ret = getCdrType(right);
                break;
            case "CONS":
                ret = getConsType(right);
                break;
            case "ATOM":
                ret = getAtomType(right);
                break;
            case "EQ":
                ret = getEqType(right);
                break;
            case "NULL":
                ret = getNullType(right);
                break;
            case "INT":
                ret = getIntType(right);
                break;
            case "PLUS":
                ret = getPlusType(right);
                break;
            case "LESS":
                ret = getLessType(right);
                break;
            case "COND":
                ret = getCondType(right);
                break;
            default:
                break;
        }
        return ret;
    }

    private MyType getCondType(Node node) {
        Node iter = node;
        MyType retType = null, prevType = null;
        while(iter.isInnerNode()) {
            if(getType(iter.left.left)!=MyType.BOOL) {
                typeError("Invalid COND Truth Check Type");
            }
            retType = getType(iter.left.right.left);
            if(prevType!=null) {
                if(prevType!=retType) {
                    typeError("Invalid COND Return Type");
                }
            }
            prevType = retType;
            iter = iter.right;
        }
        return retType;
    }

    private MyType getLessType(Node node) {
        MyType argType1 = getType(node.left);
        MyType argType2 = getType(node.right.left);

        if(argType1==MyType.NAT && argType2==MyType.NAT) {
            return MyType.BOOL;
        }
        typeError("Invalid LESS Type");
        return null;
    }

    private MyType getPlusType(Node node) {
        MyType argType1 = getType(node.left);
        MyType argType2 = getType(node.right.left);

        if(argType1==MyType.NAT && argType2==MyType.NAT) {
            return MyType.NAT;
        }
        typeError("Invalid PLUS Type");
        return null;
    }

    private MyType getIntType(Node node) {
        MyType argType = getType(node.left);
        if(argType!=null) {
            return MyType.BOOL;
        }
        typeError("Invalid INT Type");
        return null;
    }

    private MyType getNullType(Node node) {
        MyType argType = getType(node.left);

        if(argType==MyType.LIST) {
            return MyType.BOOL;
        }
        typeError("Invalid NULL Type");
        return null;
    }

    private MyType getEqType(Node node) {
        MyType argType1 = getType(node.left);
        MyType argType2 = getType(node.right.left);

        if(argType1==MyType.NAT && argType2==MyType.NAT) {
            return MyType.BOOL;
        }
        typeError("Invalid EQ Type");
        return null;
    }

    private MyType getAtomType(Node node) {
        MyType argType = getType(node.left);

        if(argType!=null) {
            return MyType.BOOL;
        }
        typeError("Invalid ATOM Type");
        return null;
    }

    private MyType getConsType(Node node) {
        MyType argType1 = getType(node.left);
        MyType argType2 = getType(node.right.left);

        if(argType1==MyType.NAT && argType2==MyType.LIST) {
            return MyType.LIST;
        }
        typeError("Invalid CONS Type");
        return null;
    }

    private MyType getCdrType(Node node) {
        MyType argType = getType(node.left);
        if(argType==MyType.LIST) {
            return MyType.LIST;
        }
        typeError("Invalid CDR Type");
        return null;
    }

    private MyType getCarType(Node node) {
        MyType argType = getType(node.left);
        if(argType==MyType.LIST) {
            return MyType.NAT;
        }
        typeError("Invalid CAR Type");
        return null;
    }

    private boolean isFunction(Node node) {
        String funcName = node.lexToken.getLiteralValue();

        switch(funcName) {
            case "CAR":
            case "CDR":
            case "CONS":
            case "ATOM":
            case "EQ":
            case "NULL":
            case "INT":
            case "PLUS":
            case "LESS":
            case "COND":
                return true;
            default:
                return false;
        }
    }

    private MyType identifyType(Node node) {
        //System.out.println("Debug Identify type: " + node.lexToken.getLiteralValue());
        if(node.lexToken.isNumericAtom) {
            return MyType.NAT;
        } else if(node.lexToken.getLiteralValue().equals("NIL")) {
            return MyType.LIST;
        } else if(node.lexToken.getLiteralValue().equals("T") || node.lexToken.getLiteralValue().equals("F")){
            return MyType.BOOL;
        } else {
            typeError("Unable to identify type.");
        }
        return null;
    }

    private void typeError(String errorMessage) {
        System.out.println("Type Error: " + errorMessage);
        System.exit(0);
    }
}
