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

import java.lang.reflect.InvocationTargetException
import org.apache.commons.lang3.reflect.ConstructorUtils

object LibraryLoader {
    @Suppress("UNCHECKED_CAST")
    fun <T> loadImplement(type: Class<T>, vararg initArgs: Any? = emptyArray()): T {
        val packageName = type.`package`.name
        val className = "${type.simpleName}Impl"
        val parameterTypes = initArgs.map { it?.javaClass }.toTypedArray()

        return try {
            val internalClass =
                Class.forName("$packageName.internal.$className", true, type.classLoader).asSubclass(type)
            val constructor = ConstructorUtils.getMatchingAccessibleConstructor(internalClass, *parameterTypes)
                ?: throw UnsupportedOperationException("${type.name} does not have Constructor for [${parameterTypes.joinToString()}]")
            constructor.newInstance() as T
        } catch (exception: ClassNotFoundException) {
            throw UnsupportedOperationException("${type.name} a does not have implement", exception)
        } catch (exception: IllegalAccessException) {
            throw UnsupportedOperationException("${type.name} constructor is not visible")
        } catch (exception: InstantiationException) {
            throw UnsupportedOperationException("${type.name} is abstract class")
        } catch (exception: InvocationTargetException) {
            throw UnsupportedOperationException(
                "${type.name} has an error occurred while creating the instance",
                exception
            )
        }
    }
}