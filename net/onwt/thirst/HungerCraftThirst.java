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

    @EventHandler
    public void onMove(PlayerMoveEvent event)
    {
    	Player p = event.getPlayer();
    	String n = p.getName();
    	if(players.get(n) == null && PermissionsManager.getInstance().isCombatant(p)) {
    		players.put(n, 100);
    	}
    	
    	if(players.get(n) <= 20 && p.isSprinting()) {
    		p.setSprinting(false);
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
                        	Biome b = p.getWorld().getBiome((int)l.getX(), (int)l.getY());
                        	
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
                    if(is.getTypeId() == 373) {
                        increaseThirst(event.getPlayer(), is, event);
                    }
                }

                if(isInWater && (a.equals(Action.RIGHT_CLICK_BLOCK) || a.equals(Action.RIGHT_CLICK_AIR))) {

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

