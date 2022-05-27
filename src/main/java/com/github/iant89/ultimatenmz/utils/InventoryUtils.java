package com.github.iant89.ultimatenmz.utils;

import net.runelite.api.*;

public class InventoryUtils {

    public static boolean hasItem(Client client, int itemId) {
        ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);

        if (container != null) {
            for (Item item : container.getItems()) {
                if(item.getId() == itemId) {
                    return true;
                }
            }
        }

        return false;
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
