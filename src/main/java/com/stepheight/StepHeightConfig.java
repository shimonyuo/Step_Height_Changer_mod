package com.stepheight;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = StepHeightMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class StepHeightConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<List<? extends CommentedConfig>> BLOCK_SETTINGS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("ステップ高さモッドの設定");
        builder.push("blocks");

        BLOCK_SETTINGS = builder
                .comment("カスタマイズ可能なブロックごとの設定")
                .defineList("blockSettings", StepHeightConfig::getDefaultBlockSettings, StepHeightConfig::validateBlockSetting);

        builder.pop();
        SPEC = builder.build();
    }

    // デフォルトのブロック設定（草ブロックと石）
    private static List<CommentedConfig> getDefaultBlockSettings() {
        List<CommentedConfig> defaults = new ArrayList<>();

        // 草ブロックの設定
        CommentedConfig grassConfig = CommentedConfig.inMemory();
        grassConfig.set("type", "minecraft:grass_block");
        grassConfig.set("stepHeightIncrease", 0.4);
        grassConfig.set("yOffset", 0.1);
        defaults.add(grassConfig);

        // 石の設定
        CommentedConfig stoneConfig = CommentedConfig.inMemory();
        stoneConfig.set("type", "minecraft:stone");
        stoneConfig.set("stepHeightIncrease", 0.4);
        stoneConfig.set("yOffset", 0.1);
        defaults.add(stoneConfig);

        return ImmutableList.copyOf(defaults);
    }

    // 設定のバリデーション
    private static boolean validateBlockSetting(Object obj) {
        if (!(obj instanceof CommentedConfig config)) return false;
        return config.contains("type") && config.get("type") instanceof String &&
                config.contains("stepHeightIncrease") && config.get("stepHeightIncrease") instanceof Number &&
                config.contains("yOffset") && config.get("yOffset") instanceof Number;
    }

    // コンフィグからブロック設定を取得
    public static List<BlockSetting> getBlockSettings() {
        List<BlockSetting> settings = new ArrayList<>();
        for (CommentedConfig config : BLOCK_SETTINGS.get()) {
            String blockId = config.get("type");
            Number stepHeightNum = config.get("stepHeightIncrease");
            Number yOffsetNum = config.get("yOffset");

            double stepHeight = stepHeightNum != null ? stepHeightNum.doubleValue() : 0.0;
            double yOffset = yOffsetNum != null ? yOffsetNum.doubleValue() : 0.0;

            if (stepHeight >= 0.0 && stepHeight <= 5.0 &&
                    yOffset >= 0.0 && yOffset <= 3.0) {
                settings.add(new BlockSetting(blockId, stepHeight, yOffset));
            }
        }
        return settings;
    }

    // ブロック設定のデータクラス
    public static class BlockSetting {
        public final String blockId;
        public final double stepHeightIncrease;
        public final double yOffset;

        public BlockSetting(String blockId, double stepHeightIncrease, double yOffset) {
            this.blockId = blockId;
            this.stepHeightIncrease = stepHeightIncrease;
            this.yOffset = yOffset;
        }
    }

    public static void register() {
        // コンフィグ登録はStepHeightModで行う
    }
}