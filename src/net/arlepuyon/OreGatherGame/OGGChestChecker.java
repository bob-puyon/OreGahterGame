package net.arlepuyon.OreGatherGame;

import java.util.HashMap;
import java.util.ListIterator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class OGGChestChecker {

	OreGatherGame plg;

	public OGGChestChecker( OreGatherGame plg_instance) {
		this.plg = plg_instance;
	}

	/*
	 * このクラスはプレイヤー情報を受け取らないこと
	 */

	/* [テスト]チェストの中身をコンソールに表示 */
	void showItemList(Block bk){
		//チェストで無い場合拒否
		if( !isChestByBlock(bk) ){ return; }

		Chest chest = (Chest)bk.getState();
		Inventory chestinv = chest.getBlockInventory();

		for( ListIterator<ItemStack> contents = chestinv.iterator(); contents.hasNext(); ){
			ItemStack item;
			if( ( item = contents.next()) == null ){ continue; }
			OreGatherGame.logger.info( OreGatherGame.logPrefix +
					"スロット番号：" + contents.previousIndex() +
					"\tアイテム名：" + item.getType().toString() +
					"\t" + "個数：" + item.getAmount() );
		}
		return;
	}

	boolean isChestByBlock( Block bk ){
		return bk.getType() == Material.CHEST ? true : false;
	}

	/* TODO:対象チェストの得点計算メソッド */
	int calcScore( Block bk ){
		int total = 0;

		//チェストで無い場合拒否
		if( !isChestByBlock(bk) ){ return total; }

		Chest chest = (Chest)bk.getState();
		Inventory chestinv = chest.getBlockInventory();

		for( ListIterator<ItemStack> contents = chestinv.iterator(); contents.hasNext(); ){
			ItemStack item;
			if( ( item = contents.next()) == null ){ continue; }
			if( point_table.containsKey( item.getType() ) ){
				total += point_table.get( item.getType() ) * item.getAmount();
			}

			/*OreGatherGame.logger.info( OreGatherGame.logPrefix +
					"スロット番号：" + contents.previousIndex() +
					"\tアイテム名：" + item.getType().toString() +
					"\t" + "個数：" + item.getAmount() );
			 */
		}
		plg.getServer().broadcastMessage( OreGatherGame.logPrefix + "このチェストの得点は " + total + "点 です");
		OreGatherGame.logger.info( OreGatherGame.logPrefix + "このチェストの得点は " + total + " です");
		return total;


	}

	static final HashMap<Material,Integer> point_table = new HashMap<Material,Integer>();
	static {
		point_table.put(Material.COAL_ORE, 1);
		point_table.put(Material.QUARTZ_ORE, 2);
		point_table.put(Material.REDSTONE_ORE, 4);
		point_table.put(Material.LAPIS_ORE, 8);
		point_table.put(Material.IRON_ORE, 16);
		point_table.put(Material.GOLD_ORE, 32);
		point_table.put(Material.EMERALD_ORE, 64);
		point_table.put(Material.DIAMOND_ORE, 128);
		point_table.put(Material.QUARTZ_BLOCK, 2);
		point_table.put(Material.REDSTONE_BLOCK, 4);
		point_table.put(Material.LAPIS_BLOCK, 8);
		point_table.put(Material.IRON_BLOCK, 16);
		point_table.put(Material.GOLD_BLOCK, 32);
		point_table.put(Material.EMERALD_BLOCK, 64);
		point_table.put(Material.DIAMOND_BLOCK, 128);
	}
}
