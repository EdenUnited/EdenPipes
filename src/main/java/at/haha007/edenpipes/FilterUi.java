package at.haha007.edenpipes;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

//EXACT/MATERIAL match
//ADD SINGLE ITEMS OR FROM COLLECTIONS
//WHITELIST/BLACKLIST

public class FilterUi {
    private final FilterListener.SignData signData;
    private ChestGui mainGui;
    private AnvilGui mainGuiSearch;
    private ChestGui collectionGui;
    private AnvilGui collectionSearch;

    public FilterUi(@NotNull FilterListener.SignData signData) {
        this.signData = signData;
    }

    private ChestGui mainUi() {
        ChestGui gui = new ChestGui(6, ComponentHolder.of(MiniMessage.miniMessage().deserialize("<gold><bold>Eden Pipes <yellow>Filters")));
        gui.setOnGlobalClick(e -> e.setCancelled(true));
        return gui;
    }

    private void buildMainUi(ChestGui gui) {
        gui.getPanes().clear();
        PaginatedPane pages = new PaginatedPane(9,6);
        Function<Integer, Pane> createPane = i -> {
            StaticPane pane = new StaticPane(9,6);

            List<Component> infoText = List.of(
                    text("<gold><bold>Click items in your inventory to add them to the filter"),
                    text("<gold><bold>Click items in your collection to remove them from the filter")
            );
            GuiItem info = new GuiItem(editMeta(new ItemStack(Material.BOOK), m -> {
                m.displayName(text("<green>----<gold>INFO<green>----"));
                m.lore(infoText);
            }), e -> {
                e.getWhoClicked().sendMessage(text("<green>----<gold>INFO<green>----"));
                infoText.forEach(e1 -> e.getWhoClicked().sendMessage(e1));
            });
            return pane;
        };
    }

    private AnvilGui searchUI(){
        AnvilGui gui = new AnvilGui(ComponentHolder.of(MiniMessage.miniMessage().deserialize("<gold><bold>Search <yellow>Filter")));
        gui.setOnGlobalClick(e -> e.setCancelled(true));
        return gui;
    }

    private ChestGui collectionUI() {
        ChestGui gui = new ChestGui(6, ComponentHolder.of(MiniMessage.miniMessage().deserialize("<gold><bold>Eden Pipes <yellow>Collections")));
        gui.setOnGlobalClick(e -> e.setCancelled(true));
        return gui;
    }

    private AnvilGui searchCollectionUI(){
        AnvilGui gui = new AnvilGui(ComponentHolder.of(MiniMessage.miniMessage().deserialize("<gold><bold>Search <yellow>Collection")));
        gui.setOnGlobalClick(e -> e.setCancelled(true));
        return gui;
    }

    private ItemStack editMeta(ItemStack itemStack, Consumer<ItemMeta> consumer) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        consumer.accept(itemMeta);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private Component text(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }
}
