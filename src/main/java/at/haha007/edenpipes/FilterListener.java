package at.haha007.edenpipes;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class FilterListener implements Listener {
    private static final NamespacedKey USE_EXACT_KEY = new NamespacedKey(EdenPipesPlugin.INSTANCE, "exact");
    private static final NamespacedKey USE_AS_WHITE_LIST = new NamespacedKey(EdenPipesPlugin.INSTANCE, "white");
    private static final NamespacedKey MATERIAL_KEY = new NamespacedKey(EdenPipesPlugin.INSTANCE, "material");
    private static final NamespacedKey EXACT_KEY = new NamespacedKey(EdenPipesPlugin.INSTANCE, "material");


    @EventHandler
    void onPipePut(PipePutEvent event) {
        Block piston = event.getTargetPiston();
        ItemStack item = event.getItem();
        byte[] exactBytes = item.serializeAsBytes();
        Material material = item.getType();
        boolean matches = getAttachedSigns(piston)
                .map(s -> s.matches(exactBytes, material))
                .collect(() -> new AtomicBoolean(true), (a, b) -> a.set(a.get() && b), (a, b) -> a.set(a.get() && b.get())).get();
        if(!matches)
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    void onPipePull(PipePullEvent event) {
        Block piston = event.getPiston();
        ItemStack item = event.getItem();
        byte[] exactBytes = item.serializeAsBytes();
        Material material = item.getType();
        boolean matches = getAttachedSigns(piston)
                .map(s -> s.matches(exactBytes, material))
                .collect(() -> new AtomicBoolean(true), (a, b) -> a.set(a.get() && b), (a, b) -> a.set(a.get() && b.get())).get();
        if(!matches)
            event.setCancelled(true);
    }

    private Stream<SignData> getAttachedSigns(Block block) {
        return Arrays.stream(new Block[]{
                        block.getRelative(0, 0, 1),
                        block.getRelative(0, 0, -1),
                        block.getRelative(1, 0, 0),
                        block.getRelative(-1, 0, 0),
                        block.getRelative(0, 1, 0),
                        block.getRelative(0, -1, 0)
                }).map(this::getSignDataSilent)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(s -> s.getAttachedTo().equals(block));
    }

    private Optional<SignData> getSignDataSilent(Block block) {
        try {
            return Optional.of(new SignData(block));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static class SignData {
        private final org.bukkit.block.data.type.Sign data;
        private final Sign state;

        public SignData(Block block) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.type.Sign))
                throw new IllegalArgumentException("Block is not a Sign");
            if (!(block.getState(false) instanceof Sign))
                throw new IllegalArgumentException("Block is not a Sign");
            this.data = (org.bukkit.block.data.type.Sign) block.getBlockData();
            this.state = (Sign) block.getState(false);
        }

        public Block getAttachedTo() {
            return state.getBlock().getRelative(data.getRotation());
        }

        public Boolean matches(byte[] exactBytes, Material material) {
            PersistentDataContainer pdc = state.getPersistentDataContainer();
            if (!pdc.has(USE_EXACT_KEY) || !pdc.has(USE_AS_WHITE_LIST))
                return true;
            Boolean useAsWhiteList = pdc.get(USE_AS_WHITE_LIST, PersistentDataType.BOOLEAN);
            useAsWhiteList = useAsWhiteList == null || useAsWhiteList;
            Boolean useExact = pdc.get(USE_EXACT_KEY, PersistentDataType.BOOLEAN);

            if (useExact == null || useExact) {
                PersistentDataContainer[] tagArray = pdc.get(EXACT_KEY, PersistentDataType.TAG_CONTAINER_ARRAY);
                if (tagArray == null)
                    return !useAsWhiteList;

                if (useAsWhiteList) {
                    for (PersistentDataContainer exactPdc : tagArray) {
                        byte[] bytes = exactPdc.get(EXACT_KEY, PersistentDataType.BYTE_ARRAY);
                        if (Arrays.equals(bytes, exactBytes))
                            return true;
                    }
                    return false;
                } else {
                    boolean found = false;
                    for (PersistentDataContainer exactPdc : tagArray) {
                        byte[] bytes = exactPdc.get(EXACT_KEY, PersistentDataType.BYTE_ARRAY);
                        if (Arrays.equals(bytes, exactBytes)) {
                            found = true;
                            break;
                        }
                    }
                    return !found;
                }

            } else {
                String typeName = material.name();
                PersistentDataContainer[] tagArray = pdc.get(MATERIAL_KEY, PersistentDataType.TAG_CONTAINER_ARRAY);
                if (tagArray == null)
                    return !useAsWhiteList;
                if (useAsWhiteList) {
                    for (PersistentDataContainer exactPdc : tagArray) {
                        String bytes = exactPdc.get(MATERIAL_KEY, PersistentDataType.STRING);
                        if (typeName.equals(bytes))
                            return true;
                    }
                    return false;
                } else {
                    boolean found = false;
                    for (PersistentDataContainer exactPdc : tagArray) {
                        String bytes = exactPdc.get(MATERIAL_KEY, PersistentDataType.STRING);
                        if (typeName.equals(bytes)) {
                            found = true;
                            break;
                        }
                    }
                    return !found;
                }

            }
        }
    }

}
