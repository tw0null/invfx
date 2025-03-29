/*
 * InvFX
 * Copyright (C) 2021 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.zeettn.invfx.util

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

fun <T> weak(referent: T) = WeakReference(referent)

operator fun <T> WeakReference<T>.getValue(any: Any?, property: KProperty<*>): T {
    return get() ?: error("NULL")
}

class LazyVal<T> internal constructor() {
    var referent: T? = null
        set(value) {
            if (field != null) error("Cannot redefine LazyValue.")
            field = value
        }
}

fun <T> lazyVal() = LazyVal<T>()

operator fun <T> LazyVal<T>.getValue(any: Any?, property: KProperty<*>): T? {
    return referent
}

operator fun <T> LazyVal<T>.setValue(any: Any?, property: KProperty<*>, value: T) {
    referent = value
}
