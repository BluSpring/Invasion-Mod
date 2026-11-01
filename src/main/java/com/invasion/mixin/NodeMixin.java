package com.invasion.mixin;

import com.invasion.entity.pathfinding.path.ActionablePathNode;
import com.invasion.entity.pathfinding.path.PathAction;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.pathfinder.Node;

@Mixin(Node.class)
abstract class NodeMixin implements ActionablePathNode {
    @Unique private PathAction invasion$action = PathAction.NONE;

    @Override
    public PathAction invasion$getAction() {
        return invasion$action;
    }

    @Override
    public void invasion$setAction(PathAction invasion$action) {
        this.invasion$action = invasion$action;
    }

    @Inject(method = "cloneAndMove", at = @At("RETURN"))
    private void invasion_after_copyWithNewPosition(int x, int y, int z, CallbackInfoReturnable<Node> info) {
        ((ActionablePathNode)info.getReturnValue()).invasion$setAction(invasion$action);
    }

    @Inject(method = "writeToStream", at = @At("RETURN"))
    private void invasion_after_write(FriendlyByteBuf buf, CallbackInfo info) {
        buf.writeEnum(invasion$action);
    }

    @Inject(method = "readContents", at = @At("RETURN"))
    private static void invasion_after_readFromBuf(FriendlyByteBuf buf, Node target, CallbackInfo info) {
        ((ActionablePathNode)target).invasion$setAction(buf.readEnum(PathAction.class));
    }

    @Expression("'}'")
    @ModifyExpressionValue(method = "toString", at = @At("MIXINEXTRAS:EXPRESSION"))
    private String invasion_attachActionToString(String original) {
        return ", action=" + this.invasion$action + original;
    }
}
