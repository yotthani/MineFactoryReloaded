package powercrystals.minefactoryreloaded;

//this import brought to you by the department of redundancies department, the department that brought you this import

import codechicken.lib.CodeChickenLib;
import cofh.CoFHCore;
import cofh.cofhworld.CoFHWorld;
import cofh.cofhworld.init.WorldHandler;
import cofh.redstoneflux.RedstoneFlux;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.CustomProperty;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.apache.logging.log4j.Logger;
import powercrystals.minefactoryreloaded.farmables.MFRFarmables;
import powercrystals.minefactoryreloaded.gui.MFRGUIHandler;
import powercrystals.minefactoryreloaded.net.CommonProxy;
import powercrystals.minefactoryreloaded.net.EntityHandler;
import powercrystals.minefactoryreloaded.net.GridTickHandler;
import powercrystals.minefactoryreloaded.net.MFRPacket;
import powercrystals.minefactoryreloaded.setup.*;
import powercrystals.minefactoryreloaded.setup.recipe.Vanilla;
import powercrystals.minefactoryreloaded.setup.village.VillageCreationHandler;
import powercrystals.minefactoryreloaded.setup.village.Zoologist;
import powercrystals.minefactoryreloaded.tile.machine.processing.TileEntityUnifier;
import powercrystals.minefactoryreloaded.world.MineFactoryReloadedWorldGen;

import java.io.IOException;
import java.util.LinkedList;

import static powercrystals.minefactoryreloaded.MineFactoryReloadedCore.*;
import static powercrystals.minefactoryreloaded.setup.MFRThings.*;

@Mod(modid = modId, name = modName, version = version, dependencies = dependencies,
		customProperties = @CustomProperty(k = "cofhversion", v = "true"))
public class MineFactoryReloadedCore extends BaseMod {

	static{FluidRegistry.enableUniversalBucket();}
	public static final String modId = "minefactoryreloaded";
	public static final String modName = "MineFactory Reloaded";
	public static final String version = "2.9.0B1";
	public static final String dependencies = CoFHCore.VERSION_GROUP + CodeChickenLib.MOD_VERSION_DEP + RedstoneFlux.VERSION_GROUP +
			CoFHWorld.VERSION_GROUP;
	public static final String modNetworkChannel = "MFReloaded";

	@SidedProxy(clientSide = "powercrystals.minefactoryreloaded.net.ClientProxy",
			serverSide = "powercrystals.minefactoryreloaded.net.ServerProxy")
	public static CommonProxy proxy;

	public static SimpleNetworkWrapper networkWrapper = null;

	public static Object balance = "balance";

	public static final String prefix = "minefactoryreloaded:";
	public static final String textureFolder = prefix + "textures/";
	public static final String guiFolder = textureFolder + "gui/";
	public static final String hudFolder = textureFolder + "hud/";
	public static final String villagerFolder = textureFolder + "villager/";
	public static final String tileEntityFolder = textureFolder + "tileentity/";
	public static final String mobTextureFolder = textureFolder + "mob/";
	public static final String modelTextureFolder = textureFolder + "itemmodels/";
	public static final String armorTextureFolder = textureFolder + "armor/";
	public static final String modelFolder = prefix + "models/";

	public static final ResourceLocation CHEST_GEN = new ResourceLocation("mfr:villageZoolologist");

	private static MineFactoryReloadedCore instance;
	private LinkedList<Vanilla> recipeSets = new LinkedList<Vanilla>();
	
	static {

		FluidRegistry.enableUniversalBucket();
	}

	public static MineFactoryReloadedCore instance() {

		return instance;
	}

	public static Logger log() {

		return instance.getLogger();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) throws IOException {

		instance = this;
		setConfigFolderBase(evt.getModConfigurationDirectory());
		MinecraftForge.EVENT_BUS.register(new MFRRegistry.RegistryHandler());

		MFRConfig.loadClientConfig(getClientConfig());
		MFRConfig.loadCommonConfig(getCommonConfig());

		MFRFluids.preInit();
		MFRThings.preInit();

		if (MFRConfig.vanillaRecipes.getBoolean(true))
			recipeSets.add(new Vanilla());

/* TODO readd when there's TE
		if (MFRConfig.thermalExpansionRecipes.getBoolean(false))
			recipeSets.add(new ThermalExpansion());
*/

		//if (MFRConfig.enderioRecipes.getBoolean(false))
		//	recipeSets.add(new EnderIO());

		Blocks.FIRE.setFireInfo(MFRFluids.biofuelLiquid, 300, 30);

		GameRegistry.registerFuelHandler(new MineFactoryReloadedFuelHandler());
		
		proxy.preInit();
	}

	private static void registerBlock(Block block, ItemBlock itemBlock) {
		
		MFRRegistry.registerBlock(block, itemBlock);

	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {

		MinecraftForge.EVENT_BUS.register(rednetCableBlock);
		MinecraftForge.EVENT_BUS.register(new EntityHandler());
		MinecraftForge.EVENT_BUS.register(MFRFluids.INSTANCE);

		proxy.init();
		MFRFarmables.load();

		NetworkRegistry.INSTANCE.registerGuiHandler(this, new MFRGUIHandler());

		MFRPacket.initialize();

		addDispenserBehavior();

		MFRLoot.init();

		Zoologist.init();

		//TODO likely remove, but added here at least for the test
		VillagerRegistry.instance().registerVillageCreationHandler(new VillageCreationHandler());

		WorldHandler.registerReloadCallback(() -> {
			WorldHandler.registerFeature(MineFactoryReloadedWorldGen.INSTANCE);
		});

		//UpdateManager.registerUpdater(new UpdateManager(this, null, CoFHProps.DOWNLOAD_URL));
	}

	private void addDispenserBehavior() {

		IBehaviorDispenseItem behavior = new BehaviorDispenseSafariNet();
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(safariNetItem, behavior);
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(safariNetSingleItem, behavior);
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(safariNetJailerItem, behavior);
	}

	private void addChestGenItems() {

		/*
		//{ DimensionalDoors chestgen compat
		// reference weights[iron: 160; coal: 120; gold: 80; golden apple: 10]
		ChestGenHooks.getInfo("dimensionalDungeonChest").addItem(
			new WeightedRandomChestContent(new ItemStack(safariNetJailerItem), 1, 1, 15));
		ChestGenHooks.getInfo("dimensionalDungeonChest").addItem(
			new WeightedRandomChestContent(new ItemStack(rubberSaplingBlock), 1, 8, 70));
		ChestGenHooks.getInfo("dimensionalDungeonChest").addItem(
			new WeightedRandomChestContent(new ItemStack(pinkSlimeItem), 1, 1, 1));
		// tempting as a sacred sapling is, chests are too common with too few possible items
		// maybe as a custom dungeon for integration
		///}

		//}*/
	}

	@EventHandler
	public void handleIMC(IMCEvent e) {

		IMCHandler.processIMC(e.getMessages());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {

		TileEntityUnifier.updateUnifierLiquids();

		String[] list = MFRConfig.rubberTreeBiomeWhitelist.getStringList();
		for (String biome : list) {
			MFRRegistry.registerRubberTreeBiome(biome);
		}

		list = MFRConfig.unifierBlacklist.getStringList();
		for (String entry : list) {
			MFRRegistry.registerUnifierBlacklist(entry);
		}

		list = MFRConfig.spawnerBlacklist.getStringList();
		for (String entry : list) {
			MFRRegistry.registerAutoSpawnerBlacklist(entry);
		}

		for (Vanilla e : recipeSets)
			e.registerRecipes();

		MFRFarmables.post();
	}

	@EventHandler
	public void loadComplete(FMLLoadCompleteEvent evt) {

		IMCHandler.processIMC(FMLInterModComms.fetchRuntimeMessages(this));

		// catch biomes whitelisted via IMC that are in the config blacklist
		String[] list = MFRConfig.rubberTreeBiomeBlacklist.getStringList();
		for (String biome : list) {
			MFRRegistry.getRubberTreeBiomes().remove(biome);
		}
		for (Property prop : MFRConfig.spawnerCustomization.values()) {
			MFRRegistry.setBaseSpawnCost(prop.getName(), prop.getInt(0));
		}
		list = MFRConfig.safarinetBlacklist.getStringList();
		for (String s : list) {
			Class<?> cl = EntityList.getClass(new ResourceLocation(s));
			if (cl != null)
				MFRRegistry.registerSafariNetBlacklist(cl);
		}

		powercrystals.minefactoryreloaded.core.OreDictionaryArbiter.initialize();
		_log.info("Load Complete.");
	}

	@EventHandler
	public void remap(FMLModIdMappingEvent evt) {

		powercrystals.minefactoryreloaded.core.OreDictionaryArbiter.bake();
	}

	@EventHandler
	public void serverStopped(FMLServerStoppedEvent evt) {

		GridTickHandler.fluid.clear();
		GridTickHandler.energy.clear();
		GridTickHandler.redstone.clear();
	}

	@Override
	public String getModId() {

		return modId;
	}
}
