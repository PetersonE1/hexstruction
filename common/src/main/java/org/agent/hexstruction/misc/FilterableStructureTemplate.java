package org.agent.hexstruction.misc;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

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

    // here for reference
    public static BlockPos transform(BlockPos targetPos, Mirror mirror, Rotation rotation, BlockPos offset) {
        int i = targetPos.getX();
        int j = targetPos.getY();
        int k = targetPos.getZ();
        boolean bl = true;
        switch (mirror) {
            case LEFT_RIGHT -> k = -k;
            case FRONT_BACK -> i = -i;
            default -> bl = false;
        }

        int l = offset.getX();
        int m = offset.getZ();
        switch (rotation) {
            case COUNTERCLOCKWISE_90 -> {
                return new BlockPos(l - m + k, j, l + m - i);
            }
            case CLOCKWISE_90 -> {
                return new BlockPos(l + m - k, j, m - l + i);
            }
            case CLOCKWISE_180 -> {
                return new BlockPos(l + l - i, j, m + m - k);
            }
            default -> {
                return bl ? new BlockPos(i, j, k) : targetPos;
            }
        }
    }

    public static Vec3 transform(Vec3 target, Mirror mirror, Rotation rotation, BlockPos centerOffset) {
        double d = target.x;
        double e = target.y;
        double f = target.z;
        boolean bl = true;
        switch (mirror) {
            case LEFT_RIGHT -> f = (double)1.0F - f;
            case FRONT_BACK -> d = (double)1.0F - d;
            default -> bl = false;
        }

        int i = centerOffset.getX();
        int j = centerOffset.getZ();
        switch (rotation) {
            case COUNTERCLOCKWISE_90 -> {
                return new Vec3((double)(i - j) + f, e, (double)(i + j + 1) - d);
            }
            case CLOCKWISE_90 -> {
                return new Vec3((double)(i + j + 1) - f, e, (double)(j - i) + d);
            }
            case CLOCKWISE_180 -> {
                return new Vec3((double)(i + i + 1) - d, e, (double)(j + j + 1) - f);
            }
            default -> {
                return bl ? new Vec3(d, e, f) : target;
            }
        }
    }

    public BlockPos getZeroPositionWithTransform(BlockPos targetPos, Mirror mirror, Rotation rotation) {
        return getZeroPositionWithTransform(targetPos, mirror, rotation, this.getSize().getX(), this.getSize().getZ());
    }

    public static BlockPos getZeroPositionWithTransform(BlockPos pos, Mirror mirror, Rotation rotation, int sizeX, int sizeZ) {
        --sizeX;
        --sizeZ;
        int i = mirror == Mirror.FRONT_BACK ? sizeX : 0;
        int j = mirror == Mirror.LEFT_RIGHT ? sizeZ : 0;
        BlockPos blockPos = pos;
        switch (rotation) {
            case COUNTERCLOCKWISE_90 -> blockPos = pos.offset(j, 0, sizeX - i);
            case CLOCKWISE_90 -> blockPos = pos.offset(sizeZ - j, 0, i);
            case CLOCKWISE_180 -> blockPos = pos.offset(sizeX - i, 0, sizeZ - j);
            case NONE -> blockPos = pos.offset(i, 0, j);
        }

        return blockPos;
    }
}
