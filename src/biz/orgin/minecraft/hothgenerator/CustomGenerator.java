package biz.orgin.minecraft.hothgenerator;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import biz.orgin.minecraft.hothgenerator.schematic.LoadedSchematic;

/**
 * This class finds all user defined schematics files and inserts them into the generated terrain.
 * @author orgin
 *
 */
public class CustomGenerator
{
	private static Vector<LoadedSchematic> schematics = null;
	

	public static void load(HothGeneratorPlugin plugin)
	{
		CustomGenerator.schematics = new Vector<LoadedSchematic>();
		
		File dataFolder = plugin.getDataFolder();
		String path = dataFolder.getAbsolutePath() + "/custom";
		File customFolder = new File(path);
		if(!customFolder.exists())
		{
			customFolder.mkdir();
		}
		else
		{
			File[] files = customFolder.listFiles();
			if(files!=null)
			{
				for(int i=0;i<files.length;i++)
				{
					File file = files[i];
					if(file.isFile() && file.getName().endsWith(".sm") && !file.getName().endsWith("example.sm"))
					{
						try
						{
							LoadedSchematic schematic = new LoadedSchematic(file);
							if(schematic.isEnabled())
							{
								CustomGenerator.schematics.add(schematic);
								plugin.getLogger().info("Loaded custom schematic: " + file.getName());
							}
							else
							{
								plugin.getLogger().info("Ignoring disabled schematic: " + file.getName());
							}
						}
						catch(IOException e)
						{
							plugin.getLogger().info("ERROR: Failed to load " + file.getName() + " " + e.getMessage());
						}
					}
				}
			}
		}
	}
	
	public static void generateCustom(HothGeneratorPlugin plugin, World world, Random random, int chunkX, int chunkZ)
	{
		if(CustomGenerator.schematics!=null)
		{
			long randomLong = random.nextLong();
			
			for(int i=0;i<CustomGenerator.schematics.size();i++)
			{
				LoadedSchematic schematic = schematics.elementAt(i);
				
				int rarity = schematic.getRarity();
				int rnd = schematic.getRandom();
				
				int place = random.nextInt(rarity);
				if(place==rnd)
				{
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new PlaceCustom(schematic, plugin, world, new Random(randomLong), chunkX, chunkZ));
				}	
			}
		}
	}


	static class PlaceCustom implements Runnable
	{
		private final LoadedSchematic schematic;
		private final HothGeneratorPlugin plugin;
		private final World world;
		private final Random random;
		private final int chunkx;
		private final int chunkz;

		public PlaceCustom(LoadedSchematic schematic, HothGeneratorPlugin plugin, World world, Random random, int chunkx, int chunkz)
		{
			this.schematic = schematic;
			this.plugin = plugin;
			this.world = world;
			this.random = random;
			this.chunkx = chunkx;
			this.chunkz = chunkz;
		}

		@Override
		public void run()
		{
			int surfaceOffset = this.plugin.getWorldSurfaceoffset();
			

			int x = this.random.nextInt(16) + this.chunkx * 16 - this.schematic.getWidth()/2;
			int z = this.random.nextInt(16) + this.chunkz * 16 - this.schematic.getLength()/2;
			int y = 128;
			
			int miny, maxy;
			
			int w = this.schematic.getWidth();
			int l = this.schematic.getLength();
			int h = this.schematic.getHeight();
			
			int hw = w/2;
			int hl = l/2;

			boolean safe = true;

			switch(this.schematic.getType())
			{
			case 0: // On surface
				int y1 = this.world.getHighestBlockYAt(x-hw, z-hl);
				int y2 = this.world.getHighestBlockYAt(x+hw, z-hl);
				int y3 = this.world.getHighestBlockYAt(x-hw, z+hl);
				int y4 = this.world.getHighestBlockYAt(x+hw, z+hl);
				int yoffset = this.schematic.getYoffset();
				
				y = y1;
				if(y2<y) y=y2;
				if(y3<y) y=y3;
				if(y4<y) y=y4;
				
				y = y + h - 1 + yoffset;

				Block block = world.getBlockAt(x,y - h,z); Material type = block.getType();
				if(safe && (type.equals(Material.STONE) || type.equals(Material.GLASS))) safe = false;
				if(yoffset==0) // Only do air safety checks if the offset value is 0
				{
					block = world.getBlockAt(x-hw,y,z-hl); type = block.getType();
					if(safe && !type.equals(Material.AIR)) safe = false;
					block = world.getBlockAt(x+hw,y,z-hl); type = block.getType();
					if(safe && !type.equals(Material.AIR)) safe = false;
					block = world.getBlockAt(x+hw,y,z+hl); type = block.getType();
					if(safe && !type.equals(Material.AIR)) safe = false;
					block = world.getBlockAt(x-hw,y,z+hl); type = block.getType();
					if(safe && !type.equals(Material.AIR)) safe = false;

					block = world.getBlockAt(x-hw,y-h,z-hl); type = block.getType();
					if(safe && type.equals(Material.AIR)) safe = false;
					block = world.getBlockAt(x+hw,y-h,z-hl); type = block.getType();
					if(safe && type.equals(Material.AIR)) safe = false;
					block = world.getBlockAt(x+hw,y-h,z+hl); type = block.getType();
					if(safe && type.equals(Material.AIR)) safe = false;
					block = world.getBlockAt(x-hw,y-h,z+hl); type = block.getType();
					if(safe && type.equals(Material.AIR)) safe = false;
				
				}
				block = world.getBlockAt(x-hw,y-h,z-hl); type = block.getType();
				if(safe && (type.equals(Material.STONE) || type.equals(Material.GLASS))) safe = false;
				block = world.getBlockAt(x+hw,y-h,z-hl); type = block.getType();
				if(safe && (type.equals(Material.STONE) || type.equals(Material.GLASS))) safe = false;
				block = world.getBlockAt(x+hw,y-h,z+hl); type = block.getType();
				if(safe && (type.equals(Material.STONE) || type.equals(Material.GLASS))) safe = false;
				block = world.getBlockAt(x-hw,y-h,z+hl); type = block.getType();
				if(safe && (type.equals(Material.STONE) || type.equals(Material.GLASS))) safe = false;
				
				
				break;
			case 1: // In ice layer
				miny = surfaceOffset + 30 + this.schematic.getHeight();
				maxy = surfaceOffset + 65;
				y = miny + this.random.nextInt(maxy-miny);
				break;
			case 2: // In stone layer
				miny = 6 + this.schematic.getHeight();
				maxy = surfaceOffset + 26;
				y = miny + this.random.nextInt(maxy-miny);
				break;
			}
			
			if(safe)
			{
				LootGenerator generator = LootGenerator.getLootGenerator(schematic.getLoot());
				if(generator==null)
				{
					HothUtils.placeSchematic(plugin, world, schematic, x-hw, y, z+hl, schematic.getLootMin(), schematic.getLootMax());
				}
				else
				{
					HothUtils.placeSchematic(plugin, world, schematic, x-hw, y, z+hl, schematic.getLootMin(), schematic.getLootMax(), generator);
				}
	
				this.plugin.logMessage("Placing " + schematic.getName() + " at " + x + "," + y + "," + z, true);
			}
			else
			{
				this.plugin.logMessage("Failed to place " + this.schematic.getName() + " at " + x + "," + y + "," + z, true);
			}
		}
	}
}
