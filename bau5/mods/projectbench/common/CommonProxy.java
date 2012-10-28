package bau5.mods.projectbench.common;

import bau5.mods.projectbench.client.ProjectBenchGUI;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler
{
	public void registerRenderInformation() 
	{
		  
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) 
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te instanceof TileEntityProjectBench)
		{
			switch(ID)
			{
			case 0: return new ContainerProjectBench(player.inventory, (TileEntityProjectBench) te);
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) 
	{
		switch(ID)
		{
		case 0:
			return new ProjectBenchGUI(player.inventory, (TileEntityProjectBench) world.getBlockTileEntity(x, y, z));
		}
		return null;
	}
}