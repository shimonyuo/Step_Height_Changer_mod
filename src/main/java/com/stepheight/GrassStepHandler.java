package com.stepheight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StepHeightMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GrassStepHandler {

    private static final String CLIMBING_KEY = "IsStepHeightClimbing";
    private static final String BLOCK_ID_KEY = "StepHeightBlockId";
    private static final String TARGET_Y_KEY = "StepHeightTargetY";

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Player player = event.player;
        AttributeInstance stepHeightAddition = player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (stepHeightAddition == null) return;

        Level level = player.level();
        boolean isClimbing = player.getPersistentData().getBoolean(CLIMBING_KEY);
        double currentStepHeight = stepHeightAddition.getBaseValue();
        String currentBlockId = getCurrentBlockId(player);

        Vec3 motion = player.getDeltaMovement();
        boolean hasHorizontalMotion = Math.abs(motion.x) > 0.001 || Math.abs(motion.z) > 0.001;

        if (hasHorizontalMotion || player.horizontalCollision) {
            StepHeightConfig.BlockSetting matchedSetting = null;
            String matchedBlockId = null;
            BlockPos collisionPos = checkBlockCollision(player, level);

            if (collisionPos != null) {
                BlockState blockState = level.getBlockState(collisionPos);
                Block block = blockState.getBlock();
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);

                for (StepHeightConfig.BlockSetting setting : StepHeightConfig.getBlockSettings()) {
                    if (blockId.toString().equals(setting.blockId)) {
                        matchedSetting = setting;
                        matchedBlockId = blockId.toString();
                        break;
                    }
                }
            }

            if (matchedSetting != null) {
                if (!matchedBlockId.equals(currentBlockId) || currentStepHeight != matchedSetting.stepHeightIncrease) {
                    stepHeightAddition.setBaseValue(matchedSetting.stepHeightIncrease);
                    setCurrentBlockId(player, matchedBlockId);

                    double targetY = player.getY() + matchedSetting.yOffset;
                    player.getPersistentData().putDouble(TARGET_Y_KEY, targetY);
                    player.getPersistentData().putBoolean(CLIMBING_KEY, true);
                }

                if (isClimbing) {
                    double targetY = player.getPersistentData().getDouble(TARGET_Y_KEY);
                    double currentY = player.getY();
                    double step = 0.05;

                    if (currentY < targetY) {
                        double newY = Math.min(currentY + step, targetY);
                        AABB newAABB = player.getBoundingBox().move(0, newY - currentY, 0);
                        if (!level.noCollision(player, newAABB)) {
                            newY = Math.floor(newY) + 0.001D;
                        }
                        player.setPos(player.getX(), newY, player.getZ());
                    } else {
                        player.getPersistentData().remove(CLIMBING_KEY);
                        player.getPersistentData().remove(TARGET_Y_KEY);
                    }
                }
            } else {
                if (currentStepHeight != 0.0D || isClimbing) {
                    stepHeightAddition.setBaseValue(0.0D);
                    setCurrentBlockId(player, null);
                    player.getPersistentData().remove(CLIMBING_KEY);
                    player.getPersistentData().remove(TARGET_Y_KEY);
                }
            }
        } else {
            if (currentStepHeight != 0.0D || isClimbing) {
                stepHeightAddition.setBaseValue(0.0D);
                setCurrentBlockId(player, null);
                player.getPersistentData().remove(CLIMBING_KEY);
                player.getPersistentData().remove(TARGET_Y_KEY);
            }
        }
    }

    private BlockPos checkBlockCollision(Player player, Level level) {
        AABB playerAABB = player.getBoundingBox().inflate(0.1, 0.0, 0.1);
        int minX = (int) Math.floor(playerAABB.minX);
        int maxX = (int) Math.ceil(playerAABB.maxX);
        int minY = (int) Math.floor(playerAABB.minY);
        int maxY = (int) Math.ceil(playerAABB.maxY);
        int minZ = (int) Math.floor(playerAABB.minZ);
        int maxZ = (int) Math.ceil(playerAABB.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    VoxelShape collisionShape = state.getCollisionShape(level, pos);
                    if (!collisionShape.isEmpty()) {
                        AABB blockAABB = collisionShape.bounds().move(pos);
                        if (playerAABB.intersects(blockAABB)) {
                            if (StepHeightConfig.getBlockSettings().stream()
                                    .anyMatch(setting -> BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().equals(setting.blockId))) {
                                return pos;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private String getCurrentBlockId(Player player) {
        return player.getPersistentData().getString(BLOCK_ID_KEY);
    }

    private void setCurrentBlockId(Player player, String blockId) {
        if (blockId == null) {
            player.getPersistentData().remove(BLOCK_ID_KEY);
        } else {
            player.getPersistentData().putString(BLOCK_ID_KEY, blockId);
        }
    }
}