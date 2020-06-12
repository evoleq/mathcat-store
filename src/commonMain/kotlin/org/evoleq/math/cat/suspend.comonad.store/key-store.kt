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
import org.evoleq.math.cat.marker.MathCatDsl
import org.evoleq.math.cat.suspend.morphism.ScopedSuspended
import org.evoleq.math.cat.suspend.morphism.by



interface KeyStore<A,B,T> : ScopedSuspended<B,ScopedSuspended<A, T>> {
    val data: A
    
    suspend infix fun <U> map(f: suspend CoroutineScope.(T)->U):KeyStore<A, B, U> = KeyStore(data) {
        b -> ScopedSuspended(f) o by(this@KeyStore)(b)
    }
    
    suspend infix fun <C> coMap(f: suspend CoroutineScope.(C)->B): KeyStore<A, C, T> = KeyStore(data) {
        c -> by(this@KeyStore) ( f(c) )
    }
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

