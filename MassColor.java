package oopVisualize;

import java.awt.Color;

public class MassColor {
	//Helper class that will generate color for vehicles that
	//move in different traffic crowdedness, supporting Mass Mode.

	static Color[] colors = new Color[] {new Color(35,0,0), new Color(80,0,0), //0,1
			new Color(139,0,0), new Color(205,0,0), new Color(238,0,0), //2,3,4
			new Color(255,0,0) //5
	};

	public static Color getColor(int near){
		if (near<5){
			return colors[near];
		}else{
			return colors[5];
		}
		
	}
	
}
