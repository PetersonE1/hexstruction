package org.agent.hexstruction.registry

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.agent.hexstruction.command.StructuresSavedCommand

object HexstructionCommands {
    @JvmStatic
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val mainCmd = Commands.literal("hexstruction")

        StructuresSavedCommand.add(mainCmd)

        dispatcher.register(mainCmd)
    }
}