package org.agent.hexstruction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class StructureManager extends SavedData {
    public Map<UUID, CompoundTag> structureRegistry = new HashMap<>();

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        structureRegistry.forEach((uuid, nbt) -> tag.put(uuid.toString(), nbt));
        return tag;
    }

    private static StructureManager CreateFromNBT(CompoundTag tag) {
        StructureManager structureManager = new StructureManager();
        tag.getAllKeys().forEach(key -> structureManager.structureRegistry.put(UUID.fromString(key), tag.getCompound(key)));
        return structureManager;
    }

    private static StructureManager GetServerManager(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(StructureManager::CreateFromNBT, StructureManager::new, Hexstruction.MODID);
    }

    public static UUID SaveStructure(Level world, CompoundTag structureNBT) {
        StructureManager structureManager = GetServerManager(world.getServer());
        UUID uuid = UUID.randomUUID();

        structureManager.structureRegistry.put(uuid, structureNBT);
        structureManager.setDirty();
        return uuid;
    }

    public static UUID SaveStructure(Level world, StructureTemplate structureTemplate) {
        CompoundTag structureNBT = structureTemplate.save(new CompoundTag());
        return SaveStructure(world, structureNBT);
    }

    public static CompoundTag GetStructure(Level world, UUID uuid) {
        StructureManager structureManager = GetServerManager(world.getServer());
        if (!structureManager.structureRegistry.containsKey(uuid))
            return null;
        return structureManager.structureRegistry.get(uuid);
    }

    public static void RemoveStructure(Level world, UUID uuid) {
        StructureManager structureManager = GetServerManager(world.getServer());
        structureManager.structureRegistry.remove(uuid);
        structureManager.setDirty();
    }

    public static Boolean CheckStructureSaved(Level world, UUID uuid) {
        StructureManager structureManager = GetServerManager(world.getServer());
        return structureManager.structureRegistry.containsKey(uuid);
    }

    public static UUID[] GetAllStructure(Level world) {
        StructureManager structureManager = GetServerManager(world.getServer());
        return structureManager.structureRegistry.keySet().toArray(new UUID[0]);
    }
}
