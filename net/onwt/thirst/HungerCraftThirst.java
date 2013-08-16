package net.onwt.thirst;

import net.hungercraft.core.api.GameState;
import net.hungercraft.core.managers.GameManager;
import net.hungercraft.core.managers.PermissionsManager;
import net.hungercraft.core.managers.SettingsManager;
import net.hungercraft.core.utils.PermissionsChangeEvent;
import net.hungercraft.core.managers.BoardManager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
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
                        	
                        	
                        	int offset = 2;
                        	Location l = p.getLocation();
                        	Biome b = l.getBlock().getBiome();
                        	
                        	//double thirst decrement in desert biomes
                        	if(b == Biome.DESERT || b == Biome.DESERT_HILLS) {
                        		offset = 4;
                        	} else {
                        		//they aren't in the desert or desert hills, check if it is raining
                        		if(p.getWorld().hasStorm()) {
                        			offset = 1;
                        		}
                        		
                        	}
                        	
                        	
                        	int thirstLevel = players.get(s) - offset;
                        	
                        	
                        	
                        	
                            if(thirstLevel <= 0) {
                                p.sendMessage("Find something to drink!");
                                thirstLevel = 0;
                                p.damage(4);
                            }

                            if(thirstLevel <= 20) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 220, 1));
                            }

                            if(thirstLevel <= 20)
                                p.sendMessage("You are very thirsty! Find something to drink.");
                            else if(thirstLevel <= 50)
                                p.sendMessage("You are getting thirsty...");

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


    
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event)
    {
    	if(event.getItem().getTypeId() == 373) {
    		increaseThirst(event.getPlayer());
    	}
    }

    //handles drinking directly from water
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event)
    {
        //if the player is a thirster, then proceed
        if(players.containsKey(event.getPlayer().getName()))
        {
            Action a = event.getAction();
            Material type = event.getPlayer().getLocation().getBlock().getType();
            int thirstlevel = players.get(event.getPlayer().getName());
            boolean isInWater = type.equals(Material.STATIONARY_WATER) || type.equals(Material.WATER);


            //if the player clicked water with a bowl, then relieve
            //his thirst
            if (thirstlevel < 100) {


                if(isInWater && (a.equals(Action.RIGHT_CLICK_BLOCK) || a.equals(Action.RIGHT_CLICK_AIR))) {

                    increaseThirst(event.getPlayer());
                }


            }
        }
    }


    public void increaseThirst(Player p) {

        if(players.get(p.getName()) >= 100) {
            p.sendMessage("You quench your thirst.");
        } else {
            setThirst(p.getName(), 100);
            p.sendMessage("You quench your thirst");
        }

    }
}

