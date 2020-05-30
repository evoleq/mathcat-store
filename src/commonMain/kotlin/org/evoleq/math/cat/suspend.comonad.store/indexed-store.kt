package org.evoleq.math.cat.suspend.comonad.store

import kotlinx.coroutines.CoroutineScope
import org.evoleq.math.cat.suspend.morphism.ScopedSuspended
import org.evoleq.math.cat.suspend.morphism.by

interface IStore<A, B, T> : ScopedSuspended<B, T> {
    val data: A
    suspend fun <U> map(f: suspend CoroutineScope.(T)->U): IStore<A, B, U> = IStore(data) {
        a -> f(by(this@IStore)(a))
    }
}

@Suppress("FunctionName")
fun <A, B, T> IStore(data: A, reader:  suspend CoroutineScope.(B)->T): IStore<A, B, T> = object : IStore<A, B, T> {
    override val data: A = data
    override val morphism: suspend CoroutineScope.(B) -> T = reader
}

fun <A, B, T> IStore(data: A): (suspend CoroutineScope.(B)->T)-> IStore<A, B, T> = {
    lambda -> IStore(data){index -> lambda(index)}
}

fun <S, A> IStore<S, S, A>.toStore(): Store<S, A> = Store(data){s -> by(this@toStore)(s)}
