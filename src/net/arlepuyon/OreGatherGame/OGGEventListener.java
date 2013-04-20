package net.arlepuyon.OreGatherGame;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class OGGEventListener implements Listener {

	OreGatherGame plg;

	public OGGEventListener( OreGatherGame plg_instance) {
		this.plg = plg_instance;
	}

	@EventHandler
	public void onPlayerInteractEvent( PlayerInteractEvent evt ){
		/*
		 * 指定アイテムを持っているか確認する
		 * まずは試験的に特定ブロックのみに対して指定を許可
		 * そのうち設定ファイルのブロックを対象に指定を許可
		 *
		 * */
		/* TODO：パーミッションチェック（できればCommandExecuter側で）*/

		Player p = evt.getPlayer();

		/* [テスト]アクションが左クリックで無かった場合拒否 */
		if( evt.getAction() != Action.LEFT_CLICK_BLOCK ){
			return;
		}

		/* [テスト]岩盤を持っていなかった場合拒否 */
		if( p.getItemInHand().getType() != OreGatherGame.wand ){
			p.sendMessage("この設定を行うためには ID:" + OreGatherGame.wand.getId() + " のアイテムを持つ必要があります");
			return;
		}


		//p.sendMessage("****制約条件クリア*****");
		//p.sendMessage( p.getName() );

		Block select_block= evt.getClickedBlock();

		/* EnumCommandStateにあわせて動作を切り替えていく*/
		//分かりやすさのため一度定数に落とし込みます
		EnumCommandStatus state = plg.getCommandStatus( p.getName().trim() );

		if( state == null ){
			p.sendMessage("あなたは現在コマンドステータスを持っていません");
			return;
		}

		//p.sendMessage("取得できたステータス：" + state.toString() );
		switch( state ){
			case ADD_POINT:
				if( !canResisteredBlock( select_block )){
					p.sendMessage("セット対象のブロックは鉄ブロックである必要があります");
					break;
				}
				//すでにブロックの場所が登録されているか確認
				//TODO:後で
				if( !idRegisteredLocation( select_block.getLocation() ) ){
					plg.genloc.add( new GenerateLocation( select_block.getLocation(), "Priority1" ));
					p.sendMessage( OreGatherGame.msgPrefix + "新しいブロック生成ポイントが作成されました！");
				}else{
					p.sendMessage( OreGatherGame.msgPrefix + "すでに登録済みのブロック生成ポイントです！");
				}
				break;
			case DEL_POINT:
				if( idRegisteredLocation( select_block.getLocation() ) ){
					plg.genloc.remove( select_block.getLocation() );
					p.sendMessage( OreGatherGame.msgPrefix + "指定したブロック生成ポイントを削除しました！");
				}else{
					p.sendMessage( OreGatherGame.msgPrefix + "指定したブロックは生成ポイントではありません！");
				}
				break;
			case SET_COLECTOR1:
				//クリック対象がチェストでは無い場合拒否
				if( select_block.getType() == Material.CHEST ){
					plg.setCollecterBox1( select_block.getLocation() );
					p.sendMessage( OreGatherGame.msgPrefix + "指定したチェストをチーム１の回収用に登録しました！");
				}else{
					p.sendMessage( OreGatherGame.msgPrefix + "指定したブロックはチェストではありません！");
				}
				break;
			case REM_COLECTOR1:
				if( plg.getCollecterBox1() != null ){
					plg.setCollecterBox1( null );
					p.sendMessage( OreGatherGame.msgPrefix + "チーム１の回収チェストの登録を削除しました！");
				}else{
					p.sendMessage( OreGatherGame.msgPrefix + "指定したブロックはチェストではありません！");
				}
				break;
			case SET_COLECTOR2:
				//クリック対象がチェストでは無い場合拒否
				if( select_block.getType() == Material.CHEST ){
					plg.setCollecterBox1( select_block.getLocation() );
					p.sendMessage( OreGatherGame.msgPrefix + "チーム２の回収チェストの登録を削除しました！");
				}else{
					p.sendMessage( OreGatherGame.msgPrefix + "指定したブロックはチェストではありません！");
				}
				break;
			case REM_COLECTOR2:
				if( plg.getCollecterBox2() != null ){
					plg.setCollecterBox2( null );
					p.sendMessage( OreGatherGame.msgPrefix + "チーム２の回収チェストの登録を削除しました！");
				}else{
					p.sendMessage( OreGatherGame.msgPrefix + "指定したブロックはチェストではありません！");
				}
				break;
			default:
				break;
		}

		return;
	}

	/* */
	/* [テスト]クリック対象のブロックを取得し鉄ブロック以外の場合は拒否 */
	boolean canResisteredBlock( Block bk ){
		return bk.getType() != OreGatherGame.gen_block ? false : true ;
	}

	/* 指定したブロックのLocationがすでに存在するか判定 */
	boolean idRegisteredLocation( Location loc ){

		for( Iterator<GenerateLocation> loc_iter = plg.genloc.iterator(); loc_iter.hasNext(); ){
			Location altloc = loc_iter.next().getLocation();
			if( altloc.equals( loc ) ){ return true; }
		}

		return false;
	}

}
