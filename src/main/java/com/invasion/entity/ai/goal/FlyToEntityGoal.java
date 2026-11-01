package com.invasion.entity.ai.goal;

import java.util.EnumSet;

import com.invasion.entity.EntityIMFlying;
import com.invasion.entity.HasAiGoals;
import com.invasion.entity.pathfinding.FlyingNavigation;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

public class FlyToEntityGoal extends Goal {
    private final EntityIMFlying theEntity;

    public FlyToEntityGoal(EntityIMFlying entity) {
        theEntity = entity;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return theEntity.hasGoal(HasAiGoals.Goal.GOTO_ENTITY) && theEntity.getTarget() != null;
    }

    @Override
    public void start() {
        FlyingNavigation nav = (FlyingNavigation)theEntity.getNavigatorNew();
        Entity target = theEntity.getTarget();
        if (target != theEntity.getNavigatorNew().getTargetEntity()) {
            nav.stop();
            nav.setMovementType(FlyingNavigation.MoveType.PREFER_WALKING);
            @Nullable
            Path path = theEntity.getNavigation().createPath(target, Mth.ceil(2 * theEntity.distanceTo(target)));
            if (path != null && path.getNodeCount() > 2 * theEntity.distanceTo(target)) {
                nav.setMovementType(FlyingNavigation.MoveType.MIXED);
            }
            theEntity.getNavigatorNew().autoPathToEntity(target);
        }
    }
}