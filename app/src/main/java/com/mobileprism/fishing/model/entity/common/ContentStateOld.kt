package com.mobileprism.fishing.model.entity.common

import com.mobileprism.fishing.model.entity.content.UserCatch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


data class ContentStateOld<T>(
    val added: MutableList<T> = mutableListOf(),
    val deleted: MutableList<T> = mutableListOf(),
    val modified: MutableList<T> = mutableListOf(),
)






sealed class ContentState<T> (items: MutableList<T>) {
    class ADDED<T>(val items: MutableList<T>): ContentState<T>(items)
    class DELETED<T>(val items: MutableList<T>): ContentState<T>(items)
    class MODIFIED<T>(val items: MutableList<T>): ContentState<T>(items)
}

@OptIn(ExperimentalContracts::class)
@SinceKotlin("1.3")
public inline fun <R, T> ContentState<T>.fold(
    onAdded: (value: MutableList<T>) -> R,
    onDeleted: (value: MutableList<T>) -> R,
    onModified: (value: MutableList<T>) -> R,
): R {
    contract {
        callsInPlace(onAdded, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onDeleted, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onModified, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is ContentState.ADDED -> onAdded(items)
        is ContentState.DELETED -> onAdded(items)
        is ContentState.MODIFIED -> onAdded(items)
    }
}
