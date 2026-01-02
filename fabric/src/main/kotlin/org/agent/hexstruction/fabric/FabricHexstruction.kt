package org.agent.hexstruction.fabric

import org.agent.hexstruction.Hexstruction
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import org.agent.hexstruction.registry.HexstructionCommands

object FabricHexstruction : ModInitializer {
    override fun onInitialize() {
        Hexstruction.init()

        CommandRegistrationCallback.EVENT.register { dp, _, _ -> HexstructionCommands.register(dp) }
    }
}
