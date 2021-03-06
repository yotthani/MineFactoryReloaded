package powercrystals.minefactoryreloaded.api.plant;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * TODO: docs
 */
@FunctionalInterface
public interface IReplacementBlock {

	/**
	 * Called to replace a block in the world.
	 *
	 * @param world
	 * 		The world object
	 * @param pos
	 * 		Block position
	 * @param stack
	 * 		The ItemStack being used to replace the block
	 *
	 * @return True if the block was set successfully
	 */
	boolean replaceBlock(World world, BlockPos pos, @Nonnull ItemStack stack);

	/**
	 * Return this for times when you do not need to do anything to the in-world block.
	 */
	IReplacementBlock NO_OP = (world, pos, stack) -> true;

	/**
	 * Get an IReplacementBlock for a particular state.
	 *
	 * @param state
	 * 		The state you wish placed into the world
	 *
	 * @return An IReplacementBlock that will place {@code state} into the world
	 */
	static IReplacementBlock of(@Nonnull final IBlockState state) {

		return (world, pos, stack) -> world.setBlockState(pos, state);
	}

}
