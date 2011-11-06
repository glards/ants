
public class Position {
	// Static part
	public static int maxRows = 0;
	public static int maxCols = 0;
	
	/**
	 * Utility method to get the index of the tile represented by row and col in a bitset or array.
	 * @param row The row index
	 * @param col The column index
	 * @return The index
	 */
	public static int getIndex(int row, int col) {
		assert(GameContext.SetupCalled);
		int idx = (row%maxRows)*maxCols + (col%maxCols);
		assert(idx < maxCols*maxRows);
		return idx;
	}
	
	// Instance part
	private int row;
	private int col;
	
	public Position(int row, int col) {
		this.row = row;
		this.col = col;
	}
	
	public int getCol() {
		return this.col;
	}
	
	public int getRow() {
		return this.row;
	}
	
	/**
	 * Utility method to get the index of the tile represented by row and col in a bitset or array.
	 * @return The index
	 */
	public int getIndex() {
		assert(GameContext.SetupCalled);
		int idx = (row%maxRows)*maxCols + (col%maxCols);
		assert(idx < maxCols*maxRows);
		return idx;
	}
	
	@Override
	public int hashCode() {
		return this.row*491+this.col;
	}
	
	public boolean equals(Position other) {
		if (other == null) return false;
		if (other == this) return true;
		
		return row == other.row && col == other.col;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Position) return equals((Position) obj);
		return false;
	}
}
