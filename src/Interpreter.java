import java.io.*;

/**
 * Created by kiran on 9/26/15.
 */
public class Interpreter {

    public static void main(String[] args) {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        Parser parser = new Parser(br);
        parser.parseStart();

    }
}