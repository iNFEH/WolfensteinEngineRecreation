/** Stores the walls that define the game level. */
public class Map {

	// @formatter:off
	// 0 = empty; 1 = gray stone; 2 = red brick; 3 = wood.
	private final int[][] map = {
		{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2},
		{1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 3, 0, 0, 3, 2, 2, 0, 0, 0, 2, 2},
		{1, 0, 0, 1, 3, 3, 3, 3, 3, 3, 1, 0, 0, 0, 1, 3, 0, 0, 3, 2, 0, 0, 0, 0, 0, 2},
		{1, 0, 0, 1, 3, 0, 0, 0, 0, 3, 1, 0, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2},
		{1, 0, 0, 0, 0, 0, 0, 0, 0, 3, 1, 0, 0, 0, 1, 3, 3, 3, 3, 2, 0, 0, 0, 2, 2, 2},
		{1, 0, 0, 1, 3, 0, 0, 0, 0, 3, 1, 0, 0, 0, 1, 2, 2, 2, 2, 2, 0, 0, 0, 2, 2, 2},
		{1, 1, 1, 1, 3, 0, 0, 0, 0, 3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2},
		{1, 1, 1, 1, 3, 0, 0, 0, 0, 3, 1, 0, 0, 0, 1, 2, 2, 2, 2, 2, 0, 0, 0, 2, 2, 2},
		{1, 1, 1, 1, 3, 3, 3, 0, 3, 3, 1, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2},
		{1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 0, 1, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 2},
		{1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 2, 2, 0, 0, 0, 2, 2, 0, 2},
		{1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 2, 2, 2, 0, 2},
		{1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2},
		{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
	};
	// @formatter:on

	/**
	 * Returns the wall grid for rendering.
	 *
	 * @return The wall type grid, 0 for empty cells.
	 */
	public int[][] getMap() {
		return map;
	}

	/**
	 * Checks whether a grid position blocks player movement.
	 *
	 * @param x The map column.
	 * @param y The map row.
	 * @return {@code true} for any nonzero cell or a position outside the map.
	 */
	public boolean checkIfWall(final int x, final int y) {
		return getCellType(x, y) != 0;
	}

	/**
	 * Returns the cell type, treating positions outside the map as gray stone walls.
	 *
	 * @param x The map column.
	 * @param y The map row.
	 * @return The stored cell value, including 0 for empty cells, or 1 if outside the map.
	 */
	public int getCellType(final int x, final int y) {
		if (y < 0 || y >= map.length || x < 0 || x >= map[y].length) {
			return 1;
		}
		return map[y][x];
	}
}
