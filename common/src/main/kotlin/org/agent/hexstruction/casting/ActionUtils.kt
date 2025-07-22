package org.agent.hexstruction

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.utils.asDouble
import at.petrak.hexcasting.api.utils.asInt
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.chat.Component
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import org.agent.hexstruction.misc.ExtendedStructurePlaceSettings
import java.util.UUID

// Getter from Args Section

fun List<Iota>.getStructureIota(idx: Int, argc: Int = 0): StructureIota {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is StructureIota) {
        return x
    } else {
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "structure")
    }
}

fun List<Iota>.getStructureUUID(idx: Int, argc: Int = 0): java.util.UUID {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is StructureIota) {
        return x.uuid
    } else {
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "structure")
    }
}

fun List<Iota>.getStructureSettings(idx: Int, argc: Int = 0): ExtendedStructurePlaceSettings {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is StructureIota) {
        return x.settings
    } else {
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "structure")
    }
}

fun List<Iota>.getStructureNBT(idx: Int, argc: Int = 0, world: Level): CompoundTag {
    val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
    if (x is StructureIota) {
        val structureNBT = StructureManager.GetStructure(world, x.uuid)
        if (structureNBT == null) {
            throw MishapInvalidIota(x, if (argc == 0) idx else argc - (idx + 1), "hexstruction.iota.structure.mishap.empty".asTranslatedComponent)
        }
        return structureNBT
    } else {
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "structure")
    }
}

// Math Stuff Section

fun getBoundingBox(structureNBT: CompoundTag): BoundingBox {
    var minX = 0
    var minY = 0
    var minZ = 0
    var maxX = 0
    var maxY = 0
    var maxZ = 0

    val blocks = structureNBT.getList("blocks", 10)
    for (tag in blocks) {
        val blockInts = (tag as CompoundTag).get("pos") as ListTag
        val x = blockInts[0].asInt
        val y = blockInts[1].asInt
        val z = blockInts[2].asInt

        if (x < minX) minX = x
        if (x > maxX) maxX = x
        if (y < minY) minY = y
        if (y > maxY) maxY = y
        if (z < minZ) minZ = z
        if (z > maxZ) maxZ = z
    }

    val bb = BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
    return bb
}