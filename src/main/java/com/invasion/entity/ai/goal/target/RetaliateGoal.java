package com.invasion.entity.ai.goal.target;

import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.PathfinderMob;

public class RetaliateGoal extends HurtByTargetGoal {
	public RetaliateGoal(PathfinderMob entity) {
		super(entity);
		setAlertOthers();
	}
}