package org.agent.hexstruction.patterns

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBlock
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.utils.asInt
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.DirectionalPlaceContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.phys.Vec3
import org.agent.hexstruction.StructureIota
import org.agent.hexstruction.StructureManager
import org.agent.hexstruction.Utils
import org.agent.hexstruction.getStructureNBT
import org.agent.hexstruction.getStructureSettings
import org.agent.hexstruction.getStructureUUID
import org.agent.hexstruction.misc.ExtendedStructurePlaceSettings
import org.agent.hexstruction.misc.FilterableStructureTemplate
import java.util.UUID

// todo: claim integration (maybe done?)
object OpLoadStructure : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        var origin = args.getBlockPos(0, argc)

        val structureNBT = args.getStructureNBT(1, argc, env.world)
        val settings = args.getStructureSettings(1, argc)
        val uuid = args.getStructureUUID(1, argc)

        val structure = FilterableStructureTemplate()
        structure.load(env.world.holderLookup(Registries.BLOCK), structureNBT)

        origin = structure.getZeroPositionWithTransform(origin, settings.mirror, settings.verticalMirror, settings.rotation, settings.rotationX, settings.rotationZ)

        val bb = structure.getBoundingBox(settings, origin)
        val result = Utils.CheckAmbitFromBoundingBox(bb, env)
        if (!result.first)
            throw MishapBadLocation(result.second)

        val particles = mutableListOf<ParticleSpray>()
        val blocks = structureNBT.getList("blocks", 10)
        for (tag in blocks) {
            val blockInts = (tag as CompoundTag).get("pos") as ListTag
            val x = blockInts[0].asInt + origin.x
            val y = blockInts[1].asInt + origin.y
            val z = blockInts[2].asInt + origin.z
            particles.add(ParticleSpray.burst(Vec3.atCenterOf(Vec3i(x, y, z)), 1.0))

            val pos = BlockPos(x, y, z)
            val placeContext = DirectionalPlaceContext(env.world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)
            val worldState = env.world.getBlockState(pos)
            if (!worldState.canBeReplaced(placeContext))
                throw MishapBadBlock.of(pos, "replaceable")
            env.assertPosInRangeForEditing(pos)
        }

        return SpellAction.Result(
            Spell(structure, settings, origin, uuid),
            Utils.GetBlockCount(structureNBT) * (MediaConstants.DUST_UNIT / 8),
            particles
        )
    }

    private data class Spell(val structure: FilterableStructureTemplate, val settings: ExtendedStructurePlaceSettings, val origin: BlockPos, val uuid: UUID) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            structure.placeInWorld(env.world, origin, origin, settings, env.world.random, Block.UPDATE_CLIENTS)

            StructureManager.RemoveStructure(env.world, uuid)
        }
    }
}