package com.craftinginterpreters.pascal;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InterpreterTest {
    private Expr parseExpr(String source) {
        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();

        var parser = new Parser(tokens, false);

        return parser.expression();
    }

    // Convenience method for parsing statements.
    //
    private List<Stmt> parseStmts(String source) {
        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();

        var parser = new Parser(tokens, false);

        return parser.parse();
    }

    // Tests Nil.
    //
    @Test
    void testNil() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Literal) parseExpr("nil");
        var result = interpreter.visitLiteralExpr(expr);

        assertNull(result, "should be null");
    }

    // Tests Boolean true.
    //
    @Test
    void testTrue() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Literal) parseExpr("True");
        var result = interpreter.visitLiteralExpr(expr);

        assertEquals(Boolean.TRUE, result, "should be true");
    }

    // Tests boolean false.
    //
    @Test
    void testFalse() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Literal) parseExpr("False");
        var result = interpreter.visitLiteralExpr(expr);

        assertEquals(Boolean.FALSE, result, "should be false");
    }

    // Tests parsing a number.
    //
    @Test
    void testNumber() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Literal) parseExpr("1");
        var result = interpreter.visitLiteralExpr(expr);

        assertEquals(1, result, "should be 1");
    }

    // Tests parsing a float.
    //
    @Test
    void testFloat() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Literal) parseExpr("1.2");
        var result = interpreter.visitLiteralExpr(expr);

        assertEquals(1.2, result, "should be 1.2");
    }

    // Tests parsing a negative number.
    //
    @Test
    void testNegativeNumber() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Unary) parseExpr("-3.14");
        var result = interpreter.visitUnaryExpr(expr);

        assertEquals(-3.14, result, "should be -3.14");
    }

    // Test Not True
    // Test Not False
    // Test Not Truthy--Nil, 0, 1, etc.

    // Tests not Boolean.
    //
    @Test
    void testNotBoolean() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Unary) parseExpr("not True");
        var result = interpreter.visitUnaryExpr(expr);

        assertEquals(Boolean.FALSE, result, "should be false");
    }

    // Negative value should fail if not a number
    //
    @Test
    void testNegateNotANumber() {
        var interpreter = new Interpreter(new TestErrorHandler());
        var expr = (Expr.Unary) parseExpr("-True");

        var ex = assertThrows(RuntimeError.class, () -> interpreter.visitUnaryExpr(expr));

        assertEquals("Operand must be a number.", ex.getMessage());
    }

    // Tests String.
    //
    @Test
    void testString() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Literal) parseExpr("'ABC'");
        var result = interpreter.visitLiteralExpr(expr);

        assertEquals("ABC", result);
    }

    // Tests Grouping.
    //
    @Test
    void testGrouping() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Grouping) parseExpr("(42)");
        var result = interpreter.visitGroupingExpr(expr);

        assertEquals(42, result);
    }


    // Parsing a grouping that's missing its right parenthesis should return a runtime error.
    //
    @Test
    void testGroupingNoRightParen() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var expr = (Expr.Grouping) parseExpr("(42");
            interpreter.visitGroupingExpr(expr);
        });
        assertEquals("Expect ')' after expression.", ex.getMessage());
    }

    // Tests Addition.
    //
    @Test
    void testAddition() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("1 + 1");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(2, result);
    }

    // Tests addition using doubles.
    //
    @Test
    void testAdditionDouble() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("1.0 + 1.0");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(2.0, result);
    }

    // Adding two strings should concatenate them.
    //
    @Test
    void testAddStrings() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("'ABC' + 'DEF'");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals("ABCDEF", result);
    }

    // Add operator should fail if left not a number.
    //
    @Test
    void testAddLeftNotANumber() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var expr = (Expr.Binary) parseExpr("True + 1");
            interpreter.visitBinaryExpr(expr);
        });
        assertEquals("Operands must be two numbers, or two strings.", ex.getMessage());
    }

    // Add operator should fail if right not a number.
    //
    @Test
    void testAddRightNotANumber() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var expr = (Expr.Binary) parseExpr("1 + False");
            interpreter.visitBinaryExpr(expr);
        });
        assertEquals("Operands must be two numbers, or two strings.", ex.getMessage());
    }

    // Tests Subtraction.
    //
    @Test
    void testSubtraction() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("2 - 1");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(1, result);
    }

    // Tests subtraction using doubles.
    //
    @Test
    void testSubtractionDoubles() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("2.0 - 1.0");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(1.0, result);
    }

    // Subtract operator should fail if left not a number.
    //
    @Test
    void testSubtractLeftNotANumber() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var expr = (Expr.Binary) parseExpr("'abc' - 1");
            interpreter.visitBinaryExpr(expr);
        });
        assertEquals("Operands must be numbers.", ex.getMessage());
    }

    // Subtract operator should fail if right not a number.
    // FIXME
    @Disabled
    @Test
    void testSubtractRightNotANumber() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var expr = (Expr.Binary) parseExpr("1 - nil");
            interpreter.visitBinaryExpr(expr);
        });
        assertEquals("Operands must be numbers.", ex.getMessage());
    }

    // Tests Multiplication.
    //
    @Test
    void testMultiplication() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("2 * 2");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(4, result);
    }

    // Tests Multiplication using doubles.
    //
    @Test
    void testMultiplicationDoubles() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("2.0 * 2.0");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(4.0, result);
    }

    // Multiply operator should fail if left not a number.
    //
    @Test
    void testMultiplyLeftNotANumber() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var expr = (Expr.Binary) parseExpr("False * 1");
            interpreter.visitBinaryExpr(expr);
        });
        assertEquals("Operands must be numbers.", ex.getMessage());
    }

    // Subtract operator should fail if right not a number.
    //
    @Test
    void testMultiplyRightNotANumber() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var expr = (Expr.Binary) parseExpr("1 * 'two'");
            interpreter.visitBinaryExpr(expr);
        });
        assertEquals("Operands must be numbers.", ex.getMessage());
    }

    // Tests Division.
    //
    @Test
    void testDivision() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("3.0 / 2.0");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(1.5, result);
    }

    // Tests division using integers.
    //
    @Test
    void testDivisionIntegers() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("3 / 2");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(1, result);
    }

    // Divide operator should fail if left not a number.
    //
    @Test
    void testDivideLeftNotANumber() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var expr = (Expr.Binary) parseExpr("False / 5");
            interpreter.visitBinaryExpr(expr);
        });
        assertEquals("Operands must be numbers.", ex.getMessage());
    }

    // Subtract operator should fail if right not a number.
    //
    @Test
    void testDivideRightNotANumber() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var expr = (Expr.Binary) parseExpr("42 / 'forty-two'");
            interpreter.visitBinaryExpr(expr);
        });
        assertEquals("Operands must be numbers.", ex.getMessage());
    }

    // Tests greater operator.
    //
    @Test
    void testGreater() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("2 > 1");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(true, result);
    }

    // Tests greater operator using doubles.
    //
    @Test
    void testGreaterDoubles() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("2.0 > 1.0");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(true, result);
    }

    // Tests greater operator using characters.
    //
    @Test
    void testGreaterChar() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("#1 > #0");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(true, result);
    }

    // Tests greater equal operator.
    //
    @Test
    void testGreaterEqual() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("1 >= 1");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(true, result);
    }

    // Tests greater equal operator using doubles.
    //
    @Test
    void testGreaterEqualDouble() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("1.0 >= 1.0");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(true, result);
    }

    // Tests greater equal operator using characters.
    //
    @Test
    void testGreaterEqualChar() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("#0 >= #0");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(true, result);
    }

    // Tests less operator.
    //
    @Test
    void testLess() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("1 < 2");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(true, result);
    }

    // Tests less operator using doubles.
    //
    @Test
    void testLessDouble() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("1.0 < 2.0");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(true, result);
    }


    // Tests less operator using characters.
    //
    @Test
    void testLessChar() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("#0 < #1");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(true, result);
    }

    // Tests less equal operator.
    //
    @Test
    void testLessEqual() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("1 <= 1");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(true, result);
    }

    // Tests less equal operator using doubles.
    //
    @Test
    void testLessEqualDouble() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("1.0 <= 1.0");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(true, result);
    }

    // Tests less equal operator using doubles.
    //
    @Test
    void testLessEqualChar() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("#0 <= #0");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(true, result);
    }

    // Tests equal operator.
    //
    @Test
    void testEqual() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("7 = 7");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(true, result);
    }

    // Equal should work with other types.
    //
    @Test
    void testEqualOtherType() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("7 = 'Abc'");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(false, result);
    }

    // Tests not equal operator.
    //
    @Test
    void testNotEqual() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("1 <> 7");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(true, result);
    }

    // Not equal should work with other types.
    //
    @Test
    void testNotEqualOtherType() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("1 <> True");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(true, result);
    }

    // Tests complex expression.
    //
    @Test
    void testComplexExpression() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Binary) parseExpr("(1.0 + 1.0) / 3.0 * 1.5 - 2.0");
        var result = interpreter.visitBinaryExpr(expr);

        assertEquals(-1.0, result);
    }

    // Tests a variable declaration.
    //
    @Test
    void testVariableDeclaration() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts  = parseStmts("var Abc := 123;");
        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));
        assertEquals(123, result);
    }

    // Variable declaration should fail if no identifier.
    //
    @Test
    void testVariableDeclarationNoIdentifier() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var expr = (Expr.Binary) parseExpr("var 123 := 123");
            interpreter.visitBinaryExpr(expr);
        });
        // TODO: Better error message?
        assertEquals("Expect expression.", ex.getMessage());
    }

    // Variable declaration should fail if no semicolon at end.
    //
    @Test
     void testVariableDeclarationNoSemicolon() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("var Abc := 123");
            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertEquals("Expect ';' after variable declaration.", ex.getMessage());

    }

    // Tests a block.
    //
    @Test
    void testBlock() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            var Abc := 2;
            begin
                Abc := 1;
            end""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(1, result);
    }

    // Block should fail if no end.
    //
    @Test
    void testBlockNoEnd() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
               var Abc := 2;
               begin
                  Abc := 1;""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);
            interpreter.interpret(stmts);
        });
        assertEquals("Expect 'end' after block.", ex.getMessage());
    }

    // Tests if statement.
    //
    @Test
    void testIfStatement() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            var Abc := 2;
            if True then Abc := 1;""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(1, result);
    }

    // If statement should treat non-boolean values as truthy.
    //
    @Test
    void testIfStatementTruthy() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            var Abc := 2;
            if 1 then Abc := 1;""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(1, result);
    }

    // If statement should fail if no then.
    //
    @Test
    void testIfNoThen() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
               var Abc := 2;
               if True Abc := 1;""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);
            interpreter.interpret(stmts);
        });
        assertEquals("Expect 'then' after if condition.", ex.getMessage());
    }

    // Test Print.  TODO: Exercising for now, remove when add WriteLn().
    //
    @Test
    void testPrint() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
               Print 1;
               Print 'ABC';""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);
    }

    // Test Clock.
    //
    @Test
    void testClock() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
               var Abc := clock();
               Print Abc;""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);

        interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));
    }

    // Tests else statement.
    //
    @Test
    void testElseStatement() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            var Abc := 2;
            if False then Abc := 3; else Abc := 1;""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(1, result);
    }

    // Tests And operator.
    //
    @Test
    void testAnd() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Logical) parseExpr("True and False");
        var result = interpreter.visitLogicalExpr(expr);

        assertEquals(false, result);
    }

    // Tests Or operator.
    //
    @Test
    void testOr() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var expr = (Expr.Logical) parseExpr("True or False");
        var result = interpreter.visitLogicalExpr(expr);

        assertEquals(true, result);
    }

    // Tests subscript operator.
    //
    @Test
    void testSubscriptOperator() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            var S := 'ABC';
            var Abc := S[1];""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals('B', result);
    }

    // A runtime error should be thrown if the subscript target is not a string or array.
    //
    @Test
    void testSubscriptInvalidTarget() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var stmts = parseStmts("""
            var S := 123;
            var Abc := S[1];""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);
            interpreter.interpret(stmts);
        });
        assertEquals("Subscript target should be an ordinal.", ex.getMessage());
    }

    // Tests while statement.
    //
    @Test
    void testWhileStatement() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            var Abc := 2;
            while Abc > 1 do
            begin
                Abc := Abc - 1;
            end""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(1, result);
    }

    // While statement should fail if no 'do'.
    //
    @Test
    void testWhileNoDo() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
               var Abc := 2;
               while Abc > 1
               begin
                  Abc := Abc - 1;
               end""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);
            interpreter.interpret(stmts);
        });
        assertEquals("Expect 'do' after condition.", ex.getMessage());
    }

    // Tests for statement.
    //
    @Test
    void testForStatement() {
        // TODO: for I in 1 to 5 do
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            var Abc := 0;
            for var I := 1; I < 5; I := I + 1 do
            begin
               Abc := Abc + 1;
            end""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(4, result);
    }

    // Parsing for statement should fail if missing 'do'
    //
    @Test
    void testForStatementMissingDo() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
            for var I := 1; I < 5; I := I + 1
            begin
               Abc := Abc + 1;
            end""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);
            interpreter.interpret(stmts);
        });
        assertEquals("Expect 'do' after for clauses.", ex.getMessage());
    }

    // Parsing for statement should fail if missing semicolon.
    //
    @Test
    void testForStatementMissingSemicolon() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
            for var I := 1 do
            begin
               Abc := Abc + 1;
            end""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);
            interpreter.interpret(stmts);
        });
        assertEquals("Expect ';' after variable declaration.", ex.getMessage());
    }

    // Tests function.
    //
    @Test
    void testFunction() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            function Test(A, B);
            begin
                Abc := 7;
            end
            
            var Abc := 1;
            Test(1, 2);""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(7, result);
    }

    // Tests function with no parameters.
    //
    @Test
    void testFunctionNoParameters() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            function Test;
                begin Abc := 7;
            end
            
            var Abc := 1;
            Test();""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(7, result);
    }

    @Test
    void testFunctionMissingName() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
                function (A,B);
                begin
                    Abc := 7;
                end""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);
            interpreter.interpret(stmts);
        });
        assertEquals("Expect function name.", ex.getMessage());
    }

    @Test
    void testFunctionMissingRightParen() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
                function Test(A,B;
                begin
                    Abc := 7;
                end""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);
            interpreter.interpret(stmts);
        });
        assertEquals("Expect ') after parameters.", ex.getMessage());
    }

    @Test
    void testFunctionMissingSemicolon() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
                function Test(A,B)
                begin
                   Abc := 7;
                end""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);
            interpreter.interpret(stmts);
        });
        assertEquals("Expect ';'", ex.getMessage());
    }

    @Test
    void testFunctionMissingBegin() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
                function Test(A,B);
                    var Abc := 1;""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);
            interpreter.interpret(stmts);
        });
        assertEquals("Expect 'begin' before function body.", ex.getMessage());
    }

    // Function define should fail if missing parameter name.
    //
    @Test
    void testFunctionMissingParameterName() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
                function Test(A,)
                begin
                    Abc := 7;
                end""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);
            interpreter.interpret(stmts);
        });
        assertEquals("Expect parameter name.", ex.getMessage());
    }

    // Tests procedure.
    //
    @Test
    void testProcedure() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            procedure Test(A, B);
            begin
                Abc := 7;
            end
            
            var Abc := 1;
            Test(1, 2);""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(7, result);
    }

    // Procedure define should fail if adding a return type.
    //
    @Test
    void testProcedureWithReturnType() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
                procedure Test(A,B) : Integer;
                begin
                    Abc := 7;
                end""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);
            interpreter.interpret(stmts);
        });
        assertEquals("Procedures cannot have return type.", ex.getMessage());
    }

    // Tests Exit statement.
    //
    @Test
    void testExit() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            function Test();
            begin
                Exit 123;
            end
            
            var Abc := Test();""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(123, result);
    }

    // .A function should return null if no Exit statement.
    //
    @Test
    void testFunctionNoExit() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            function Test();
            begin
            end
            
            var Abc := Test();""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertNull(result);
    }

    // .A function should return null if Exit statement has no value.
    //
    @Test
    void testFunctionExitNoValue() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            function Test();
            begin
               Exit;
            end
            
            var Abc := Test();""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertNull(result);
    }

    // Exit should fail if not in a function.
    //
    @Test
    void testExitNotInFunction() {
        var interpreter = new Interpreter(new TestErrorHandler());
        var stmts = parseStmts("exit 777;");

        var ex = assertThrows(Return.class, () -> {
            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertNull(ex.getMessage());
    }

    // Exit should fail no terminating semicolon.
    //
    @Test
    void testExitNoSemicolon() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
                function Test;
                begin
                   exit 123
                end""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertEquals("Expect ';' after exit value.", ex.getMessage());

    }

    // Tests Call statement.
    //
    @Test
    void testCall() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var stmts = parseStmts("Test();");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });

        // FIXME
        assertEquals("Undefined variable 'Test'.", ex.getMessage());
    }

    // Call statement with wrong number of parameters should fail.
    //
    @Test
    void testCallBadArity() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var stmts = parseStmts("""
               function Test(A, B, C);
               begin
               end

               var Abc := Test();""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertEquals("No matching signature for function.", ex.getMessage());
    }

    // Tests class declaration.
    //
    @Test
    void testClassDeclaration() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            class Test;
            begin
            end
          
            var Abc := Test();""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("Test instance", result.toString());
    }

    // Tests class inheritance.
    //
    @Test
    void testClassInheritance() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            class Animal;
            begin
            end
            
            class Dog (Animal);
            begin
            end
          
            var Abc := Dog();""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);
        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("Dog instance", result.toString());
    }

    // Should fail if super property is not defined.
    //
    @Test
    void testSuperNoProperty() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            class Animal;
            begin
            end

            class Dog (Animal);
            begin
               Method;
               begin
                  super.Other();
               end
            end

            var TheDog := Dog();
            TheDog.Method();""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        var ex = assertThrows(RuntimeError.class, () -> interpreter.interpret(stmts));

        assertEquals("Undefined property 'Other'.", ex.getMessage());

    }

    // Superclass declaration should fail if no identifier.
    //
    @Test
    void testSuperclassNoIdentifier() {
        var ex = assertThrows(Parser.ParseError.class, () -> parseStmts("""
           class Cat();
           begin
           end"""));
        assertEquals("Expect superclass name.", ex.getMessage());
    }

    // Superclass declaration should fail if attempting to inherit from self.
    //
    @Disabled
    @Test
    void testInheritFromSelf() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var stmts = parseStmts("""
               class Test (Test);
               begin
               end""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });

        // FIXME: [line 1] Error at 'Test': A class can't inherit from itself.
        assertEquals("Undefined variable 'Test'.", ex.getMessage());
    }

    // Parsing a class should fail if it doesn't have a name.
    //
    @Test
    void testClassWithNoName() {
        var ex = assertThrows(Parser.ParseError.class, () -> parseStmts("""
           class;
           begin
           end"""));

        assertEquals("Expect class name.", ex.getMessage());
    }

    // Parsing a class should fail if it doesn't have a 'begin'.
    //
    @Test
    void testClassNoBegin() {
        var ex = assertThrows(Parser.ParseError.class, () -> parseStmts("""
           class Test;
           
           end"""));

        assertEquals("Expect 'begin' before class body.", ex.getMessage());
    }

    // Parsing a class should fail if it doesn't have an 'end'.
    //
    @Test
    void testClassNoEnd() {
        var ex = assertThrows(Parser.ParseError.class, () -> parseStmts("""
           class Test;
           begin"""));

        assertEquals("Expect 'end' after class body.", ex.getMessage());
    }

    // Tests get property.
    //
    @Test
    void testGetProperty() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            class Test;
            begin
                Init;
                begin
                   this.Field := 'ABC';
                end
            end
          
            var T := Test();
            var Abc := T.Field;""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("ABC", result.toString());
    }

    // Tests implicit 'this' property.
    //
    @Test
    void testImplicitThis() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            class Test;
            begin
                constructor Init();
                begin
                   this.Field := 'ABC';
                end
                
                function DoSomething();
                begin
                   Field := 'HIJ';
                end
                
                function GetSomething();
                begin
                   exit Field;
                end
                
                function GetSomethingElse();
                begin
                   exit GetSomething();
                end
            end
          
            var T := Test();
            T.DoSomething();
            
            var Abc := T.GetSomethingElse();""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("HIJ", result.toString());
    }

    // Parsing a class should fail if it doesn't have a 'end'.
    //
    @Test
    void testClassNoName() {
        var ex = assertThrows(Parser.ParseError.class, () -> parseStmts("""
           class;
           begin
           end"""));

        assertEquals("Expect class name.", ex.getMessage());
    }

    // Tests Set property.
    //
    @Test
    void testSetProperty() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            class Test;
            begin
            end
          
            var T := Test();
            
            T.Field := 123;
            var Abc := T.Field;""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("123", result.toString());
    }

    // Tests invoking a method.
    //
    @Test
    void testInvokeMethod() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            var Abc := 1;
            
            class Test;
            begin
               ChangeGlobal(Value);
               begin
                  Abc := Value;
               end
            end
          
            var T := Test();
            T.ChangeGlobal(42);""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("42", result.toString());
    }

    // Parsing a method should fail if invalid name.
    //
    @Test
    void testMethodInvalidName() {
        var ex = assertThrows(Parser.ParseError.class, () -> parseStmts("""
           class Test;
           begin
              123();
              begin
              end
           end"""));

        assertEquals("Expect method name.", ex.getMessage());
    }

    // Should fail if invoking a non-instance.  TODO:  This will be added... soon.
    //
    @Test
    void testInvokeNonInstance() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var stmts = parseStmts("var Abc := 123.ToString();");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });

        assertEquals("Only instances have properties.", ex.getMessage());
    }

    // Should fail if setting property on non-instance
    //
    @Test
    void testSetNonInstance() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var stmts = parseStmts("123.Length := 5;");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });

        assertEquals("Only instances have fields.", ex.getMessage());
    }

    // Should fail if calling a non-function
    //
    @Test
    void testCallNonFunction() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var stmts = parseStmts("var Abc := 123();");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });

        assertEquals("Can only call functions and classes.", ex.getMessage());
    }

    // Tests super.
    //
    @Test
    void testSuper() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            var Abc := 1;
            
            class A;
            begin
               Test;
               begin
                  Abc := Abc + 1;
               end
            end
            
            class B (A);
            begin
               Test;
               begin
                  super.Test();
                  Abc := Abc + 1;
               end
            end
          
            var TheB := B();
            TheB.Test();""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("3", result.toString());
    }

    // Should fail if superclass is not a class.
    //
    @Test
    void testSuperclassIsClass() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var stmts = parseStmts("""
               var Abc := 123;
               
               class Def (Abc);
               begin
               end
               
               var X := Def();""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);

        });

        assertEquals("Superclass must be a class.", ex.getMessage());
    }

    // Should fail if calling this outside of class.
    //
    @Disabled
    @Test
    void testThisOutsideClass() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var stmts = parseStmts("var Abc := this.Field;");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);
        });

        assertEquals("Can't use 'this' outside a class.", ex.getMessage());
    }

    // Should fail if calling super outside of class.
    //
    @Disabled
    @Test
    void testSuperOutsideClass() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var stmts = parseStmts("var Abc := super.Field;");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);
        });

        //
        assertEquals("Can't use 'super' outside a class.", ex.getMessage());
    }

    // Should fail if calling super in class without superclass.
    //
    @Disabled
    @Test
    void testSuperWithoutAncestor() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var stmts = parseStmts("""
               class Test;
               begin
                  Method();
                  begin
                     super.Method();
                  end
               end
               
               var Abc := Test();
               Abc.Method();""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertEquals("Can't use 'super' outside a class.", ex.getMessage());
    }

    // Tests enums.
    //
    @Test
    void testEnum() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            type Color = (Red, Green, Blue);
            
            var Abc := Red;""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("Red", result.toString());
    }

    // Enums should throw runtime exception if no name.
    //
    @Test
    void testEnumNoName() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
               type = (Red, Green, Blue)""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertEquals("Expect enum name.", ex.getMessage());
    }

    // Enums should throw runtime exception if no equals.
    //
    @Test
    void testEnumNoEquals() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
               type Color (Red, Green, Blue)""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertEquals("Expect '=' after enum declaration.", ex.getMessage());
    }

    // Enums should throw runtime exception if no opening paren
    //
    @Test
    void testEnumNoLeftParen() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
               type Color = Red, Green, Blue""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertEquals("Expect '('", ex.getMessage());
    }

    // Enums should throw runtime exception if no enums :)
    // TODO: think about this one!
    //
    @Test
    void testEnumNoEnums() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
               type Color = ()""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertEquals("Expect enum identifier.", ex.getMessage());
    }

    // Enums should throw runtime exception if no closing paren.
    //
    @Test
    void testEnumNoRightParen() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
               type Color = (Red, Blue""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertEquals("Expect ')'", ex.getMessage());
    }

    // Enums should throw runtime exception if no semicolon.
    //
    @Test
    void testEnumNoSemicolon() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
               type Color = (Red, Green, Blue)""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertEquals("Expect ';'", ex.getMessage());
    }

    // Tests defining a map.
    //
    @Test
    void testMap() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            var map := [1:'ABC',2:'DEF'];
            
            var Abc := map.get(1);""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("ABC", result.toString());
    }

    // Parsing a map should fail if no colons.
    // TODO: Valid list?
    //
    @Test
    void testMapNoColon() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
              var map := [1, 2, 3]""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertEquals("Expect ':' after key.", ex.getMessage());
    }

    // Map should fail if not ended with bracket
    // TODO: Valid list?
    //
    @Test
    void testMapNoEndingBracket() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
              var map := [1:'ABC', 2:'DEF'""");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertEquals("Expect ']' after map.", ex.getMessage());
    }

    // Tests case statement
    //
    @Test
    void testCaseStatement() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            function Nop(); begin end
            var A := #0;
            var Abc;
            
            case A of
               'X': Nop();
               'Y': Nop();
               else
                 Abc := True;
            end""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(true, result);
    }

    // Tests Array.
    //
    @Test
    void testArray() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            var A := Array(1);
            A.set(0, 'ABC');
            
            var Abc := A.get(0);""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("ABC", result);
    }

    // Tests List.
    //
    @Test
    void testList() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            var A := List();
            A.add('ABC');
            
            var Abc := A[0];""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("ABC", result);
    }

    // Tests Map without sugar syntax.
    //
    @Test
    void testMapNoSugar() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            var A := Map();
            A.put(1,'XYZ');
            
            var Abc := A.get(1);""");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("XYZ", result);
    }

    // Tests Write.
    //
    @Test
    void testWrite() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("Write('Abc');");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);
    }

    // Tests WriteLn.
    //
    @Test
    void testWriteLn() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("WriteLn('xyz');");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);
    }

    // Tests Length.
    //
    @Test
    void testLength() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("var X := Length('ABCDEF');");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "X", "", 0, 0, "test"));

        assertEquals(6, result);
    }

    // Tests Copy.
    //
    @Test
    void testCopy() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("var X := Copy('ABCDEF', 0, 3);");

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "X", "", 0, 0, "test"));

        assertEquals("ABC", result);
    }


    // Tests Raise.
    //
    @Test
    void testRaise() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(RuntimeError.class, () -> {
            var stmts = parseStmts("raise 'Error!';");

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertEquals("Error!", ex.getMessage());
    }

    // Tests try/except.
    //
    @Test
    void testTryExcept() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            var Abc := False;
            try
               raise 'Hello';
            except
               on e : String do Abc := True;
            end
            """);

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(true, result);
    }

    // Tests try/except.
    //
    @Test
    void testTryExceptNoThrow() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
            var Abc := False;
            try
               WriteLn ('Test');
            except
               Abc := True;
            end
            """);

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);

        var result =  interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(false, result);
    }

    // Tests try missing variable name.
    //
    @Test
    void testTryExpectVariableName() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
            var Abc := False;
            try
               WriteLn ('Test');
            except
               on 1 do
            end
            """);

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertEquals("Expected variable name.", ex.getMessage());
    }

    // Tests try missing variable type.
    //
    @Test
    void testTryExpectVariableType() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
            var Abc := False;
            try
               WriteLn ('Test');
            except
               on e : do
            end
            """);

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertEquals("Expected type.", ex.getMessage());
    }

    // Tests try missing do
    //
    @Test
    void testTryMissingDo() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var ex = assertThrows(Parser.ParseError.class, () -> {
            var stmts = parseStmts("""
            var Abc := False;
            try
               WriteLn ('Test');
            except
               on e : String WriteLn('Abc');
            end
            """);

            var resolver = new Resolver(interpreter);
            resolver.resolve(stmts);

            interpreter.interpret(stmts);
        });
        assertEquals("Expected 'do'.", ex.getMessage());
    }
}