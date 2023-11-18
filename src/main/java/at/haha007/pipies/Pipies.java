package at.haha007.pipies;

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

public final class Pipies extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onRedstoneUpdate(BlockRedstoneEvent event) {
        System.out.println("Redstone update: " + event.getBlock());
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
        System.out.println("Starting pipe: " + sourcePistonBlock);
        BlockData blockData = sourcePistonBlock.getBlockData();
        if (!(blockData instanceof Piston sourcePiston))
            return;
        System.out.println("Piston: " + sourcePiston);
        Block sourceInventoryBlock = sourcePistonBlock.getRelative(sourcePiston.getFacing());
        BlockState state = sourceInventoryBlock.getState(false);
        if (!(state instanceof Container container))
            return;
        System.out.println("Container: " + container);
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
        System.out.println("Item: " + item);
        PipePathfinder pipePathfinder = new PipePathfinder(sourcePistonBlock);
        PipePathfinder.PathResult pathResult = pipePathfinder.findNext();
        System.out.println("Path result: " + pathResult.targetPiston());
        while (pathResult != null) {
            Block targetPistonBlock = pathResult.targetPiston();
            Piston targetPiston = (Piston) targetPistonBlock.getBlockData();
            Block targetInventoryBlock = targetPistonBlock.getRelative(targetPiston.getFacing());
            BlockState targetState = targetInventoryBlock.getState(false);
            System.out.println("Target block" + targetInventoryBlock);
            if (!(targetState instanceof Container targetContainer))
                return;
            PipePutEvent event = new PipePutEvent(sourceInventoryBlock, targetInventoryBlock, sourcePistonBlock, targetPistonBlock, item);
            getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                pathResult = pipePathfinder.findNext();
                continue;
            }
            HashMap<Integer, ItemStack> remaining = targetContainer.getInventory().addItem(item);
            ItemStack min = remaining.values().stream().min(Comparator.comparingInt(ItemStack::getAmount)).orElse(null);
            System.out.println("Remaining: " + min);
            sourceInventory.setItem(sourceItemIndex, min);
            if (min == null) return;
            item = min;
            pathResult = pipePathfinder.findNext();
        }
    }
}






























