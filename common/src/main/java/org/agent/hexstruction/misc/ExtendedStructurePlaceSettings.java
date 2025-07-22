package org.agent.hexstruction.misc;

import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public class ExtendedStructurePlaceSettings extends StructurePlaceSettings {
    private boolean verticalMirror = false;
    private Rotation rotationX = Rotation.NONE;
    private Rotation rotationZ = Rotation.NONE;

    public boolean getVerticalMirror() {
        return verticalMirror;
    }

    public Rotation getRotationX() {
        return rotationX;
    }

    public Rotation getRotationZ() {
        return rotationZ;
    }

    public void setVerticalMirror(boolean verticalMirror) {
        this.verticalMirror = verticalMirror;
    }

    public void setRotationX(Rotation rotationX) {
        this.rotationX = rotationX;
    }

    public void setRotationZ(Rotation rotationZ) {
        this.rotationZ = rotationZ;
    }
}
