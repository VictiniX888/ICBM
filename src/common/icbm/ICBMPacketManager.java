package icbm;

import icbm.dianqi.ItHuoLuanQi;
import icbm.dianqi.ItLeiDaQiang;
import icbm.dianqi.ItLeiShiZhiBiao;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet250CustomPayload;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.ItemElectric;
import universalelectricity.prefab.network.PacketManager;

import com.google.common.io.ByteArrayDataInput;

/**
 * This class is used for sending and receiving packets between the server and the client. You can
 * directly use this by registering this packet manager with NetworkMod. Example:
 * 
 * @NetworkMod(channels = { "BasicComponents" }, clientSideRequired = true, serverSideRequired =
 * false, packetHandler = PacketManager.class)
 * 
 * Check out {@link #BasicComponents} for better reference.
 * 
 * @author Calclavia
 */
public class ICBMPacketManager extends PacketManager
{
	public enum ICBMPacketType
	{
		UNSPECIFIED, TILEENTITY, RADAR_GUN, LASER_DESIGNATOR, SIGNAL_DISRUPTER;

		public static ICBMPacketType get(int id)
		{
			if (id >= 0 && id < ICBMPacketType.values().length) { return ICBMPacketType.values()[id]; }
			return UNSPECIFIED;
		}
	}

	@Override
	public void handlePacketData(INetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		ICBMPacketType icbmPacketType = ICBMPacketType.get(packetType);

		if (icbmPacketType == ICBMPacketType.RADAR_GUN)
		{
			if (player.inventory.getCurrentItem().getItem() instanceof ItLeiDaQiang)
			{
				ItemStack itemStack = player.inventory.getCurrentItem();
				// Saves the frequency in the
				// itemstack
				if (itemStack.stackTagCompound == null)
				{
					itemStack.setTagCompound(new NBTTagCompound());
				}

				itemStack.stackTagCompound.setInteger("x", dataStream.readInt());
				itemStack.stackTagCompound.setInteger("y", dataStream.readInt());
				itemStack.stackTagCompound.setInteger("z", dataStream.readInt());
				((ItemElectric) ZhuYao.itLeiDaQiang).onUse(ItLeiDaQiang.YONG_DIAN_LIANG, itemStack);
			}
		}
		else if (icbmPacketType == ICBMPacketType.LASER_DESIGNATOR)
		{
			if (player.inventory.getCurrentItem().getItem() instanceof ItLeiShiZhiBiao)
			{
				ItemStack itemStack = player.inventory.getCurrentItem();
				Vector3 position = new Vector3(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());

				((ItLeiShiZhiBiao) ZhuYao.itLeiSheZhiBiao).setLauncherCountDown(itemStack, 119);

				player.worldObj.playSoundEffect(position.intX(), player.worldObj.getHeightValue(position.intX(), position.intZ()), position.intZ(), "icbm.airstrike", 5.0F, (1.0F + (player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);

				player.worldObj.spawnEntityInWorld(new EGuang(player.worldObj, position, 5 * 20, 0F, 1F, 0F));

				((ItemElectric) ZhuYao.itLeiDaQiang).onUse(ItLeiShiZhiBiao.YONG_DIAN_LIANG, itemStack);
			}
		}
		else if (icbmPacketType == ICBMPacketType.SIGNAL_DISRUPTER)
		{
			if (player.inventory.getCurrentItem().getItem() instanceof ItHuoLuanQi)
			{
				ItemStack itemStack = player.inventory.getCurrentItem();

				((ItHuoLuanQi) itemStack.getItem()).setFrequency(dataStream.readShort(), itemStack);
			}
		}
	}
}
