package org.agent.hexstruction.command;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.lib.HexItems;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.agent.hexstruction.StructureIota;
import org.agent.hexstruction.StructureManager;
import org.agent.hexstruction.misc.ExtendedStructurePlaceSettings;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class StructuresSavedCommand {
    public static void add(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        cmd.then(Commands.literal("structuresSaved")
                .requires(dp -> dp.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("list")
                        .executes(ctx -> list(ctx.getSource())))
                .then(Commands.literal("remove")
                        .then(Commands.argument("uuid", StringArgumentType.string())
                                .executes(ctx ->
                                        remove(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "uuid")))))
                .then(Commands.literal("give")
                        .then(Commands.argument("uuid", StringArgumentType.string())
                                .executes(ctx ->
                                        give(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "uuid"),
                                                getDefaultTarget(ctx.getSource())))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(ctx ->
                                                give(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "uuid"),
                                                        EntityArgument.getPlayers(ctx, "targets"))))))
        );
    }

    private static Collection<ServerPlayer> getDefaultTarget(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return List.of(player);
        }
        return List.of();
    }

    private static int list(CommandSourceStack source) {
        var keys = StructureManager.GetAllStructure(source.getLevel());
        var listing = Arrays.stream(keys)
                .sorted()
                .toList();

        source.sendSuccess(() -> Component.translatable("command.hexstruction.saved.listing"), false);
        for (var key: listing) {
            MutableComponent message = Component.literal(key.toString());
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("command.hexstruction.saved.hover_copy"));
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, key.toString());
            message.withStyle(Style.EMPTY.withHoverEvent(hoverEvent).withClickEvent(clickEvent));
            source.sendSuccess(() -> message, false);
        }

        return keys.length;
    }

    private static int remove(CommandSourceStack source, String uuid) {
        UUID uuidObj = UUID.fromString(uuid);
        if (!StructureManager.CheckStructureSaved(source.getLevel(), uuidObj)) {
            source.sendSuccess(() -> Component.translatable("command.hexstruction.saved.remove.not_found").withStyle(ChatFormatting.RED), true);
            return 0;
        }
        StructureManager.RemoveStructure(source.getLevel(), uuidObj);
        source.sendSuccess(() -> Component.translatable("command.hexstruction.saved.remove.success"), true);
        return 1;
    }

    private static int give(CommandSourceStack source, String uuid, Collection<ServerPlayer> targets) {
        UUID uuidObj = UUID.fromString(uuid);
        if (!StructureManager.CheckStructureSaved(source.getLevel(), uuidObj)) {
            source.sendSuccess(() -> Component.translatable("command.hexstruction.saved.remove.not_found").withStyle(ChatFormatting.RED), true);
            return 0;
        }
        StructureIota iota = new StructureIota(uuidObj, new ExtendedStructurePlaceSettings(), source.getLevel());

        CompoundTag tag = new CompoundTag();
        CompoundTag dataTag = new CompoundTag();
        dataTag.putString("hexcasting:type", "hexstruction:structure");
        dataTag.put("hexcasting:data", iota.serialize());
        tag.put("data", dataTag);

        ItemStack stack = new ItemStack(HexItems.FOCUS);
        stack.setTag(tag);

        source.sendSuccess(() -> Component.translatable("command.hexstruction.saved.give.success"), true);
        for (ServerPlayer player : targets) {
            var stackEntity = player.drop(stack, false);
            if (stackEntity != null) {
                stackEntity.setNoPickUpDelay();
                stackEntity.setThrower(player.getUUID());
            }
        }

        return targets.size();
    }
}
