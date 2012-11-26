package icbm;

import icbm.api.ICBM;

import java.util.ArrayList;

import net.minecraft.src.Block;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;

public class ICBMBlock extends Block
{
	private int maxMetadata = 0;

	public ICBMBlock(int par1, int par2, Material par3Material)
	{
		super(par1, par2, par3Material);
	}

	public ICBMBlock(int par1, int par2, Material par3Material, int i)
	{
		this(par1, par2, par3Material);
		this.maxMetadata = i;
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	@Override
	public int getBlockTextureFromSideAndMetadata(int par1, int par2)
	{
		if (this.maxMetadata > 0)
		{
			return this.blockIndexInTexture + par2;
		}
		else
		{
			return this.blockIndexInTexture;
		}
	}

	@Override
	public String getTextureFile()
	{
		return ICBM.BLOCK_TEXTURE_FILE;
	}

	@Override
	public int damageDropped(int metadata)
	{
		return metadata;
	}

	@Override
	public void addCreativeItems(ArrayList list)
	{
		for (int i = 0; i < this.maxMetadata; i++)
		{
			list.add(new ItemStack(this, 1, i));
		}
	}
}
