/**
 * Copyright (c) 2020 Dr. Florian Schmidt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.evoleq.math.cat.suspend.comonad.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import org.evoleq.math.cat.marker.MathCatDsl
import org.evoleq.math.cat.suspend.morphism.ScopedSuspended
import org.evoleq.math.cat.suspend.morphism.by

/**
 * The Store comonad
 */
interface Store<S, A>: ScopedSuspended<S, A>  {
    val data: S
    suspend infix fun <B> map(f: suspend CoroutineScope.(A)->B): Store<S, B> = Store(data){
        a -> f(by(this@Store)(a))
    }
}

/**
 * [Store] comonad constructor function
 */
@MathCatDsl
@Suppress("FunctionName")
fun <S, A> Store(data: S, reader: suspend CoroutineScope.(S) -> A): Store<S, A> = object : Store<S, A> {
    override val data: S  = data
    
    override val morphism: suspend CoroutineScope.(S) -> A  = reader
}

/**
 * CoKleisli [Store]
 */
interface CoKlStore<S, A, B> : ScopedSuspended<Store<S, A>, B>

/**
 * CoKleisli [Store] constructor function
 */
@MathCatDsl
@Suppress("FunctionName")
fun <S, A, B> CoKlStore(arrow: suspend CoroutineScope.(Store<S, A>)->B): CoKlStore<S, A, B> = object : CoKlStore<S, A, B> {
    override val morphism: suspend CoroutineScope.(Store<S, A>) -> B = arrow
}

/**
 * Extract function of the [Store] comonad
 */
@MathCatDsl
suspend fun <S, A> Store<S, A>.extract(): A = coroutineScope { by(this@extract)(data) }

/**
 * Duplicate function of the [Store] comonad
 */
@MathCatDsl
suspend fun <S, A> Store<S, A>.duplicate(): Store<S, Store<S , A>> = Store(data) {
    d -> Store(d){ s -> by(this@duplicate)(s) }
}

@MathCatDsl
suspend operator fun <S, A, B, C> CoKlStore<S, A, B>.times(other: CoKlStore<S, B, C>): CoKlStore<S, A, C> = CoKlStore {
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
