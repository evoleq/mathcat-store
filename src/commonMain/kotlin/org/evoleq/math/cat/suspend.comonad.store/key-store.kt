package org.evoleq.math.cat.suspend.comonad.store

import kotlinx.coroutines.CoroutineScope
import org.evoleq.math.cat.marker.MathCatDsl
import org.evoleq.math.cat.suspend.morphism.ScopedSuspended
import org.evoleq.math.cat.suspend.morphism.by



interface KeyStore<A,B,T> : ScopedSuspended<B,ScopedSuspended<A, T>> {
    val data: A
}

@MathCatDsl
@Suppress("FunctionName")
fun <W,K,P>KeyStore(data: W,arrow: suspend CoroutineScope.(K)->ScopedSuspended<W, P>):KeyStore<W,K,P> = object : KeyStore<W,K,P> {
    override val data: W =  data
    override val morphism: suspend CoroutineScope.(K) -> ScopedSuspended<W, P> = arrow
}

@MathCatDsl
fun <W,K,P> KeyStore<W,K,P>.indexed(): IStore<W,K,P> = IStore(data) {
    k -> by(by(this@indexed)(k))(data)
}

fun <W,K,P,L,Q> KeyStore<W,K,P>.times(other: KeyStore<P,L,Q>): KeyStore<W,Pair<K,L>,Q> = KeyStore(data){
    pair -> ScopedSuspended {
        w -> by(by(other)(pair.second))(by(by(this@times)(pair.first))(w))
    }
}

