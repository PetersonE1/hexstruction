package org.agent.hexstruction.patterns

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import org.agent.hexstruction.getStructureIota

object OpMirrorFrontBack: ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val structureIota = args.getStructureIota(0, argc)
        when (structureIota.settings.mirror) {
            Mirror.NONE -> structureIota.settings.mirror = Mirror.FRONT_BACK
            Mirror.FRONT_BACK -> structureIota.settings.mirror = Mirror.NONE
            Mirror.LEFT_RIGHT -> {
                structureIota.settings.mirror = Mirror.NONE
                structureIota.settings.rotation = Rotation.entries[((structureIota.settings.rotation.ordinal + 2) % 4)]
            }
        }
        return listOf(structureIota)
    }
}