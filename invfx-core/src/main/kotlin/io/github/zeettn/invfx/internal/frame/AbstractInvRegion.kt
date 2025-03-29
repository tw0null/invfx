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

package io.github.zeettn.invfx.internal.frame

import io.github.zeettn.invfx.frame.InvRegion
import org.bukkit.event.inventory.InventoryClickEvent

abstract class AbstractInvRegion(
    override val minX: Int,
    override val minY: Int,
    override val maxX: Int,
    override val maxY: Int
) : io.github.zeettn.invfx.frame.InvRegion {
    abstract fun onClick(x: Int, y: Int, event: InventoryClickEvent)
}
