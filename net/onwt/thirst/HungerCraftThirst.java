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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;


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
                for(String s : players.keySet())
                {
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
            //if the player clicked water with a bowl, then relieve
            //his thirst
            if (players.get(event.getPlayer().getName()) < 100 && event.getItem() != null) {
                if(event.getItem().getTypeId() == 373) {



                    if(SettingsManager.getInstance().getHardcoreMode())
                    {
                        players.put(event.getPlayer().getName(), Math.min(players.get(event.getPlayer()) + 25, 100));

                        if(players.get(event.getPlayer()) >= 100)
                            event.getPlayer().sendMessage("You quench your thirst.");
                        else
                            event.getPlayer().sendMessage("You take a sip of water.");
                    }
                    else
                    {
                        players.put(event.getPlayer().getName(), 100);
                        event.getPlayer().sendMessage("You quench your thirst");
                    }

                    //place an empty bottle in the players inventory
                    if(event.getItem() != null && event.getItem().getTypeId() == 373)
                    {
                        event.setUseItemInHand(Event.Result.DENY);
                        event.getItem().setTypeId(374);
                    }
                }

                if(event.getItem() != null && event.getItem().getTypeId() == 373 && event.getItem().getDurability() == 0)
                    event.setUseItemInHand(Event.Result.DENY);
            }
        }
    }
}
