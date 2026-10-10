package com.cuac_xd.zencore.engine.mutator

import com.cuac_xd.zencore.engine.context.ZenExecutionContext

fun interface ZenMutator {
    fun mutate(context: ZenExecutionContext, args: Map<String, @JvmSuppressWildcards Any>)
}
