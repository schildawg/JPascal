package com.craftinginterpreters.pascal;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Type Checker.
 *
 * # TODO:
 * Undefined type
 * Sub-class
 * Type coercion
 *
 */
public class TypeCheckerTest {
    // Convenience method for parsing statements.
    //
    private List<Stmt> parseStmts(String source) {
        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();

        var parser = new Parser(tokens, false);

        return parser.parse();
    }

    // Tests String type.
    //
    @Test
    void testStringType() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("var Abc : String := 'Abc';");

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("Abc", result);
    }

    // Should get Type Mismatch error if passing a non-String type a String variable.
    //
    @Test
    void testStringTypeMismatch() {
        var stmts = parseStmts("var Abc : String := 123;");

        var ex = assertThrows(RuntimeError.class, () -> {
            var checker = new TypeChecker();
            checker.resolve(stmts);
        });
        assertEquals("Type mismatch!", ex.getMessage());
    }

    // Tests Integer type.
    //
    @Test
    void testIntegerType() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("var Abc : Integer := 123;");

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(123, result);
    }

    // Should get Type Mismatch error if passing a non-Integer to an Integer variable.
    //
    @Test
    void testIntegerTypeMismatch() {
        var stmts = parseStmts("var Abc : Integer := True;");

        var ex = assertThrows(RuntimeError.class, () -> {
            var checker = new TypeChecker();
            checker.resolve(stmts);
        });
        assertEquals("Type mismatch!", ex.getMessage());
    }

    // Tests Boolean type.
    //
    @Test
    void testBooleanType() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("var Abc : Boolean := True;");

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(true, result);
    }

    // Should get Type Mismatch error if passing a non-Boolean to a Boolean variable.
    //
    @Test
    void testBooleanTypeMismatch() {
        var stmts = parseStmts("var Abc : Boolean := 'ABC';");

        var ex = assertThrows(RuntimeError.class, () -> {
            var checker = new TypeChecker();
            checker.resolve(stmts);
        });
        assertEquals("Type mismatch!", ex.getMessage());
    }

    // Tests Char type.
    //
    @Test
    void testCharType() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("var Abc : Char := 'J';");

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals('J', result);
    }

    // Tests Char type using the # format.
    //
    @Test
    void testAltCharType() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("var Abc : Char := #74;");

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals('J', result);
    }

    // Should get Type Mismatch error if passing a non-Char to a Char variable.
    //
    @Test
    void testCharTypeMismatch() {
        var stmts = parseStmts("var Abc : Char := False;");

        var ex = assertThrows(RuntimeError.class, () -> {
            var checker = new TypeChecker();
            checker.resolve(stmts);
        });
        assertEquals("Type mismatch!", ex.getMessage());
    }

    // Tests Double type.
    //
    @Test
    void testDoubleType() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("var Abc : Double := 123.0;");

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(123.0, result);
    }

    // Should get Type Mismatch error if passing a non-Double to a Double variable.
    //
    @Test
    void testDoubleTypeMismatch() {
        var stmts = parseStmts("var Abc : Double := False;");

        var ex = assertThrows(RuntimeError.class, () -> {
            var checker = new TypeChecker();
            checker.resolve(stmts);
        });
        assertEquals("Type mismatch!", ex.getMessage());
    }

    // Tests Enum types.
    //
    @Test
    void testEnumType() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
           type Dual = (No, Yes);
           
           var Abc : Dual := Yes;
           """);

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("Yes", result.toString());
    }

    // Should get Type Mismatch error if passing a non-Enum to an Enum variable.
    //
    @Test
    void testEnumTypeMismatch() {
        var stmts = parseStmts("""
           type Dual = (No, Yes);
           
           var Abc : Dual := 5;
           """);

        var ex = assertThrows(RuntimeError.class, () -> {
            var checker = new TypeChecker();
            checker.resolve(stmts);
        });
        assertEquals("Type mismatch!", ex.getMessage());
    }

    // Should get Type Mismatch error if passing a different Enum to an Enum variable.
    //
    @Test
    void testDifferentEnumTypeMismatch() {
        var stmts = parseStmts("""
           type Dual = (No, Yes);
           type Dual2 = (On, Off);
                      
           var Abc : Dual := Off;
           """);

        var ex = assertThrows(RuntimeError.class, () -> {
            var checker = new TypeChecker();
            checker.resolve(stmts);
        });
        assertEquals("Type mismatch!", ex.getMessage());
    }

    // Tests Class types.
    //
    @Test
    void testClassType() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
           class Item;
           begin
           end
           
           var Abc : Item := Item();
           """);

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("Item instance", result.toString());
    }

    // Should get Type Mismatch error if passing a non-Class to a Class variable.
    //
    @Test
    void testClassTypeMismatch() {
        var stmts = parseStmts("""
           class Item;
           begin
           end
           
           var Abc : Item := 8;
           """);

        var ex = assertThrows(RuntimeError.class, () -> {
            var checker = new TypeChecker();
            checker.resolve(stmts);
        });
        assertEquals("Type mismatch!", ex.getMessage());
    }

    // Should get Type Mismatch error if passing a different Class to an Class variable.
    // TODO: Add sub-class functionality!!!
    //
    @Test
    void testDifferentClassTypeMismatch() {
        var stmts = parseStmts("""
           class Item;
           begin
           end
           
           class Room;
           begin
           end
           
           var Abc : Item := Room();
           """);

        var ex = assertThrows(RuntimeError.class, () -> {
            var checker = new TypeChecker();
            checker.resolve(stmts);
        });
        assertEquals("Type mismatch!", ex.getMessage());
    }

    // Tests Any.
    //
    @Test
    void testAnyType() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
           var Abc : Any := 123;
           """);

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(123, result);
    }

    // Tests assigning a function.
    //
    @Test
    void testAssignFunction() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
           function DoSomething : String;
           begin
              Exit 'ABC';
           end
           
           var Abc : String := DoSomething();
           """);

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("ABC", result.toString());
    }

    // Should get Type Mismatch error if assigning a function with different type.
    //
    @Test
    void testAssignFunctionTypeMismatch() {
        var stmts = parseStmts("""
           function DoSomething : String;
           begin
              Exit 'ABC';
           end
           
           var Abc : Integer := DoSomething();
           """);

        var ex = assertThrows(RuntimeError.class, () -> {
            var checker = new TypeChecker();
            checker.resolve(stmts);
        });
        assertEquals("Type mismatch!", ex.getMessage());
    }


    // Tests assigning a method.
    //
    @Test
    void testAssignMethod() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
           class Item;
           begin
              function DoSomething : String;
              begin
                 Exit 'ABC';
              end
           end
           
           var Thing := Item();
           
           var Abc : String := Thing.DoSomething();
           """);

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals("ABC", result.toString());
    }

    // Tests assigning a logical expression.
    //
    @Test
    void testAssignLogicalExpression() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("var Abc : Boolean := True or False;");

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(true, result);
    }

    // Should get Type Mismatch error if mixing different types for logical.
    //
    @Test
    void testAssignLogicalTypeMismatch() {
        var stmts = parseStmts("var Abc : Boolean := True or 1;");

        var ex = assertThrows(RuntimeError.class, () -> {
            var checker = new TypeChecker();
            checker.resolve(stmts);
        });
        assertEquals("Type mismatch.", ex.getMessage());
    }

    // Tests integer arithmetic.
    //
    @Test
    void testIntegerArithmetic() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("var Abc : Integer := 1 + 1;");

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(2, result);
    }

    // Should get Type Mismatch error if mixing different types for logical.
    //
    @Test
    void testBinaryTypeMismatch() {
        var stmts = parseStmts("var Abc : Boolean := 1 + false;");

        var ex = assertThrows(RuntimeError.class, () -> {
            var checker = new TypeChecker();
            checker.resolve(stmts);
        });
        assertEquals("Type mismatch.", ex.getMessage());
    }

    // Tests Integer unary.
    //
    @Test
    void testIntegerUnary() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("var Abc : Integer := -1;");

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(-1, result);
    }

    // Tests Boolean unary.
    //
    @Test
    void testBooleanUnary() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("var Abc : Boolean := Not True;");

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);

        var result = interpreter.globals.get(new Token(TokenType.IDENTIFIER, "Abc", "", 0, 0, "test"));

        assertEquals(false, result);
    }

    // Tests invalid assign.
    //
    @Test
    void testInvalidAssign() {
        var stmts = parseStmts("""
           var Abc : String := 'ABC';
           Abc := 42;
           """);

        var ex = assertThrows(RuntimeError.class, () -> {
            var checker = new TypeChecker();
            checker.resolve(stmts);
        });
        assertEquals("Type mismatch!", ex.getMessage());
    }

    // Tests invalid Exit.
    //
    @Test
    void testInvalidExit() {
        var stmts = parseStmts("""
           function DoSomething() : String;
           begin
              Exit 123;
           end
           """);

        var ex = assertThrows(RuntimeError.class, () -> {
            var checker = new TypeChecker();
            checker.resolve(stmts);
        });
        assertEquals("Type mismatch!", ex.getMessage());
    }

    // Tests procedure returning value.
    //
    @Test
    void testProcedureReturningValue() {
        var stmts = parseStmts("""
           procedure DoSomething();
           begin
              Exit 123;
           end
           """);

        var ex = assertThrows(RuntimeError.class, () -> {
            var checker = new TypeChecker();
            checker.resolve(stmts);
        });
        assertEquals("Can't return value from procedure.", ex.getMessage());
    }

    // Tests variable section
    //
    @Test
    void testVariableSection() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
          procedure DoSomething();
          var
            A, B, C : Integer := 5;
          
          begin
             A := 1;
          end

          DoSomething();
           """);

        var checker = new TypeChecker();
        checker.resolve(stmts);

        var resolver = new Resolver(interpreter);
        resolver.resolve(stmts);

        interpreter.interpret(stmts);
    }

    // Subscript should throw a type mismatch if not using generics
    //
    @Test
    void testSubscriptNoGeneric() {
        var stmts = parseStmts("""
           var Tokens : List := List();
           Tokens.Add('ABC');

           var Token : String := Tokens[0];
           """);

        var ex = assertThrows(RuntimeError.class, () -> {
            var checker = new TypeChecker();
            checker.resolve(stmts);
        });
        assertEquals("Type mismatch!", ex.getMessage());
    }

    // Subscript should return generic type of List
    //
    @Test
    void testSubscriptWithGeneric() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
           var Tokens : List of String := List();
           Tokens.Add('ABC');

           var Token : String := Tokens[0];
           """);

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);
    }

    // Subscript should return generic type of List
    //
    @Test
    void testAssignSubclass() {
        var interpreter = new Interpreter(new TestErrorHandler());

        var stmts = parseStmts("""
           class Animal;
           begin
           end
           
           class Dog (Animal);
           begin
           end
           
           var Pet : Animal := Dog();
           """);

        var checker = new TypeChecker();
        checker.resolve(stmts);

        interpreter.interpret(stmts);
    }
}
