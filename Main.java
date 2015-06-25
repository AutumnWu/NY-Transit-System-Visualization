package oopVisualize;

import java.awt.BorderLayout;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.vividsolutions.jts.geom.Coordinate;

public class Main extends JFrame {
	//The JFrame that contains all of the rest.
	final static int DELAY=50;
	public static void main(String[] args) throws InterruptedException {
		final int W = 620;
		final int H = 620;
		JFrame frame = new JFrame("Map Visualize");
		Board board = new Board();
		frame.add(board);
		frame.setSize(W, H);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//while (true) {
			//board.repaint();
			//Thread.sleep(DELAY); // delay time for each cycle.
	//	}
	}
}



