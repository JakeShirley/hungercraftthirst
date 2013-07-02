package net.onwt.thirst;

import net.hungercraft.core.api.GameState;
import net.hungercraft.core.managers.GameManager;
import net.hungercraft.core.managers.SettingsManager;
import net.hungercraft.core.utils.PermissionsChangeEvent;
import net.hungercraft.core.managers.BoardManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Iterator;


/**
 * @author Bart
 *         Created for HungerCraft http://hungercraft.net
 */
public class HungerCraftThirst extends JavaPlugin implements Listener, Runnable {

    /**
     * Holds HashMap for Players -> Thirst level
     */
    private HashMap<String, Integer> players;


    public void onEnable()
    {
        players = new HashMap<String, Integer>();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 0L, 600L);
    }

    public void onDisable()
    {

    }

    @EventHandler
    public void onPermissionsChange(PermissionsChangeEvent event)
    {
        if(event.getGroup() == null)
        {

            players.remove(event.getPlayer().getName());
            BoardManager.getInstance().removeSpecificValue(event.getPlayer(), ChatColor.GREEN + "Thirst");

        }
        else if(event.getGroup().equalsIgnoreCase("combatant")) {

            players.put(event.getPlayer().getName(), 100);
            BoardManager.getInstance().setSpecificValue(event.getPlayer(),
                    ChatColor.GREEN + "Thirst",
                    players.get(event.getPlayer().getName()));



        } else {

            players.remove(event.getPlayer().getName());
            BoardManager.getInstance().removeSpecificValue(event.getPlayer(), ChatColor.GREEN + "Thirst");

        }

    }

    @Override
    public void run () {
        if(GameState.isInGame()) {
            if(GameManager.getInstance().getGame().getArena().getThirstEnabled()) {
                Iterator<String> it = players.keySet().iterator();

                while(it.hasNext()) {
                    String s  = it.next();

                    Player p = getServer().getPlayer(s);

                    if(p != null) {
                        if(p.isOnline()) {
                            int thirstLevel = players.get(s) - 2;

                            if(thirstLevel <= 0) {
                                p.sendMessage("Find something to drink!");
                                thirstLevel = 0;
                                p.damage(4);
                            }

                            if(thirstLevel <= 20) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1));
                            }

                            if(thirstLevel == 20)
                                p.sendMessage("Thirst level: 20");
                            else if(thirstLevel == 50)
                                p.sendMessage("Thirst level: 50");

                            setThirst(s, thirstLevel);
                        }
                    }
                }
            }
        }
    }

    public void setThirst(String playerName, int amount) {
        players.put(playerName, amount);
        BoardManager.getInstance().setSpecificValue(getServer().getPlayer(playerName), ChatColor.GREEN + "Thirst", amount);
    }



    @EventHandler (priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event)
    {
        //if the player is a thirster, then proceed
        if(players.containsKey(event.getPlayer().getName()))
        {
            ItemStack is = event.getItem();
            Action a = event.getAction();
            Material type = event.getPlayer().getLocation().getBlock().getType();
            int thirstlevel = players.get(event.getPlayer().getName());
            boolean isInWater = type.equals(Material.STATIONARY_WATER) || type.equals(Material.WATER);


            //if the player clicked water with a bowl, then relieve
            //his thirst
            if (thirstlevel < 100) {

                if(is != null) {
                    if(is.getTypeId() == 373)
                        increaseThirst(event.getPlayer(), is, event);
                }

                if(isInWater && a.equals(Action.RIGHT_CLICK_BLOCK) || a.equals(Action.RIGHT_CLICK_AIR)) {
                    increaseThirst(event.getPlayer(), is, event);
                }


            }
        }
    }


    public void increaseThirst(Player p, ItemStack is, PlayerInteractEvent e) {

        if(players.get(p.getName()) >= 100) {
            p.sendMessage("You quench your thirst.");
        } else {
            setThirst(p.getName(), 100);
            p.sendMessage("You quench your thirst");
        }

        //place an empty bottle in the players inventory
        if(is != null && is.getTypeId() == 373) {
            e.setUseItemInHand(Event.Result.DENY);
            e.getItem().setTypeId(374);
        }

    }
}

