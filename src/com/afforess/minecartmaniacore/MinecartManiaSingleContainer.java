package com.afforess.minecartmaniacore;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
/**
 * This class represents a single container (unlike double containers, like a double chest) and provides utility methods for dealing with items inside the container
 * @author Afforess
 */
public abstract class MinecartManiaSingleContainer implements MinecartManiaInventory{
	private Inventory inventory;
	public MinecartManiaSingleContainer(Inventory i) {
		inventory = i;
	}
	
	/**
	 * The bukkit inventory that this container represents
	 * 
	 * @return the inventory
	 * 
	 */
	public Inventory getInventory() {
		return inventory;
	}
	
	/**
	 * Forces the inventory to update to the new given inventory.
	 * Needed because Bukkit inventories become garbage when the chunks they are in are unloaded, or the player dies, etc...
	 * 
	 * @param inventory for this container to use
	 * 
	 */
	public void updateInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	/**
	 * Attempts to add an itemstack. It adds items in a 'smart' manner, merging with existing itemstacks, until they
	 * reach the maximum size (64). If it fails, it will not alter the previous contents.
	 * @param item to add
	 * @return true if the item was successfully added
	 */
	public boolean addItem(ItemStack item) {
		if (item == null) {
			return true;
		}
		if (item.getTypeId() == Material.AIR.getId()) {
			return false;
		}
		//Backup contents
		ItemStack[] backup = getContents().clone();
		ItemStack backupItem = new ItemStack(item.getTypeId(), item.getAmount(), item.getDurability());
		
		//First attempt to merge the itemstack with existing item stacks that aren't full (< 64)
		for (int i = 0; i < size(); i++) {
			if (getItem(i) != null) {
				if (getItem(i).getTypeId() == item.getTypeId() && getItem(i).getDurability() == item.getDurability()) {
					if (getItem(i).getAmount() + item.getAmount() <= 64) {
						setItem(i, new ItemStack(item.getTypeId(), getItem(i).getAmount() + item.getAmount(), item.getDurability()));
						return true;
					}
					else {
						int diff = getItem(i).getAmount() + item.getAmount() - 64;
						setItem(i, new ItemStack(item.getTypeId(), 64, item.getDurability()));
						item = new ItemStack(item.getTypeId(), diff, item.getDurability());
					}
				}
			}
		}
		
		//Attempt to add the item to an empty slot
		int emptySlot = firstEmpty();
		if (emptySlot > -1) {
			setItem(emptySlot, item);
			return true;
		}
			
		//if we fail, reset the inventory and item back to previous values
		getInventory().setContents(backup);
		item = backupItem;
		return false;
	}
	
	/**
	 * Attempts to add an itemstack. If it fails, it will not alter the previous contents
	 * @param type to add
	 * @param amount to add
	 * @return true if the item of the given amount was successfully added
	 */
	public boolean addItem(int type, int amount) {
		return addItem(new ItemStack(type, amount));
	}
	
	/**
	 * Attempts to add a single item. If it fails, it will not alter the previous contents
	 * @param itemtype to add
	 * @return true if the item was successfully added
	 */
	public boolean addItem(int type) {
		return addItem(new ItemStack(type, 1));
	}
	
	/**
	 * Attempts to remove the specified amount of an item type. If it fails, it will not alter the previous contents.
	 * If the durability is -1, it will only match item type id's and ignore durability
	 * @param type to remove
	 * @param amount to remove
	 * @param durability of the item to remove 
	 * @return true if the items were successfully removed
	 */
	public boolean removeItem(int type, int amount, short durability) {
		//Backup contents
		ItemStack[] backup = getContents().clone();
		
		for (int i = 0; i < size(); i++) {
			if (getItem(i) != null) {
				if (getItem(i).getTypeId() == type && (durability == -1 || (getItem(i).getDurability() == durability))) {
					if (getItem(i).getAmount() - amount > 0) {
						setItem(i, new ItemStack(type, getItem(i).getAmount() - amount, durability));
						return true;
					}
					else if (getItem(i).getAmount() - amount == 0) {
						setItem(i, null);
						return true;
					}
					else{
						amount -=  inventory.getItem(i).getAmount();
						setItem(i, null);
					}
				}
			}
		}

		//if we fail, reset the inventory back to previous values
		getInventory().setContents(backup);
		return false;
	}

	/**
	 * Attempts to remove the specified amount of an item type. If it fails, it will not alter the previous contents.
	 * @param type to remove
	 * @param amount to remove
	 * @return true if the items were successfully removed
	 */
	public boolean removeItem(int type, int amount) {
		return removeItem(type, amount, (short) -1);
	}
	
	/**
	 * attempts to remove a single item type. If it fails, it will not alter the previous contents.
	 * @param type to remove
	 * @return true if the item was successfully removed
	 */
	public boolean removeItem(int type) {
		return removeItem(type, 1);
	}

	/**
	 * Gets the itemstack at the given slot, or null if empty
	 * @param slot to get
	 * @return the itemstack at the given slot
	 */
	public ItemStack getItem(int slot) {
		ItemStack i = getInventory().getItem(slot);
		//WTF is it with bukkit and returning air instead of null?
		return i == null ? null : (i.getTypeId() == Material.AIR.getId() ? null : i);
	}

	/**
	 * Sets the itemstack at the given slot. If the itemstack is null, it will clear the slot.
	 * @param slot to set
	 * @param item to set at given slot
	 */
	public void setItem(int slot, ItemStack item) {
		if (item == null) {
			getInventory().clear(slot);
		}
		else {
			getInventory().setItem(slot, item);
		}
	}

	/**
	 * Gets the first empty slot in this inventory, or -1 if it is full
	 * @return the first empty slot
	 */
	public int firstEmpty() {
		return getInventory().firstEmpty();
	}

	/**
	 * Gets the size of this inventory
	 * @return the size of this inventory
	 */
	public int size() {
		return getInventory().getSize();
	}

	/**
	 * Get's an array containing all the contents of this inventory. Empty slots are represented by air.
	 * @return An array containing the contents of this inventory
	 */
	public ItemStack[] getContents() {
		return getInventory().getContents();
	}

	/**
	 * Get's the first slot containing the given material, or -1 if none contain it
	 * @param material to search for
	 * @return the first slot with the given material
	 */
	public int first(Material material) {
		return first(material.getId(), (short) -1);
	}
	
	/**
	 * Get's the first slot containing the given item, or -1 if none contain it
	 * @param item to search for
	 * @return the first slot with the given item
	 */
	public int first(Item item) {
		return first(item.getId(), (short) (item.hasData() ? item.getData() : -1));
	}

	/**
	 * Get's the first slot containing the given type id, or -1 if none contain it
	 * @param type id to search for
	 * @return the first slot with the given item
	 */
	public int first(int type) {
		return first(type, (short)-1);
	}
	
	/**
	 * Get's the first slot containing the given type id and matching durability, or -1 if none contain it.
	 * If the durability is -1, it get's the first slot with the matching type id, and ignores durability
	 * @param type id to search for
	 * @param durability of the type id to search for
	 * @return the first slot with the given type id and durability
	 */
	public int first(int type, short durability) {
		for (int i = 0; i < size(); i++) {
			if (getItem(i) != null) {
				if (getItem(i).getTypeId() == type && ((durability == -1 || getItem(i).getDurability() == -1) || (getItem(i).getDurability() == durability))) {
					return i;
				}
			}
		}
		return -1;
	}
	
	/**
	 * Searches the inventory for any items that match the given Material
	 * @param material to search for
	 * @return true if the material is found
	 */
	public boolean contains(Material material) {
		return first(material) != -1;
	}
	
	/**
	 * Searches the inventory for any items that match the given Item
	 * @param item to search for
	 * @return true if the Item is found
	 */
	public boolean contains(Item item){
		return first(item) != -1;
	}

	/**
	 * Searches the inventory for any items that match the given type id
	 * @param type id to search for
	 * @return true if an item matching the type id is found
	 */
	public boolean contains(int type) {
		return first(type) != -1;
	}
	
	/**
	 * Searches the inventory for any items that match the given type id and durability
	 * @param type id to search for
	 * @param durability to search for
	 * @return true if an item matching the type id and durability is found
	 */
	public boolean contains(int type, short durability) {
		return first(type, durability) != -1;
	}

	/**
	 * Searches the inventory for any items
	 * @return true if the inventory contains no items
	 */
	public boolean isEmpty() {
		for (ItemStack i : getContents()) {
			//I hate you too, air.
			if (i != null && i.getType() != Material.AIR) {
				return false;
			}
		}
		return true;
	}
}
