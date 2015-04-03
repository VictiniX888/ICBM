package com.builtbroken.icbm.content.launcher;

import codechicken.lib.math.MathHelper;
import com.builtbroken.icbm.ICBM;
import com.builtbroken.icbm.api.ILauncher;
import com.builtbroken.icbm.api.IMissileItem;
import com.builtbroken.icbm.content.Assets;
import com.builtbroken.icbm.content.crafting.missile.casing.Missile;
import com.builtbroken.icbm.content.crafting.missile.casing.MissileCasings;
import com.builtbroken.icbm.content.display.TileMissileContainer;
import com.builtbroken.icbm.content.launcher.launcher.GuiSmallLauncher;
import com.builtbroken.icbm.content.missile.EntityMissile;
import com.builtbroken.mc.api.items.ISimpleItemRenderer;
import com.builtbroken.mc.api.tile.IGuiTile;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.core.registry.implement.IPostInit;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.helper.MathUtility;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.gui.ContainerDummy;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.lwjgl.opengl.GL11;

/**
 * Prefab for all missile launchers and silos.
 * Created by robert on 1/18/2015.
 */
public abstract class TileAbstractLauncher extends TileMissileContainer implements ILauncher, IPacketIDReceiver
{
    protected Pos target = new Pos(0, -1, 0);
    protected short link_code;

    public TileAbstractLauncher(String name, Material mat, int slots)
    {
        super(name, mat, 1);
    }

    public void setTarget(Pos target)
    {
        this.target = target;
        if (isClient())
        {
            Engine.instance.packetHandler.sendToServer(new PacketTile(this, 1, target));
        }
    }

    @Override
    public void update()
    {
        super.update();
        if (isServer())
        {
            if (ticks % 20 == 0)
            {
                if (world().isBlockIndirectlyGettingPowered(xi(), yi(), zi()))
                {
                    fireMissile(target);
                }
            }
        }
    }


    public void fireMissile()
    {
        fireMissile(target);
    }

    public void fireMissile(final Pos target)
    {
        Missile missile = getMissile();
        if (missile != null)
        {
            if (isServer())
            {
                //Create and setup missile
                EntityMissile entity = new EntityMissile(world());
                entity.setMissile(missile);

                //Set location data
                Pos start = new Pos(this).add(getMissileLaunchOffset());
                entity.setPositionAndRotation(start.x(), start.y(), start.z(), 0, 0);
                entity.setVelocity(0, 2, 0);

                //Set target data
                entity.setTarget(target, true);
                entity.sourceOfProjectile = new Pos(this);

                //Spawn and start moving
                world().spawnEntityInWorld(entity);
                entity.setIntoMotion();

                //Empty inventory slot
                this.setInventorySlotContents(0, null);
                sendDescPacket();
            }
            else
            {
                triggerLaunchingEffects();
            }
        }
    }

    /**
     * Called to ensure the missile doesn't clip the edge of a multi-block
     * structure that holds the missile.
     * @return Position in relation to the launcher base, do not add location data
     */
    public Pos getMissileLaunchOffset()
    {
        return new Pos(0.5, 3, 0.5);
    }

    /**
     * Called to load up and populate some effects in addition to the missile's own
     * launching effects.
     */
    public void triggerLaunchingEffects()
    {
        //TODO add more effects
        for (int l = 0; l < 20; ++l)
        {
            double f = x() + 0.5 + 0.3 * (world().rand.nextFloat() - world().rand.nextFloat());
            double f1 = y() + 0.1 + 0.5 * (world().rand.nextFloat() - world().rand.nextFloat());
            double f2 = z() + 0.5 + 0.3 * (world().rand.nextFloat() - world().rand.nextFloat());
            world().spawnParticle("largesmoke", f, f1, f2, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType type)
    {
        if (isServer())
        {
            if (id == 1)
            {
                this.target = new Pos(buf);
                return true;
            }
        }
        else
        {
            if (id == 0)
            {
                this.target = new Pos(buf);
                ItemStack stack = ByteBufUtils.readItemStack(buf);
                if (stack.getItem() instanceof IMissileItem)
                    this.setInventorySlotContents(0, stack);
                else
                    this.setInventorySlotContents(0, null);
                return true;
            }
        }
        return false;
    }

    @Override
    public PacketTile getDescPacket()
    {
        return new PacketTile(this, 0, target, getStackInSlot(0) != null ? getStackInSlot(0) : new ItemStack(Blocks.stone));
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        if (nbt.hasKey("target"))
            this.target = new Pos(nbt.getCompoundTag("target"));
        if(nbt.hasKey("link_code"))
            this.link_code = nbt.getShort("link_code");
        else
            this.link_code = (short)MathUtility.rand.nextInt(Short.MAX_VALUE);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        if (target != null)
            nbt.setTag("target", target.toNBT());
        nbt.setShort("link_code", link_code);
    }
}
