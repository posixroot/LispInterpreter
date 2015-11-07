/**
 * Created by kiran on 9/26/15.
 */
public class LexToken {

    LexTokenID tokenID;

    boolean isNumericAtom;

    int numericValue;

    String literalValue;

    public LexToken(LexTokenID tokenID) {
        this.tokenID = tokenID;
        numericValue = -100;
        literalValue = "";
        isNumericAtom = false;
    }

    public LexToken(LexTokenID tokenID, boolean isNumericAtom ) {
        this.tokenID = tokenID;
        this.isNumericAtom = isNumericAtom;
        numericValue = -100;
        literalValue = "";
    }

    public void setNumericValue(int numericValue) {
        this.numericValue = numericValue;
    }

    public void setLiteralValue(String literalValue){
        this.literalValue = literalValue;
    }

    public int getNumericValue() {
        return numericValue;
    }

    public String getLiteralValue() {
        return literalValue;
    }

}