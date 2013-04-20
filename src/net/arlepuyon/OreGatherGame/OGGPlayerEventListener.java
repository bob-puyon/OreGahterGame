package net.arlepuyon.OreGatherGame;

import java.util.ListIterator;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class OGGPlayerEventListener implements Listener {

	OreGatherGame plg;

	public OGGPlayerEventListener( OreGatherGame plg_instance ) {
		this.plg = plg_instance;
	}

	@EventHandler
	public void onPlayerDropItemEvent( PlayerDropItemEvent evt ){
		Player player = evt.getPlayer();
		//【TODO:プレイヤーの重さを0.1~0.2で調整】
		player.setWalkSpeed(0.2f);
		calcOreWeight( player );

	}

	@EventHandler
	public void onPlayerPickupItemEvent( PlayerPickupItemEvent evt ){
		Player player = evt.getPlayer();
		//デフォルトのプレイヤー速度は0.2
		player.setWalkSpeed(0.2f);
		calcOreWeight( player );
	}

	private int calcOreWeight(Player p){
		int weight = 0;
		ItemStack item;
		for( ListIterator<ItemStack> inv = p.getInventory().iterator(); inv.hasNext();){
			item = inv.next();
			if( item == null ){ continue; }
			if( OGGChestChecker.point_table.containsKey( item.getType()) ){
				weight += OGGChestChecker.point_table.get( item.getType() ) * item.getAmount();
			}
			OreGatherGame.logger.info( OreGatherGame.logPrefix +
					"スロット番号：" + inv.previousIndex() +
					"\tアイテム名：" + item.getType().toString() +
					"\t" + "個数：" + item.getAmount() );

		}
		p.sendMessage( OreGatherGame.msgPrefix + "現在のあなたの得点は " + weight + "点 です");
		return weight;
	}
}
