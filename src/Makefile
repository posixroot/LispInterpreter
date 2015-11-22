JAVAC = javac

JAVAFLAGS = -g

COMPILE = $(JAVAC) $(JAVAFLAGS)

.SUFFIXES: .java .class

.java.class:
	$(COMPILE) $*.java

CLASS_FILES = Interpreter.java LexAnalyzer.java Parser.java Node.java LexToken.java LexTokenID.java TreePrinter.java LispEvaluator.java TypeChecker.java MyType.java

default: classes

classes: $(CLASS_FILES:.java=.class)

clean:
	$(RM) *.class