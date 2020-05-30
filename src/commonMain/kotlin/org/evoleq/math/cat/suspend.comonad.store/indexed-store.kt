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
