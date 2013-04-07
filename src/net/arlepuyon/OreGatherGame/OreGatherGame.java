package net.arlepuyon.OreGatherGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * 【ゲーム】
 * 「鉱石集め（奪い合い）」
 *
 * 【目的】
 * ２チームに分かれてフィールド上のランダムな位置に
 * 特定の鉱石（要ピッケル採取）が複数個出現
 * 制限時間内に獲得した鉱石を自陣のチェストに入れる
 * 終了後、チェスト内の鉱石に応じてポイント計算
 * ポイントの多いチームの勝利
 *
 * 【ゲーム性を高めるためのバニラ動作からの変更案or要望】
 * 取得した鉱石量に応じてプレイヤーの動作を遅くさせる
 * 鉱石取得プレイヤーに対して何らかのエフェクトを追加
 *
 * 【PVP観点からの問題】
 * 鉱石を持っているプレイヤーがバニラ状態では分からない
 * 　↑ただの無差別PVPと変わらない可能性がある
 * 鉱石の出現位置や相手陣地が明確だと待ち伏せが発生する
 * 　↑プレイヤーの流動性の低下、プレイヤー同士の接触率低下
 *
 * 【残実装】
 * [## OK ##] ゲーム開始前の実装
 * [## OK ##] タイマーの作成
 * [## OK ##] ランダムブロック生成の作成
 * [## OK ##] ランダムブロックのポイント登録
 * [## OK ##] ブロック生成に応じたメッセージ表示の設定
 * [## OK ##] チェスト内の得点計算の作成
 *
 * 開催ドメインの予定：dl.arle-puyon.net
 * VMware使う?(Windows側で開催)
 * (メインサーバー：arle-puyon.netはイベント中だけ停止
 *  WEBサーバー専用になってもらう)
 *
 * 参加目標人数：20名(10vs10)
 *
 * 【放送向け実装】
 * 得点計算と結果表示はコマンドによるコール制にする

*/

public class OreGatherGame extends JavaPlugin{

	//チェストを取り扱うクラスの定義
	OGGChestChecker chestchk;
	//ブロック生成を取り扱うクラスの定義
	OGGBlockGenerator blockgen;

	//扱うloggerの取得
	public final static Logger logger = Logger.getLogger("Minecraft");
	public final static String logPrefix = "[OreGatherGame] ";
	public final static String msgPrefix = "\u00A77[OreGatherGame] \u00A7f";

	//編集状態の確保
	HashMap<String,EnumCommandStatus> cmdstate;

	//ランダムブロック出現位置の定義
	ArrayList<Location> genloc;

	//アイテムを集めるチェストの定義
	//TODO:ラージチェストの判定をどのように行うか？
	//対バージョンアップに備えて、今のところシングルチェストで対策
	Location CollecterBox1;
	Location CollecterBox2;

	@Override
    public void onEnable(){
		//initialize
		chestchk = new OGGChestChecker( this );
		blockgen = new OGGBlockGenerator( this );

		cmdstate = new HashMap<String, EnumCommandStatus>();
		genloc = new ArrayList<Location>();

		//イベントリスナーの登録
		getServer().getPluginManager().registerEvents( new OGGEventListener(this) , this);

		//コマンド「oregathergame(ogg)」を登録（詳しい動作は引数で捌く）
		getCommand("oregathergame").setExecutor(new OGGCommandExecutor(this));

		//起動メッセージ
		PluginDescriptionFile file_pdf = this.getDescription();
		logger.info("[" +file_pdf.getName()+ "] v" + file_pdf.getVersion() + " is enabled!");
    }

    @Override
    public void onDisable() {
		PluginDescriptionFile file_pdf = this.getDescription();
		logger.info("[" +file_pdf.getName()+ "] v" + file_pdf.getVersion() + " is disabled!");
    }

    /* ブロック生成ポイント用 Getter Setter*/
	void setCommandState( String playername, EnumCommandStatus state){
		cmdstate.put( playername, state);
	}
	EnumCommandStatus getCommandStatus( String playername ){
		return cmdstate.get( playername );
	}

	/* チームチェスト用 Getter Setter */
	public Location getCollecterBox1() {
		return CollecterBox1;
	}

	public void setCollecterBox1(Location loc1) {
		CollecterBox1 = loc1;
	}

	public Location getCollecterBox2() {
		return CollecterBox2;
	}

	public void setCollecterBox2(Location loc2) {
		CollecterBox2 = loc2;
	}



}


