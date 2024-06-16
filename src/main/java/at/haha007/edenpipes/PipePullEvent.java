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
 * The PipePullEvent class is a custom event class that extends the Event class from the Bukkit API.
 * It represents an event where a pipe pulls items from a container, and provides information about the container block, piston block, and item being pulled.
 */
@Getter
public class PipePullEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @SuppressWarnings("unused") //Used by Bukkit, needed by every class that extends Event
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private final Block container;
    private final Block piston;
    private final ItemStack item;
    @Setter
    private boolean cancelled = false;

    public PipePullEvent(Block container, Block piston, ItemStack item) {
        this.container = container;
        this.piston = piston;
        this.item = item;
    }
}
