package at.haha007.edenpipes;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Piston;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Comparator;
import java.util.HashMap;

public final class EdenPipesPlugin extends JavaPlugin implements Listener {
    public static EdenPipesPlugin INSTANCE;

    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new DefaultFilterListener(), this);
        getServer().getPluginManager().registerEvents(new CraftbookFilterListener(), this);
    }

    @EventHandler
    public void onRedstoneUpdate(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        Block[] blocks = new Block[]{
                block.getRelative(0, 0, 1),
                block.getRelative(0, 0, -1),
                block.getRelative(1, 0, 0),
                block.getRelative(-1, 0, 0),
                block.getRelative(0, 1, 0),
                block.getRelative(0, -1, 0)
        };
        for (Block b : blocks) {
            if (b.getType() != Material.STICKY_PISTON)
                continue;
            startPipe(b);
        }
    }

    private void startPipe(Block sourcePistonBlock) {
        BlockData blockData = sourcePistonBlock.getBlockData();
        if (!(blockData instanceof Piston sourcePiston))
            return;
        Block sourceInventoryBlock = sourcePistonBlock.getRelative(sourcePiston.getFacing());
        BlockState state = sourceInventoryBlock.getState(false);
        if (!(state instanceof Container container))
            return;
        Inventory sourceInventory = container.getInventory();
        ItemStack item = null;
        int sourceItemIndex = 0;
        for (int i = 0; i < sourceInventory.getSize(); i++) {
            item = sourceInventory.getItem(i);
            sourceItemIndex = i;
            if (item != null)
                break;
        }
        if (item == null)
            return;
        PipePullEvent pullEvent = new PipePullEvent(sourceInventoryBlock, sourcePistonBlock, item);
        getServer().getPluginManager().callEvent(pullEvent);
        if (pullEvent.isCancelled())
            return;
        PipePathfinder pipePathfinder = new PipePathfinder(sourcePistonBlock, sourceInventoryBlock, item);
        PipePathfinder.PathResult pathResult = pipePathfinder.findNext();
        while (pathResult.state() == PipePathfinder.State.FOUND_TARGET) {
            Block targetPistonBlock = pathResult.targetPiston();
            Piston targetPiston = (Piston) targetPistonBlock.getBlockData();
            Block targetInventoryBlock = targetPistonBlock.getRelative(targetPiston.getFacing());
            BlockState targetState = targetInventoryBlock.getState(false);
            if (!(targetState instanceof Container targetContainer))
                return;
            PipePutEvent putEvent = new PipePutEvent(sourceInventoryBlock, targetInventoryBlock, sourcePistonBlock, targetPistonBlock, item);
            getServer().getPluginManager().callEvent(putEvent);
            if (putEvent.isCancelled()) {
                pathResult = pipePathfinder.findNext();
                continue;
            }
            HashMap<Integer, ItemStack> remaining = targetContainer.getInventory().addItem(item);
            ItemStack min = remaining.values().stream().min(Comparator.comparingInt(ItemStack::getAmount)).orElse(null);
            sourceInventory.setItem(sourceItemIndex, min);
            if (min == null) return;
            item.setAmount(min.getAmount());
            pathResult = pipePathfinder.findNext();
        }
    }
}






























