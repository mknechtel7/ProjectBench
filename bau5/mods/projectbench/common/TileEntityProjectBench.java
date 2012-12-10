package bau5.mods.projectbench.common;

import net.minecraft.src.Container;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryBasic;
import net.minecraft.src.InventoryCraftResult;
import net.minecraft.src.InventoryCrafting;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet;
import net.minecraft.src.TileEntity;

public class TileEntityProjectBench extends TileEntity implements IInventory
{
	class LocalInventoryCrafting extends InventoryCrafting
	{
		public LocalInventoryCrafting() {
			super(new Container(){
				public boolean canInteractWith(EntityPlayer var1) {
					// TODO Auto-generated method stub
					return false;
				}
			}, 3, 3);
		}
		public Container eventHandler;
	};
	
	private ItemStack[] inv;
	private byte facing;
	
	public IInventory craftResult;
	public IInventory craftSupplyMatrix;
	private ItemStack result;
	private int numPlayersUsing;
	private boolean visited = false;
	private int sync = 0;
	
	public void onInventoryChanged()
	{
		super.onInventoryChanged();
		worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
	}
	public TileEntityProjectBench()
	{
		craftSupplyMatrix = new InventoryBasic("pbCraftingSupply", 18);
		craftResult = new InventoryCraftResult();
		inv = new ItemStack[27];
	}
	public ItemStack findRecipe() {
		InventoryCrafting craftMatrix = new LocalInventoryCrafting();

		for (int i = 0; i < craftMatrix.getSizeInventory(); ++i) 
		{
			ItemStack stack = getStackInSlot(i);
			craftMatrix.setInventorySlotContents(i, stack);
		}

		ItemStack recipe = CraftingManager.getInstance().findMatchingRecipe(craftMatrix, worldObj);
		onInventoryChanged();
		System.out.println("Recipe is " +recipe);
		this.result = recipe;
		return recipe;
	}
	public void setDirectionFacing(byte byt)
	{
		facing = byt;
	}
	public byte getDirectionFacing()
	{
		return facing;
	}
	@Override
	public int getSizeInventory() 
	{
		return inv.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) 
	{
		return inv[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) 
	{
		ItemStack stack = getStackInSlot(slot);
		if(stack != null)
		{
			if(stack.stackSize <= amount)
			{
				setInventorySlotContents(slot, null);
			} else
			{
				stack = stack.splitStack(amount);
				if(stack.stackSize == 0) 
				{
					setInventorySlotContents(slot, null);
				}
			}
		}
		onInventoryChanged();
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) 
	{
		ItemStack stack = getStackInSlot(slot);
		if(stack != null)
		{
			setInventorySlotContents(slot, null);
		}
		onInventoryChanged();
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) 
	{
		inv[slot] = stack;
		if(stack != null && stack.stackSize > getInventoryStackLimit())
		{
			stack.stackSize = getInventoryStackLimit();
		}
		onInventoryChanged();
	}

	@Override
	public String getInvName()
	{
		return "Project Bench";
	}

	@Override
	public int getInventoryStackLimit() 
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) 
	{
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this &&
				player.getDistanceSq(xCoord +0.5, yCoord +0.5, zCoord +0.5) < 64;
	}
	
	public int[] getRecipeStacksForPacket()
	{
		ItemStack result = findRecipe();
		if(result != null)
		{
			int[] craftingStacks = new int[27];
			int index = 0;
			for(int i = 0; i < 9; i++)
			{
				if(inv[i] != null)
				{
					craftingStacks[index++] = inv[i].itemID;
					craftingStacks[index++] = inv[i].stackSize;
					craftingStacks[index++] = inv[i].getItemDamage();
				} else
				{
					craftingStacks[index++] = 0;
					craftingStacks[index++] = 0;
					craftingStacks[index++] = 0;
				}
			}
			return craftingStacks;
		} else
			return null;
	}

	public void buildResultFromPacket(int[] stacksData)
	{
		System.out.println("asdf");
		if(stacksData.length != 0)
		{
			int index = 0;
			for(int i = 0; i < 9; i++)
			{
				if(stacksData[index + 2] != 0)
				{
					ItemStack stack = new ItemStack(stacksData[index], stacksData[index+1], stacksData[index+2]);
					System.out.println("Building from packet @ " +i +" : " +stack);
				}
			}
			findRecipe();
		}
	}
	@Override
	public Packet getDescriptionPacket()
	{
		return PBPacketHandler.prepPacket(this);
		
	}
	@Override
	public void receiveClientEvent(int code, int info)
	{
		if(code == 1)
		{
			findRecipe();
		}
	}

	@Override
	public void openChest() 
	{
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, ProjectBench.instance.projectBench.blockID, 1, 1);
	}

	@Override
	public void closeChest() 
	{
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, ProjectBench.instance.projectBench.blockID, 1, 1);
	}
	@Override
	public void updateEntity() 
	{
		super.updateEntity();
		if((++sync % 20)*4 == 0)
		{
			System.out.println("Syncing");
			worldObj.addBlockEvent(xCoord, yCoord, zCoord, ProjectBench.instance.projectBench.blockID, 1, 1);
			findRecipe();
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);
		
		setDirectionFacing(tagCompound.getByte("Facing"));
		
		NBTTagList tagList = tagCompound.getTagList("Inventory");
		for(int i = 0; i < tagList.tagCount(); i++)
		{
			NBTTagCompound tag = (NBTTagCompound) tagList.tagAt(i);
			byte slot = tag.getByte("Slot");
			if(slot >= 0 && slot < inv.length)
			{
				inv[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}
	}
	@Override
	public void writeToNBT(NBTTagCompound tagCompound)
	{
		super.writeToNBT(tagCompound);
		
		tagCompound.setByte("Facing", getDirectionFacing());
		
		NBTTagList itemList = new NBTTagList();	
		
		for(int i = 0; i < inv.length; i++)
		{
			ItemStack stack = inv[i];
			if(stack != null)
			{
				NBTTagCompound tag = new NBTTagCompound();	
				tag.setByte("Slot", (byte)i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}
		tagCompound.setTag("Inventory", itemList);
		tagCompound.setByte("Facing", getDirectionFacing());
	}
}
