package org.agent.hexstruction.misc;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FilterableStructureTemplate extends StructureTemplate {

    public void fillFromWorld(Level level, BlockPos blockPos, Vec3i size, boolean withEntities, BlockCheckerInterface blockChecker) {
        if (size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1) {
            BlockPos blockPos2 = blockPos.offset(size).offset(-1, -1, -1);
            List<StructureBlockInfo> list = Lists.newArrayList();
            List<StructureBlockInfo> list2 = Lists.newArrayList();
            List<StructureBlockInfo> list3 = Lists.newArrayList();
            BlockPos blockPos3 = new BlockPos(Math.min(blockPos.getX(), blockPos2.getX()), Math.min(blockPos.getY(), blockPos2.getY()), Math.min(blockPos.getZ(), blockPos2.getZ()));
            BlockPos blockPos4 = new BlockPos(Math.max(blockPos.getX(), blockPos2.getX()), Math.max(blockPos.getY(), blockPos2.getY()), Math.max(blockPos.getZ(), blockPos2.getZ()));
            this.size = size;

            for(BlockPos blockPos5 : BlockPos.betweenClosed(blockPos3, blockPos4)) {
                BlockPos blockPos6 = blockPos5.subtract(blockPos3);
                BlockState blockState = level.getBlockState(blockPos5);
                if (blockChecker.checkBlock(blockState, blockPos5)) {
                    BlockEntity blockEntity = level.getBlockEntity(blockPos5);
                    StructureBlockInfo structureBlockInfo;
                    if (blockEntity != null) {
                        structureBlockInfo = new StructureBlockInfo(blockPos6, blockState, blockEntity.saveWithId());
                    } else {
                        structureBlockInfo = new StructureBlockInfo(blockPos6, blockState, null);
                    }

                    addToLists(structureBlockInfo, list, list2, list3);
                }
            }

            List<StructureBlockInfo> list4 = buildInfoList(list, list2, list3);

            this.palettes.clear();
            this.palettes.add(new Palette(list4));

            if (withEntities) {
                this.fillEntityList(level, blockPos3, blockPos4.offset(1, 1, 1));
            } else {
               this.entityInfoList.clear();
            }
        }
    }

    public boolean placeInWorld(ServerLevelAccessor serverLevel, BlockPos offset, BlockPos pos, ExtendedStructurePlaceSettings settings, RandomSource random, int flags) {
        if (this.palettes.isEmpty()) {
            return false;
        } else {
            List<StructureBlockInfo> list = settings.getRandomPalette(this.palettes, offset).blocks();
            if ((!list.isEmpty() || !settings.isIgnoreEntities() && !this.entityInfoList.isEmpty()) && this.size.getX() >= 1 && this.size.getY() >= 1 && this.size.getZ() >= 1) {
                BoundingBox boundingBox = settings.getBoundingBox();
                List<BlockPos> list2 = Lists.newArrayListWithCapacity(settings.shouldKeepLiquids() ? list.size() : 0);
                List<BlockPos> list3 = Lists.newArrayListWithCapacity(settings.shouldKeepLiquids() ? list.size() : 0);
                List<Pair<BlockPos, CompoundTag>> list4 = Lists.newArrayListWithCapacity(list.size());
                int i = Integer.MAX_VALUE;
                int j = Integer.MAX_VALUE;
                int k = Integer.MAX_VALUE;
                int l = Integer.MIN_VALUE;
                int m = Integer.MIN_VALUE;
                int n = Integer.MIN_VALUE;

                for(StructureBlockInfo structureBlockInfo : processBlockInfos(serverLevel, offset, pos, settings, list)) {
                    BlockPos blockPos = structureBlockInfo.pos;
                    if (boundingBox == null || boundingBox.isInside(blockPos)) {
                        FluidState fluidState = settings.shouldKeepLiquids() ? serverLevel.getFluidState(blockPos) : null;
                        BlockState blockState = structureBlockInfo.state.mirror(settings.getMirror()).rotate(settings.getRotation());
                        if (structureBlockInfo.nbt != null) {
                            BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
                            Clearable.tryClear(blockEntity);
                            serverLevel.setBlock(blockPos, Blocks.BARRIER.defaultBlockState(), 20);
                        }

                        if (serverLevel.setBlock(blockPos, blockState, flags)) {
                            i = Math.min(i, blockPos.getX());
                            j = Math.min(j, blockPos.getY());
                            k = Math.min(k, blockPos.getZ());
                            l = Math.max(l, blockPos.getX());
                            m = Math.max(m, blockPos.getY());
                            n = Math.max(n, blockPos.getZ());
                            list4.add(Pair.of(blockPos, structureBlockInfo.nbt));
                            if (structureBlockInfo.nbt != null) {
                                BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
                                if (blockEntity != null) {
                                    if (blockEntity instanceof RandomizableContainerBlockEntity) {
                                        structureBlockInfo.nbt.putLong("LootTableSeed", random.nextLong());
                                    }

                                    blockEntity.load(structureBlockInfo.nbt);
                                }
                            }

                            if (fluidState != null) {
                                if (blockState.getFluidState().isSource()) {
                                    list3.add(blockPos);
                                } else if (blockState.getBlock() instanceof LiquidBlockContainer) {
                                    ((LiquidBlockContainer)blockState.getBlock()).placeLiquid(serverLevel, blockPos, blockState, fluidState);
                                    if (!fluidState.isSource()) {
                                        list2.add(blockPos);
                                    }
                                }
                            }
                        }
                    }
                }

                boolean bl = true;
                Direction[] directions = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

                while(bl && !list2.isEmpty()) {
                    bl = false;
                    Iterator<BlockPos> iterator = list2.iterator();

                    while(iterator.hasNext()) {
                        BlockPos blockPos2 = iterator.next();
                        FluidState fluidState2 = serverLevel.getFluidState(blockPos2);

                        for(int o = 0; o < directions.length && !fluidState2.isSource(); ++o) {
                            BlockPos blockPos3 = blockPos2.relative(directions[o]);
                            FluidState fluidState3 = serverLevel.getFluidState(blockPos3);
                            if (fluidState3.isSource() && !list3.contains(blockPos3)) {
                                fluidState2 = fluidState3;
                            }
                        }

                        if (fluidState2.isSource()) {
                            BlockState blockState2 = serverLevel.getBlockState(blockPos2);
                            Block block = blockState2.getBlock();
                            if (block instanceof LiquidBlockContainer) {
                                ((LiquidBlockContainer)block).placeLiquid(serverLevel, blockPos2, blockState2, fluidState2);
                                bl = true;
                                iterator.remove();
                            }
                        }
                    }
                }

                if (i <= l) {
                    if (!settings.getKnownShape()) {
                        DiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(l - i + 1, m - j + 1, n - k + 1);
                        int p = i;
                        int q = j;
                        int o = k;

                        for(Pair<BlockPos, CompoundTag> pair : list4) {
                            BlockPos blockPos4 = pair.getFirst();
                            discreteVoxelShape.fill(blockPos4.getX() - p, blockPos4.getY() - q, blockPos4.getZ() - o);
                        }

                        updateShapeAtEdge(serverLevel, flags, discreteVoxelShape, p, q, o);
                    }

                    for(Pair<BlockPos, CompoundTag> pair2 : list4) {
                        BlockPos blockPos5 = pair2.getFirst();
                        if (!settings.getKnownShape()) {
                            BlockState blockState2 = serverLevel.getBlockState(blockPos5);
                            BlockState blockState3 = Block.updateFromNeighbourShapes(blockState2, serverLevel, blockPos5);
                            if (blockState2 != blockState3) {
                                serverLevel.setBlock(blockPos5, blockState3, flags & -2 | 16);
                            }

                            serverLevel.blockUpdated(blockPos5, blockState3.getBlock());
                        }

                        if (pair2.getSecond() != null) {
                            BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos5);
                            if (blockEntity != null) {
                                blockEntity.setChanged();
                            }
                        }
                    }
                }

                /*if (!settings.isIgnoreEntities()) {
                    this.placeEntities(serverLevel, offset, settings.getMirror(), settings.getRotation(), settings.getRotationPivot(), boundingBox, settings.shouldFinalizeEntities());
                }*/

                return true;
            } else {
                return false;
            }
        }
    }

    public static List<StructureBlockInfo> processBlockInfos(ServerLevelAccessor serverLevel, BlockPos offset, BlockPos pos, ExtendedStructurePlaceSettings settings, List<StructureBlockInfo> blockInfos) {
        List<StructureBlockInfo> list = new ArrayList();
        List<StructureBlockInfo> list2 = new ArrayList();

        for(StructureBlockInfo structureBlockInfo : blockInfos) {
            BlockPos blockPos = calculateRelativePosition(settings, structureBlockInfo.pos).offset(offset);
            StructureBlockInfo structureBlockInfo2 = new StructureBlockInfo(blockPos, structureBlockInfo.state, structureBlockInfo.nbt != null ? structureBlockInfo.nbt.copy() : null);

            for (Iterator<StructureProcessor> iterator = settings.getProcessors().iterator(); structureBlockInfo2 != null
                    && iterator.hasNext(); structureBlockInfo2 = iterator.next().processBlock(serverLevel, offset, pos, structureBlockInfo, structureBlockInfo2, settings)) {}

            if (structureBlockInfo2 != null) {
                list2.add(structureBlockInfo2);
                list.add(structureBlockInfo);
            }
        }

        for(StructureProcessor structureProcessor : settings.getProcessors()) {
            list2 = structureProcessor.finalizeProcessing(serverLevel, offset, pos, list, list2, settings);
        }

        return list2;
    }

    public static BlockPos calculateRelativePosition(ExtendedStructurePlaceSettings settings, BlockPos pos) {
        return transform(pos, settings.getMirror(), settings.getVerticalMirror(), settings.getRotation(), settings.getRotationX(), settings.getRotationZ(), settings.getRotationPivot());
    }

    public static BlockPos transform(BlockPos targetPos, Mirror mirror, boolean verticalMirror, Rotation rotationY, Rotation rotationX, Rotation rotationZ, BlockPos offset) {
        int x = targetPos.getX();
        int y = targetPos.getY();
        int z = targetPos.getZ();
        boolean has_mirror = true;
        switch (mirror) {
            case LEFT_RIGHT -> z = -z;
            case FRONT_BACK -> x = -x;
            default -> has_mirror = false;
        }
        if (verticalMirror) {
            y = -y;
            has_mirror = true;
        }

        int xOffset = offset.getX();
        int zOffset = offset.getZ();
        int yOffset = offset.getY();

        BlockPos newPos = targetPos;

        switch (rotationX) {
            case COUNTERCLOCKWISE_90 -> newPos = new BlockPos(x, yOffset - zOffset + z, yOffset + zOffset - y);
            case CLOCKWISE_90 -> newPos = new BlockPos(x, yOffset + zOffset - z, zOffset - yOffset + y);
            case CLOCKWISE_180 -> newPos = new BlockPos(x, yOffset * 2 - y, zOffset * 2 - z);
            default -> newPos = has_mirror ? new BlockPos(x, y, z) : newPos;
        }

        x = newPos.getX();
        y = newPos.getY();
        z = newPos.getZ();
        switch (rotationY) {
            case COUNTERCLOCKWISE_90 -> newPos = new BlockPos(xOffset - zOffset + z, y, xOffset + zOffset - x);
            case CLOCKWISE_90 -> newPos = new BlockPos(xOffset + zOffset - z, y, zOffset - xOffset + x);
            case CLOCKWISE_180 -> newPos = new BlockPos(xOffset * 2 - x, y, zOffset * 2 - z);
            default -> newPos = has_mirror ? new BlockPos(x, y, z) : newPos;
        }

        x = newPos.getX();
        y = newPos.getY();
        z = newPos.getZ();
        switch (rotationZ) {
            case COUNTERCLOCKWISE_90 -> newPos = new BlockPos(xOffset + yOffset - y, yOffset - xOffset + x, z);
            case CLOCKWISE_90 -> newPos = new BlockPos(xOffset - yOffset + y, xOffset + yOffset - x, z);
            case CLOCKWISE_180 -> newPos = new BlockPos(xOffset * 2 - x, yOffset * 2 - y, z);
            default -> newPos = has_mirror ? new BlockPos(x, y, z) : newPos;
        }

        return newPos;
    }

    public BlockPos getZeroPositionWithTransform(BlockPos targetPos, Mirror mirror, boolean verticalMirror, Rotation rotationY, Rotation rotationX, Rotation rotationZ) {
        Vec3i size = this.getSize();
        return getZeroPositionWithTransform(targetPos, mirror, verticalMirror, rotationY, rotationX, rotationZ, size.getX(), size.getZ(), size.getY());
    }


    public static BlockPos getZeroPositionWithTransform(BlockPos pos, Mirror mirror, boolean verticalMirror, Rotation rotationY, Rotation rotationX, Rotation rotationZ, int sizeX, int sizeZ, int sizeY) {
        --sizeX; --sizeY; --sizeZ;

        int cornerX = 0, cornerY = 0, cornerZ = 0;
        int curSizeX = sizeX, curSizeY = sizeY, curSizeZ = sizeZ;

        // Step 1: Mirrors
        if (mirror == Mirror.FRONT_BACK) cornerX = curSizeX;
        if (mirror == Mirror.LEFT_RIGHT) cornerZ = curSizeZ;
        if (verticalMirror) cornerY = curSizeY;

        // Step 2: Rotation around X (YZ plane)
        switch (rotationX) {
            case COUNTERCLOCKWISE_90 -> {
                int newY = cornerZ;
                int newZ = curSizeY - cornerY;
                cornerY = newY; cornerZ = newZ;
                int temp = curSizeY; curSizeY = curSizeZ; curSizeZ = temp;
            }
            case CLOCKWISE_90 -> {
                int newY = curSizeZ - cornerZ;
                int newZ = cornerY;
                cornerY = newY; cornerZ = newZ;
                int temp = curSizeY; curSizeY = curSizeZ; curSizeZ = temp;
            }
            case CLOCKWISE_180 -> {
                cornerY = curSizeY - cornerY;
                cornerZ = curSizeZ - cornerZ;
            }
        }

        // Step 3: Rotation around Y (XZ plane)
        switch (rotationY) {
            case COUNTERCLOCKWISE_90 -> {
                int newX = cornerZ;
                int newZ = curSizeX - cornerX;
                cornerX = newX; cornerZ = newZ;
                int temp = curSizeX; curSizeX = curSizeZ; curSizeZ = temp;
            }
            case CLOCKWISE_90 -> {
                int newX = curSizeZ - cornerZ;
                int newZ = cornerX;
                cornerX = newX; cornerZ = newZ;
                int temp = curSizeX; curSizeX = curSizeZ; curSizeZ = temp;
            }
            case CLOCKWISE_180 -> {
                cornerX = curSizeX - cornerX;
                cornerZ = curSizeZ - cornerZ;
            }
        }

        // Step 4: Rotation around Z (XY plane)
        switch (rotationZ) {
            case COUNTERCLOCKWISE_90 -> {
                int newX = curSizeY - cornerY;
                int newY = cornerX;
                cornerX = newX; cornerY = newY;
                int temp = curSizeX; curSizeX = curSizeY; curSizeY = temp;
            }
            case CLOCKWISE_90 -> {
                int newX = cornerY;
                int newY = curSizeX - cornerX;
                cornerX = newX; cornerY = newY;
                int temp = curSizeX; curSizeX = curSizeY; curSizeY = temp;
            }
            case CLOCKWISE_180 -> {
                cornerX = curSizeX - cornerX;
                cornerY = curSizeY - cornerY;
            }
        }

        return pos.offset(cornerX, cornerY, cornerZ);
    }
}
