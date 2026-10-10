package com.cuac_xd.zencore.engine.effect

import com.cuac_xd.zencore.engine.context.ZenExecutionContext

fun interface ZenEffect {
    fun execute(context: ZenExecutionContext, args: Map<String, @JvmSuppressWildcards Any>)
}
