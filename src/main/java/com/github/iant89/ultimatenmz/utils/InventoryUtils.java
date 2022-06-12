package com.github.iant89.ultimatenmz.utils;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

public class InventoryUtils {

    public static boolean hasItem(Client client, int itemId) {
        return hasOneOfItems(client, itemId);
    }

    public static boolean hasOneOfItems(Client client, int... ids) {
        ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);

        if (container != null) {
            for (Item item : container.getItems()) {
                for(int id : ids) {
                    if(item.getId() == id) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
