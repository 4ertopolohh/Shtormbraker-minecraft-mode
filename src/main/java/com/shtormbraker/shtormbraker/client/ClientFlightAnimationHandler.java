package com.shtormbraker.shtormbraker.client;

import com.shtormbraker.shtormbraker.network.packet.S2CFlightAnimationStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ClientFlightAnimationHandler {
    private static final float BLEND_IN_SPEED = 0.18F;
    private static final float BLEND_OUT_SPEED = 0.14F;
    private static final float DIRECTION_LERP = 0.25F;
    private static final int STALE_TICKS_BEFORE_CLEANUP = 20;

    private static final Map<Integer, FlightAnimState> STATES = new HashMap<>();
    private static final Map<Integer, PoseSnapshot> SNAPSHOTS = new HashMap<>();

    private ClientFlightAnimationHandler() {
    }

    public static void onFlightStatePacket(S2CFlightAnimationStatePacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        FlightAnimState state = STATES.computeIfAbsent(packet.getPlayerId(), key -> new FlightAnimState());
        state.targetFlying = packet.isFlying();
        state.activeHand = packet.getHand();
        if (packet.getDirection().lengthSqr() > 1.0E-6D) {
            Vec3 normalizedDirection = packet.getDirection().normalize();
            state.targetDirection = normalizedDirection;
            if (state.smoothedDirection.lengthSqr() <= 1.0E-6D) {
                state.smoothedDirection = normalizedDirection;
            }
        }
        state.lastUpdatedTick = minecraft.level.getGameTime();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            STATES.clear();
            SNAPSHOTS.clear();
            return;
        }

        long now = minecraft.level.getGameTime();
        Iterator<Map.Entry<Integer, FlightAnimState>> iterator = STATES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, FlightAnimState> entry = iterator.next();
            FlightAnimState state = entry.getValue();
            if (minecraft.level.getEntity(entry.getKey()) == null) {
                state.targetFlying = false;
            }
            state.previousBlend = state.currentBlend;
            float targetBlend = state.targetFlying ? 1.0F : 0.0F;
            float speed = state.targetFlying ? BLEND_IN_SPEED : BLEND_OUT_SPEED;
            state.currentBlend = Mth.approach(state.currentBlend, targetBlend, speed);
            state.smoothedDirection = lerpDirection(state.smoothedDirection, state.targetDirection, DIRECTION_LERP);

            boolean canCleanup = !state.targetFlying
                    && state.currentBlend <= 0.01F
                    && now - state.lastUpdatedTick > STALE_TICKS_BEFORE_CLEANUP;
            if (canCleanup) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) {
            return;
        }
        FlightAnimState state = STATES.get(player.getId());
        if (state == null) {
            return;
        }

        float blend = Mth.lerp(event.getPartialTick(), state.previousBlend, state.currentBlend);
        if (blend <= 0.01F) {
            return;
        }

        PlayerModel<?> model = event.getRenderer().getModel();
        SNAPSHOTS.put(player.getId(), PoseSnapshot.capture(model));

        Vec3 flightDirection = state.smoothedDirection.lengthSqr() > 1.0E-6D ? state.smoothedDirection.normalize() : player.getLookAngle().normalize();
        applyFlightPose(player, model, flightDirection, state.activeHand, blend, event.getPartialTick());
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        PoseSnapshot snapshot = SNAPSHOTS.remove(event.getEntity().getId());
        if (snapshot == null) {
            return;
        }

        snapshot.restore(event.getRenderer().getModel());
    }

    private static void applyFlightPose(AbstractClientPlayer player, PlayerModel<?> model, Vec3 worldDirection,
                                        InteractionHand activeHand, float blend, float partialTick) {
        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        float yawRad = bodyYaw * Mth.DEG_TO_RAD;
        float cosYaw = Mth.cos(yawRad);
        float sinYaw = Mth.sin(yawRad);

        double localX = worldDirection.x * cosYaw + worldDirection.z * sinYaw;
        double localZ = -worldDirection.x * sinYaw + worldDirection.z * cosYaw;
        double localY = worldDirection.y;
        double horizontal = Math.sqrt(localX * localX + localZ * localZ);

        float pitch = (float) Math.atan2(localY, horizontal);
        float yaw = (float) Math.atan2(localX, Math.max(1.0E-4D, localZ));

        float bodyPitch = (-pitch * 0.75F - 0.35F) * blend;
        float bodyRoll = (-yaw * 0.35F) * blend;
        model.body.xRot += bodyPitch;
        model.body.zRot += bodyRoll;
        model.jacket.xRot += bodyPitch;
        model.jacket.zRot += bodyRoll;

        float legBlend = blend;
        dampPartRotation(model.rightLeg, legBlend);
        dampPartRotation(model.leftLeg, legBlend);
        dampPartRotation(model.rightPants, legBlend);
        dampPartRotation(model.leftPants, legBlend);

        float armPitch = (-Mth.HALF_PI - pitch) * blend;
        float armYaw = yaw * 0.9F * blend;
        float neutralDamp = 1.0F - blend * 0.75F;

        if (activeHand == InteractionHand.MAIN_HAND) {
            setArmPose(model.rightArm, model.rightSleeve, armPitch, armYaw);
            dampPartRotation(model.leftArm, 1.0F - neutralDamp);
            dampPartRotation(model.leftSleeve, 1.0F - neutralDamp);
        } else {
            setArmPose(model.leftArm, model.leftSleeve, armPitch, armYaw);
            dampPartRotation(model.rightArm, 1.0F - neutralDamp);
            dampPartRotation(model.rightSleeve, 1.0F - neutralDamp);
        }

        model.head.xRot = Mth.lerp(blend * 0.35F, model.head.xRot, pitch * 0.25F);
        model.head.yRot = Mth.lerp(blend * 0.25F, model.head.yRot, yaw * 0.2F);
        model.hat.xRot = model.head.xRot;
        model.hat.yRot = model.head.yRot;
    }

    private static void setArmPose(ModelPart arm, ModelPart sleeve, float xRot, float yRot) {
        arm.xRot = xRot;
        arm.yRot = yRot;
        arm.zRot = 0.0F;
        sleeve.xRot = xRot;
        sleeve.yRot = yRot;
        sleeve.zRot = 0.0F;
    }

    private static void dampPartRotation(ModelPart part, float amount) {
        float factor = Mth.clamp(1.0F - amount, 0.0F, 1.0F);
        part.xRot *= factor;
        part.yRot *= factor;
        part.zRot *= factor;
    }

    private static Vec3 lerpDirection(Vec3 current, Vec3 target, float alpha) {
        Vec3 currentDirection = current.lengthSqr() > 1.0E-6D ? current : new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 targetDirection = target.lengthSqr() > 1.0E-6D ? target : currentDirection;
        Vec3 blended = currentDirection.add(targetDirection.subtract(currentDirection).scale(alpha));
        return blended.lengthSqr() > 1.0E-6D ? blended.normalize() : targetDirection;
    }

    private static final class FlightAnimState {
        private boolean targetFlying;
        private float currentBlend;
        private float previousBlend;
        private Vec3 targetDirection = new Vec3(0.0D, 0.0D, 1.0D);
        private Vec3 smoothedDirection = new Vec3(0.0D, 0.0D, 1.0D);
        private InteractionHand activeHand = InteractionHand.MAIN_HAND;
        private long lastUpdatedTick;
    }

    private static final class PoseSnapshot {
        private final PartRotation head;
        private final PartRotation hat;
        private final PartRotation body;
        private final PartRotation jacket;
        private final PartRotation rightArm;
        private final PartRotation rightSleeve;
        private final PartRotation leftArm;
        private final PartRotation leftSleeve;
        private final PartRotation rightLeg;
        private final PartRotation rightPants;
        private final PartRotation leftLeg;
        private final PartRotation leftPants;

        private PoseSnapshot(PlayerModel<?> model) {
            this.head = PartRotation.capture(model.head);
            this.hat = PartRotation.capture(model.hat);
            this.body = PartRotation.capture(model.body);
            this.jacket = PartRotation.capture(model.jacket);
            this.rightArm = PartRotation.capture(model.rightArm);
            this.rightSleeve = PartRotation.capture(model.rightSleeve);
            this.leftArm = PartRotation.capture(model.leftArm);
            this.leftSleeve = PartRotation.capture(model.leftSleeve);
            this.rightLeg = PartRotation.capture(model.rightLeg);
            this.rightPants = PartRotation.capture(model.rightPants);
            this.leftLeg = PartRotation.capture(model.leftLeg);
            this.leftPants = PartRotation.capture(model.leftPants);
        }

        private static PoseSnapshot capture(PlayerModel<?> model) {
            return new PoseSnapshot(model);
        }

        private void restore(PlayerModel<?> model) {
            this.head.restore(model.head);
            this.hat.restore(model.hat);
            this.body.restore(model.body);
            this.jacket.restore(model.jacket);
            this.rightArm.restore(model.rightArm);
            this.rightSleeve.restore(model.rightSleeve);
            this.leftArm.restore(model.leftArm);
            this.leftSleeve.restore(model.leftSleeve);
            this.rightLeg.restore(model.rightLeg);
            this.rightPants.restore(model.rightPants);
            this.leftLeg.restore(model.leftLeg);
            this.leftPants.restore(model.leftPants);
        }
    }

    private static final class PartRotation {
        private final float xRot;
        private final float yRot;
        private final float zRot;

        private PartRotation(float xRot, float yRot, float zRot) {
            this.xRot = xRot;
            this.yRot = yRot;
            this.zRot = zRot;
        }

        private static PartRotation capture(ModelPart part) {
            return new PartRotation(part.xRot, part.yRot, part.zRot);
        }

        private void restore(ModelPart part) {
            part.xRot = this.xRot;
            part.yRot = this.yRot;
            part.zRot = this.zRot;
        }
    }
}
