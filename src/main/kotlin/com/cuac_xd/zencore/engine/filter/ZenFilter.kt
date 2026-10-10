package com.cuac_xd.zencore.engine.filter

import com.cuac_xd.zencore.engine.context.ZenExecutionContext

fun interface ZenFilter {
    fun test(context: ZenExecutionContext, args: Map<String, @JvmSuppressWildcards Any>): Boolean
}
