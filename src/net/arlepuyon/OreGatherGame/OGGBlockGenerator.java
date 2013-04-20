package net.arlepuyon.OreGatherGame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class OGGBlockGenerator implements Runnable{

	OreGatherGame plg;

	public OGGBlockGenerator( OreGatherGame plg_instance) {
		this.plg = plg_instance;
	}
	/* ブロックランダム生成のメソッド */
	private final static double kakuritu = 0.75;


	/* TODO:generateBlockには生成鉱石IDと確率を渡す設計にする */
	void generateBlock(){
		//フィールドのクリア
		//人為的な妨害ブロックを置かせない機能もListener側に追加
		preClearBlock();

		//生成ポイントランダム化のために配列複製後シャッフル
		@SuppressWarnings("unchecked")
		ArrayList<GenerateLocation> genpoint = (ArrayList<GenerateLocation>) plg.genloc.clone();
		Collections.shuffle(genpoint);

		//シャッフルされた配列に対して前半○○％が生成ポイントに選出
		for( int i=0; i<genpoint.size() * kakuritu ; i++ ){
			Block bk = genpoint.get(i).getLocation().getBlock().getRelative( BlockFace.UP );
			//TODO:まだ前ウェーブの鉱石が破壊されていない場合の処理はどうする？
			//基本的に試合後半のウェーブの鉱石を価値の高いものにするため
			//すでに鉱石が存在しているとしても上書きする
			if( bk.isEmpty() ){
				bk.setType(Material.GOLD_BLOCK);
				plg.getServer().broadcastMessage( OreGatherGame.msgPrefix + "鉱石が出現しました！");
			}else{
				plg.getServer().broadcastMessage( OreGatherGame.msgPrefix + "一部の鉱石の生成に失敗しました！");
			}
		}
	}

	/* 鉱石ブロック生成前のブロックのクリア */
	void preClearBlock(){
		for( Iterator<GenerateLocation> iter_genloc = plg.genloc.iterator() ; iter_genloc.hasNext();){
			iter_genloc.next().getLocation().getBlock().getRelative( BlockFace.UP ).setType( Material.AIR );
		}
	}

	/* 時間経過ごとにブロックを生成する */
	@Override
	public void run() {
		if( plg.genloc.isEmpty() ) return;
		generateBlock();
	}

}
