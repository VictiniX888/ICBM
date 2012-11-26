package icbm.gui;

import icbm.ZhuYao;
import icbm.api.ICBM;
import icbm.daodan.EDaoDan;
import icbm.jiqi.TLeiDaTai;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.GuiTextField;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.vector.Vector2;
import universalelectricity.prefab.network.PacketManager;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GLeiDaTai extends ICBMGui
{
	private TLeiDaTai tileEntity;

	private int containerPosX;
	private int containerPosY;

	private GuiTextField textFieldAlarmRange;
	private GuiTextField textFieldSafetyZone;

	private List<Vector2> missileCoords = new ArrayList<Vector2>();

	private Vector2 mouseOverCoords;

	// Radar Map
	private Vector2 radarCenter;
	private float radarMapRadius;

	public GLeiDaTai(TLeiDaTai tileEntity)
	{
		this.tileEntity = tileEntity;
		mouseOverCoords = new Vector2(this.tileEntity.xCoord, this.tileEntity.zCoord);
		this.xSize = 256;
		radarCenter = new Vector2(this.containerPosX + this.xSize / 3 - 14, this.containerPosY + this.ySize / 2 + 4);
		radarMapRadius = this.tileEntity.MAX_BIAN_JING / 63.8F;
	}

	public void initGui()
	{
		super.initGui();

		this.textFieldSafetyZone = new GuiTextField(fontRenderer, 155, 83, 30, 12);
		this.textFieldSafetyZone.setMaxStringLength(3);
		this.textFieldSafetyZone.setText(this.tileEntity.safetyBanJing + "");

		this.textFieldAlarmRange = new GuiTextField(fontRenderer, 155, 110, 30, 12);
		this.textFieldAlarmRange.setMaxStringLength(3);
		this.textFieldAlarmRange.setText(this.tileEntity.alarmBanJing + "");

		PacketDispatcher.sendPacketToServer(PacketManager.getPacket("ICBM", this.tileEntity, (int) -1, true));
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		PacketDispatcher.sendPacketToServer(PacketManager.getPacket("ICBM", this.tileEntity, (int) -1, false));
	}

	/**
	 * Call this method from you GuiScreen to process the keys into textbox.
	 */
	@Override
	public void keyTyped(char par1, int par2)
	{
		super.keyTyped(par1, par2);
		this.textFieldSafetyZone.textboxKeyTyped(par1, par2);
		this.textFieldAlarmRange.textboxKeyTyped(par1, par2);

		try
		{
			int newSafetyRadius = Math.min(this.tileEntity.MAX_BIAN_JING, Math.max(0, Integer.parseInt(this.textFieldSafetyZone.getText())));
			this.tileEntity.safetyBanJing = newSafetyRadius;
			PacketDispatcher.sendPacketToServer(PacketManager.getPacket(ZhuYao.CHANNEL, this.tileEntity, (int) 2, this.tileEntity.safetyBanJing));
		}
		catch (NumberFormatException e)
		{
		}

		try
		{
			int newAlarmRadius = Math.min(this.tileEntity.MAX_BIAN_JING, Math.max(0, Integer.parseInt(this.textFieldAlarmRange.getText())));
			this.tileEntity.alarmBanJing = newAlarmRadius;
			PacketDispatcher.sendPacketToServer(PacketManager.getPacket(ZhuYao.CHANNEL, this.tileEntity, (int) 3, this.tileEntity.alarmBanJing));
		}
		catch (NumberFormatException e)
		{
		}

	}

	/**
	 * Args: x, y, buttonClicked
	 */
	@Override
	public void mouseClicked(int par1, int par2, int par3)
	{
		super.mouseClicked(par1, par2, par3);
		this.textFieldAlarmRange.mouseClicked(par1 - containerPosX, par2 - containerPosY, par3);
		this.textFieldSafetyZone.mouseClicked(par1 - containerPosX, par2 - containerPosY, par3);
	}

	/**
	 * Called when the mouse is moved or a mouse button is released. Signature: (mouseX, mouseY,
	 * which) which==-1 is mouseMove, which==0 or which==1 is mouseUp
	 */
	protected void mouseMovedOrUp(int x, int y, int which)
	{
		super.mouseMovedOrUp(x, y, which);

		// Check if mouse click is within map
		// region
		if (which == -1)
		{
			float difference = (int) this.tileEntity.MAX_BIAN_JING / this.radarMapRadius;

			if (x > this.radarCenter.x - difference && x < this.radarCenter.x + difference && y > this.radarCenter.y - difference && y < this.radarCenter.y + difference)
			{
				// Calculate from the mouse
				// position the relative position
				// on the grid

				int xDifference = (int) (x - this.radarCenter.x);
				int yDifference = (int) (y - this.radarCenter.y);
				int xBlockDistance = (int) (xDifference * this.radarMapRadius);
				int yBlockDistance = (int) (yDifference * this.radarMapRadius);

				this.mouseOverCoords = new Vector2(this.tileEntity.xCoord + xBlockDistance, this.tileEntity.zCoord - yBlockDistance);
			}
		}
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer()
	{
		this.fontRenderer.drawString("Radar Station", this.xSize / 2 - 87, 6, 4210752);

		this.fontRenderer.drawString("Settings", this.xSize / 2 + 63, 6, 4210752);

		this.fontRenderer.drawString("Coordinates:", 155, 37, 4210752);
		this.fontRenderer.drawString("X: " + (int) Math.round(mouseOverCoords.x) + " Z: " + (int) Math.round(mouseOverCoords.y), 155, 50, 4210752);

		this.fontRenderer.drawString("Safe Zone:", 155, 70, 4210752);
		this.textFieldSafetyZone.drawTextBox();
		this.fontRenderer.drawString("Blocks", 190, 85, 4210752);

		this.fontRenderer.drawString("Alarm Range:", 155, 98, 4210752);
		this.textFieldAlarmRange.drawTextBox();
		this.fontRenderer.drawString("Blocks", 190, 112, 4210752);

		this.fontRenderer.drawString("Voltage: " + this.tileEntity.getVoltage() + "v", 155, 138, 4210752);

		// Shows the status of the radar
		String color = "\u00a74";
		String status = "Idle";

		if (this.tileEntity.isDisabled())
		{
			status = "Disabled!";
		}
		else if (this.tileEntity.prevDian >= TLeiDaTai.YAO_DIAN)
		{
			color = "\u00a72";
			status = "Radar On!";
		}
		else
		{
			status = "No Electricity!";
		}

		this.fontRenderer.drawString(color + status, 155, 150, 4210752);
	}

	/**
	 * Draw the background layer for the GuiContainer (everything behind the items)
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		int var4 = this.mc.renderEngine.getTexture(ICBM.TEXTURE_FILE_PATH + "RadarGUI.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(var4);
		this.containerPosX = (this.width - this.xSize) / 2;
		this.containerPosY = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(containerPosX, containerPosY, 0, 0, this.xSize, this.ySize);

		radarCenter = new Vector2(this.containerPosX + this.xSize / 3 - 10, this.containerPosY + this.ySize / 2 + 4);
		radarMapRadius = this.tileEntity.MAX_BIAN_JING / 65F;

		if (this.tileEntity.detectedMissiles.size() > 0)
		{
			for (EDaoDan missile : this.tileEntity.detectedMissiles)
			{
				float x = (int) (missile.posX - this.tileEntity.xCoord) / radarMapRadius;
				float z = (int) (missile.posZ - this.tileEntity.zCoord) / radarMapRadius;

				if (Vector2.distance(missile.muBiao.toVector2(), new Vector2(this.tileEntity.xCoord, this.tileEntity.zCoord)) < this.tileEntity.safetyBanJing)
				{
					var4 = this.mc.renderEngine.getTexture(ICBM.TEXTURE_FILE_PATH + "reddot.png");
				}
				else
				{
					var4 = this.mc.renderEngine.getTexture(ICBM.TEXTURE_FILE_PATH + "yellowdot.png");
				}

				this.mc.renderEngine.bindTexture(var4);
				this.drawTexturedModalRect((int) (radarCenter.x + x), (int) (radarCenter.y - z), 0, 0, 2, 2);
			}
		}

		for (TLeiDaTai radarStation : this.tileEntity.detectedRadarStations)
		{
			float x = (int) (radarStation.xCoord - this.tileEntity.xCoord) / radarMapRadius;
			float z = (int) (radarStation.zCoord - this.tileEntity.zCoord) / radarMapRadius;

			var4 = this.mc.renderEngine.getTexture("/icbm/yellowdot.png");

			this.mc.renderEngine.bindTexture(var4);
			this.drawTexturedModalRect((int) (radarCenter.x + x), (int) (radarCenter.y - z), 0, 0, 2, 2);
		}
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		if (!this.textFieldSafetyZone.isFocused())
			this.textFieldSafetyZone.setText(this.tileEntity.safetyBanJing + "");
		if (!this.textFieldAlarmRange.isFocused())
			this.textFieldAlarmRange.setText(this.tileEntity.alarmBanJing + "");
	}
}
