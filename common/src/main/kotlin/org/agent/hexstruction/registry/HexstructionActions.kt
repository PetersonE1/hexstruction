package org.agent.hexstruction.registry

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.common.lib.hex.HexActions
import org.agent.hexstruction.patterns.OpDisplayStructure
import org.agent.hexstruction.patterns.OpGetBoundingBox
import org.agent.hexstruction.patterns.OpGetTransformations
import org.agent.hexstruction.patterns.OpLoadStructure
import org.agent.hexstruction.patterns.OpMirrorFrontBack
import org.agent.hexstruction.patterns.OpMirrorLeftRight
import org.agent.hexstruction.patterns.OpMirrorVertical
import org.agent.hexstruction.patterns.OpRotateClockwise
import org.agent.hexstruction.patterns.OpRotateClockwiseX
import org.agent.hexstruction.patterns.OpRotateClockwiseZ
import org.agent.hexstruction.patterns.OpRotateCounterClockwise
import org.agent.hexstruction.patterns.OpRotateCounterClockwiseX
import org.agent.hexstruction.patterns.OpRotateCounterClockwiseZ
import org.agent.hexstruction.patterns.OpSaveStructure

object HexstructionActions : HexstructionRegistrar<ActionRegistryEntry>(
    HexRegistries.ACTION,
    { HexActions.REGISTRY },
) {
    val SAVE_STRUCTURE = make("save_structure", HexDir.WEST, "dqeqdwdqeqd", OpSaveStructure)
    val LOAD_STRUCTURE = make("load_structure", HexDir.EAST, "aeqeawaeqea", OpLoadStructure)
    val DISPLAY_STRUCTURE = make("display_structure", HexDir.EAST, "aeqeawaeqeaqed", OpDisplayStructure)

    val MIRROR_LEFT_RIGHT = make("mirror_left_right", HexDir.EAST, "aeqeawaeqeaaewq", OpMirrorLeftRight)
    val MIRROR_FRONT_BACK = make("mirror_front_back", HexDir.EAST, "aeqeawaeqeaqqwe", OpMirrorFrontBack)
    val MIRROR_VERTICAL = make("mirror_vertical", HexDir.EAST, "eawaeqeawaeaw", OpMirrorVertical)

    val ROTATE_CLOCKWISE = make("rotate_clockwise", HexDir.EAST, "aeqeawaeqeaaede", OpRotateClockwise)
    val ROTATE_COUNTERCLOCKWISE = make("rotate_counterclockwise", HexDir.EAST, "aeqeawaeqeaqqaq",OpRotateCounterClockwise)
    val ROTATE_CLOCKWISE_X = make("rotate_clockwise_x", HexDir.EAST, "aeqeawaeqeaeedd", OpRotateClockwiseX)
    val ROTATE_COUNTERCLOCKWISE_X = make("rotate_counterclockwise_x", HexDir.EAST, "aeqeawaeqeadqaa", OpRotateCounterClockwiseX)
    val ROTATE_CLOCKWISE_Z = make("rotate_clockwise_z", HexDir.EAST, "aeqeawaeqeaeed", OpRotateClockwiseZ)
    val ROTATE_COUNTERCLOCKWISE_Z = make("rotate_counterclockwise_z", HexDir.EAST, "aeqeawaeqeadqa", OpRotateCounterClockwiseZ)

    val BOUNDING_BOX = make("bounding_box", HexDir.EAST, "aeqeawaeqeaqqeqaqeq", OpGetBoundingBox)
    val TRANSFORMATIONS = make("transformations", HexDir.EAST, "aeqeawaeqeaaee", OpGetTransformations)

    private fun make(name: String, startDir: HexDir, signature: String, action: Action) =
        make(name, startDir, signature) { action }

    private fun make(name: String, startDir: HexDir, signature: String, getAction: () -> Action) = register(name) {
        ActionRegistryEntry(HexPattern.fromAngles(signature, startDir), getAction())
    }
}
