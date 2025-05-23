package com.stepheight;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(StepHeightMod.MODID)
public class StepHeightMod {
    public static final String MODID = "stepheightmod";

    public StepHeightMod() {
        // 設定ファイルを登録
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, StepHeightConfig.SPEC, "stepheightchangerconfig.toml");
        // イベントハンドラを登録
        MinecraftForge.EVENT_BUS.register(new GrassStepHandler());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.literal("StepHeightMod 動作中"));
        }
    }
}