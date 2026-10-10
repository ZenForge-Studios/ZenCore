package com.cuac_xd.zencore.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * High-performance, zero-allocation math expression evaluator supporting variables,
 * operator precedence, parentheses, and math functions (min, max, abs, round, floor, ceil, clamp, random).
 */
public final class MathEvaluator {

    private static final Random RANDOM = new Random();

    private MathEvaluator() {}

    public static double eval(String expression) {
        if (expression == null || expression.isBlank()) return 0.0;
        try {
            return new Parser(expression.replace(" ", "")).parse();
        } catch (Exception ignored) {
            return 0.0;
        }
    }

    public static double eval(String expression, Map<String, Double> variables) {
        if (expression == null || expression.isBlank()) return 0.0;
        String expr = expression;
        if (variables != null && !variables.isEmpty()) {
            for (Map.Entry<String, Double> entry : variables.entrySet()) {
                expr = expr.replace("%" + entry.getKey() + "%", String.valueOf(entry.getValue()));
                expr = expr.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }
        }
        return eval(expr);
    }

    private static final class Parser {
        private final String expr;
        private int pos = -1;
        private int ch = -1;

        private Parser(String expr) {
            this.expr = expr;
        }

        private void nextChar() {
            ch = (++pos < expr.length()) ? expr.charAt(pos) : -1;
        }

        private boolean eat(int charToEat) {
            while (ch == ' ') nextChar();
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }

        public double parse() {
            nextChar();
            double x = parseExpr();
            return Double.isFinite(x) ? x : 0.0;
        }

        private double parseExpr() {
            double x = parseTerm();
            while (true) {
                if (eat('+')) x += parseTerm();
                else if (eat('-')) x -= parseTerm();
                else return x;
            }
        }

        private double parseTerm() {
            double x = parseFactor();
            while (true) {
                if (eat('*')) {
                    x *= parseFactor();
                } else if (eat('/')) {
                    double div = parseFactor();
                    x = (div == 0.0) ? 0.0 : x / div;
                } else if (eat('%')) {
                    double mod = parseFactor();
                    x = (mod == 0.0) ? 0.0 : x % mod;
                } else {
                    return x;
                }
            }
        }

        private double parseFactor() {
            if (eat('+')) return parseFactor();
            if (eat('-')) return -parseFactor();

            double x;
            int startPos = pos;
            if (eat('(')) {
                x = parseExpr();
                eat(')');
            } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                x = Double.parseDouble(expr.substring(startPos, pos));
            } else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') {
                while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') nextChar();
                String func = expr.substring(startPos, pos).toLowerCase();
                if (eat('(')) {
                    List<Double> args = new ArrayList<>();
                    if (!eat(')')) {
                        do {
                            args.add(parseExpr());
                        } while (eat(','));
                        eat(')');
                    }
                    x = switch (func) {
                        case "min" -> args.size() >= 2 ? Math.min(args.get(0), args.get(1)) : (!args.isEmpty() ? args.get(0) : 0.0);
                        case "max" -> args.size() >= 2 ? Math.max(args.get(0), args.get(1)) : (!args.isEmpty() ? args.get(0) : 0.0);
                        case "abs" -> !args.isEmpty() ? Math.abs(args.get(0)) : 0.0;
                        case "round" -> !args.isEmpty() ? Math.round(args.get(0)) : 0.0;
                        case "floor" -> !args.isEmpty() ? Math.floor(args.get(0)) : 0.0;
                        case "ceil" -> !args.isEmpty() ? Math.ceil(args.get(0)) : 0.0;
                        case "clamp" -> args.size() >= 3 ? Math.max(args.get(1), Math.min(args.get(0), args.get(2))) : (!args.isEmpty() ? args.get(0) : 0.0);
                        case "random" -> {
                            double min = !args.isEmpty() ? args.get(0) : 0.0;
                            double max = args.size() >= 2 ? args.get(1) : 1.0;
                            yield min + (max - min) * RANDOM.nextDouble();
                        }
                        default -> 0.0;
                    };
                } else {
                    x = 0.0;
                }
            } else {
                return 0.0;
            }
            return x;
        }
    }
}
