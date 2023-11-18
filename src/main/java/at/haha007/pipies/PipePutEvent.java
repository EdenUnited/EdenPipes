package at.haha007.pipies;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The PipePutEvent class is a custom event class that extends the Event class from the Bukkit API.
 * It represents an event where a pipe puts items into a container, and provides information about the container block, piston block, item being put, and the path the item takes.
 */
public class PipePutEvent extends Event {
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

    private final Block sourceContainer;
    private final Block targetContainer;
    private final Block sourcePiston;
    private final Block targetPiston;
    private final ItemStack item;
    private boolean cancelled = false;

    public PipePutEvent(Block sourceContainer, Block targetContainer, Block sourcePiston, Block targetPiston, ItemStack item) {
        this.sourceContainer = sourceContainer;
        this.targetContainer = targetContainer;
        this.sourcePiston = sourcePiston;
        this.targetPiston = targetPiston;
        this.item = item;
    }

    /**
     * Get the container block.
     *
     * @return the container block
     */
    public Block getSourceContainer() {
        return sourceContainer;
    }

    /**
     * Get the piston block.
     *
     * @return the piston block
     */
    public Block getTargetContainer() {
        return targetContainer;
    }

    /**
     * Get the piston block.
     *
     * @return the piston block
     */
    public Block getSourcePiston() {
        return sourcePiston;
    }

    /**
     * Get the piston block.
     *
     * @return the piston block
     */
    public Block getTargetPiston() {
        return targetPiston;
    }

    /**
     * Get the item being put.
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
