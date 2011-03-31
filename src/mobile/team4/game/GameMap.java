package mobile.team4.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

public class GameMap {
	int[][] map;
	
	public void print_map() {
		for (int row = 0; row < map.length; row++) {
			for (int col = 0; col < map[row].length; col++) {
		        System.out.print(Integer.toString(map[row][col]) + " ");
			}
			System.out.println();
		}
	}
	
	GameMap (int rows, int cols) {
		map = new int[rows][cols];
		
		// do i actually need to initialize to zero
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				map[i][j] = 0;
			}
		}
		
	}
	
	public void placeWall(WallPiece piece, Point location) {
		// using 1 to marked filled, change to represent wall pieces 
		// vs cannon vs filled floor, etc
		for (Point point : piece.shape) {
			map[location.x + point.x][location.y + point.y] = 1;
		}
	}
	
	public void placeCannon(Cannon cannon, Point location) {
		
	}
}