package org.evoleq.math.cat.comonad.store

import org.evoleq.math.cat.marker.MathCatDsl
import org.evoleq.math.cat.morphism.Morphism
import org.evoleq.math.cat.morphism.by

/**
 * The Store comonad
 */
interface Store<S, A>: Morphism<S, A> {
    val data: S
    infix fun <B> map(f: (A)->B): Store<S, B> = Store(data){
        a -> f(by(this@Store)(a))
    }
}

/**
 * [Store] comonad constructor function
 */
@MathCatDsl
@Suppress("FunctionName")
fun <S, A> Store(data: S, reader: (S) -> A): Store<S, A> = object : Store<S, A> {
    override val data: S  = data
    
    override val morphism: (S) -> A  = reader
}

/**
 * CoKleisli [Store]
 */
interface CoKlStore<S, A, B> : Morphism<Store<S, A>, B>

/**
 * CoKleisli [Store] constructor function
 */
@MathCatDsl
@Suppress("FunctionName")
fun <S, A, B> CoKlStore(arrow: (Store<S, A>)->B): CoKlStore<S, A, B> = object : CoKlStore<S, A, B> {
    override val morphism: (Store<S, A>) -> B = arrow
}

/**
 * Extract function of the [Store] comonad
 */
@MathCatDsl
fun <S, A> Store<S, A>.extract(): A =  by(this@extract)(data)

/**
 * Duplicate function of the [Store] comonad
 */
@MathCatDsl
fun <S, A> Store<S, A>.duplicate(): Store<S, Store<S , A>> = Store(data) {
    d -> Store(d){ s -> by(this@duplicate)(s) }
}

@MathCatDsl
operator fun <S, A, B, C> CoKlStore<S, A, B>.times(other: CoKlStore<S, B, C>): CoKlStore<S, A, C> = CoKlStore {
    store -> by(other)( store.duplicate() map by(this@times) )
}

/**
 * Map a [Store] to an [IStore]
 */
@MathCatDsl
fun <S, A> Store<S, A>.indexed(): IStore<S, S, A> = IStore(data){
    s -> by(this@indexed)(s)
}

/**
 * Map a [Store] to a [KeyStore]
 */
@MathCatDsl
fun <S, A> Store<S, A>.keyStore(): KeyStore<S, S, A> = KeyStore(data) {
    this@keyStore
}
