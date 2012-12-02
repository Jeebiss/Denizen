package net.aufdemrand.denizen.tags.core;

import java.util.Map.Entry;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.minecraft.server.NBTTagCompound;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTags implements Listener {

    public PlayerTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void playerTags(ReplaceableTagEvent event) {

        if (!event.matches("PLAYER") || event.getPlayer() == null) return;

        Player p = event.getPlayer();
        String type = event.getType().toUpperCase();
        String subType = "";

        if (event.getType().split("\\.").length > 1) {
            type = event.getType().split("\\.")[0].toUpperCase();
            subType = event.getType().split("\\.")[1].toUpperCase();
        }

        if (type.equals("ITEM_IN_HAND")) {
            if (subType.equals("QTY"))
                event.setReplaceable(String.valueOf(p.getItemInHand().getAmount()));
            else if (subType.equals("ID"))
                event.setReplaceable(String.valueOf(p.getItemInHand().getTypeId()));
            else if (subType.equals("DURABILITY"))
                event.setReplaceable(String.valueOf(p.getItemInHand().getDurability()));
            else if (subType.equals("DATA"))
                event.setReplaceable(String.valueOf(p.getItemInHand().getData()));
            else if (subType.equals("MAX_STACK"))
                event.setReplaceable(String.valueOf(p.getItemInHand().getMaxStackSize()));
            else if (subType.equals("ENCHANTMENTS")) {
                String enchantments = "";
                int enchants = 0;
                for (Entry<Enchantment, Integer> e : p.getItemInHand().getEnchantments().entrySet()) {
                    if (enchants > 0) enchantments = enchantments + "|";
                    enchantments = enchantments +e.getKey().getName();
                    enchants++;
                }
                event.setReplaceable(enchantments);
            }
            else if (subType.equals("ENCHANTMENTS_WITH_LEVEL")) {
                String enchantments = "";
                int enchants = 0;
                for (Entry<Enchantment, Integer> e : p.getItemInHand().getEnchantments().entrySet()) {
                    if (enchants > 0) enchantments = enchantments + "|";
                    enchantments = enchantments + e.getKey().getName() + ":" + e.getValue();
                    enchants++;
                }
                event.setReplaceable(enchantments);
            }
            else if (subType.equals("LORE")) {
                String lore = "";
                int lores = 0;
                NBTTagCompound d = ((CraftItemStack) p.getItemInHand()).getHandle().getTag();
                for (lores = 0; lores < d.getList("Lore").size(); lores++) {
                    if (lores > 0) lore = lore + "|";
                    lore = lore + d.getList("Lore").get(lores);
                }
                event.setReplaceable(lore);
            }
            else if (subType.equals("NAME")) {
                String name = "";
                name = ((CraftItemStack) p.getItemInHand()).getHandle().getTag().getCompound("display").getString("Name");
                event.setReplaceable(name);
            }
            else // No subType, send back material_type
                event.setReplaceable(p.getItemInHand().getType().name());
            return;


        } else if (type.equals("NAME")) { 
            event.setReplaceable(p.getName());
            if (subType.equals("DISPLAY"))
                event.setReplaceable(p.getDisplayName());
            else if (subType.equals("LIST"))
                event.setReplaceable(p.getPlayerListName());
            return;


        } else if (type.equals("LOCATION")) {
            event.setReplaceable("x" + p.getLocation().getBlockX() 
                    + ",y" + p.getLocation().getBlockY()
                    + ",z" + p.getLocation().getBlockZ()
                    + "," + p.getWorld().getName());
            if (subType.equals("FORMATTED")) 
                event.setReplaceable("X '" + p.getLocation().getBlockX() 
                        + "', Y '" + p.getLocation().getBlockY()
                        + "', Z '" + p.getLocation().getBlockZ()
                        + "', in world '" + p.getWorld().getName() + "'");
            else if (subType.equals("STANDING_ON"))
                event.setReplaceable(p.getLocation().add(0, -1, 0).getBlock().getType().name());
            else if (subType.equals("WORLD_SPAWN"))
                event.setReplaceable("x" + p.getWorld().getSpawnLocation().getBlockX() 
                        + ",y" + p.getWorld().getSpawnLocation().getBlockY()
                        + ",z" + p.getWorld().getSpawnLocation().getBlockZ()
                        + "," + p.getWorld().getName());
            else if (subType.equals("BED_SPAWN"))
                event.setReplaceable("x" + p.getBedSpawnLocation().getBlockX() 
                        + ",y" + p.getBedSpawnLocation().getBlockY()
                        + ",z" + p.getBedSpawnLocation().getBlockZ()
                        + "," + p.getWorld().getName());
            else if (subType.equals("WORLD"))
                event.setReplaceable(p.getWorld().getName());
            return;


        } else if (type.equals("HEALTH")) {
            event.setReplaceable(String.valueOf(p.getHealth()));
            if (subType.equals("FORMATTED")) {
                int maxHealth = p.getMaxHealth();
                if (event.getType().split(".").length > 2)
                    maxHealth = Integer.valueOf(event.getType().split(".")[2]);
                if ((float)p.getHealth() / maxHealth < .10)
                    event.setReplaceable("dying");
                else if ((float) p.getHealth() / maxHealth < .40)
                    event.setReplaceable("seriously wounded");
                else if ((float) p.getHealth() / maxHealth < .75)
                    event.setReplaceable("injured");
                else if ((float) p.getHealth() / maxHealth < 1)
                    event.setReplaceable("scraped");
                else 
                    event.setReplaceable("healthy");
            } else if (subType.equals("PERCENTAGE")) {
                int maxHealth = p.getMaxHealth();
                if (event.getType().split(".").length > 2)
                    maxHealth = Integer.valueOf(event.getType().split(".")[2]);
                event.setReplaceable(String.valueOf(((float) p.getHealth() / maxHealth) * 100));
            }


        } else if (type.equals("FOOD_LEVEL")) {
            event.setReplaceable(String.valueOf(p.getFoodLevel()));
            if (subType.equals("FORMATTED")) {
                int maxFood = 20;
                if (event.getType().split(".").length > 2)
                    maxFood = Integer.valueOf(event.getType().split(".")[2]);
                if ((float)p.getHealth() / maxFood < .10)
                    event.setReplaceable("starving");
                else if ((float) p.getFoodLevel() / maxFood < .40)
                    event.setReplaceable("famished");
                else if ((float) p.getFoodLevel() / maxFood < .75)
                    event.setReplaceable("hungry");
                else if ((float) p.getFoodLevel() / maxFood < 1)
                    event.setReplaceable("parched");
                else 
                    event.setReplaceable("healthy");
            } else if (subType.equals("PERCENTAGE")) {
                int maxFood = 20;
                if (event.getType().split(".").length > 2)
                    maxFood = Integer.valueOf(event.getType().split(".")[2]);
                event.setReplaceable(String.valueOf(((float) p.getFoodLevel() / maxFood) * 100));
            }


        } else if (event.getType().startsWith("EQUIPMENT")) {
            event.setReplaceable(String.valueOf(event.getNPC().getEntity().getHealth()));


        } else if (event.getType().startsWith("INVENTORY")) {
            event.setReplaceable(String.valueOf(event.getNPC().getEntity().getHealth()));


        } else if (event.getType().startsWith("XP")) {
            event.setReplaceable(String.valueOf(event.getPlayer().getExp() * 100));
            if (subType.equals("TO_LEVEL"))
                event.setReplaceable(String.valueOf(p.getExpToLevel()));
            else if (subType.equals("TOTAL"))
                event.setReplaceable(String.valueOf(p.getTotalExperience()));
            else if (subType.equals("LEVEL"))
                event.setReplaceable(String.valueOf(p.getLevel()));
            return;

    }

}
}



///*    .replace("<^PLAYER.ITEM_IN_HAND.MATERIAL>", itemInHandMaterial)
//    .replace("<^PLAYER.ITEM_IN_HAND.NAME>", itemInHandName)
//    .replace("<^PLAYER.ITEM_IN_HAND.QTY>", itemInHandQty)
//    .replace("<^PLAYER.ITEM_IN_HAND.ID>", itemInHandId)
//    .replace("<^PLAYER.NAME>", thePlayer.getName())
//    .replace("<^PLAYER>", thePlayer.getName())
//    .replace("<^PLAYER.KILLER>", playerKiller)
//    .replace("<^PLAYER.HEALTH>", String.valueOf(thePlayer.getHealth()))
//    .replace("<^PLAYER.HELM>", playerHelm)
//    .replace("<^PLAYER.LEGGINGS>", playerLeggings)
//    .replace("<^PLAYER.BOOTS>", playerBoots)
//    .replace("<^PLAYER.CHESTPLATE>", playerChestplate)
//    .replace("<^PLAYER.WORLD>", thePlayer.getWorld().getName())
//    .replace("<^PLAYER.MONEY>", playerMoney)
//    .replace("<^PLAYER.EXP_TO_NEXT_LEVEL>", String.valueOf(thePlayer.getExpToLevel()))
//    .replace("<^PLAYER.EXP>", String.valueOf(thePlayer.getTotalExperience()))
//    .replace("<^PLAYER.FOOD_LEVEL>", String.valueOf(thePlayer.getFoodLevel()));*/
//
//    @EventHandler
//    public void NPCTags(ReplaceableTagEvent event) {
//
//    }
//
//}
//
//
//
//
///*
// * Fills in replaceable data for flags and more.
// * 
// * Use quickReplaceable = true 
// * for queue-time. This type of replacement is made when the script is being put into the player queue.
// * Use quickReplaceable = false
// * for run-time. This type of replacement is made right at the time of code-execution.
// */
//
//
//// Player object flag replacement
//if (thePlayer != null && filledString.contains("<")) {
//
//    String itemInHandMaterial = "AIR"; 
//    if (thePlayer.getItemInHand() != null) 
//        itemInHandMaterial = thePlayer.getItemInHand().getType().name();
//
//    String itemInHandName = "nothing"; 
//    if (thePlayer.getItemInHand() != null) 
//        itemInHandName = thePlayer.getItemInHand().getType().name().toLowerCase().replace("_", " ");
//
//    String itemInHandQty = "0"; 
//    if (thePlayer.getItemInHand() != null) 
//        itemInHandQty = String.valueOf(thePlayer.getItemInHand().getAmount());
//
//    String itemInHandId = "0"; 
//    if (thePlayer.getItemInHand() != null) 
//        itemInHandId = String.valueOf(thePlayer.getItemInHand().getTypeId());
//
//    String playerKiller = "nobody"; 
//    if (thePlayer.getKiller() != null) 
//        playerKiller = thePlayer.getKiller().getName();
//
//    String playerHelm = "FALSE"; 
//    if (thePlayer.getInventory().getHelmet() != null) 
//        playerHelm = thePlayer.getInventory().getHelmet().getType().name();
//
//    String playerBoots = "FALSE"; 
//    if (thePlayer.getInventory().getBoots() != null) 
//        playerBoots = thePlayer.getInventory().getBoots().getType().name();
//
//    String playerChestplate = "FALSE"; 
//    if (thePlayer.getInventory().getChestplate() != null) 
//        playerChestplate = thePlayer.getInventory().getChestplate().getType().name();
//
//    String playerLeggings = "FALSE"; 
//    if (thePlayer.getInventory().getLeggings() != null) 
//        playerLeggings = thePlayer.getInventory().getLeggings().getType().name();
//
//    // TODO: Fix this!
//    String playerMoney = "0";
//    //  if (plugin.economy != null) 
//    //	  playerMoney = String.valueOf(plugin.economy.getBalance(thePlayer.getName()));
//
//    if (quickReplaceable) 
//        filledString = filledString
//        .replace("<^PLAYER.ITEM_IN_HAND.MATERIAL>", itemInHandMaterial)
//        .replace("<^PLAYER.ITEM_IN_HAND.NAME>", itemInHandName)
//        .replace("<^PLAYER.ITEM_IN_HAND.QTY>", itemInHandQty)
//        .replace("<^PLAYER.ITEM_IN_HAND.ID>", itemInHandId)
//        .replace("<^PLAYER.NAME>", thePlayer.getName())
//        .replace("<^PLAYER>", thePlayer.getName())
//        .replace("<^PLAYER.KILLER>", playerKiller)
//        .replace("<^PLAYER.HEALTH>", String.valueOf(thePlayer.getHealth()))
//        .replace("<^PLAYER.HELM>", playerHelm)
//        .replace("<^PLAYER.LEGGINGS>", playerLeggings)
//        .replace("<^PLAYER.BOOTS>", playerBoots)
//        .replace("<^PLAYER.CHESTPLATE>", playerChestplate)
//        .replace("<^PLAYER.WORLD>", thePlayer.getWorld().getName())
//        .replace("<^PLAYER.MONEY>", playerMoney)
//        .replace("<^PLAYER.EXP_TO_NEXT_LEVEL>", String.valueOf(thePlayer.getExpToLevel()))
//        .replace("<^PLAYER.EXP>", String.valueOf(thePlayer.getTotalExperience()))
//        .replace("<^PLAYER.FOOD_LEVEL>", String.valueOf(thePlayer.getFoodLevel()));
//
//    else 
//        filledString = filledString
//        .replace("<PLAYER.ITEM_IN_HAND.MATERIAL>", itemInHandMaterial)
//        .replace("<PLAYER.ITEM_IN_HAND.NAME>", itemInHandName)
//        .replace("<PLAYER.ITEM_IN_HAND.QTY>", itemInHandQty)
//        .replace("<PLAYER.ITEM_IN_HAND.ID>", itemInHandId)
//        .replace("<PLAYER.NAME>", thePlayer.getName())
//        .replace("<PLAYER>", thePlayer.getName())
//        .replace("<PLAYER.KILLER>", playerKiller)
//        .replace("<PLAYER.HEALTH>", String.valueOf(thePlayer.getHealth()))
//        .replace("<PLAYER.HELM>", playerHelm)
//        .replace("<PLAYER.LEGGINGS>", playerLeggings)
//        .replace("<PLAYER.BOOTS>", playerBoots)
//        .replace("<PLAYER.CHESTPLATE>", playerChestplate)
//        .replace("<PLAYER.WORLD>", thePlayer.getWorld().getName())
//        .replace("<PLAYER.MONEY>", playerMoney)
//        .replace("<PLAYER.EXP_TO_NEXT_LEVEL>", String.valueOf(thePlayer.getExpToLevel()))
//        .replace("<PLAYER.EXP>", String.valueOf(thePlayer.getTotalExperience()))
//        .replace("<PLAYER.FOOD_LEVEL>", String.valueOf(thePlayer.getFoodLevel()));
//
//}
//
//// Replaceables for Denizen
//if (theDenizen != null && filledString.contains("<")) {
//    if (quickReplaceable) 
//        filledString = filledString
//        .replace("<^DENIZEN.NPCID>", String.valueOf(theDenizen.getId()))
//        .replace("<^NPCID>", String.valueOf(theDenizen.getId()))
//        .replace("<^NPC>", theDenizen.getName())
//        .replace("<^DENIZEN.NAME>", theDenizen.getName());
//
//    else
//        filledString = filledString
//        .replace("<DENIZEN.NPCID>", String.valueOf(theDenizen.getId()))
//        .replace("<NPCID>", String.valueOf(theDenizen.getId()))
//        .replace("<NPC>", theDenizen.getName())
//        .replace("<DENIZEN.NAME>", theDenizen.getName());
//}
//
//// Done!
//return filledString;
//}
//
//
//
//}