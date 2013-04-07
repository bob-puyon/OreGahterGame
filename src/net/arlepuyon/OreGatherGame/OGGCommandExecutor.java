package net.arlepuyon.OreGatherGame;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/*
 * 実装するべきコマンド
 * cmd: oregathergame
 * alias: ogg
 *
 * TODO:
 * 先に複数ステージ向けに定義しないと後々えらいことになる気が
 *
 * args:
 * addpoint:鉱石生成ポイントの追加
 * delpoint:鉱石生成ポイントの削除
 * clearpoint:すべての鉱石生成ポイントのクリア
 * listpoint:現在設定されていポイントのリストアップ
 *
 * setcollector t1: チームAの回収チェストの追加
 * setcollector t2: チームBの回収チェストの削除
 * removecollector t1: チームAの回収チェストの追加
 * removecollector t2: チームBの回収チェストの削除
 *
 * start: ゲームスタートコマンド
 *
*/
public class OGGCommandExecutor implements CommandExecutor {

	OreGatherGame plg;



	public OGGCommandExecutor( OreGatherGame plg_instance ) {
		this.plg = plg_instance;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player;

		//コマンド発行者をプレイヤーに限定
		if ( !(sender instanceof Player) ) {
			return true;
		}else{
			player = (Player)sender;
		}

		//コマンド長検査
		if( args.length < 1 ){
			player.sendMessage(  OreGatherGame.msgPrefix + "コマンドの指定が不足しています");
			player.sendMessage(  OreGatherGame.msgPrefix + "[ /oregahtergame help ] で確認できます");
			return true;
		}

		/*
		 * コマンド実行後指定アイテムによるクリックで対象のブロックを確定
		 * １、コマンド実行→何らかのフラグを立てる
		 * ２、EventListener側のIntaractEvent内でフラグがあった場合のみそのブロックを設定？
		 *     （判定する基準：コマンド発行フラグありか？そのプレイヤーか？
		 *       手に持っているアイテムはプラグイン用のアイテムか？gethand?何でintaractが発生したか？）
		 */

		if( args[0].equalsIgnoreCase("addpoint")){
			//鉱石生成ポイントの追加
			plg.setCommandState( player.getName() , EnumCommandStatus.ADD_POINT );
			player.sendMessage( plg.cmdstate.get(player.getName()).toString() );
			player.sendMessage(  OreGatherGame.msgPrefix + "指定されたアイテムで生成させたいポイントを選んでください");
		}else if( args[0].equalsIgnoreCase("delpoint") ){
			plg.setCommandState( player.getName() , EnumCommandStatus.DEL_POINT );
			player.sendMessage(  OreGatherGame.msgPrefix + "指定されたアイテムで消去したいポイントを選んでください");
		}else if( args[0].equalsIgnoreCase("setcollectorA") ){
			plg.setCommandState( player.getName() , EnumCommandStatus.SET_COLECTOR1 );
			player.sendMessage(  OreGatherGame.msgPrefix + "指定されたアイテムでチームAの回収チェストを選んで下さい");
		}else if( args[0].equalsIgnoreCase("setcollectorB") ){
			plg.setCommandState( player.getName() , EnumCommandStatus.SET_COLECTOR2 );
			player.sendMessage(  OreGatherGame.msgPrefix + "指定されたアイテムでチームBの回収チェストを選んで下さい");
		}else if( args[0].equalsIgnoreCase("remcollectorB") ){
			//削除処理
			player.sendMessage(  OreGatherGame.msgPrefix + "チームAの回収チェストの登録が解除されました");
		}else if( args[0].equalsIgnoreCase("remcollectorB") ){
			//削除処理
			player.sendMessage(  OreGatherGame.msgPrefix + "チームBの回収チェストの登録が解除されました");
		}else if( args[0].equalsIgnoreCase("clist1") ){
			if( plg.CollecterBox1 != null ){
				plg.chestchk.showItemList( plg.CollecterBox1.getBlock() );
			}else{
				player.sendMessage(  OreGatherGame.msgPrefix + "チームAのチェストが登録されていません");
			}
		}else if( args[0].equalsIgnoreCase("ccalc1") ){
			if( plg.CollecterBox1 != null ){
				plg.chestchk.calcScore( plg.CollecterBox1.getBlock() );
			}else{
				player.sendMessage(  OreGatherGame.msgPrefix + "チームAのチェストが登録されていません");
			}
		}else if( args[0].equalsIgnoreCase("gen") ){
			plg.blockgen.generateBlock();
		}else{
			return true;
		}

		return true;
	}

	/* --------------------------------------------------------- */






}

