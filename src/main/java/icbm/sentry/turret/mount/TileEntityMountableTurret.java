package icbm.sentry.turret.mount;

import icbm.sentry.turret.TileEntityTurret;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovingObjectPosition;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.minecraft.interfaces.IBlockActivated;
import com.builtbroken.minecraft.network.PacketHandler;

/** Mountable Turret
 * 
 * @author Calclavia */
public abstract class TileEntityMountableTurret extends TileEntityTurret implements IBlockActivated
{
    /** Fake entity this sentry uses for mounting the player in position */
    protected EntityMountPoint entityFake = null;

    public TileEntityMountableTurret()
    {
        this.enableRotationHelper = false;
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        // Creates a fake entity to be mounted on
        if (this.entityFake == null || this.entityFake.isDead)
        {
            this.entityFake = new EntityMountPoint(this.worldObj, new Vector3(this.xCoord + 0.5, this.yCoord + 1.2, this.zCoord + 0.5), this, true);
            this.worldObj.spawnEntityInWorld(this.entityFake);
        }

        if (this.entityFake.riddenByEntity instanceof EntityPlayer)
        {
            EntityPlayer mountedPlayer = (EntityPlayer) this.entityFake.riddenByEntity;

            if (mountedPlayer.rotationPitch > this.getPitchServo().getLimits().left())
            {
                mountedPlayer.rotationPitch = this.getPitchServo().getLimits().left();
            }
            if (mountedPlayer.rotationPitch < this.getPitchServo().getLimits().right())
            {
                mountedPlayer.rotationPitch = this.getPitchServo().getLimits().right();
            }
            this.getPitchServo().setRotation(mountedPlayer.rotationPitch);
            this.getYawServo().setRotation(mountedPlayer.rotationYaw);
        }
    }

    /** Performs a ray trace for the distance specified and using the partial tick time. Args:
     * distance, partialTickTime */
    public MovingObjectPosition rayTrace(double distance)
    {
        return this.getAimingDirection().rayTrace(this.worldObj, this.getYawServo().getRotation(), this.getPitchServo().getRotation(), true, distance);
    }

    @Override
    public boolean onActivated(EntityPlayer entityPlayer)
    {
        if (!entityPlayer.isSneaking())
        {
            if (this.entityFake != null)
            {
                if (this.entityFake.riddenByEntity instanceof EntityPlayer)
                {
                    if (!this.worldObj.isRemote)
                    {
                        PacketHandler.instance().sendPacketToClients(this.getRotationPacket());
                    }
                    return true;
                }
            }
        }

        this.mount(entityPlayer);

        return true;
    }

    public void mount(EntityPlayer entityPlayer)
    {
        if (!this.worldObj.isRemote)
        {
            entityPlayer.rotationYaw = this.getYawServo().getRotation();
            entityPlayer.rotationPitch = this.getPitchServo().getRotation();
            entityPlayer.mountEntity(this.entityFake);
        }
    }

    @Override
    public boolean canApplyPotion(PotionEffect par1PotionEffect)
    {
        return false;
    }

}