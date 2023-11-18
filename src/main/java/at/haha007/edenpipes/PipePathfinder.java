package at.haha007.edenpipes;

import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Piston;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

/**
 * Helper class for bfs search
 */
public class PipePathfinder {
    public enum State {
        FOUND_TARGET,
        NO_TARGET
    }

    public record PathResult(State state, Block targetPiston, Set<Block> visited) {
    }

    private final Queue<Block> frontier = new LinkedList<>();
    private final Set<Block> visited = new HashSet<>();

    public PipePathfinder(@NotNull Block source) {
        getNeighbors(source).forEach(this::enqueue);
    }

    @NotNull
    public PathResult findNext() {
        Block block;
        while (!frontier.isEmpty()) {
            block = advance();
            if (block != null)
                return new PathResult(State.FOUND_TARGET, block, visited);
        }
        return new PathResult(State.NO_TARGET, null, visited);
    }

    //step 1 -> get all valid neighbors and enqueue them
    //step 2 -> if the source is a piston, and its targetPiston is a container return the block
    private Block advance() {
        Block block = frontier.poll();
        assert block != null;
        Material material = block.getType();

        if (material == Material.PISTON || material == Material.GLASS) {
            getNeighbors(block).forEach(this::enqueue);
        }else if(MaterialTags.STAINED_GLASS.isTagged(material)){
            getNeighbors(block).stream()
                    .filter(b -> b.getType() == Material.PISTON || b.getType() == Material.GLASS || b.getType() == material)
                    .forEach(this::enqueue);
        }

        //if piston check targetPiston block for container
        if (material == Material.PISTON) {
            Block target = block.getRelative(((Piston) block.getBlockData()).getFacing());
            if (!target.getChunk().isLoaded()) return null;
            BlockState targetState = target.getState(false);
            if (targetState instanceof Container) {
                return block;
            }
        }
        return null;
    }

    private List<Block> getNeighbors(Block block) {
        return Stream.of(
                        block.getRelative(0, 0, 1),
                        block.getRelative(0, 0, -1),
                        block.getRelative(1, 0, 0),
                        block.getRelative(-1, 0, 0),
                        block.getRelative(0, 1, 0),
                        block.getRelative(0, -1, 0))
                //Filter out unloaded chunks and invalid blocks
                .filter(b -> {
                    if (!b.getChunk().isLoaded()) return false;
                    Material material = b.getType();
                    return material == Material.GLASS || material == Material.PISTON || MaterialTags.STAINED_GLASS.isTagged(material);
                })
                //Filter out blocks that are already visited
                .filter(b -> !visited.contains(b))
                //Filter out blocks that are prohibited by piston direction
                .filter(b -> {
                    if (b.getType() != Material.PISTON) return true;
                    Piston piston = (Piston) b.getBlockData();
                    return !b.getRelative(piston.getFacing()).equals(block);
                })
                .toList();
    }

    private void enqueue(Block block) {
        if (visited.contains(block)) return;
        visited.add(block);
        if (block.getChunk().isLoaded())
            frontier.add(block);
    }
}
