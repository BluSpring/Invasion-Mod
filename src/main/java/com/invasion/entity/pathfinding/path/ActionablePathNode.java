package com.invasion.entity.pathfinding.path;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public interface ActionablePathNode {
    PathAction invasion$getAction();

    void invasion$setAction(PathAction invasion$action);

    static PathAction invasion$getAction(@Nullable Node node) {
        return node instanceof ActionablePathNode a ? a.invasion$getAction() : PathAction.NONE;
    }

    static @Nullable Node setActionIfNotPresent(@Nullable Node node, PathAction action) {
        return invasion$getAction(node) == PathAction.NONE ? invasion$setAction(node, action) : node;
    }

    static @Nullable Node invasion$setAction(@Nullable Node node, PathAction action) {
        if (node instanceof ActionablePathNode a) {
            a.invasion$setAction(action);
        }
        return node;
    }

    static Path combine(Path path1, Path path2, int lowerBoundP1, int upperBoundP1) {
        List<Node> newNodes = new ArrayList<>();
        for (int i = lowerBoundP1; i < upperBoundP1; i++) {
            newNodes.add(path1.getNode(i));
        }
        for (int i = 0; i < path2.getNodeCount(); i++) {
            newNodes.add(path2.getNode(i));
        }
        return new Path(newNodes, path2.getTarget(), path2.canReach());
    }
}
