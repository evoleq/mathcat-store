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
package org.evoleq.math.cat.comonad.store

import org.evoleq.math.cat.marker.MathCatDsl
import org.evoleq.math.cat.morphism.Morphism
import org.evoleq.math.cat.morphism.by

/**
 * Functor IStore.
 * It is covariant in T an contravariant in B
 */
interface IStore<A, B, T> : Morphism<B, T> {
    /**
     * Stored data
     */
    val data: A
    /**
     * Map
     */
    fun <U> map(f: (T)->U): IStore<A, B, U> = IStore(data) {
        b -> f(by(this@IStore)(b))
    }
    
    /**
     * Contra map
     */
    fun <C> coMap(f: (C)->B): IStore<A, C, T> = IStore(data) {
        c -> by(this@IStore)(f(c))
    }
}

/**
 * The [IStore] constructor function
 */
@MathCatDsl
@Suppress("FunctionName")
fun <A, B, T> IStore(data: A, reader:(B)->T): IStore<A, B, T> = object : IStore<A, B, T> {
    override val data: A = data
    override val morphism: (B) -> T = reader
}

@MathCatDsl
@Suppress("FunctionName")
fun <A, B, T> IStore(data: A): ((B)->T)-> IStore<A, B, T> = {
    lambda -> IStore(data){index -> lambda(index)}
}



/**
 * Map an [IStore] to a [Store]
 */
@MathCatDsl
fun <S, A> IStore<S, S, A>.toStore(): Store<S, A> = Store(data){s -> by(this@toStore)(s)}

/**
 * Map an [IStore] to a [Store]
 */
@MathCatDsl
fun <A, B, T> IStore<A, B, T>.toStore(f: (A)->B): Store<A, T> = Store(data){
    a -> by(this@toStore)(f (a))
}
