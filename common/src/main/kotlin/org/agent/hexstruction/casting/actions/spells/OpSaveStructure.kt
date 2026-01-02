package org.agent.hexstruction.patterns

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import org.agent.hexstruction.StructureIota
import org.agent.hexstruction.StructureManager
import org.agent.hexstruction.config.HexstructionConfig
import org.agent.hexstruction.misc.ExtendedStructurePlaceSettings
import org.agent.hexstruction.misc.FilterableStructureTemplate
import org.agent.hexstruction.tags.HexstructionBlockTags
import java.util.UUID
import kotlin.math.floor

// origin of structures is lower north-west
object OpSaveStructure : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val LSW_bound = getVec3i((args.getVec3(0, argc)))
        val UNE_bound = getVec3i((args.getVec3(1, argc)))

        val bb = BoundingBox.fromCorners(LSW_bound, UNE_bound)

        assertAmbitFromBoundingBox(bb, env)

        val costs = arrayOf(0)

        return SpellAction.Result(
            Spell(bb, this.argc, costs),
            costs[0] * (MediaConstants.DUST_UNIT / 8),
            listOf(ParticleSpray.burst(Vec3.atCenterOf(bb.center), 1.0))
        )
    }

    private fun assertAmbitFromBoundingBox(
        bb: BoundingBox,
        env: CastingEnvironment
    ) {
        for (x in bb.minX()..bb.maxX()) {
            for (y in bb.minY()..bb.maxY()) {
                for (z in bb.minZ()..bb.maxZ()) {
                    env.assertPosInRangeForEditing(BlockPos(x, y, z))
                }
            }
        }
    }

    private data class Spell(val bb: BoundingBox, val argc: Int, val costs: Array<Int>) : RenderedSpell {
        var uuid: UUID? = null

        override fun cast(env: CastingEnvironment) {
            val origin = BlockPos(bb.minX(), bb.minY(), bb.minZ())
            val bounds = BlockPos(bb.xSpan, bb.ySpan, bb.zSpan)

            val structure = FilterableStructureTemplate()

            structure.fillFromWorld(env.world, origin, bounds, false) {
                blockState, pos -> blockCheck(blockState, pos, env)
            }
            for (i in bb.minX()..bb.maxX()) {
                for (j in bb.maxY()downTo bb.minY()) {
                    for (k in bb.minZ()..bb.maxZ()) {
                        val pos = BlockPos(i, j, k)
                        val blockState = env.world.getBlockState(pos)
                        if (blockCheck(blockState, pos, env)) {
                            if (blockState.hasBlockEntity())
                                env.world.removeBlockEntity(pos)
                            if (!blockState.isAir)
                                env.world.setBlock(pos, Blocks.AIR.defaultBlockState(), 18)
                        }
                    }
                }
            }
            this.uuid = StructureManager.SaveStructure(env.world, structure)
            costs[0] = structure.palettes[0].blocks().size
        }

        override fun cast(env: CastingEnvironment, image: CastingImage): CastingImage? {
            cast(env)
            val stack = image.stack.toMutableList()
            stack.add(StructureIota(uuid!!, ExtendedStructurePlaceSettings(), env.world))

            val image2 = image.copy(stack = stack)

            return image2
        }

        fun blockCheck(blockState: BlockState, pos: BlockPos, env: CastingEnvironment): Boolean {
            if (blockState.isAir) return false
            if (blockState.`is`(HexstructionBlockTags.STRUCTURE_SAVE_BANNED) && !HexstructionConfig.server.structureConsumeWhitelist.contains(blockState.block.`arch$registryName`().toString())) return false
            if (blockState.block.defaultDestroyTime() == -1f) return false
            if (blockState.getDestroySpeed(env.world, pos) < 0f) return false
            if (!IXplatAbstractions.INSTANCE.isBreakingAllowed(
                    env.world,
                    pos,
                    env.world.getBlockState(pos),
                    env.castingEntity as? ServerPlayer
                )) return false

            return true
        }
    }
}

fun getVec3i(vec: Vec3): Vec3i {
    val x = floor(vec.x).toInt()
    val y = floor(vec.y).toInt()
    val z = floor(vec.z).toInt()

    return Vec3i(x, y, z)
}