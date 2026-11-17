package com.shahromali.opl_gg_msa.item;

import com.shahromali.opl_gg_msa.OplGgMsa;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the mod's items and puts them in the vanilla creative tabs. */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OplGgMsa.MODID);

    public static final DeferredItem<RedSwordItem> RED_SWORD =
            ITEMS.registerItem("red_sword", RedSwordItem::new, toolProperties());
    public static final DeferredItem<BlueAxeItem> BLUE_AXE =
            ITEMS.registerItem("blue_axe", BlueAxeItem::new, toolProperties());
    public static final DeferredItem<GreenHoeItem> GREEN_HOE =
            ITEMS.registerItem("green_hoe", GreenHoeItem::new, toolProperties());

    private ModItems() {
    }

    private static Item.Properties toolProperties() {
        return new Item.Properties().fireResistant().rarity(Rarity.EPIC);
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(ModItems::addToCreativeTabs);
    }

    private static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(RED_SWORD);
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(BLUE_AXE);
            event.accept(GREEN_HOE);
        }
    }
}
