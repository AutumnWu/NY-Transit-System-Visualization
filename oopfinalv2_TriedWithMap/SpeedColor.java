package oopfinalv2;

import java.awt.Color;

public class SpeedColor {
	static Color[] colors = new Color[] {new Color(102,0,0), //0
			new Color(204,0,0), new Color(255,128,0), new Color(255,178,102), //1,2,3
			new Color(255,255,0), //4
			new Color(255,255,102), new Color(204,255,153), new Color(153,255,51), //5,6,7
			new Color(128,255,0),new Color(51,255,51),new Color(0,204,0), //8,9,10
			new Color(0,153,0)//11
	};

	public static Color getColor(int speed){
		if (speed<4){
			return colors[0];
		}else if (speed>13){
			return colors[11];
		}else{
			return colors[speed-3];
		}
	}
	
}
