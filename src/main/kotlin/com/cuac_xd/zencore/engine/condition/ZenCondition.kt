package com.cuac_xd.zencore.engine.condition

import com.cuac_xd.zencore.engine.context.ZenExecutionContext

fun interface ZenCondition {
    fun test(context: ZenExecutionContext, args: Map<String, @JvmSuppressWildcards Any>): Boolean
}
