package icbm;
 
import java.util.Random;

import net.minecraft.src.Block;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.ore.OreGenReplace;
import universalelectricity.prefab.Vector3;
 
/**
 * @author CovertJaguar, Modified by Calclavia
 */

public class GenLiu extends OreGenReplace
{
    public GenLiu(String name, String oreDiectionaryName, ItemStack stack, int replaceID, int minGenerateLevel, int maxGenerateLevel, int amountPerChunk, int amountPerBranch, String harvestTool, int harvestLevel)
    {
        super(name, oreDiectionaryName, stack, 0, replaceID, maxGenerateLevel, amountPerChunk, amountPerBranch, "pickaxe", 1);
    	this.generateSurface = true;
    }
    
    public GenLiu(String name, String oreDiectionaryName, ItemStack stack, int replaceID, int maxGenerateLevel, int amountPerChunk, int amountPerBranch)
    {
        this(name, oreDiectionaryName, stack, 0, replaceID, maxGenerateLevel, amountPerChunk, amountPerBranch, "pickaxe", 1);
    }
    
    @Override
    public void generate(World world, Random random, int varX, int varZ)
    {
    	for(int y = this.minGenerateLevel; y < this.maxGenerateLevel; y ++)
    	{
    		for(int x = 0; x < 16; x ++)
    		{
    			for(int z = 0; z < 16; z ++)
    			{
    				this.generateReplace(world, random, varX + x, y, varZ + z);
    			}
    		}
    	}
	}
 
    @Override
    public boolean generateReplace(World world, Random rand, int x, int y, int z)
    {
        if(nearLava(world, x, y, z))
        {
            placeOre(world, rand, x, y, z);
            return true;
        }
        
        return false;
    }
 
    private void placeOre(World world, Random rand, int x, int y, int z)
    {
        Vector3 position = new Vector3(x, y, z);

        for(int amount = 0; amount < this.amountPerBranch; amount++)
        {
            Block block = Block.blocksList[world.getBlockId(x, y, z)];
            
            if(block != null && block.isGenMineableReplaceable(world, x, y, z))
            {
                world.setBlockAndMetadata(x, y, z, this.oreID, this.oreMeta);
            }

            ForgeDirection dir = ForgeDirection.values()[rand.nextInt(6)];
            
            position.modifyPositionFromSide(dir);
        }
    }
 
    private boolean nearLava(World world, int x, int y, int z)
    {
        for(int side = 2; side < 6; side++) 
        {
			Vector3 position = new Vector3(x, y, z);
			 
			ForgeDirection s = ForgeDirection.values()[side];
			 
			position.modifyPositionFromSide(s);
 
            if(world.blockExists(position.intX(), position.intY(), position.intZ()))
            {
                int id = world.getBlockId(position.intX(), position.intY(), position.intZ());
                
                if(id == Block.lavaStill.blockID || id == Block.lavaMoving.blockID)
                {
                    return true;
                }
            }
        }
        
        for(int j = 0; j < 4; j++)
        {
            int id = world.getBlockId(x, y - j, z);
 
            if(id == Block.lavaStill.blockID || id == Block.lavaMoving.blockID)
            {
                return true;
            }
            else if(id != 0)
            {
                return false;
            }
        }
        
        return false;
    }
}