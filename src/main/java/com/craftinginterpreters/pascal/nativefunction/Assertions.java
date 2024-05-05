package com.craftinginterpreters.pascal.nativefunction;

import com.craftinginterpreters.pascal.Expr;
import com.craftinginterpreters.pascal.PascalEnum;
import com.craftinginterpreters.pascal.RuntimeError;

public class Assertions {
    public static void AssertTrue(Expr expr, Object obj) {
        Expr.Call call = (Expr.Call) expr;

        if (!isTruthy(obj)) {
            throw new RuntimeError(call.paren, "Assertion 'left = right' failed.");
        }
    }

    public static void AssertEqual(Expr.Call call, Object left, Object right) {
        if (!isEqual(left, right)) {
            throw new RuntimeError(call.paren, "Assertion 'left = right' failed.  Expected '" + left + "' but got '" + right + "'.");
        }
    }

    private static boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        if (object instanceof Integer) return ((int)object != 0);
        if (object instanceof PascalEnum e) return e.value != 0;

        return true;
    }

    private static boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }
}
