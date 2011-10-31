import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Stack;
import java.util.Vector;

/**
 * This class is the context of the game. 
 * 
 * It extends AbstractSystemInputParser that read from the input and call the method in the manner defined below:
 * 1. (game started)
 * 2. setup()
 * 3. doTurn()
 * 4. beforeUpdate()
 * 5. Multiple calls to addAnt()/addFood()/addHill()/addWater()
 * 6. afterUpdate()
 * 7. doTurn()
 * 8. (go to step 4 until the end of the game)
 * 
 * @author glards
 *
 */
public class GameContext extends AbstractSystemInputParser {
	// Some constants
	private final int MAX_HILL = 2;
	private final int MAX_PLAYER = 8;
	
	// Public 
	public int loadTime;
	public int turnTime;
	public int rows;
	public int cols;
	public int turns;
	public int viewRadiusSquared;
	public int attackRadiusSquared;
	public int spawnRadiusSquared;

	// The current turn
	private int _turn = 0;
	
	// Number of tiles on the map
	private int _nbTiles = 0;
	
	// The bitset that represent the water tiles
	private BitSet _waters;
	
	// The bitset for the visible portion of the map
	private BitSet _visible;
	
	// The bitset for the discovered portion of the map
	private BitSet _discovered;
	
	// The bitset for the area discovered in this turn
	private BitSet _newWorld;
	
	private int[] _distances;
	
	// Our hills
	private Vector<Position> _ownHills = new Vector<Position>(MAX_HILL);
	
	// Enemy hills
	private Vector<Position> _enemyHills = new Vector<Position>(MAX_HILL * MAX_PLAYER);
	
	// Our ants
	private Vector<Position> _ownAnts = new Vector<Position>(10);
	private Vector<Position> _enemyAnts = new Vector<Position>(25);
	
	
	@Override
	public void setup(int loadTime, int turnTime, int rows, int cols,
			int turns, int viewRadius2, int attackRadius2, int spawnRadius2) {
		this.loadTime = loadTime;
		this.turnTime = turnTime;
		this.rows = rows;
		this.cols = cols;
		this.turns = turns;
		this.viewRadiusSquared = viewRadius2;
		this.attackRadiusSquared = attackRadius2;
		this.spawnRadiusSquared = spawnRadius2;
		
		// Calculate the number of tiles
		_nbTiles = cols*rows;
		
		// Create the map data structures
		_waters = new BitSet(_nbTiles);
		_visible = new BitSet(_nbTiles);
		_discovered = new BitSet(_nbTiles);
		_newWorld = new BitSet(_nbTiles);
		
		_distances = new int[_nbTiles];
		
		Arrays.fill(_distances, 0);
	}
	
	/**
	 * Utility method to get the index of the tile represented by row and col in a bitset or array.
	 * @param row The row index
	 * @param col The column index
	 * @return The index
	 */
	private int getIndex(int row, int col) {
		int idx = (row%rows)*cols + (col%cols);
		assert(idx < _nbTiles);
		return idx;
	}
	
	private void setSquare(BitSet s, int row, int col, int radius2) {
		int r = (int) Math.ceil(Math.sqrt(radius2));
		
		for (int x = 0; x <= r; x++) {
			int x2 = x*x;
			for (int y = 0; y <= r; y++) {
				int y2=y*y;
				if (x2+y2 < radius2) {
					s.set(getIndex(row+x, col+y));
					s.set(getIndex(row-x, col+y));
					s.set(getIndex(row+x, col-y));
					s.set(getIndex(row-x, col-y));
				}
			}
		}
	}

	@Override
	public void beforeUpdate() {
		// before receiving new state
		
		// We clear the visible portion
		_visible.clear();
		
		// Clear the new world
		_newWorld.clear();
		_newWorld.or(_discovered);
	}

	@Override
	public void addWater(int row, int col) {
		_waters.set(getIndex(row,col));
	}

	@Override
	public void addAnt(int row, int col, int owner) {
		if (owner == 0) {
			// Our ant
			setSquare(_visible, row, col, this.viewRadiusSquared);
			setSquare(_discovered, row, col, this.viewRadiusSquared);
		}
	}

	@Override
	public void addFood(int row, int col) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAnt(int row, int col, int owner) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void addHill(int row, int col, int owner) {
		if (owner > 0) {
			_enemyHills.add(new Position(row,col));
			_distances[getIndex(row,col)] = 0;
		} else {
			_ownHills.add(new Position(row,col));
		}
	}
	
	private boolean computeDistances(int idx) {
		return false;
	}
	
	private void computeDistancesForNewWorld() {
		Stack<Integer> s = new Stack<Integer>();
		
		 for (int i = _newWorld.nextSetBit(0); i >= 0; i = _newWorld.nextSetBit(i+1)) {
			 if (!computeDistances(i)) {
				 s.push(i);
			 }
		 }
		 
		 while (s.isEmpty()) {
			 int idx = s.pop();
			 
			 if (computeDistances(idx)) {
				 
			 } else {
				 
			 }
		 }
	}
	
	@Override
	public void afterUpdate() {
		/* Handle after update computation like :
		 *  - Compute the new territory		
		 *  - Compute the distance from the hills for new territory
		 */
		
		// Compute the new territory
		_newWorld.xor(_discovered);
		//computeDistancesForNewWorld();
		
		//TODO: Do we have to do special handling for case with two hills
		//computeTerritory(row, col);
	}

	@Override
	public void doTurn() {
		_turn++;
		
		if (Constants.DEBUG) {
			PrintWriter debug = Debug.getDebugFile();
			debug.println(String.format("#################### TURN %04d ####################", _turn));
			debug.println();
			debug.println("Map:");
			debug.println(Debug.drawMapBitSet(_waters, rows, cols));
			debug.println();
			debug.println();
			debug.println("Discovered:");
			debug.println(Debug.drawMapBitSet(_discovered, rows, cols));
			debug.println();
			debug.println();
			debug.println("New world:");
			debug.println(Debug.drawMapBitSet(_newWorld, rows, cols));
			debug.println();
			debug.println();
		}
	}

}
