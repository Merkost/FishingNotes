package com.mobileprism.fishing.domain.entity.common

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


data class ContentStateOld<T>(
    val added: MutableList<T> = mutableListOf(),
    val deleted: MutableList<T> = mutableListOf(),
    val modified: MutableList<T> = mutableListOf(),
)

sealed class ContentState<T> (item: T) {
    class ADDED<T>(val item: T): ContentState<T>(item)
    class DELETED<T>(val item: T): ContentState<T>(item)
    class MODIFIED<T>(val item: T): ContentState<T>(item)
}

@OptIn(ExperimentalContracts::class)
@SinceKotlin("1.3")
public inline fun <R, T> ContentState<T>.fold(
    onAdded: (value: T) -> R,
    onDeleted: (value: T) -> R,
    onModified: (value: T) -> R,
): R {
    contract {
        callsInPlace(onAdded, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onDeleted, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onModified, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is ContentState.ADDED -> onAdded(item)
        is ContentState.DELETED -> onAdded(item)
        is ContentState.MODIFIED -> onAdded(item)
    }
}
