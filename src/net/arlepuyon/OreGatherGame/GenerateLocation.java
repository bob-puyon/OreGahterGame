package net.arlepuyon.OreGatherGame;

import org.bukkit.Location;

public class GenerateLocation {
	Location loc;
	String blockgrade;

	GenerateLocation( Location location, String bg){
		loc = location;
		blockgrade = bg;
	}


	//
	public Location getLocation(){
		return this.loc;
	}
	//
	public String getBlockGrade(){
		return this.blockgrade;
	}

	//ブロック生成ポイントの設定用文字列化
    public String getStringForConfig(){
    	return ( convertLocationToString( loc ) +","+ blockgrade.toString() );
    }

    //ロケーション部分を設定用文字列化
	private String convertLocationToString( Location loc ){
		return (loc.getWorld().getName() +","+ loc.getBlockX() +","+ loc.getBlockY() +","+ loc.getBlockZ() );
	}

}
