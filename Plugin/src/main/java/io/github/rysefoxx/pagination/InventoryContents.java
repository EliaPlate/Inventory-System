package io.github.rysefoxx.pagination;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import io.github.rysefoxx.SlotIterator;
import io.github.rysefoxx.content.IntelligentItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnegative;
import javax.print.DocFlavor;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Rysefoxx | Rysefoxx#6772
 * @since 2/17/2022
 */
public class InventoryContents {

    private final Player player;
    private final Pagination pagination;
    private final HashMap<String, Object> data;
    private final RyseInventory inventory;

    public InventoryContents(Player player, RyseInventory inventory) {
        this.player = player;
        this.inventory = inventory;
        this.pagination = new Pagination(inventory);
        this.data = new HashMap<>();
    }

    /**
     * With this method you can see if a slot exists in the inventory.
     *
     * @param slot Which slot to look for.
     * @return true if the slot exists in the inventory.
     * @throws IllegalArgumentException if slot > 53
     */
    public boolean hasSlot(@Nonnegative int slot) throws IllegalArgumentException {
        if (slot > 53) {
            throw new IllegalArgumentException("The slot must not be larger than 53.");
        }
        return slot <= this.inventory.size();
    }

    /**
     * Removes the item from the inventory and the associated consumer.
     *
     * @param slot Which slot should be removed?
     * @throws IllegalArgumentException When slot is greater than 53
     */
    public void removeItemWithConsumer(@Nonnegative int slot) throws IllegalArgumentException {
        if (slot > 53) {
            throw new IllegalArgumentException("The slot must not be larger than 53.");
        }

        this.pagination.getPermanentItems().remove(slot);

        get(slot).ifPresent(IntelligentItem::clearConsumer);
        Optional<Inventory> inventoryOptional = this.inventory.inventoryBasedOnOption(this.player.getUniqueId());
        if (!inventoryOptional.isPresent()) return;
        inventoryOptional.get().setItem(slot, null);
    }

    /**
     * Removes the first item that matches the parameter.
     *
     * @param item The item to filter for.
     */
    public void removeFirst(ItemStack item) {
        for (int i = 0; i < this.inventory.size(); i++) {
            Optional<IntelligentItem> optional = get(i);
            if (!optional.isPresent()) continue;

            ItemStack itemStack = optional.get().getItemStack();
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            if (!itemStack.isSimilar(item)) continue;

            remove(i);

            Optional<Inventory> inventoryOptional = this.inventory.inventoryBasedOnOption(this.player.getUniqueId());
            if (!inventoryOptional.isPresent()) break;
            inventoryOptional.get().setItem(i, null);
            optional.get().clearConsumer();
            break;
        }
    }

    /**
     * Removes the first item that matches the parameter.
     *
     * @param material The material to filter for.
     */
    public void removeFirst(Material material) {
        for (int i = 0; i < this.inventory.size(); i++) {
            Optional<IntelligentItem> optional = get(i);
            if (!optional.isPresent()) continue;

            ItemStack itemStack = optional.get().getItemStack();
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            if (itemStack.getType() != material) continue;

            remove(i);

            Optional<Inventory> inventoryOptional = this.inventory.inventoryBasedOnOption(this.player.getUniqueId());
            if (!inventoryOptional.isPresent()) break;
            inventoryOptional.get().setItem(i, null);
            optional.get().clearConsumer();
            break;
        }
    }

    /**
     * Subtracts a value from an ItemStack.
     *
     * @param item   The itemstack what should be reduced.
     * @param amount How much to remove.
     * @throws IllegalArgumentException if amount > 64
     * @apiNote If the itemstack Amount is < 1, the ItemStack will be deleted from the inventory.
     */
    public void subtractFirst(ItemStack item, @Nonnegative int amount) throws IllegalArgumentException {
        if (amount > 64) {
            throw new IllegalArgumentException("Amount must not be larger than 64.");
        }

        for (int i = 0; i < this.inventory.size(); i++) {
            Optional<IntelligentItem> optional = get(i);
            if (!optional.isPresent()) continue;

            ItemStack itemStack = optional.get().getItemStack();
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            if (!itemStack.isSimilar(item)) continue;

            Optional<Inventory> inventoryOptional = this.inventory.inventoryBasedOnOption(this.player.getUniqueId());
            if (itemStack.getAmount() - amount < 1) {
                remove(i);
                if (!inventoryOptional.isPresent()) continue;
                inventoryOptional.get().setItem(i, null);
                continue;
            }
            if (!inventoryOptional.isPresent()) continue;
            itemStack.setAmount(itemStack.getAmount() - amount);
            inventoryOptional.get().setItem(i, itemStack);
            break;
        }
    }

    /**
     * Removes all items that match the parameter.
     *
     * @param item The item to filter for.
     */
    public void removeAll(ItemStack item) {
        for (int i = 0; i < this.inventory.size(); i++) {
            Optional<IntelligentItem> optional = get(i);
            if (!optional.isPresent()) continue;

            ItemStack itemStack = optional.get().getItemStack();
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            if (!itemStack.isSimilar(item)) continue;

            remove(i);

            Optional<Inventory> inventoryOptional = this.inventory.inventoryBasedOnOption(this.player.getUniqueId());
            if (!inventoryOptional.isPresent()) break;
            inventoryOptional.get().setItem(i, null);
            optional.get().clearConsumer();
        }
    }

    /**
     * Removes all items that match the parameter.
     *
     * @param item   The item to filter for.
     * @param amount How much to remove.
     * @throws IllegalArgumentException if amount > 64
     */
    public void removeAll(ItemStack item, @Nonnegative int amount) throws IllegalArgumentException {
        if (amount > 64) {
            throw new IllegalArgumentException("Amount must not be larger than 64.");
        }

        for (int i = 0; i < this.inventory.size(); i++) {
            Optional<IntelligentItem> optional = get(i);
            if (!optional.isPresent()) continue;

            ItemStack itemStack = optional.get().getItemStack();
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;
            if (!itemStack.isSimilar(item)) continue;

            Optional<Inventory> inventoryOptional = this.inventory.inventoryBasedOnOption(this.player.getUniqueId());
            if (itemStack.getAmount() - amount < 1) {
                remove(i);
                if (!inventoryOptional.isPresent()) continue;
                inventoryOptional.get().setItem(i, null);
                optional.get().clearConsumer();
                continue;
            }
            if (!inventoryOptional.isPresent()) continue;
            itemStack.setAmount(itemStack.getAmount() - amount);
            inventoryOptional.get().setItem(i, itemStack);
        }
    }

    /**
     * Removes the very first item that can be found.
     */
    public void removeFirst() {
        for (int i = 0; i < this.inventory.size(); i++) {
            Optional<IntelligentItem> optional = get(i);
            if (!optional.isPresent()) continue;

            ItemStack itemStack = optional.get().getItemStack();
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;

            remove(i);

            Optional<Inventory> inventoryOptional = this.inventory.inventoryBasedOnOption(this.player.getUniqueId());
            if (!inventoryOptional.isPresent()) break;
            inventoryOptional.get().setItem(i, null);
            optional.get().clearConsumer();
            break;
        }
    }

    /**
     * Removes the very first item that can be found.
     *
     * @param amount How much to remove
     * @throws IllegalArgumentException if amount > 64
     */
    public void removeFirst(@Nonnegative int amount) throws IllegalArgumentException {
        if (amount > 64) {
            throw new IllegalArgumentException("Amount must not be larger than 64.");
        }
        for (int i = 0; i < this.inventory.size(); i++) {
            Optional<IntelligentItem> optional = get(i);
            if (!optional.isPresent()) continue;

            ItemStack itemStack = optional.get().getItemStack();
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) continue;

            Optional<Inventory> inventoryOptional = this.inventory.inventoryBasedOnOption(this.player.getUniqueId());
            if (itemStack.getAmount() - amount < 1) {
                remove(i);
                if (!inventoryOptional.isPresent()) break;
                inventoryOptional.get().setItem(i, null);
                optional.get().clearConsumer();
                break;
            }
            if (!inventoryOptional.isPresent()) break;
            itemStack.setAmount(itemStack.getAmount() - amount);
            inventoryOptional.get().setItem(i, itemStack);
            break;
        }
    }

    /**
     * Outputs all slots of the inventory to perform operations on the slots.
     *
     * @return a list with all slots
     */
    public List<Integer> slots() {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < this.inventory.size(); i++) {
            slots.add(i);
        }
        return slots;
    }

    /**
     * With this method you can update the inventory title.
     *
     * @param plugin   The JavaPlugin
     * @param newTitle The new title
     */
    public void updateTitle(JavaPlugin plugin, String newTitle) {
        this.inventory.updateTitle(this.player, newTitle);
    }


    /**
     * Fills the Border with a intelligent ItemStack regardless of inventory size.
     *
     * @param item The ItemStack which should represent the border
     */
    public void fillBorders(IntelligentItem item) {
        int size = this.inventory.size();
        int rows = (size + 1) / 9;

        for (int i = 0; i < rows * 9; i++) {
            if ((i <= 8) || (i >= (rows * 9) - 9)
                    || i == 9 || i == 18
                    || i == 27 || i == 36
                    || i == 17 || i == 26
                    || i == 35 || i == 44)
                set(i, item);
        }
    }

    /**
     * Method to cache data in the content
     *
     * @param key
     * @param value
     */
    public <T> void setData(String key, T value) {
        this.data.put(key, value);
    }

    /**
     * Method to get data in the content
     *
     * @param key
     * @return The cached value
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String key) {
        if (!this.data.containsKey(key)) return null;

        return (T) this.data.get(key);
    }

    /**
     * Removes data with the associated key.
     *
     * @param key The key that will be removed together with the value.
     */
    @Beta
    public void removeData(String key) {
        if (!this.data.containsKey(key)) return;

        this.data.remove(key);
    }

    /**
     * Removes all data in the inventory.
     */
    @Beta
    public void clearData() {
        this.data.clear();
    }

    /**
     * Removes all data in the inventory and returns all values in the consumer.
     */
    @Beta
    public void clearData(Consumer<List<?>> consumer) {
        List<Object> data = Arrays.asList(this.data.values().toArray());
        consumer.accept(data);

        this.data.clear();
    }

    /**
     * Removes all data in the inventory and returns all keys and values in the consumer.
     */
    @Beta
    public void clearData(BiConsumer<List<String>, List<?>> consumer) {
        List<String> keys = new ArrayList<>(this.data.keySet());
        List<Object> values = Arrays.asList(this.data.values().toArray());

        consumer.accept(keys, values);
        this.data.clear();
    }

    /**
     * Removes data with the associated key. The value is then passed in the consumer.
     *
     * @param key      The key that will be removed together with the value.
     * @param consumer The value that is removed.
     */
    @SuppressWarnings("unchecked")
    @Beta
    public <T> void removeData(String key, Consumer<T> consumer) {
        if (!this.data.containsKey(key)) return;

        consumer.accept((T) this.data.remove(key));
    }

    /**
     * Method to get data in the content
     *
     * @param key
     * @param defaultValue value when key is invalid
     * @return The cached value
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String key, Object defaultValue) {
        if (!this.data.containsKey(key)) return (T) defaultValue;

        return (T) this.data.get(key);
    }

    /**
     * @return The first slot that is empty.
     */
    public Optional<Integer> firstEmpty() {
        for (int i = 0; i < this.inventory.size(); i++) {
            Optional<IntelligentItem> item = get(i);

            if (item.isPresent()) continue;
            return Optional.of(i);
        }
        return Optional.empty();
    }

    /**
     * @return The last slot that is empty.
     */
    public Optional<Integer> lastEmpty() {
        int nextSlot = -1;

        for (int i = 0; i < this.inventory.size(); i++) {
            Optional<IntelligentItem> item = get(i);

            if (item.isPresent()) continue;
            nextSlot = i;
        }
        return Optional.of(nextSlot);
    }


    /**
     * Adds an item to the inventory in the first free place.
     *
     * @param item The ItemStack to be displayed in the inventory
     */
    public void add(IntelligentItem item) {
        firstEmpty().ifPresent(integer -> {
            this.pagination.setItem(integer, item);

            Optional<Inventory> inventoryOptional = this.inventory.inventoryBasedOnOption(this.player.getUniqueId());
            inventoryOptional.ifPresent(savedInventory -> savedInventory.setItem(integer, item.getItemStack()));
        });
    }

    /**
     * Add multiple items to the inventory in the first free place.
     *
     * @param items The ItemStacks to be displayed in the inventory
     */
    public void add(IntelligentItem... items) {
        for (IntelligentItem item : items) {
            add(item);
        }
    }

    /**
     * This method allows you to reload the contents of the inventory.
     */
    @Beta
    public void reload() {
        clear(this.pagination.page() - 1);

        InventoryContents contents = new InventoryContents(this.player, this.inventory);
        this.inventory.getManager().setContents(this.player.getUniqueId(), contents);

        if (this.inventory.getSlideAnimator() == null) {
            this.inventory.getProvider().init(this.player, contents);
        } else {
            this.inventory.getProvider().init(this.player, contents, this.inventory.getSlideAnimator());
        }

        this.inventory.splitInventory(contents);
        this.inventory.load(contents.pagination(), this.inventory.getSharedInventory(), this.player, contents.pagination().page() - 1);
    }

    private void clear(int page) {
        this.pagination.getPermanentItems().forEach((integer, item) -> removeItemWithConsumer(integer));
        this.pagination.getPageItems().get(page).forEach((integer, item) -> removeItemWithConsumer(integer));
    }

    /**
     * Sets a fixed smart ItemStack in the inventory on a specified page.
     *
     * @param slot Where should the item be placed?
     * @param page On which page should the item be placed?
     * @param item The ItemStack to be displayed in the inventory
     * @throws IllegalArgumentException when slot > 53
     * @apiNote When you define a page please start at 1.
     */
    public void setWithinPage(@Nonnegative int slot, @Nonnegative int page, IntelligentItem item) throws IllegalArgumentException {
        if (slot > 53) {
            throw new IllegalArgumentException("The slot must not be larger than 53.");
        }
        if (slot > this.inventory.size()) {
            throw new IllegalArgumentException("The slot must not be larger than the inventory size.");
        }
        this.pagination.setItem(slot, page, item);

        if (page > this.pagination.lastPage()) return;

        Optional<Inventory> inventoryOptional = this.inventory.inventoryBasedOnOption(this.player.getUniqueId());
        inventoryOptional.ifPresent(savedInventory -> savedInventory.setItem(slot, item.getItemStack()));
    }

    /**
     * Sets an item to multiple slots within a page.
     *
     * @param slots Where should the item be placed everywhere?
     * @param page  On which page should the item be placed?
     * @param item  The ItemStack to be displayed in the inventory
     * @apiNote When you define a page please start at 1.
     */
    public void setWithinPage(List<Integer> slots, int page, IntelligentItem item) {
        slots.forEach(slot -> setWithinPage(slot, page, item));
    }

    /**
     * Places an item on multiple pages in multiple slots.
     *
     * @param slots Where should the item be placed everywhere?
     * @param pages On which pages should the item be placed?
     * @param item  The ItemStack to be displayed in the inventory
     * @apiNote When you define a page please start at 1.
     */
    public void setWithinPage(List<Integer> slots, List<Integer> pages, IntelligentItem item) {
        slots.forEach(slot -> pages.forEach(page -> setWithinPage(slot, page, item)));
    }

    /**
     * Fills the whole inventory with an ItemStack.
     *
     * @param item The item with which the inventory should be filled.
     */
    public void fill(IntelligentItem item) {
        Optional<Inventory> inventoryOptional = this.inventory.inventoryBasedOnOption(this.player.getUniqueId());
        for (int i = 0; i < this.inventory.size(); i++) {
            this.pagination.setItem(i, item);
            int finalI = i;
            inventoryOptional.ifPresent(savedInventory -> savedInventory.setItem(finalI, item.getItemStack()));
        }
    }


    /**
     * Sets a fixed intelligent ItemStack in the inventory.
     *
     * @param slot Where should the item be placed?
     * @param item The ItemStack to be displayed in the inventory
     * @throws IllegalArgumentException when slot > 53 or > inventory size
     */
    public void set(@Nonnegative int slot, IntelligentItem item) throws IllegalArgumentException {
        if (slot > 53) {
            throw new IllegalArgumentException("The slot must not be larger than 53.");
        }
        if (slot > this.inventory.size()) {
            throw new IllegalArgumentException("The slot must not be larger than the inventory size.");
        }
        this.pagination.setItem(slot, item);

        Optional<Inventory> inventoryOptional = this.inventory.inventoryBasedOnOption(this.player.getUniqueId());
        inventoryOptional.ifPresent(savedInventory -> savedInventory.setItem(slot, item.getItemStack()));
    }

    /**
     * Sets a fixed intelligent ItemStack in the inventory.
     *
     * @param slots Where should the item be placed everywhere?
     * @param item  The ItemStack to be displayed in the inventory
     */
    public void set(List<Integer> slots, IntelligentItem item) {
        slots.forEach(slot -> set(slot, item));
    }

    /**
     * Sets a fixed intelligent ItemStack in the inventory.
     *
     * @param row    The row
     * @param column The column
     * @param item   The ItemStack to be displayed in the inventory
     * @throws IllegalArgumentException if row > 5, column > 8 or > inventory size
     */
    public void set(@Nonnegative int row, @Nonnegative int column, IntelligentItem item) throws IllegalArgumentException {
        if (row > 5) {
            throw new IllegalArgumentException("The row must not be larger than 5.");
        }
        if (column > 8) {
            throw new IllegalArgumentException("The column must not be larger than 9.");
        }

        int slot = row * 9 + column;

        if (slot > this.inventory.size()) {
            throw new IllegalArgumentException("The slot must not be larger than the inventory size.");
        }

        set(slot, item);
    }

    /**
     * Fetches a intelligent ItemStack based on the slot.
     *
     * @param slot The slot
     * @return The intelligent ItemStack or an empty Optional instance.
     * @throws IllegalArgumentException if slot > 53
     */
    public Optional<IntelligentItem> get(@Nonnegative int slot) throws IllegalArgumentException {
        if (slot > 53) {
            throw new IllegalArgumentException("The slot must not be larger than 53.");
        }
        if (this.pagination.getPermanentItems().containsKey(slot))
            return Optional.ofNullable(this.pagination.getPermanentItems().get(slot));


        int page = this.pagination.page() - 1;

        if (!this.pagination.getPageItems().containsKey(page)) return Optional.empty();
        return Optional.ofNullable(this.pagination.getPageItems().get(page).get(slot));
    }

    /**
     * Get a intelligent ItemStack based on the row and column.
     *
     * @param row    The row
     * @param column The column
     * @return The intelligent ItemStack or an empty Optional instance.
     * @throws IllegalArgumentException if row > 5 or column > 8
     */
    public Optional<IntelligentItem> get(@Nonnegative int row, @Nonnegative int column) throws IllegalArgumentException {
        if (row > 5) {
            throw new IllegalArgumentException("The row must not be larger than 5.");
        }
        if (column > 8) {
            throw new IllegalArgumentException("The column must not be larger than 9.");
        }
        return get(row * 9 + column);
    }


    /**
     * Get all intelligent ItemStacks.
     *
     * @param slots All slots where you want to watch.
     * @return All intelligent ItemStacks that could be found.
     */
    public List<IntelligentItem> get(List<Integer> slots) {
        List<IntelligentItem> items = new ArrayList<>();
        slots.forEach(slot -> get(slot).ifPresent(items::add));
        return items;
    }

    /**
     * Get multiple Intelligent ItemStacks within the specified range.
     *
     * @param areaStart The start slot
     * @param areaStop  The end slot
     * @return All intelligent ItemStacks that could be found.
     * @throws IllegalArgumentException if areaStart, areaStop > 53
     */
    public List<IntelligentItem> getInArea(@Nonnegative int areaStart, @Nonnegative int areaStop) throws IllegalArgumentException {
        if (areaStart > 53) {
            throw new IllegalArgumentException("The areaStart must not be larger than 53.");
        }
        if (areaStop > 53) {
            throw new IllegalArgumentException("The areaStop must not be larger than 53.");
        }

        Preconditions.checkArgument(areaStart <= areaStop, "areaStop must be at least 1 greater than areaStart.");

        List<IntelligentItem> items = new ArrayList<>();
        for (int i = areaStart; i <= areaStop; i++) {
            get(i).ifPresent(items::add);
        }
        return items;
    }

    /**
     * In which slot in the inventory is the ItemStack located.
     *
     * @param index Where in the lore should the new line be located.
     * @param line  The new line in the ItemStack
     * @throws IllegalArgumentException if slot > 53 or index >= lore.size()
     * @throws IllegalStateException    if ItemStack has no ItemMeta or no Lore
     */
    @Beta
    public void updateLore(@Nonnegative int row, @Nonnegative int column, @Nonnegative int index, String line) throws IllegalArgumentException, IllegalStateException {
        updateLore(row * 9 + column, index, line);
    }

    /**
     * In which slot in the inventory is the ItemStack located.
     *
     * @param slot  In which slot in the inventory is the ItemStack located.
     * @param index Where in the lore should the new line be located.
     * @param line  The new line in the ItemStack
     * @throws IllegalArgumentException if slot > 53 or index >= lore.size()
     * @throws IllegalStateException    if ItemStack has no ItemMeta or no Lore
     */
    @Beta
    public void updateLore(@Nonnegative int slot, @Nonnegative int index, String line) throws IllegalArgumentException, IllegalStateException {
        if (slot > 53) {
            throw new IllegalArgumentException("The slot must not be larger than 53.");
        }
        Optional<IntelligentItem> itemOptional = get(slot);
        if (!itemOptional.isPresent()) return;

        IntelligentItem item = itemOptional.get();
        ItemStack itemStack = item.getItemStack();
        if (!itemStack.hasItemMeta()) {
            throw new IllegalStateException("ItemStack has no ItemMeta");
        }
        if (!itemStack.getItemMeta().hasLore()) {
            throw new IllegalStateException("ItemStack has no lore");
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = itemMeta.getLore();

        if (index >= lore.size()) {
            throw new IllegalArgumentException("The index must not be larger than " + lore.size());
        }
        lore.set(index, line);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);

        update(slot, itemStack);
    }

    /**
     * Updates the ItemStack in the same place with a new ItemStack.
     *
     * @param slot      The slot
     * @param itemStack The new ItemStack what should be displayed.
     * @throws IllegalArgumentException if slot > 53
     */
    public void update(@Nonnegative int slot, ItemStack itemStack) throws IllegalArgumentException {
        if (slot > 53) {
            throw new IllegalArgumentException("The slot must not be larger than 53.");
        }
        Optional<IntelligentItem> itemOptional = get(slot);
        if (!itemOptional.isPresent()) return;

        IntelligentItem item = itemOptional.get();
        IntelligentItem newItem = item.update(itemStack);

        if (this.pagination.getPageItems().get(this.pagination.page() - 1).containsKey(slot)) {
            this.pagination.getPageItems().get(this.pagination.page() - 1).put(slot, newItem);
        } else {
            set(slot, newItem);
        }
        Optional<Inventory> inventoryOptional = this.inventory.inventoryBasedOnOption(this.player.getUniqueId());
        inventoryOptional.ifPresent(savedInventory -> savedInventory.setItem(slot, newItem.getItemStack()));
    }

    /**
     * Updates the ItemStack in the same place with a new IntelligentItem.
     *
     * @param slot            The slot
     * @param intelligentItem The new IntelligentItem what should be displayed.
     * @throws IllegalArgumentException if slot > 53
     */
    public void update(@Nonnegative int slot, IntelligentItem intelligentItem) throws IllegalArgumentException {
        if (slot > 53) {
            throw new IllegalArgumentException("The slot must not be larger than 53.");
        }
        Optional<IntelligentItem> itemOptional = get(slot);
        if (!itemOptional.isPresent()) return;

        IntelligentItem item = itemOptional.get();
        IntelligentItem newItem = item.update(intelligentItem);

        if (this.pagination.getPageItems().get(this.pagination.page() - 1).containsKey(slot)) {
            this.pagination.getPageItems().get(this.pagination.page() - 1).put(slot, newItem);
        } else {
            set(slot, newItem);
        }
        Optional<Inventory> inventoryOptional = this.inventory.inventoryBasedOnOption(this.player.getUniqueId());
        inventoryOptional.ifPresent(savedInventory -> savedInventory.setItem(slot, newItem.getItemStack()));
    }

    /**
     * Updates the ItemStack in the same place with a new ItemStack.
     *
     * @param slots     The slots
     * @param itemStack The new ItemStack what should be displayed.
     */
    public void update(List<Integer> slots, ItemStack itemStack) {
        slots.forEach(slot -> {
            Optional<IntelligentItem> itemOptional = get(slot);
            if (!itemOptional.isPresent()) return;

            IntelligentItem item = itemOptional.get();
            IntelligentItem newItem = item.update(itemStack);


            if (this.pagination.getPageItems().get(this.pagination.page() - 1).containsKey(slot)) {
                this.pagination.getPageItems().get(this.pagination.page() - 1).put(slot, newItem);
            } else {
                set(slot, newItem);
            }

            Optional<Inventory> inventoryOptional = this.inventory.inventoryBasedOnOption(this.player.getUniqueId());
            inventoryOptional.ifPresent(savedInventory -> savedInventory.setItem(slot, newItem.getItemStack()));
        });
    }

    /**
     * Updates item and puts it in a new place in the inventory.
     *
     * @param itemSlot  The slot from the old ItemStack
     * @param newSlot   The slot where the new ItemStack will be placed.
     * @param itemStack The new ItemStack what should be displayed.
     * @throws IllegalArgumentException if itemSlot > 53 or newSlot > 53
     */
    public void update(@Nonnegative int itemSlot, @Nonnegative int newSlot, ItemStack itemStack) throws IllegalArgumentException {
        if (itemSlot > 53) {
            throw new IllegalArgumentException("The itemSlot must not be larger than 53.");
        }
        if (newSlot > 53) {
            throw new IllegalArgumentException("The newSlot must not be larger than 53.");
        }

        Optional<IntelligentItem> itemOptional = get(itemSlot);
        if (!itemOptional.isPresent()) return;

        IntelligentItem item = itemOptional.get();
        IntelligentItem newItem = item.update(itemStack);

        if (this.pagination.getPageItems().get(this.pagination.page() - 1).containsKey(itemSlot)) {
            this.pagination.getPageItems().get(this.pagination.page() - 1).put(newSlot, newItem);
        } else {
            set(newSlot, newItem);
        }


        Optional<Inventory> inventoryOptional = this.inventory.inventoryBasedOnOption(this.player.getUniqueId());
        inventoryOptional.ifPresent(savedInventory -> {
            savedInventory.setItem(itemSlot, null);
            savedInventory.setItem(newSlot, newItem.getItemStack());
        });
    }

    /**
     * The pagination of the inventory.
     *
     * @return The pagination
     */
    public Pagination pagination() {
        return this.pagination;
    }

    /**
     * The SlotIterator of the inventory.
     *
     * @return null if SlotIterator is not defined
     */
    public SlotIterator iterator() {
        return this.pagination.getSlotIterator();
    }

    protected void transferData(InventoryContents transferTo) {
        if (this.data.isEmpty()) return;

        this.data.forEach(transferTo::setData);
    }

    private void remove(@Nonnegative int i) {
        this.pagination.getPermanentItems().remove(i);
        this.pagination.getPageItems().get(this.pagination.page() - 1).remove(i);
    }

    protected Optional<IntelligentItem> getInPage(@Nonnegative int page, @Nonnegative int slot) throws IllegalArgumentException {
        if (slot > 53) {
            throw new IllegalArgumentException("The slot must not be larger than 53.");
        }
        if (this.pagination.getPermanentItems().containsKey(slot))
            return Optional.ofNullable(this.pagination.getPermanentItems().get(slot));


        if (!this.pagination.getPageItems().containsKey(page)) return Optional.empty();
        return Optional.ofNullable(this.pagination.getPageItems().get(page).get(slot));
    }

}
