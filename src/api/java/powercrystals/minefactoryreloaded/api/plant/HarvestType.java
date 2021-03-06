package powercrystals.minefactoryreloaded.api.plant;

/**
 * Determines what algorithm the Harvester uses when it encounters this
 * IFactoryHarvestable in the world.
 *
 * @author PowerCrystals
 */
public enum HarvestType {

	/**
	 * Just break the single block - no special action needed. e.g. Carrots,
	 * flowers, pumpkins, wheat.
	 */
	Normal,
	/**
	 * Search for harvestable blocks adjacent to this block but leave this
	 * block. e.g. Pumpkin stem, melon stem
	 */
	PlantStem,
	/**
	 * Search for identical blocks above.
	 */
	Column,
	/**
	 * Search for identical blocks above but leave the bottom one for the
	 * future. e.g. Cactus, sugarcane.
	 */
	LeaveBottom,
	/**
	 * This block is the base of a tree and the harvester should enter
	 * tree-cutting mode.
	 */
	Tree,
	/**
	 * This block is part of a tree as above, but leaves are cut before logs.
	 * The tree is searched for in the current mode.
	 * <p>
	 * If not in tree-cutting mode, tree-cutting mode will be entered as though
	 * the type was Tree.
	 */
	TreeLeaf,
	/**
	 * This block is part of a tree as above, but fruits are cut before logs.
	 * e.g. cocoa
	 * The tree is not searched for.
	 */
	TreeFruit

}
