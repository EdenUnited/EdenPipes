package at.haha007.edenpipes;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The PipePutEvent class is a custom event class that extends the Event class from the Bukkit API.
 * It represents an event where a pipe puts items into a container, and provides information about the container block, piston block, item being put, and the path the item takes.
 * This event is fired when an item is put into a container or into another pipe, basically every time a item passes a piston.
 */
@Getter
public class PipePutEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    //Used by Bukkit, needed by every class that extends Event
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private final Block sourceContainer;
    private final Block targetContainer;
    private final Block sourcePiston;
    private final Block targetPiston;
    private final ItemStack item;
    @Setter
    private boolean cancelled = false;

    public PipePutEvent(Block sourceContainer, Block targetContainer, Block sourcePiston, Block targetPiston, ItemStack item) {
        this.sourceContainer = sourceContainer;
        this.targetContainer = targetContainer;
        this.sourcePiston = sourcePiston;
        this.targetPiston = targetPiston;
        this.item = item;
    }
}
