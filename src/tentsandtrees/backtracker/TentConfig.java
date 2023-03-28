package tentsandtrees.backtracker;

import tentsandtrees.test.ITentsAndTreesTest;

import java.io.*;
import java.util.Collection;
import java.util.ArrayList;

/**
 *  The full representation of a configuration in the TentsAndTrees puzzle.
 *  It can read an initial configuration from a file, and supports the
 *  Configuration methods necessary for the Backtracker solver.
 *
 *  @author RIT CS
 *  @author Tiffany Lee
 */
public class TentConfig implements Configuration, ITentsAndTreesTest {
    /** square dimension of field */
    private static int DIM;
    /** amount of tents in each row */
    private static ArrayList<Integer> rowTents;
    /** amount of tents in each column */
    private static ArrayList<Integer> columnTents;
    /** grid of cells containing empty(.), grass(-), tree(%), or tent(^) */
    private final char[][] grid;
    /** cursor's row position */
    private int cursorRow;
    /** cursor's column position */
    private int cursorColumn;

    /**
     * Construct the initial configuration from an input file whose contents
     * are, for example:
     * <pre>
     * 3        # square dimension of field
     * 2 0 1    # row looking values, top to bottom
     * 2 0 1    # column looking values, left to right
     * . % .    # row 1, .=empty, %=tree
     * % . .    # row 2
     * . % .    # row 3
     * </pre>
     * @param filename the name of the file to read from
     * @throws IOException if the file is not found or there are errors reading
     */
    public TentConfig(String filename) throws IOException {
        try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
            // get the field dimension
            DIM = Integer.parseInt(in.readLine());
            // get tent amount in each row
            String[] rowVals = in.readLine().split("\\s+");
            rowTents = new ArrayList<>();
            for(String value : rowVals){
                rowTents.add(Integer.parseInt(value));
            }
            // get tent amount in each column
            String[] columnVals = in.readLine().split("\\s+");
            columnTents = new ArrayList<>();
            for(String value : columnVals){
                columnTents.add(Integer.parseInt(value));
            }
            // fills cells in grid
            grid = new char[DIM][DIM];
            for(int r = 0; r < DIM; r++){
                String[] row = in.readLine().split("\\s+");
                for(int c = 0; c < DIM; c++){
                    for(char cell : row[c].toCharArray()){
                        grid[r][c] = cell;
                    }
                }
            }
            this.cursorRow = 0;
            this.cursorColumn = -1;
        } // <3 Jim
    }

    /**
     * Copy constructor. Takes a config, other, and makes a full "deep" copy
     * of its instance data.
     * @param other the config to copy
     */
    private TentConfig(TentConfig other) {
        if (other.getCursorCol() + 1 < DIM) {
            this.cursorRow = other.getCursorRow();
            this.cursorColumn = other.getCursorCol() + 1;
        } else if(other.getCursorCol() + 1 >= DIM && other.getCursorRow() + 1 < DIM){
            this.cursorRow = other.getCursorRow() + 1;
            this.cursorColumn = 0;
        }
        this.grid = new char[other.getDIM()][other.getDIM()];
        for(int r = 0; r < DIM; r++){
            System.arraycopy(other.grid[r], 0, this.grid[r], 0, DIM);
        }
    }

    /**
     * Get the collection of successors from the current one
     *
     * @return All successors, valid and invalid
     */
    @Override
    public Collection<Configuration> getSuccessors() {
        Collection<Configuration> successorList = new ArrayList<>();
        TentConfig s1 = new TentConfig(this);
        TentConfig s2 = new TentConfig(this);
        if(s1.grid[s1.cursorRow][s1.cursorColumn] == TREE){
            successorList.add(s1);
        } else if(s1.grid[s1.cursorRow][s1.cursorColumn] == EMPTY){
            s1.grid[s1.cursorRow][s1.cursorColumn] = GRASS;
            s2.grid[s2.cursorRow][s2.cursorColumn] = TENT;
            successorList.add(s1);
            successorList.add(s2);
        }
        return successorList;
    }

    /**
     * Is the current configuration valid or not?
     *
     * @return true if valid, false otherwise
     */
    @Override
    public boolean isValid() {
        char cell = grid[this.cursorRow][this.cursorColumn];
        ArrayList<Integer> rowCheck = new ArrayList<>();
        ArrayList<Integer> columnCheck = new ArrayList<>();
        //Checks if at last cell in grid
        if(this.cursorColumn == DIM-1 && this.cursorRow == DIM-1){
            //Check each cell if the row and column values equal corresponding tents
            // and that each tree has at least one tent
            for(int r = 0; r < DIM; r++) {
                int rNum = 0;
                int cNum = 0;
                for (int c = 0; c < DIM; c++) {
                    int upRow, downRow, leftCol, rightCol;
                    upRow = downRow = r;
                    leftCol = rightCol = c;
                    upRow -= 1;
                    downRow += 1;
                    leftCol -= 1;
                    rightCol += 1;
                    //Check if each tree has at least one tent
                    if (getCell(r, c) == TREE) {
                        boolean checkTree = (upRow >= 0 && this.grid[upRow][c] == TENT) || //N
                                (downRow < DIM && this.grid[downRow][c] == TENT) ||        //S
                                (leftCol >= 0 && this.grid[r][leftCol] == TENT) ||         //W
                                (rightCol < DIM && this.grid[r][rightCol] == TENT);        //E
                        if (!checkTree) {
                            return false;
                        }
                    }
                    //Check row and column tent values
                    char cellR = getCell(r, c);
                    char cellC = getCell(c, r);
                    if (cellR == TENT) {
                        rNum += 1;
                    }
                    if (cellC == TENT) {
                        cNum += 1;
                    }
                }
                rowCheck.add(rNum);
                columnCheck.add(cNum);
            }
            return rowCheck.equals(rowTents) && columnCheck.equals(columnTents);
        //Checks if cell contains a tent
        } else if(cell == TENT){
            int upRow, downRow, leftCol, rightCol;
            upRow = downRow = this.cursorRow;
            leftCol = rightCol = this.cursorColumn;
            upRow -= 1;
            downRow += 1;
            leftCol -= 1;
            rightCol += 1;
            //Checks if a tent has at least one neighboring tree
            if((downRow >= DIM || this.grid[downRow][this.cursorColumn] != TREE) &&     //N
                    (upRow < 0 || this.grid[upRow][this.cursorColumn] != TREE) &&       //S
                    (leftCol < 0 || this.grid[this.cursorRow][leftCol] != TREE) &&      //W
                    (rightCol >= DIM || this.grid[this.cursorRow][rightCol] != TREE)) { //E
                return false;
            }
            //Checks that a tent has NO adjacent tents
            return (upRow < 0 || leftCol < 0 || this.grid[upRow][leftCol] != TENT) &&            //NW
                    (upRow < 0 || this.grid[upRow][this.cursorColumn] != TENT) &&                //N
                    (upRow < 0 || rightCol >= DIM || this.grid[upRow][rightCol] != TENT) &&      //NE
                    (leftCol < 0 || this.grid[this.cursorRow][leftCol] != TENT) &&               //W
                    (rightCol >= DIM || this.grid[this.cursorRow][rightCol] != TENT) &&          //E
                    (downRow >= DIM || leftCol < 0 || this.grid[downRow][leftCol] != TENT) &&    //SW
                    (downRow >= DIM || this.grid[downRow][this.cursorColumn] != TENT) &&         //S
                    (downRow >= DIM || rightCol >= DIM || this.grid[downRow][rightCol] != TENT); //SE

        }
        return true;
    }

    /**
     * Is the current configuration a goal?
     *
     * @return true if goal, false otherwise
     */
    @Override
    public boolean isGoal() {
        return this.cursorColumn == DIM-1 && this.cursorRow == DIM-1;
    }

    /**
     * Displays a string representation of the grid to standard output.
     *
     * @return string representation of the grid to standard output
     */
    @Override
    public String toString() {
        return getDisplay();
    }

    /**
     * Get the square dimension of the puzzle
     *
     * @return square dimension
     */
    @Override
    public int getDIM() {
        return DIM;
    }

    /**
     * Get the number of tents for a row
     *
     * @param row the row
     * @return number of tents
     */
    @Override
    public int getTentsRow(int row) {
        return rowTents.get(row);
    }

    /**
     * Get the number of tents for a column
     *
     * @param col the column
     * @return number of tents
     */
    @Override
    public int getTentsCol(int col) {
        return columnTents.get(col);
    }

    /**
     * Get the contents at a cell
     *
     * @param row the row
     * @param col the column
     * @return the contents
     */
    @Override
    public char getCell(int row, int col) {
        return grid[row][col];
    }

    /**
     * Get the cursor row location
     *
     * @return cursor row
     */
    @Override
    public int getCursorRow() {
        return cursorRow;
    }

    /**
     * Get the cursor column location
     *
     * @return cursor column
     */
    @Override
    public int getCursorCol() {
        return cursorColumn;
    }
}
