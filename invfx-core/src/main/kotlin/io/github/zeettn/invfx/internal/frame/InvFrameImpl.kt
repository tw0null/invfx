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

import io.github.zeettn.invfx.InvWindow
import io.github.zeettn.invfx.frame.InvFrame
import io.github.zeettn.invfx.frame.InvList
import io.github.zeettn.invfx.frame.InvPane
import io.github.zeettn.invfx.frame.InvSlot
import io.github.zeettn.invfx.util.getValue
import io.github.zeettn.invfx.util.lazyVal
import io.github.zeettn.invfx.util.setValue
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.Cancellable
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class InvFrameImpl(
    lines: Int,
    title: Component
) : InvFrame, io.github.zeettn.invfx.InvWindow {
    private val inv = Bukkit.createInventory(this, lines * 9, title)

    private val slots = arrayListOf<InvSlotImpl>()

    private val regions = arrayListOf<AbstractInvRegion>()

    private var onOpen: ((InventoryOpenEvent) -> Unit)? by lazyVal()

    private var onClose: ((InventoryCloseEvent) -> Unit)? by lazyVal()

    private var onClick: ((Int, Int, InventoryClickEvent) -> Unit)? by lazyVal()

    private var onClickBottom: ((InventoryClickEvent) -> Unit)? by lazyVal()

    private var onClickOutside: ((InventoryClickEvent) -> Unit)? by lazyVal()

    override fun getInventory(): Inventory = inv

    private fun checkItemSlot(x: Int, y: Int) {
        val lines = inv.size / 9
        require(x in 0 until 9) { "require 0 <= x <= 8 ($x)" }
        require(y in 0 until lines) { "require 0 <= y < $lines ($y)" }
    }

    override fun slot(x: Int, y: Int, init: InvSlot.() -> Unit): InvSlotImpl {
        checkItemSlot(x, y)
        require(slots.find { it.x == x && it.y == y } == null) { "Overlaps with other slot" }
        require(regions.find { it.contains(x, y) } == null) { "Overlaps with other region" }

        return io.github.zeettn.invfx.internal.frame.InvSlotImpl(this, x, y).apply(init).also { slots += it }
    }

    private fun checkRegion(minX: Int, minY: Int, maxX: Int, maxY: Int) {
        val lines = inv.size / 9

        require(minX in 0 until 9) { "require 0 <= x <= 8 ($minX)" }
        require(minY in 0 until lines) { "require 0 <= y < $lines ($minY)" }
        require(maxX in 0 until 9) { "require 0 <= x <= 8 ($maxX)" }
        require(maxY in 0 until lines) { "require 0 <= y < $lines ($maxY)" }

        require(minX <= maxX) { "require minX <= maxX ($minX <= $maxX)" }
        require(minY <= maxY) { "require minY <= maxY ($minY <= $maxY)" }

        require(slots.none { slot ->
            slot.x in minX..maxX && slot.y in minY..maxY
        }) { "Overlaps with other slot" }
        require(regions.none { region ->
            region.overlaps(minX, minY, maxX, maxY)
        }) { "Overlaps with other region" }
    }

    override fun pane(minX: Int, minY: Int, maxX: Int, maxY: Int, init: InvPane.() -> Unit): InvPane {
        checkRegion(minX, minY, maxX, maxY)

        return InvPaneImpl(this, minX, minY, maxX, maxY).apply(init).also {
            regions += it
        }
    }

    override fun <T> list(
        minX: Int,
        minY: Int,
        maxX: Int,
        maxY: Int,
        trim: Boolean,
        item: () -> List<T>,
        init: (InvList<T>.() -> Unit)?
    ): InvList<T> {
        checkRegion(minX, minY, maxX, maxY)

        return InvListImpl(this, minX, minY, maxX, maxY, trim, item).apply {
            init?.let { it() }
        }.also {
            regions += it
        }
    }

    override fun onOpen(onOpen: (InventoryOpenEvent) -> Unit) {
        this.onOpen = onOpen
    }

    override fun onClose(onClose: (InventoryCloseEvent) -> Unit) {
        this.onClose = onClose
    }

    override fun onClick(onClick: (x: Int, y: Int, event: InventoryClickEvent) -> Unit) {
        this.onClick = onClick
    }

    override fun onClickBottom(onClickBottom: (InventoryClickEvent) -> Unit) {
        this.onClickBottom = onClickBottom
    }

    override fun onClickOutside(onClickOutside: (InventoryClickEvent) -> Unit) {
        this.onClickOutside = onClickOutside
    }

    fun trim() {
        regions.trimToSize()
        slots.trimToSize()
    }

    override fun item(x: Int, y: Int): ItemStack? {
        checkItemSlot(x, y)
        return inv.getItem(x + y * 9)
    }

    override fun item(x: Int, y: Int, item: ItemStack?) {
        checkItemSlot(x, y)
        inv.setItem(x + y * 9, item)
    }

    override fun onOpen(event: InventoryOpenEvent) {
        onOpen?.runCatching { invoke(event) }

        regions.forEach { region ->
            if (region is io.github.zeettn.invfx.internal.frame.InvListImpl<*>) {
                region.refresh()
            }
        }
    }

    override fun onClose(event: InventoryCloseEvent) {
        onClose?.runCatching { invoke(event) }
    }

    override fun onClick(event: InventoryClickEvent) {
        event.cancel()

        val slot = event.slot
        val x = slot % 9
        val y = slot / 9

        onClick?.runCatching { invoke(x, y, event) }

        regions.find { it.contains(x, y) }?.let {
            it.onClick(x - it.minX, y - it.minY, event)
        }

        slots.find { it.x == x && it.y == y }?.let {
            it.onClick?.runCatching { invoke(event) }
        }
    }

    override fun onClickBottom(event: InventoryClickEvent) {
        event.cancel()

        onClickBottom?.runCatching { invoke(event) }
    }

    override fun onClickOutside(event: InventoryClickEvent) {
        event.cancel()

        onClickOutside?.runCatching { invoke(event) }
    }

    override fun onDrag(event: InventoryDragEvent) {
        event.cancel()
    }

    override fun onPickupItem(event: EntityPickupItemEvent) {
        event.cancel()
    }

    override fun onDropItem(event: PlayerDropItemEvent) {
        event.cancel()
    }
}

private fun Cancellable.cancel() {
    isCancelled = true
}
