package xyz.undeaD_D.pokemononline;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class BarbedWire extends JavaPlugin implements Listener{
	private String prefix;
	private FileConfiguration config;
	private boolean debug;
		
	private ArrayList<Player> inside;
	
	public void onEnable() {
		config = getConfig();
		config.options().copyDefaults(true);
		saveConfig();
		
		setupVars();
		getServer().getPluginManager().registerEvents(this, this);
	}
	

	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		if(cmd.getName().equalsIgnoreCase("bw")) {
			if(args.length == 1 && args[0].equalsIgnoreCase("reload")) {
				if(sender instanceof Player) {
					if(((Player)sender).hasPermission("bw.reload")) {
						reloadConfig();
						config = getConfig();
						setupVars();
						((Player)sender).sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + " " + config.getString("msg.reloaded")));
						return true;
					}else {
						((Player)sender).sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + " " + config.getString("msg.noperm")));
						return true;
					}
				}else {
					reloadConfig();
					config = getConfig();
					setupVars();
					System.out.println(ChatColor.stripColor(prefix + " " + config.getString("msg.reloaded")));
					return true;
				}
			}
		}		
		return false;
	}

	
	// [Events]-----------------------------------------------------------------------------------------------------------------------------

	
	@EventHandler
    public void onQuit(PlayerQuitEvent e) {
		inside.remove(e.getPlayer());
	}
	
	
	@EventHandler
    public void onInteract(PlayerInteractEvent e) {
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if(e.getClickedBlock().getType().equals(Material.WEB)) {
				if(e.getPlayer().hasPermission("bw.use")) {
					if(config.getBoolean("item.mustusetobreak")) {
						if(e.getPlayer().getItemInHand().getType().equals(Material.valueOf(config.getString("item.material")))) {
							handle(true, e.getPlayer(), e.getClickedBlock());
							e.getPlayer().getItemInHand().setDurability((short) (e.getPlayer().getItemInHand().getDurability() + config.getInt("onbreak.remove-durability")));	
							e.setCancelled(true);
							return;
						}
					}
				}
			}
		}
    }
	

	@EventHandler
    public void onBreak(BlockBreakEvent e) {
		if(e.getBlock().getType().equals(Material.WEB)) {
			if(e.getPlayer().hasPermission("bw.use")) {
				if(config.getBoolean("item.mustusetobreak")) {
					if(e.getPlayer().getItemInHand().getType().equals(Material.valueOf(config.getString("item.material")))) {
						handle(true, e.getPlayer(), e.getBlock());
						e.setCancelled(true);
						return;
					}else {
						e.setCancelled(true);
						return;
					}
				}else {
					handle(true, e.getPlayer(), e.getBlock());
					e.setCancelled(true);
					return;
				}
			}else {
				if(config.getBoolean("item.protect-barbedwire"))
				e.setCancelled(true);
			}
		}
    }
	
	
	@EventHandler
    public void OnMove(PlayerMoveEvent e) {
		if(!inside.contains(e.getPlayer())) {
			if(e.getPlayer().getLocation().getBlock().getType().equals(Material.WEB)) {
				if(!e.getPlayer().hasPermission("bw.bypass")) {
					inside.add(e.getPlayer());
					handle(false, e.getPlayer(), null);
				}
			}
		}else {
			if(e.getPlayer().getLocation().getBlock().getType() != Material.WEB) {
				inside.remove(e.getPlayer());
			}
		}
	}
		

	// Helper -------------------------------------------------------------------------------------------------------------------------------
	
	
	@SuppressWarnings("deprecation")
	private void handle(boolean type, Player p, Block block) {
		if(type) {
			if(config.getBoolean("onbreak.dropblock")) {
				block.breakNaturally();
			}else {
				block.breakNaturally(new ItemStack(Material.AIR, 1));
			}
				if(config.getBoolean("onbreak.particle-toggle")) {
					p.playEffect(block.getLocation(), Effect.valueOf(config.getString("onbreak.particle-type")), 0);
				}
				if(config.getBoolean("onbreak.sound-toggle")) {
					p.playSound(block.getLocation(), Sound.valueOf(config.getString("onbreak.sound-type")), Float.parseFloat(config.getString("onbreak.sound-volume")), Float.parseFloat(config.getString("onbreak.sound-pitch")));
				}
		}else {
			Entity entity = p.getWorld().spawnEntity(p.getLocation().add(0, 300, 0), EntityType.WITHER_SKULL);
			entity.setCustomName("BarbedWire");
				p.damage(config.getDouble("oncontact.damage"), entity);
			entity.remove();
				if(config.getBoolean("oncontact.particle-toggle")) {
					p.playEffect(p.getLocation(), Effect.valueOf(config.getString("oncontact.particle-type")), 0);
				}
				if(config.getBoolean("oncontact.sound-toggle")) {
					p.playSound(p.getLocation(), Sound.valueOf(config.getString("oncontact.sound-type")), Float.parseFloat(config.getString("oncontact.sound-volume")), Float.parseFloat(config.getString("oncontact.sound-pitch")));
				}
		}
	}
	
	
	private void setupVars() {
		prefix = ChatColor.translateAlternateColorCodes('&',config.getString("settings.prefix"));
		boolean b = config.getBoolean("settings.updater");
		if(b) {
			new Updater(this);
		}
		
		debug = config.getBoolean("settings.debug");
		if(debug) {
			System.out.println(ChatColor.stripColor(prefix + "-----------------------------------------------"));
			System.out.println(ChatColor.stripColor(prefix + " BarbedWire DEBUG-MODE was activated."));
			System.out.println(ChatColor.stripColor(prefix + " It is advisable to turn it back off ..."));
			System.out.println(ChatColor.stripColor(prefix + " this can be done in the config.yml"));
			System.out.println(ChatColor.stripColor(prefix + "-----------------------------------------------"));
		}	
		
		inside = new ArrayList<Player>();
	}
	
		
	// UPDATER ------------------------------------------------------------------------------------------------------------------------------
	
	
	protected void say(Player p, boolean console) {
		if(console) {
			System.out.println(ChatColor.stripColor(prefix  + "-----------------------------------------------------"));
			System.out.println(ChatColor.stripColor(prefix + " BarbedWire is outdated. Get the new version here:"));
			System.out.println(ChatColor.stripColor(prefix + " http://www.pokemon-online.xyz/plugin"));
			System.out.println(ChatColor.stripColor(prefix + "-----------------------------------------------------"));
		}else {
		   	p.sendMessage(prefix + "------------------------------------------------------");
		   	p.sendMessage(prefix + " BarbedWire  is outdated. Get the new version here:");
		   	p.sendMessage(prefix + " http://www.pokemon-online.xyz/plugin");
		   	p.sendMessage(prefix + "------------------------------------------------------");
		}
	}
	
	
}
