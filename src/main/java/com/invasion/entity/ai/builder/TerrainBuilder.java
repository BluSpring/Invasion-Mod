package com.invasion.entity.ai.builder;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.invasion.entity.NexusEntity;
import com.invasion.entity.pathfinding.ClimberUtil;
import com.invasion.entity.pathfinding.IMLandPathNodeMaker;
import com.invasion.nexus.ai.scaffold.Scaffold;
import com.invasion.util.math.PosUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;

public class TerrainBuilder implements ITerrainBuild {
    private static final float LADDER_COST = 25;
    private static final float PLANKS_COST = 45;
    private static final float COBBLE_COST = 65;

    private final NexusEntity mob;
    private float buildRate;

    public TerrainBuilder(NexusEntity entity, float buildRate) {
        mob = entity;
        this.buildRate = buildRate;
    }

    public void setBuildRate(float buildRate) {
        this.buildRate = buildRate;
    }

    public float getBuildRate() {
        return this.buildRate;
    }

    @Override
    public Stream<ModifyBlockEntry> askBuildScaffoldLayer(BlockPos pos) {
        @Nullable
        Scaffold scaffold = mob.getNexus().getAttackerAI().getScaffolds().getAt(pos);
        if (scaffold == null) {
            return Stream.empty();
        }

        int height = pos.getY() - scaffold.getNode().bottom();
        Direction offset = scaffold.getNode().orientation();
        BlockPos.MutableBlockPos mutable = pos.mutable();

        Level world = mob.asEntity().level();
        BlockState block = world.getBlockState(mutable.set(pos).move(offset).move(Direction.DOWN));
        Stream.Builder<ModifyBlockEntry> builder = Stream.builder();

        if (height == 1) {
            if (!block.isCollisionShapeFullBlock(world, mutable)) {
                builder.add(new ModifyBlockEntry(mutable.immutable(), Blocks.OAK_PLANKS.defaultBlockState(), (int) (PLANKS_COST / buildRate)));
            }
            if (world.isEmptyBlock(mutable.set(pos).move(Direction.DOWN))) {
                builder.add(new ModifyBlockEntry(mutable.immutable(), Blocks.LADDER.defaultBlockState(), (int) (LADDER_COST / buildRate)));
            }
        }

        if (!world.getBlockState(mutable.set(pos).move(offset)).isCollisionShapeFullBlock(world, mutable)) {
            builder.add(new ModifyBlockEntry(mutable.immutable(), Blocks.OAK_PLANKS.defaultBlockState(), (int) (PLANKS_COST / buildRate)));
        }
        if (!world.getBlockState(pos).is(Blocks.LADDER)) {
            builder.add(new ModifyBlockEntry(pos, Blocks.LADDER.defaultBlockState(), (int) (LADDER_COST / buildRate)));
        }

        if (scaffold.isPlatformLayer(height)) {
            for (Vec3i i : PosUtils.OFFSET_RING) {
                if (!i.equals(offset.getNormal()) && !world.getBlockState(mutable.set(pos).move(i)).isCollisionShapeFullBlock(world, mutable)) {
                    builder.add(new ModifyBlockEntry(mutable.immutable(), Blocks.OAK_PLANKS.defaultBlockState(), (int) (PLANKS_COST / buildRate)));
                }
            }
        }

        return builder.build();
    }

    @Override
    public Stream<ModifyBlockEntry> askBuildLadderTower(BlockPos pos, Direction orientation, int layersToBuild) {
        Stream.Builder<ModifyBlockEntry> builder = Stream.builder();

        BlockPos.MutableBlockPos mutable = pos.mutable();
        Level world = mob.asEntity().level();

        if (!world.getBlockState(mutable.set(pos).move(orientation).move(Direction.DOWN)).isCollisionShapeFullBlock(world, mutable)) {
            builder.add(new ModifyBlockEntry(mutable.immutable(), Blocks.OAK_PLANKS.defaultBlockState(), (int) (PLANKS_COST / buildRate)));
        }
        if (world.isEmptyBlock(mutable.move(Direction.DOWN))) {
            builder.add(new ModifyBlockEntry(mutable.immutable(), Blocks.LADDER.defaultBlockState(), (int) (LADDER_COST / buildRate)));
        }

        for (int i = 0; i < layersToBuild; i++) {
            if (!world.getBlockState(mutable.set(pos).move(orientation).move(Direction.UP, i)).isCollisionShapeFullBlock(world, mutable)) {
                builder.add(new ModifyBlockEntry(mutable.immutable(), Blocks.OAK_PLANKS.defaultBlockState(), (int) (PLANKS_COST / buildRate)));
            }
            if (world.getBlockState(mutable.move(Direction.UP, i)).is(Blocks.LADDER)) {
                builder.add(new ModifyBlockEntry(mutable.immutable(), Blocks.LADDER.defaultBlockState(), (int) (LADDER_COST / buildRate)));
            }
        }

        return builder.build();
    }

    @Override
    public Stream<ModifyBlockEntry> askBuildLadder(BlockPos pos, Direction orientation) {
        Stream.Builder<ModifyBlockEntry> builder = Stream.builder();

        Level world = mob.asEntity().level();
        BlockState ladderState = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, orientation);
        BlockPos.MutableBlockPos mutable = pos.mutable();

        if (!world.getBlockState(pos).is(Blocks.LADDER)) {
            if (!ClimberUtil.canPositionSupportLadder(world, mutable, orientation)) {
                return Stream.empty();
            }

            builder.add(new ModifyBlockEntry(mutable.immutable(), ladderState, (int) (LADDER_COST / buildRate)));
            for (int i = 0; i < 4; i++) {
                if (ClimberUtil.canPositionSupportLadder(world, mutable.move(Direction.UP), orientation)) {
                    builder.add(new ModifyBlockEntry(mutable.immutable(), ladderState, (int) (LADDER_COST / buildRate)));
                }
            }
        }

        return builder.build();
    }

    @Override
    public Stream<ModifyBlockEntry> askBuildBridge(BlockPos pos) {
        Stream.Builder<ModifyBlockEntry> builder = Stream.builder();

        BlockPos.MutableBlockPos mutable = pos.mutable();
        BlockPos posBelow = pos.below();
        Level world = mob.asEntity().level();

        boolean isFluid = world.getBlockState(pos).liquid();
        boolean isAirBelow = world.isEmptyBlock(posBelow);

        if (isAirBelow || isFluid) {
            if (isFluid) {
                posBelow = pos;
            }
            boolean needsSupport = IMLandPathNodeMaker.avoidsBlock(mob.asEntity(), mutable.set(pos).move(Direction.DOWN, 2))
                                || IMLandPathNodeMaker.avoidsBlock(mob.asEntity(), mutable.set(pos).move(Direction.DOWN, 3));
            builder.add(new ModifyBlockEntry(posBelow,
                    (needsSupport ? Blocks.COBBLESTONE : Blocks.OAK_PLANKS).defaultBlockState(),
                    (int) ((needsSupport ? COBBLE_COST : PLANKS_COST) / buildRate))
            );
        }

        return builder.build();
    }
}