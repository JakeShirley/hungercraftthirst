package net.onwt.thirst;

import net.hungercraft.core.api.PermissionsChangeEvent;
import net.hungercraft.core.managers.BoardManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
        for(String s : players.keySet())
        {
            Player p = getServer().getPlayer(s);
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

                if(thirstLevel <= 25)
                    p.sendMessage("Thirst level: 25");
                else if(thirstLevel <= 50)
                    p.sendMessage("Thirst level: 50");
                else if(thirstLevel <= 75)
                    p.sendMessage("Thirst level: 75");

                players.put(s, thirstLevel);
                BoardManager.getInstance().setSpecificValue(p, ChatColor.GREEN + "Thirst", thirstLevel);
            }
        }

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {

    }
}
