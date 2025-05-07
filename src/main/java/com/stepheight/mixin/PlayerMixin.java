package com.stepheight.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public class PlayerMixin {
    private boolean hasSentMessage = false;

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!hasSentMessage && event.getEntity() instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.literal("Mixin 動作中"));
            hasSentMessage = true;
        }
    }
}
