package org.agent.hexstruction.patterns

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.getDoubleBetween
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBlock
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.asInt
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.DirectionalPlaceContext
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import net.minecraft.world.phys.Vec3
import org.agent.hexstruction.getStructureNBT
import org.agent.hexstruction.getStructureSettings
import org.agent.hexstruction.misc.ExtendedStructurePlaceSettings
import org.agent.hexstruction.misc.FilterableStructureTemplate
import org.agent.hexstruction.misc.TimedBlockDisplay

object OpDisplayStructure : SpellAction {
    override val argc = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val origin = args.getBlockPos(0, argc)

        val structureNBT = args.getStructureNBT(1, argc, env.world)
        val settings = args.getStructureSettings(1, argc)

        val structure = FilterableStructureTemplate()
        structure.load(env.world.holderLookup(Registries.BLOCK), structureNBT)

        val particles = mutableListOf<ParticleSpray>()
        val blocks = structure.palettes[0].blocks()
        for (block in blocks) {
            val pos = FilterableStructureTemplate.calculateRelativePosition(settings, block.pos).offset(origin)
            particles.add(ParticleSpray.burst(pos.center, 1.0))

            val placeContext = DirectionalPlaceContext(env.world, pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)
            val worldState = env.world.getBlockState(pos)
            env.assertPosInRangeForEditing(pos)
            if (!worldState.canBeReplaced(placeContext))
                throw MishapBadBlock.of(pos, "replaceable")
        }

        return SpellAction.Result(
            Spell(structure, settings, origin, structureNBT, (args.getDoubleBetween(2, 0.0, 72000.0, argc))),
            blocks.size * (MediaConstants.DUST_UNIT / 100),
            particles
        )
    }

    private data class Spell(val structure: FilterableStructureTemplate, val settings: ExtendedStructurePlaceSettings, val origin: BlockPos, val structureNBT: CompoundTag, val lifeTime: Double) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val newOrigin = structure.getZeroPositionWithTransform(origin, settings.mirror, settings.verticalMirror, settings.rotation, settings.rotationX, settings.rotationZ)

            val palette = structureNBT.getList("palette", 10)
            val blocks = structureNBT.getList("blocks", 10)
            for (tag in blocks) {
                val blockInts = (tag as CompoundTag).get("pos") as ListTag
                var x = blockInts[0].asInt
                val y = blockInts[1].asInt
                var z = blockInts[2].asInt

                when (settings.mirror) {
                    Mirror.NONE -> null
                    Mirror.LEFT_RIGHT -> z *= -1
                    Mirror.FRONT_BACK -> x *= -1
                }

                val pos = BlockPos(x, y, z).rotate(settings.rotation).offset(newOrigin.x, newOrigin.y, newOrigin.z)
                val blockName = palette[tag.getInt("state")].asCompound.getString("Name")
                val blockState = BuiltInRegistries.BLOCK.get(ResourceLocation(blockName)).defaultBlockState()
                val blockDisplay = TimedBlockDisplay(EntityType.BLOCK_DISPLAY, env.world, env.world.gameTime, lifeTime.toLong()).apply {
                    setPos(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
                }

                blockDisplay.setBlockState(blockState)
                env.world.addFreshEntity(blockDisplay)
                blockDisplay.tick()
            }
        }
    }
}