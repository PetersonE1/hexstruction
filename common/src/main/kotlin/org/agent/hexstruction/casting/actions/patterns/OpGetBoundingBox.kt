package org.agent.hexstruction.patterns

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.utils.asDouble
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import org.agent.hexstruction.getBoundingBox
import org.agent.hexstruction.getStructureNBT

object OpGetBoundingBox : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val structureNBT = args.getStructureNBT(0, argc, env.world)

        val bb = getBoundingBox(structureNBT)

        val x = bb.xSpan.toDouble()
        val y = bb.ySpan.toDouble()
        val z = bb.zSpan.toDouble()

        return listOf(ListIota(listOf(DoubleIota(x), DoubleIota(y), DoubleIota(z))))
    }
}