package net.nexusmod.mixin;

import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.nexusmod.client.config.NexusConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Vanilla's particle option is a coarse ALL/DECREASED/MINIMAL toggle.
 * NexusConfig.particleDensity is a 0-100 slider for finer control than
 * that; this mixin probabilistically drops a fraction of particle spawn
 * requests to approximate the slider value, on top of whatever vanilla's
 * own ParticlesMode already filtered out.
 *
 * At density=100 nothing is dropped (fast-path, no RNG call). Below 100,
 * roughly (100 - density)% of spawns are skipped.
 *
 * As with the entity culling mixin, verify the exact `addParticle`
 * overload signature against this project's Yarn mappings — Loom will
 * fail the build clearly if the target method doesn't match rather than
 * silently no-op the mixin.
 */
@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void nexus$densityLimit(ParticleEffect parameters, double x, double y, double z,
                                     double velocityX, double velocityY, double velocityZ,
                                     CallbackInfo ci) {
        NexusConfig cfg = NexusConfig.get();
        if (cfg.particleDensity >= 100) return;
        if (cfg.particleDensity <= 0) {
            ci.cancel();
            return;
        }
        if (ThreadLocalRandom.current().nextInt(100) >= cfg.particleDensity) {
            ci.cancel();
        }
    }
}
