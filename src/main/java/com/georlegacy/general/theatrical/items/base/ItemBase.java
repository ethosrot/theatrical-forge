package com.georlegacy.general.theatrical.items.base;

import com.georlegacy.general.theatrical.entities.core.IHasModel;
import com.georlegacy.general.theatrical.init.TheatricalItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/**
 * Base for item creation
 *
 * @author James Conway
 */
public class ItemBase extends Item implements IHasModel {

    public ItemBase(String name) {
        setUnlocalizedName(name);
        setRegistryName(name);
        setCreativeTab(CreativeTabs.REDSTONE);

        TheatricalItems.ITEMS.add(this);
    }

    @Override
    public void registerModels() {
        TheatricalItems.registerItemRenderer(this, 0, "inventory");
    }

}