package net.arlepuyon.OreGatherGame;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
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

	//扱うコンフィグファイル名の定義
	private final static String PLUGIN_CONFIGFILE = "config.yml";
	public YamlConfiguration configData;

	//ゲームワールド名の定義
	public final static String GAME_WORLD = "world";

	//ゲームに必要なブロック・アイテム定義
	public static Material wand;
	public static Material gen_block;


	public final static long REALTIME_PERIOD = 1; //in Minute
	public final static long TASK_PERIOD = 1200 * REALTIME_PERIOD;

	//編集状態の確保
	HashMap<String,EnumCommandStatus> cmdstate;

	//ランダムブロック出現位置の定義
	ArrayList<GenerateLocation> genloc;

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
		genloc = new ArrayList<GenerateLocation>();

		//設定データの読み出し
		loadGameConfig();

		//イベントリスナーの登録
		getServer().getPluginManager().registerEvents( new OGGEventListener(this) , this);
		getServer().getPluginManager().registerEvents( new OGGPlayerEventListener(this) , this);

		//コマンド「oregathergame(ogg)」を登録（詳しい動作は引数で捌く）
		getCommand("oregathergame").setExecutor(new OGGCommandExecutor(this));

		//【要トリガー設置】ブロックジェネレーターの起動
		//getServer().getScheduler().scheduleSyncRepeatingTask(this, blockgen, 0L, TASK_PERIOD );

		//起動メッセージ
		PluginDescriptionFile file_pdf = this.getDescription();
		logger.info("[" +file_pdf.getName()+ "] v" + file_pdf.getVersion() + " is enabled!");
	}

	@Override
	public void onDisable() {
		saveGameConfig();

		PluginDescriptionFile file_pdf = this.getDescription();
		logger.info("[" +file_pdf.getName()+ "] v" + file_pdf.getVersion() + " is disabled!");
	}

	/* プラグイン load 用 */
	public void loadGameConfig(){
		File configFile = new File(getDataFolder(), PLUGIN_CONFIGFILE );
		configData = new YamlConfiguration();

		//別の方法でファイルの有無を取得した方がいい？
		if( !configFile.exists() ){
			saveDefaultConfig();
			logger.info( logPrefix + "Create Default Configration" );
			configFile = new File(getDataFolder(), PLUGIN_CONFIGFILE );
		}

		//catch節内のreturnについて要調査
		try {
			configData.load( configFile );
		} catch (IOException e) {
			logger.warning( logPrefix + "Plugin IO Exception Occured");
			return;
		} catch (InvalidConfigurationException e) {
			logger.warning( logPrefix + "Invalid " +  PLUGIN_CONFIGFILE + ", will be overwritten");
			return;
		}

		//ゲーム設定用ワンドアイテムの読み込み
		wand = Material.getMaterial( configData.getInt("setup.setting_wand") );
		if( wand  == null ){
			//生成土台IDが間違っている場合はOBSIDIANに変更
			wand = Material.GOLD_AXE;
			logger.warning( logPrefix + "Config of Wand Item ID is incollect!" );
			logger.warning( logPrefix + "Wand Item is defined " + wand.name() );
		}
		//ブロック生成土台の読み込み
		gen_block = Material.getMaterial( configData.getInt("option.generate_block_type") );
		if( gen_block == null ){
			//生成土台IDが間違っている場合はOBSIDIANに変更
			gen_block = Material.OBSIDIAN;
			logger.warning( logPrefix + "Config of Generate Block ID is incollect!" );
			logger.warning( logPrefix + "Generate Block is defined " + gen_block.name() );
		}
		String locstr;
		//チームAの回収ボックス場所保存
		if( (locstr = configData.getString("register_positon.chestA")) != null ){
			CollecterBox1 = convertStringToLocation(locstr);
		}
		//チームBの回収ボックス場所保存
		if( (locstr = configData.getString("register_positon.chestB")) != null ){
			CollecterBox2 = convertStringToLocation(locstr);
		}
		//ブロック生成ポイントの読み込み
		List<String> conf_genloc = configData.getStringList("register_positon.block_loc");
		genloc.clear();

		for( Iterator<String> itgp = conf_genloc.iterator(); itgp.hasNext();){
			genloc.add( convertStringToGenerateLocation( itgp.next() ) );
		}
		//設定保存がうまくいかない場合はリストクリアを行う（開発終了後消去）
		if( genloc.contains( null )){
			genloc.clear();
			logger.info( logPrefix + "Registerd BlockLocation is cleared to mend" );
		}
		return;
	}

	/* プラグイン設定 save コマンド用 */
	public void saveGameConfig(){

		//チームAの回収ボックス場所保存
		if( CollecterBox1 != null ){
			configData.set("register_positon.chestA", convertLocationToString(CollecterBox1) );
		}else{
			configData.set("register_positon.chestA", null );
		}
		//チームBの回収ボックス場所保存
		if( CollecterBox2 != null ){
			configData.set("register_positon.chestB", convertLocationToString(CollecterBox2) );
		}else{
			configData.set("register_positon.chestB", null );
		}

		//鉱石生成ポイントを鉱石のプライオリティと一緒に保存
		List<String> reg = new LinkedList<String>();
		for( Iterator<GenerateLocation> iter_genloc = genloc.iterator(); iter_genloc.hasNext();){
			GenerateLocation gl = iter_genloc.next();
			reg.add( convertLocationToString( gl.getLocation()) +","+ gl.getBlockGrade() );
		}
		configData.set("register_positon.block_loc" , reg);

		//【TODO:ファイル保存順の整理】
		File playerFile = new File(getDataFolder(), PLUGIN_CONFIGFILE );
		try {
			configData.save(playerFile);
		} catch (IOException e) {
			logger.warning("Failed to save players.yml, fly times will not be saved!");
		}
	}

	/* プラグイン設定 reload コマンド用 */
	public void reloadConfig(){
		return;
	}

	// 回収チェスト用の文字列からゲーム用Location作成
	public Location convertStringToLocation(String str){
		String[] genlocstr = str.split(",");

		if( genlocstr.length != 4 ){
			logger.severe( logPrefix + "Registerd BlockLocation format is broken!" );
			return null;
		}

		return new Location(
				this.getServer().getWorld(genlocstr[0]),
				Double.valueOf(genlocstr[1]),
				Double.valueOf(genlocstr[2]),
				Double.valueOf(genlocstr[3]));
	}

	/* 生成ポイント設定用の文字列からゲーム用リスト作成 */
	public GenerateLocation convertStringToGenerateLocation(String str){
		String[] genlocstr = str.split(",");

		if( genlocstr.length != 5){
			logger.severe( logPrefix + "Registerd GenerateLocation format is broken!" );

			return null;
		}

		Location loc = new Location(
				this.getServer().getWorld(genlocstr[0]),
				Double.valueOf(genlocstr[1]),
				Double.valueOf(genlocstr[2]),
				Double.valueOf(genlocstr[3]));
		String blockgrade = genlocstr[4];
		return new GenerateLocation( loc , blockgrade );
	}

	// Locationを設定用に文字列化
	private String convertLocationToString( Location loc ){
		return (loc.getWorld().getName() +","+ loc.getBlockX() +","+ loc.getBlockY() +","+ loc.getBlockZ() );
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


