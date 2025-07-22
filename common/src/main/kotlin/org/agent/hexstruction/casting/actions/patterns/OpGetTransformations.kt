package org.agent.hexstruction.patterns;

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import org.agent.hexstruction.getStructureSettings

object OpGetTransformations: ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val settings = args.getStructureSettings(0, argc)

        return listOf(ListIota(listOf(
            DoubleIota(settings.mirror.ordinal.toDouble()),
            DoubleIota(if (settings.verticalMirror) 1.0 else 0.0),
            DoubleIota(settings.rotation.ordinal.toDouble()),
            DoubleIota(settings.rotationX.ordinal.toDouble()),
            DoubleIota(settings.rotationZ.ordinal.toDouble())
            )))
    }
}