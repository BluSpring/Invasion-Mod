// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement

package com.invasion.entity.ai;

import java.util.Optional;

import com.invasion.entity.NexusSpiderEntity;
import com.invasion.util.math.PosUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;

public class IMSpiderMoveControl extends ClimbableMoveControl {
    private static final Direction[] DIRECTIONS = Direction.values();

    public IMSpiderMoveControl(NexusSpiderEntity entity) {
        super(entity);
    }

    @Override
    protected Optional<Direction> getClimbFace(BlockPos pos) {
        pos = BlockPos.ofFloored(Vec3d.of(pos).subtract(entity.getWidth() * 0.5F, 0, entity.getWidth() * 0.5F));

        int index = getMoveDirection();

        BlockPos.Mutable mutable = pos.mutableCopy();
        for (Vec3i offset : PosUtils.OFFSET_ADJACENT_2) {
            BlockState state = entity.getWorld().getBlockState(mutable.set(pos).move(offset));
            if (state.isFullCube(entity.getWorld(), mutable)) {
                return Optional.of(DIRECTIONS[(index % 8) / 2]);
            }
            index++;
        }
        return Optional.empty();
    }

    private int getMoveDirection() {
        Path path = entity.getNavigation().getCurrentPath();
        if (path != null && !path.isFinished()) {
            PathNode currentPoint = path.getCurrentNode();
            int pathLength = path.getLength();
            for (int i = path.getCurrentNodeIndex(); i < pathLength; i++) {
                PathNode point = path.getNode(i);
                if (point.x > currentPoint.x) {
                    return 0;
                }
                if (point.x < currentPoint.x) {
                    return 2;
                }
                if (point.z > currentPoint.z) {
                    return 4;
                }
                if (point.z < currentPoint.z) {
                    return 6;
                }
            }
        }
        return 0;
    }

}