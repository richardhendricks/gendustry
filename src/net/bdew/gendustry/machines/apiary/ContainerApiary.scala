/*
 * Copyright (c) bdew, 2013 - 2017
 * https://github.com/bdew/gendustry
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.gendustry.machines.apiary

import net.bdew.gendustry.Gendustry
import net.bdew.gendustry.gui.rscontrol.ContainerRSControllable
import net.bdew.lib.Misc
import net.bdew.lib.data.base.ContainerDataSlots
import net.bdew.lib.gui.{BaseContainer, SlotValidating}
import net.bdew.lib.items.ItemUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.{ClickType, IInventory}
import net.minecraft.item.ItemStack

class ContainerApiary(val te: TileApiary, player: EntityPlayer) extends BaseContainer(te) with ContainerDataSlots with ContainerRSControllable {
  lazy val dataSource = te

  addSlotToContainer(new SlotValidating(te, 0, 39, 29))
  addSlotToContainer(new SlotValidating(te, 1, 39, 52))

  if (!te.getWorld.isRemote && (te.owner :== null)) {
    Gendustry.logInfo("Owner information missing on apiary at (%s), assigning: %s".format(te.getPos, player.getGameProfile))
    te.owner := player.getGameProfile
  }

  class SlotUpgrade(inv: IInventory, slot: Int, x: Int, y: Int) extends SlotValidating(inv, slot, x, y) {
    // Fixes glitch in nei mouse scroll support
    override def isItemValid(stack: ItemStack): Boolean = stack == this.getStack || super.isItemValid(stack)
  }

  for (i <- 0 to 1; j <- 0 to 1)
    addSlotToContainer(new SlotUpgrade(te, 2 + j + i * 2, 70 + j * 18, 43 + i * 18))

  for (i <- 0 to 2; j <- 0 to 2)
    addSlotToContainer(new SlotValidating(te, 6 + j + i * 3, 116 + j * 18, 25 + i * 18))

  bindPlayerInventory(player.inventory, 8, 84, 142)

  override def transferStackInSlot(player: EntityPlayer, slot: Int): ItemStack = {
    val stack = getSlot(slot).getStack
    if (getSlot(slot).inventory == player.inventory && te.isUpgrade(stack)) {
      val canAdd = Misc.min(te.getMaxAdditionalUpgrades(stack), stack.getCount)
      if (canAdd > 0) {
        val remains = ItemUtils.addStackToSlots(stack.splitStack(canAdd), te, te.slots.upgrades, true)
        if (!remains.isEmpty)
          stack.grow(remains.getCount)
      }
      getSlot(slot).putStack(stack)
      return ItemStack.EMPTY
    }
    return super.transferStackInSlot(player, slot)
  }

  override def slotClick(slot: Int, button: Int, clickType: ClickType, player: EntityPlayer): ItemStack = {
    var oldStack = player.inventory.getItemStack
    if (te.slots.upgrades.contains(slot) && te.isUpgrade(oldStack)) {
      val idx = inventorySlots.get(slot).getSlotIndex
      if (te.slots.upgrades.contains(idx) && clickType == ClickType.PICKUP && button <= 1) {
        if (te.getStackInSlot(idx).isEmpty || ItemUtils.isSameItem(oldStack, te.getStackInSlot(idx))) {
          var canAdd = te.getMaxAdditionalUpgrades(oldStack)
          if (canAdd > 0) {
            if (button == 1) canAdd = 1
            val newStack = if (canAdd >= oldStack.getCount) {
              val t = oldStack
              oldStack = ItemStack.EMPTY
              t
            } else {
              oldStack.splitStack(canAdd)
            }
            player.inventory.setItemStack(newStack)
            val res = super.slotClick(slot, button, clickType, player)
            player.inventory.setItemStack(oldStack)
            return res
          }
        }
      }
    }
    return super.slotClick(slot, button, clickType, player)
  }
}