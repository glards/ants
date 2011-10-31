import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.BitSet;


public class Debug {
	private static PrintWriter _debug = null;
	
	public static String drawMapBitSet(BitSet s, int rows, int cols) {
		StringBuffer sb = new StringBuffer();
		String lineSep = System.getProperty("line.separator");
		
		for (int i = 0; i < s.length(); i++) {
			if (s.get(i)) {
				sb.append('#');
			} else {
				sb.append(' ');
			}
			
			if (i%cols == 0) {
				sb.append(lineSep);
			}
		}
		
		return sb.toString();
	}
	
	public static PrintWriter getDebugFile() {
		if (_debug == null) {
			try {
				_debug = new PrintWriter("debug.log");
			} catch (FileNotFoundException fnfe) {
			}
		}
		return _debug;
	}
}
