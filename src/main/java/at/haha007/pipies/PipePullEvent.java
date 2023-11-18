package at.haha007.pipies;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The PipePullEvent class is a custom event class that extends the Event class from the Bukkit API.
 * It represents an event where a pipe pulls items from a container, and provides information about the container block, piston block, and item being pulled.
 */
public class PipePullEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * Get the handler list for this event. {@link HandlerList}.
     *
     * @return the handler list
     */
    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * Get the handler list for this event. {@link HandlerList#getHandlerLists()}.
     *
     * @return the handler list
     */
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private final Block container;
    private final Block piston;
    private final ItemStack item;
    private boolean cancelled = false;

    public PipePullEvent(Block container, Block piston, ItemStack item) {
        this.container = container;
        this.piston = piston;
        this.item = item;
    }

    /**
     * Get the container block.
     *
     * @return the container block
     */
    public Block getContainer() {
        return container;
    }

    /**
     * Get the piston block.
     *
     * @return the piston block
     */
    public Block getPiston() {
        return piston;
    }

    /**
     * Get the item being pulled.
     *
     * @return the item
     */
    public ItemStack getItem() {
        return item;
    }

    /**
     * Check if the event is cancelled.
     *
     * @return true if the event is cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Set if the event is cancelled.
     *
     * @param cancelled true if the event is cancelled
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
