package xyz.undeaD_D.pokemononline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Updater implements Listener{
	protected boolean update_available = false;
	protected int version_number = 2;
	protected String permission_update = "bw.update";
	protected String website = "http://www.pokemon-online.xyz/plugin";
	protected String version_identifier = "bw";
		
	private BarbedWire plugin;
	
	public Updater(BarbedWire plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		check();
		
		if(update_available) {
		    for (Player p : plugin.getServer().getOnlinePlayers()) {
		    	if(p.isOp() || p.hasPermission(permission_update)) {
		    		plugin.say(p, false);
		    	}
		    }
		}
	}

	
	@EventHandler
	public void login(PlayerJoinEvent e) {
		if(update_available) {
	    	if(e.getPlayer().isOp() || e.getPlayer().hasPermission(permission_update)) {
	    		plugin.say(e.getPlayer(), false);
	    	}
		}
	}

	
	private void check() {
		String sourceLine = null;
        try {
	        URL url = new URL(website);
	        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	        String str;
	        while ((str = in.readLine()) != null) {
	            if(str.startsWith(version_identifier + ":")) {
	            	sourceLine = str.split(":")[1];
	            	break;
	            }
	        }
        } catch (IOException ex) {}
        
	    if(sourceLine != null && Integer.parseInt(sourceLine) != version_number){
	    	update_available  = true;
	    	plugin.say(null, true);
	    }
	}
	

}
