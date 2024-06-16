package at.haha007.edenpipes;

import io.papermc.paper.event.player.PlayerOpenSignEvent;
import lombok.Getter;
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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class DefaultFilterListener implements Listener {
    private static final NamespacedKey USE_EXACT_KEY = new NamespacedKey(EdenPipesPlugin.INSTANCE, "use_exact");
    private static final NamespacedKey USE_AS_WHITE_LIST = new NamespacedKey(EdenPipesPlugin.INSTANCE, "whitelist");
    private static final NamespacedKey ITEMS = new NamespacedKey(EdenPipesPlugin.INSTANCE, "items");

    @EventHandler
    void onPlayerOpenSign(PlayerOpenSignEvent event) {
        Optional<SignData> data = getSignDataSilent(event.getSign().getBlock());
        if (data.isEmpty())
            return;
        new FilterUi(data.get());
    }

    @EventHandler
    void onPipePut(PipePutEvent event) {
        Block piston = event.getTargetPiston();
        ItemStack item = event.getItem();
        boolean matches = getAttachedSigns(piston)
                .map(s -> s.matches(item))
                .collect(() -> new AtomicBoolean(true), (a, b) -> a.set(a.get() && b), (a, b) -> a.set(a.get() && b.get())).get();
        if (!matches)
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    void onPipePull(PipePullEvent event) {
        Block piston = event.getPiston();
        ItemStack item = event.getItem();
        boolean matches = getAttachedSigns(piston)
                .map(s -> s.matches(item))
                .collect(() -> new AtomicBoolean(true), (a, b) -> a.set(a.get() && b), (a, b) -> a.set(a.get() && b.get())).get();
        if (!matches)
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

    public static class SignData {
        private final org.bukkit.block.data.type.Sign data;
        @Getter
        private final Sign state;
        @Getter
        private final Set<ItemStack> filterItems = new HashSet<>();
        @Getter
        private final Set<Material> filterMaterials = new HashSet<>();
        @Getter
        private boolean useExact;
        @Getter
        private boolean useAsWhiteList;

        public SignData(Block block) {
            if (!(block.getBlockData() instanceof org.bukkit.block.data.type.Sign))
                throw new IllegalArgumentException("Block is not a Sign");
            if (!(block.getState(false) instanceof Sign))
                throw new IllegalArgumentException("Block is not a Sign");
            this.data = (org.bukkit.block.data.type.Sign) block.getBlockData();
            this.state = (Sign) block.getState(false);
            update();
        }

        public void update() {
            PersistentDataContainer pdc = state.getPersistentDataContainer();
            useExact = pdc.getOrDefault(USE_EXACT_KEY, PersistentDataType.BOOLEAN, true);
            useAsWhiteList = pdc.getOrDefault(USE_AS_WHITE_LIST, PersistentDataType.BOOLEAN, true);
            PersistentDataContainer[] itemContainers = pdc.getOrDefault(ITEMS, PersistentDataType.TAG_CONTAINER_ARRAY, new PersistentDataContainer[0]);
            filterItems.clear();
            for (PersistentDataContainer itemContainer : itemContainers) {
                byte[] encodedItem = itemContainer.getOrDefault(ITEMS, PersistentDataType.BYTE_ARRAY, new byte[0]);
                ItemStack itemStack = ItemStack.deserializeBytes(encodedItem);
                filterMaterials.add(itemStack.getType());
                filterItems.add(itemStack);
            }
        }

        public Block getAttachedTo() {
            return state.getBlock().getRelative(data.getRotation());
        }

        public void saveToSign() {
            PersistentDataContainer pdc = state.getPersistentDataContainer();
            pdc.set(USE_EXACT_KEY, PersistentDataType.BOOLEAN, useExact);
            pdc.set(USE_AS_WHITE_LIST, PersistentDataType.BOOLEAN, useAsWhiteList);
            PersistentDataContainer[] itemContainers = filterItems.stream().map(i -> {
                PersistentDataContainer itemContainer = pdc.getAdapterContext().newPersistentDataContainer();
                itemContainer.set(ITEMS, PersistentDataType.BYTE_ARRAY, i.serializeAsBytes());
                return itemContainer;
            }).toArray(PersistentDataContainer[]::new);
            pdc.set(ITEMS, PersistentDataType.TAG_CONTAINER_ARRAY, itemContainers);
        }

        public boolean matches(ItemStack itemStack) {
            if (useAsWhiteList) {
                if(useExact)
                    return filterItems.contains(itemStack);
                else
                    return filterMaterials.contains(itemStack.getType());
            }else{
                if(useExact)
                    return !filterItems.contains(itemStack);
                else
                    return !filterMaterials.contains(itemStack.getType());
            }
        }
    }

}
