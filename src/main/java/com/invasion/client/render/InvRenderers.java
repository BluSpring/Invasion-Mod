package com.invasion.client.render;

import com.invasion.client.particle.DazeParticle;
import com.invasion.client.render.entity.*;
import com.invasion.entity.InvEntities;
import com.invasion.item.InvItems;
import com.invasion.particle.InvParticles;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public interface InvRenderers {
    static void bootstrap() {
        ParticleFactoryRegistry.getInstance().register(InvParticles.DAZE, DazeParticle::factory);

        EntityRendererRegistry.register(InvEntities.ZOMBIE, AbstractIMZombieEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.ZOMBIE_PIGMAN, ZombiePigmanEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.SKELETON, IMSkeletonEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.SPIDER, SpiderRenderer::new);
        EntityRendererRegistry.register(InvEntities.JUMPING_SPIDER, context -> new IMSpiderEntityRenderer<>(context, IMSpiderEntityRenderer.JUMPER));
        EntityRendererRegistry.register(InvEntities.QUEEN_SPIDER, context -> new IMSpiderEntityRenderer<>(context, IMSpiderEntityRenderer.MOTHER));
        EntityRendererRegistry.register(InvEntities.PIGMAN_ENGINEER, PigmanEngineerEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.IMP, ImpEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.THROWER, ThrowerEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.BURROWER, BurrowerEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.BOULDER, BoulderEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.TNT, TntEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.WOLF, IMWolfEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.TRAP, TrapEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.BOLT, ElectricityBoltEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.SFX, NoopRenderer::new);
        EntityRendererRegistry.register(InvEntities.SPAWN_PROXY, NoopRenderer::new);
        EntityRendererRegistry.register(InvEntities.SPIDER_EGG, SpiderEggEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.CREEPER, IMCreeperEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.BIRD, VultureEntityRenderer::new);
        EntityRendererRegistry.register(InvEntities.VULTURE, RenderGiantBird::new);

        ItemProperties.register(InvItems.SEARING_BOW, ResourceLocation.withDefaultNamespace("pull"), (stack, world, entity, seed) -> {
            return entity == null || entity.getUseItem() != stack ? 0.0F : (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20F;
        });
        ItemProperties.register(InvItems.SEARING_BOW, ResourceLocation.withDefaultNamespace("pulling"),
            (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1 : 0
        );
    }
}
