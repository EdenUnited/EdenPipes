package at.haha007.edenpipes;

import lombok.Getter;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.HangingSign;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CraftbookFilterListener implements Listener {
    private final Map<String, String> cbVariables = new HashMap<>();
    private final boolean hasCraftBook;

    public CraftbookFilterListener() {
        File pluginFolder = Bukkit.getPluginsFolder();
        File craftBookFolder = new File(pluginFolder, "CraftBook");
        File variablesFile = new File(craftBookFolder, "variables.yml");
        hasCraftBook = variablesFile.exists();
        if (!hasCraftBook)
            return;
        ConfigurationSection config = YamlConfiguration.loadConfiguration(variablesFile);
        config = config.getConfigurationSection("variables");
        if (config == null)
            return;
        for (String key : config.getKeys(false)) {
            if (!key.startsWith("global|"))
                continue;
            key = key.replaceFirst("global|", "");
            if (key.isEmpty())
                continue;
            cbVariables.put("%" + key + "%", config.getString(key));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    void onPipePut(PipePutEvent event) {
        if (!hasCraftBook)
            return;
        Block piston = event.getTargetPiston();
        ItemStack item = event.getItem();
        Optional<SignData> sign = getAttachedSigns(piston).filter(this::isPipeSign).findFirst();
        if (sign.isEmpty())
            return;
        String[] lines = sign.get().getLines();
        String[] whitelist = lines[2].toLowerCase().split(",");
        if (!matches(item, whitelist, true)) {
            event.setCancelled(true);
            return;
        }
        String[] blacklist = lines[3].toLowerCase().split(",");
        if (matches(item, blacklist, false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    void onPipePull(PipePullEvent event) {
        if (!hasCraftBook)
            return;
        Block piston = event.getPiston();
        ItemStack item = event.getItem();
        Optional<SignData> sign = getAttachedSigns(piston).filter(this::isPipeSign).findFirst();
        if (sign.isEmpty())
            return;
        String[] lines = sign.get().getLines();
        String[] whitelist = lines[2].toLowerCase().split(",");
        if (!matches(item, whitelist, true)) {
            event.setCancelled(true);
            return;
        }
        String[] blacklist = lines[3].toLowerCase().split(",");
        if (matches(item, blacklist, false)) {
            event.setCancelled(true);
        }
    }

    private boolean matches(ItemStack item, String[] strings, boolean defaultValue) {
        if (strings.length == 0)
            return defaultValue;
        if (item.hasItemMeta())
            return !defaultValue;
        String type = item.getType().name().toLowerCase();
        for (String s : strings) {
            if (s.equals(type)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPipeSign(SignData signData) {
        return signData.getLine(1).equals("[Pipe]");
    }

    private Stream<CraftbookFilterListener.SignData> getAttachedSigns(Block block) {
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

    private Optional<CraftbookFilterListener.SignData> getSignDataSilent(Block block) {
        try {
            return Optional.of(new CraftbookFilterListener.SignData(block));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private class SignData {
        private final BlockFace facing;
        @Getter
        private final String[] lines;
        private final Sign state;

        public SignData(Block block) {
            BlockData blockData = block.getBlockData();
            if (!(block.getState(false) instanceof Sign))
                throw new IllegalArgumentException("Block is not a Sign");
            if (blockData instanceof HangingSign) {
                facing = BlockFace.UP;
            } else if (blockData instanceof Sign) {
                facing = BlockFace.DOWN;
            } else if (blockData instanceof WallSign sign) {
                facing = sign.getFacing().getOppositeFace();
            } else {
                throw new IllegalArgumentException("Block is not a Sign");
            }

            this.state = (Sign) block.getState(false);
            lines = state.getSide(Side.FRONT).lines()
                    .stream()
                    .map(c -> PlainTextComponentSerializer.plainText().serialize(c))
                    .toArray(String[]::new);

            lines[2] = Arrays.stream(lines[2].split(","))
                    .flatMap(s -> Arrays.stream(cbVariables.getOrDefault(s, s).split(",")))
                    .collect(Collectors.joining(","));

            lines[3] = Arrays.stream(lines[3].split(","))
                    .flatMap(s -> s.startsWith("%") && s.endsWith("%") ? Arrays.stream(cbVariables.getOrDefault(s, s).split(",")) : Stream.of(s))
                    .collect(Collectors.joining(","));
        }

        public Block getAttachedTo() {
            return state.getBlock().getRelative(facing);
        }

        public String getLine(int index) {
            return lines[index];
        }
    }
}
