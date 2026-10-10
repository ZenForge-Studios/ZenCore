package com.cuac_xd.zencore.util

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.*

/**
 * High-performance math expression evaluator supporting expression AST/token caching,
 * operator precedence, parentheses, and math functions (min, max, abs, round, floor, ceil, clamp, random).
 */
object MathEvaluator {
    private val parsedCache = ConcurrentHashMap<String, ExpressionNode>()

    @JvmStatic
    @JvmOverloads
    fun eval(expression: String?, variables: Map<String, Double> = emptyMap()): Double {
        if (expression.isNullOrBlank()) return 0.0
        var expr: String = expression
        for ((k, v) in variables) {
            expr = expr.replace("%$k%", v.toString())
            expr = expr.replace("{$k}", v.toString())
        }
        val cleaned = expr.replace(" ", "")

        val node = parsedCache.computeIfAbsent(cleaned) {
            try {
                Parser(it).parseNode()
            } catch (_: Exception) {
                ConstantNode(0.0)
            }
        }
        val result = node.eval()
        return if (result.isFinite()) result else 0.0
    }

    fun interface ExpressionNode {
        fun eval(): Double
    }

    class ConstantNode(private val value: Double) : ExpressionNode {
        override fun eval(): Double = value
    }

    class BinaryNode(
        private val left: ExpressionNode,
        private val op: Char,
        private val right: ExpressionNode
    ) : ExpressionNode {
        override fun eval(): Double {
            val a = left.eval()
            val b = right.eval()
            return when (op) {
                '+' -> a + b
                '-' -> a - b
                '*' -> a * b
                '/' -> if (b == 0.0) 0.0 else a / b
                '%' -> if (b == 0.0) 0.0 else a % b
                '^' -> a.pow(b)
                '>' -> if (a > b) 1.0 else 0.0
                '<' -> if (a < b) 1.0 else 0.0
                '=' -> if (abs(a - b) < 0.000001) 1.0 else 0.0
                else -> 0.0
            }
        }
    }

    class FunctionNode(
        private val funcName: String,
        private val args: List<ExpressionNode>
    ) : ExpressionNode {
        override fun eval(): Double {
            val evaluated = args.map { it.eval() }
            return when (funcName.lowercase()) {
                "min" -> evaluated.minOrNull() ?: 0.0
                "max" -> evaluated.maxOrNull() ?: 0.0
                "abs" -> abs(evaluated.getOrElse(0) { 0.0 })
                "round" -> round(evaluated.getOrElse(0) { 0.0 })
                "floor" -> floor(evaluated.getOrElse(0) { 0.0 })
                "ceil" -> ceil(evaluated.getOrElse(0) { 0.0 })
                "sqrt" -> sqrt(max(0.0, evaluated.getOrElse(0) { 0.0 }))
                "clamp" -> {
                    val v = evaluated.getOrElse(0) { 0.0 }
                    val minV = evaluated.getOrElse(1) { 0.0 }
                    val maxV = evaluated.getOrElse(2) { 1.0 }
                    v.coerceIn(minV, maxV)
                }
                "random" -> {
                    val minV = evaluated.getOrElse(0) { 0.0 }
                    val maxV = evaluated.getOrElse(1) { 1.0 }
                    minV + Math.random() * (maxV - minV)
                }
                else -> 0.0
            }
        }
    }

    private class Parser(private val expr: String) {
        private var pos = -1
        private var ch = -1

        private fun nextChar() {
            ch = if (++pos < expr.length) expr[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parseNode(): ExpressionNode {
            nextChar()
            val node = parseExpr()
            return node
        }

        private fun parseExpr(): ExpressionNode {
            var x = parseComparison()
            while (true) {
                when {
                    eat('+'.code) -> x = BinaryNode(x, '+', parseTerm())
                    eat('-'.code) -> x = BinaryNode(x, '-', parseTerm())
                    else -> return x
                }
            }
        }

        private fun parseComparison(): ExpressionNode {
            var x = parseTerm()
            while (true) {
                when {
                    eat('>'.code) -> x = BinaryNode(x, '>', parseTerm())
                    eat('<'.code) -> x = BinaryNode(x, '<', parseTerm())
                    eat('='.code) -> x = BinaryNode(x, '=', parseTerm())
                    else -> return x
                }
            }
        }

        private fun parseTerm(): ExpressionNode {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x = BinaryNode(x, '*', parseFactor())
                    eat('/'.code) -> x = BinaryNode(x, '/', parseFactor())
                    eat('%'.code) -> x = BinaryNode(x, '%', parseFactor())
                    eat('^'.code) -> x = BinaryNode(x, '^', parseFactor())
                    else -> return x
                }
            }
        }

        private fun parseFactor(): ExpressionNode {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return BinaryNode(ConstantNode(0.0), '-', parseFactor())

            val startPos = pos
            return when {
                eat('('.code) -> {
                    val x = parseExpr()
                    eat(')'.code)
                    x
                }
                ch in '0'.code..'9'.code || ch == '.'.code -> {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    val num = expr.substring(startPos, pos).toDoubleOrNull() ?: 0.0
                    ConstantNode(num)
                }
                ch in 'a'.code..'z'.code || ch in 'A'.code..'Z'.code || ch == '_'.code -> {
                    while (ch in 'a'.code..'z'.code || ch in 'A'.code..'Z'.code || ch in '0'.code..'9'.code || ch == '_'.code) nextChar()
                    val func = expr.substring(startPos, pos)
                    if (eat('('.code)) {
                        val args = mutableListOf<ExpressionNode>()
                        if (!eat(')'.code)) {
                            do {
                                args.add(parseExpr())
                            } while (eat(','.code))
                            eat(')'.code)
                        }
                        FunctionNode(func, args)
                    } else {
                        ConstantNode(0.0)
                    }
                }
                else -> ConstantNode(0.0)
            }
        }
    }
}
