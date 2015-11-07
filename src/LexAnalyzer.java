import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by kiran on 9/26/15.
 */
public class LexAnalyzer {

    char peek;
    BufferedReader br;

    public LexAnalyzer(BufferedReader br) {
        this.br = br;
    }

    public char getPeek() {
        return peek;
    }

    public int peekAhead() {
        int readc = 0;
        try {
            readc = br.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        peek = (char)readc;
        return readc;
    }

    public LexToken getNextToken() {

        int readc = 0;
        if(peek!='\0') {
            readc = (int)peek;
        } else {
            readc = peekAhead();
        }

        boolean potentialLiteral = false;
        boolean potentialNumeral = false;
        int tokenLen = 0;
        LexTokenID tokenID = LexTokenID.UNKNOWN;
        LexToken lexToken = null;
        StringBuilder stringValue = new StringBuilder("");

        while( readc != -1 ){

            char letter = (char) readc;
            //System.out.println("letter: " + letter);

            if (letter == '(') {
                tokenID = LexTokenID.OPEN_PAR;
                lexToken = new LexToken(tokenID);
                peek='\0';
                break;
            } else if (letter == ')') {
                tokenID = LexTokenID.CLOSE_PAR;
                lexToken = new LexToken(tokenID);
                peek = '\0';
                break;
            } else if (letter == '.') {
                tokenID = LexTokenID.DOT;
                lexToken = new LexToken(tokenID);
                peek = '\0';
                break;
            } else if ((letter >= 'A' && letter <= 'Z') || (potentialLiteral && letter>='0' && letter<='9')) {
                if(!potentialLiteral && !potentialNumeral && tokenLen==0) {
                    potentialLiteral = true;
                    potentialNumeral = false;
                    tokenLen = 1;
                    tokenID = LexTokenID.ATOM;
                    lexToken = new LexToken(tokenID, false);
                    lexToken.isNumericAtom = false;
                    stringValue.insert(0, letter);
                } else if (potentialNumeral && tokenLen>0) {
                    //error: literal started with numeric character(s)
                    System.out.println("ERROR: Unidentified atom " + stringValue.toString() + letter);
                    tokenID = LexTokenID.UNKNOWN;
                    System.exit(0);
                } else {
                    stringValue.append(letter);
                    tokenLen++;
                }
            } else if ((letter>='0' && letter<='9') || letter=='-' ) {
                if(!potentialLiteral && !potentialNumeral && tokenLen==0) {
                    potentialLiteral = false;
                    potentialNumeral = true;
                    tokenLen = 1;
                    tokenID = LexTokenID.ATOM;
                    lexToken = new LexToken(tokenID, true);
                    lexToken.isNumericAtom = true;
                    stringValue.insert(0, (char)letter);
                } else {
                    stringValue.append(letter);
                    tokenLen++;
                }

            }else if(letter==' ' || letter=='\t' || letter=='\n') {
                peek = '\0';
            } else {
                System.out.println("ERROR: Invalid character " + letter);
                tokenID = LexTokenID.UNKNOWN;
                peek='\0';
                break;
            }

            readc=peekAhead();
            if(peek==')' || peek=='(' || peek=='.' || peek==' ' || peek=='\t' || peek=='\n') { // || peek=='\0'
                //break out and return the token
                if (tokenID != LexTokenID.UNKNOWN) {
                    lexToken.setLiteralValue(stringValue.toString());
                    break;
                }
            }
        }

        if(tokenID != LexTokenID.UNKNOWN) {
            return lexToken;
        } else {
            return null;
        }
    }

}