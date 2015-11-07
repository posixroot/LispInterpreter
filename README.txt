Kiran Maddipati
maddipati.5@osu.edu

CSE 6431 - Project Part2

1. The project contains 8 java files:
	a. Interpreter.java -  Contains the main function. This class creates the Parser object and calls ParseStart() function.
	b. LexAnalyzer.java - This class reads the input file byte by byte and returns a LexToken. This class implements the getNextToken() function.
	c. Parser.java - This class parses the S-exp by getting tokens from LexAnalyzer class ( by calling getNextToken() ) and also appropiately prints parse tree.
	d. Node.java - This class defines the Node structure for each inner-node and leaf-node of the parse tree.
	e. LexToken.java - This class defines the Token object that is returned by the getNextToken() function.
	f. LexTokenID.java - This class defines the enum for the terminal symbols.
	g. TreePrinter.java - This class defines the functions that are used to print the parse tree in either Dot or List notation.
	h. ListEvaluator.java - This class defines the eval function. Also, it contains implementations of all other lisp functions.

2. Useful Funtion References:
	getNextToken() - Implemented in LexAnalyzer.java
	parseStart() and parseSexp() - Implemented in Parser.java
	main() - Implemented in Interpreter.java
	printList() - Implemented in TreePrinter.java. This function prints the list notation of the S-exp.
	printNode() - Implemented in TreePrinter.java. This function prints the dot notation of teh S-exp.
	eval() - Implemented in LispEvaluator.java. This function call other helper eval functions defined in the same class.

3. Makefile is enclosed. Simply run make to build the project.

4. Runfile is enclosed.

5. Valid test input can be found in file: valid-test.

6. Invalid test input can be found in file: invalid-test

