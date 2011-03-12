package com.afforess.minecartmaniacore;

import org.bukkit.Material;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
/**
 * This class represents a Minecart Mania Storage Minecart, that wraps a bukkit minecart (which in turn, wraps a Minecraft EntityMinecart)
 * @author Afforess
 */
public class MinecartManiaStorageCart extends MinecartManiaMinecart implements MinecartManiaInventory{

	/**
	 * Creates a storage minecart from the given bukkit minecart
	 * @param cart to create from
	 */
	public MinecartManiaStorageCart(Minecart cart) {
		super(cart);
	}
	
	/**
	 * Creates a storage minecart from the given bukkit minecart, with the given owner
	 * @param cart to create from
	 * @param owner that created the minecart
	 */
	public MinecartManiaStorageCart(Minecart cart, String owner) {
		super(cart, owner);
	}
	
	/**
	 * Gets the bukkit inventory for this storage minecart
	 * @return bukkit inventory
	 */
	public Inventory getInventory() {
		return ((StorageMinecart)minecart).getInventory();
	}
	
	/**
	 * attempts to add an itemstack to this storage minecart. It adds items in a 'smart' manner, merging with existing itemstacks, until they
	 * reach the maximum size (64). If it fails, it will not alter the storage minecart's previous contents.
	 * @param item to add
	 * @return true if the item was successfully added
	 */
	public boolean addItem(ItemStack item) {
		if (item == null) {
			return true;
		}
		if (item.getTypeId() == Item.AIR.getId()) {
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
						setItem(i, new ItemStack(item.getTypeId(), getItem(i).getAmount() + item.getAmount(), item.getDurability()));
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
	 ** attempts to add a single item of the given type to this storage minecart. If it fails, it will not alter the storage minecart's previous contents
	 ** @param type to add
	 **/
	public boolean addItem(int type) {
		return addItem(new ItemStack(type, 1));
	}
	
	/**
	 ** attempts to add a given amount of a given type to this storage minecart. If it fails, it will not alter the storage minecart's previous contents
	 ** @param type to add
	 ** @param amount to add
	 **/
	public boolean addItem(int type, int amount) {
		return addItem(new ItemStack(type, amount));
	}
	
	/**
	 * attempts to remove the specified amount of an item type from this storage minecart. If it fails, it will not alter the storage minecart's previous contents.
	 * @param type to remove
	 * @param amount to remove
	 * @param durability of the item to remove (-1 for generic durability)
	 * @return true if the items were successfully removed
	 */
	@Override
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
						amount -=  getItem(i).getAmount();
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
	 * attempts to remove the specified amount of an item type from this storage minecart. If it fails, it will not alter the storage minecart's previous contents.
	 * @param type to remove
	 * @param amount to remove
	 * @return true if the items were successfully removed
	 */
	public boolean removeItem(int type, int amount) {
		return removeItem(type, amount, (short) -1);
	}
	
	/**
	 * attempts to remove a single item type from this storage minecart. If it fails, it will not alter the storage minecart previous contents.
	 * @param type to remove
	 * @return true if the item was successfully removed
	 */
	public boolean removeItem(int type) {
		return removeItem(type, 1);
	}
	
	/**
	 * Gets the size of the inventory of this storage minecart
	 * @return the size of the inventory
	 */
	public int size() {
		return getInventory().getSize();
	}

	/**
	 * Gets an array of the Itemstack's inside this storage minecart. Empty slots are represented by air stacks
	 * @return the contents of this inventory
	 */
	public ItemStack[] getContents() {
		return getInventory().getContents();
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
	 * Sets the given slot to the given itemstack. If the itemstack is null, the slot's contents will be cleared.
	 * @param slot to set.
	 * @param item to set in the slot
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
	 * Get's the first empty slot in this storage minecart
	 * @return the first empty slot in this storage minecart
	 */
	public int firstEmpty() {
		return getInventory().firstEmpty();
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

          public boolean isRedstonePower() {
             return false;
         }
}
