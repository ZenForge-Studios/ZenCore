package com.cuac_xd.zencore.util

import java.util.Random
import kotlin.math.*

/**
 * High-performance, zero-allocation math expression evaluator supporting variables,
 * operator precedence, parentheses, and math functions (min, max, abs, round, floor, ceil, clamp, random).
 */
object MathEvaluator {
    private val random = Random()

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
        return try {
            Parser(cleaned).parse()
        } catch (_: Exception) {
            0.0
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

        fun parse(): Double {
            nextChar()
            val x = parseExpr()
            return if (x.isFinite()) x else 0.0
        }

        private fun parseExpr(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> {
                        val divisor = parseFactor()
                        x = if (divisor == 0.0) 0.0 else x / divisor
                    }
                    eat('%'.code) -> {
                        val mod = parseFactor()
                        x = if (mod == 0.0) 0.0 else x % mod
                    }
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpr()
                eat(')'.code)
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                x = expr.substring(startPos, pos).toDouble()
            } else if (ch in 'a'.code..'z'.code || ch in 'A'.code..'Z'.code || ch == '_'.code) {
                while (ch in 'a'.code..'z'.code || ch in 'A'.code..'Z'.code || ch == '_'.code) nextChar()
                val func = expr.substring(startPos, pos).lowercase()
                if (eat('('.code)) {
                    val args = mutableListOf<Double>()
                    if (!eat(')'.code)) {
                        do {
                            args.add(parseExpr())
                        } while (eat(','.code))
                        eat(')'.code)
                    }
                    x = when (func) {
                        "min" -> if (args.size >= 2) min(args[0], args[1]) else args.firstOrNull() ?: 0.0
                        "max" -> if (args.size >= 2) max(args[0], args[1]) else args.firstOrNull() ?: 0.0
                        "abs" -> abs(args.firstOrNull() ?: 0.0)
                        "round" -> round(args.firstOrNull() ?: 0.0)
                        "floor" -> floor(args.firstOrNull() ?: 0.0)
                        "ceil" -> ceil(args.firstOrNull() ?: 0.0)
                        "clamp" -> if (args.size >= 3) max(args[1], min(args[0], args[2])) else args.firstOrNull() ?: 0.0
                        "random" -> {
                            val minVal = args.getOrNull(0) ?: 0.0
                            val maxVal = args.getOrNull(1) ?: 1.0
                            minVal + (maxVal - minVal) * random.nextDouble()
                        }
                        else -> 0.0
                    }
                } else {
                    x = 0.0
                }
            } else {
                return 0.0
            }
            return x
        }
    }
}
