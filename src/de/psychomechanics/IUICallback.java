package de.psychomechanics;

public interface IUICallback {
	void updateTileLabel(int row, int col, int value);
	void paintTile(int row, int col, int value);
}
