package org.agent.hexstruction

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.utils.asCompound
import at.petrak.hexcasting.api.utils.asInt
import at.petrak.hexcasting.api.utils.asList
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.api.utils.darkGreen
import at.petrak.hexcasting.api.utils.getBoolean
import at.petrak.hexcasting.api.utils.putCompound
import at.petrak.hexcasting.api.utils.putList
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import org.agent.hexstruction.misc.ExtendedStructurePlaceSettings
import org.agent.hexstruction.misc.TimedBlockDisplay
import java.util.UUID

class StructureIota(structureUUID: UUID, val settings: ExtendedStructurePlaceSettings, val world: Level) : Iota(TYPE, structureUUID) {
    override fun isTruthy(): Boolean = StructureManager.CheckStructureSaved(world, uuid)
    override fun toleratesOther(that: Iota) = typesMatch(this, that) && this.payload == (that as StructureIota).payload
    val uuid = payload as UUID

    override fun serialize(): CompoundTag {
        val tag = CompoundTag()
        tag.putUUID("uuid", uuid)
        tag.putBoolean("referenceExists", StructureManager.CheckStructureSaved(world, uuid))

        val settingsTag = CompoundTag()
        settingsTag.putString("mirror", settings.mirror.toString())
        settingsTag.putString("rotation", settings.rotation.toString())
        settingsTag.putBoolean("vertical_mirror", settings.verticalMirror)
        settingsTag.putString("rotationX", settings.rotationX.toString())
        settingsTag.putString("rotationZ", settings.rotationZ.toString())

        tag.putCompound("settings", settingsTag)

        return tag
    }

    companion object {
        @JvmField
        val TYPE: IotaType<StructureIota> = object : IotaType<StructureIota>() {
            override fun deserialize(tag: Tag, world: ServerLevel) : StructureIota {
                tag as CompoundTag
                val uuid = tag.getUUID("uuid")
                tag.putBoolean("referenceExists", StructureManager.CheckStructureSaved(world, uuid))

                val settingsTag = tag.getCompound("settings")
                val settings = ExtendedStructurePlaceSettings()
                settings.mirror = Mirror.valueOf(settingsTag.getString("mirror"))
                settings.rotation = Rotation.valueOf(settingsTag.getString("rotation"))
                settings.verticalMirror = settingsTag.getBoolean("vertical_mirror")
                settings.rotationX = Rotation.valueOf(settingsTag.getString("rotationX"))
                settings.rotationZ = Rotation.valueOf(settingsTag.getString("rotationZ"))

                return StructureIota(uuid, settings, world)
            }

            override fun display(tag: Tag) : Component {
                val uuid = (tag as CompoundTag).getUUID("uuid")
                val referenceExists = tag.getBoolean("referenceExists")
                if (referenceExists) {
                    val settingsTag = tag.getCompound("settings")

                    val mirror = settingsTag.getString("mirror")
                    val rotation = settingsTag.getString("rotation")
                    val verticalMirror = settingsTag.getBoolean("vertical_mirror")
                    val rotationX = settingsTag.getString("rotationX")
                    val rotationZ = settingsTag.getString("rotationZ")

                    val text1 = "hexstruction.iota.structure.identifier".asTranslatedComponent(uuid.toString().substring(0, 8))
                        .withStyle(Style.EMPTY.withFont(ResourceLocation("minecraft:illageralt")))
                    val text2 = "hexstruction.iota.structure.display"
                        .asTranslatedComponent(mirror, verticalMirror, rotation, rotationX, rotationZ)
                        .withStyle(Style.EMPTY.withFont(Style.DEFAULT_FONT)).darkGreen
                    return text1.append(text2)
                }
                return "hexstruction.iota.structure.empty".asTranslatedComponent.darkGreen
            }

            override fun color() = 0x118840
        }

        fun mirrorVertical(structureNBT: CompoundTag) {
            val blocks = structureNBT.getList("blocks", 10)

            val maxY = getBoundingBox(structureNBT).maxY()

            for (tag in blocks) {
                val pos = tag.asCompound.get("pos")!!.asList
                pos[1] = IntTag.valueOf(pos[1].asInt * -1 + maxY)
            }
        }
    }
}