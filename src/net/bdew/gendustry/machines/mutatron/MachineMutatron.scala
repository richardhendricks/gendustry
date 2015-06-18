/*
 * Copyright (c) bdew, 2013 - 2014
 * https://github.com/bdew/gendustry
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.gendustry.machines.mutatron

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.bdew.gendustry.config.Tuning
import net.bdew.lib.gui.GuiProvider
import net.bdew.lib.machine.{Machine, ProcessorMachine}
import net.bdew.lib.recipes.gencfg.EntryStr
import net.minecraft.entity.player.EntityPlayer

object MutationSetting extends Enumeration {
  val ENABLED, DISABLED, REQUIREMENTS = Value
}

object MachineMutatron extends Machine("Mutatron", BlockMutatron) with GuiProvider with ProcessorMachine {
  def guiId = 2
  type TEClass = TileMutatron

  lazy val tankSize = tuning.getInt("TankSize")
  lazy val mutagenPerItem = tuning.getInt("MutagenPerItem")
  lazy val labwareConsumeChance = tuning.getFloat("LabwareConsumeChance")
  lazy val degradeChanceNatural = tuning.getFloat("DegradeChanceNatural")
  lazy val deathChanceArtificial = tuning.getFloat("DeathChanceArtificial")
  lazy val secretChance = tuning.getFloat("SecretMutationChance")

  lazy val mutatronOverrides =
    (for ((key, value) <- Tuning.getSection("Genetics").getSection("MutatronOverrides").filterType(classOf[EntryStr]))
      yield key -> (value.v.toUpperCase match {
        case "ENABLED" => MutationSetting.ENABLED
        case "DISABLED" => MutationSetting.DISABLED
        case "REQUIREMENTS" => MutationSetting.REQUIREMENTS
        case _ => MutationSetting.ENABLED
      })).toMap.withDefaultValue(MutationSetting.ENABLED)

  @SideOnly(Side.CLIENT)
  def getGui(te: TileMutatron, player: EntityPlayer) = new GuiMutatron(te, player)
  def getContainer(te: TileMutatron, player: EntityPlayer) = new ContainerMutatron(te, player)
}
