import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public class Solver extends JFrame {
//puzzleGrid stores the sudoku puzzle
private int[][] puzzleGrid = new int[9][9];
private JFormattedTextField[][] guiGrid = new JFormattedTextField[9][9];
private int stepCount = 0;
private static MaskFormatter cellFormat;
private JButton solve = new JButton("Solve");
private JButton clear = new JButton("Clear");
private GridLayout gridLay = new GridLayout(9,9);

public Solver() {
	super("Sudoku Solver");
	setResizable(false);
}

private boolean verifyCol(int x, int val) {
	//verifies that column x of puzzleGrid only contains 1 instance of val
	int count = 0;
	for (int i=0; i<9; i++) {
		if (puzzleGrid[x][i] == val)
				count++;
	}
	return (count <= 1);
}

private boolean verifyRow(int y, int val) {
	//verifies that row y of puzzleGrid only contains 1 instance of val
	int count = 0;
	for (int i=0; i<9; i++) {
		if (puzzleGrid[i][y] == val)
				count++;
	}
	return (count <= 1);
}

private boolean verifyBox(int x, int y, int val) {
	//verifies that the 3x3 grid of values at position x,y contains only 1 instance of val
	//for the purposes of this method, the puzzle is viewed as a 3x3 grid of boxes, so for example the (x,y) values (0, 1) correspond to the middle box of the top row of the puzzle
	int count = 0;
	int xShift = 3*x;
	int yShift = 3*y;
	for (int i=0; i<3; i++) {
		for (int j=0; j<3; j++) {
			if (puzzleGrid[xShift+i][yShift+j] == val)
				count++;
		}
	}
	return (count <= 1);
}

private void loadPuzzleGrid() {
	//transfers the values of the GUI grid into the puzzleGrid array for processing
	for (int x=0;x<9;x++) {
		for (int y=0;y<9;y++) {
			try {
				puzzleGrid[y][x] = Integer.parseInt((String)guiGrid[x][y].getValue());
			}catch (Exception ex) {
				puzzleGrid[y][x] = 0;
			}
		}
	}
}

private void loadGuiGrid() {
	//transfers the values of the puzzleGrid array into the GUI for display
	for (int x=0;x<9;x++) {
		for (int y=0;y<9;y++) {
			if (puzzleGrid[x][y] != 0)
				guiGrid[x][y].setValue(new Integer(puzzleGrid[y][x]));
		}
	}
}

private void initializeGrid(final Container pane) {
	//initializes the GUI's layout
	JPanel grid = new JPanel();
	grid.setLayout(gridLay);
	JPanel controls = new JPanel();
	controls.setLayout(new GridLayout(1,2));
	Dimension cellSize = new Dimension(40,40);
	Font cellFont = new Font("Arial", Font.BOLD, 40);
	
	try {
		cellFormat = new MaskFormatter("#");
	}catch (java.text.ParseException e) {
		System.err.println("Formatter is bad: " + e.getMessage());
		System.exit(-1);
	}
	
	for (int y=0;y<9;y++) {
		for (int x=0;x<9;x++) {
			guiGrid[x][y] = new JFormattedTextField(cellFormat);
			guiGrid[x][y].setColumns(1);
			guiGrid[x][y].setFont(cellFont);
			guiGrid[x][y].setHorizontalAlignment(SwingConstants.CENTER);
			guiGrid[x][y].setPreferredSize(cellSize);
			if (((x/3)+(y/3))%2 == 1) {
				guiGrid[x][y].setBackground(Color.LIGHT_GRAY);
			}
			guiGrid[x][y].setValue(null);
			grid.add(guiGrid[x][y]);
		}
	}
	
	controls.add(solve);
	controls.add(clear);
	
	solve.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			//load values from guiGrid into puzzleGrid
			loadPuzzleGrid();
			if (!sanityCheck(0,0)) {
				System.out.println("Sanity check failed. This puzzle is unsolvable!");
				JOptionPane.showMessageDialog(pane, "This puzzle cannot be solved. The same value appears twice within a single row, column or box.");
			} else if (solveGrid(0,0)){
				//load values from puzzleGrid to guiGrid
				loadGuiGrid();
			}
			else {
				System.out.println("Unsolvable!");
				JOptionPane.showMessageDialog(pane, "This puzzle cannot be solved.");
			}
			
		}
	});
	
	clear.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			for (int x=0;x<9;x++) {
				for (int y=0;y<9;y++) {
					guiGrid[x][y].setValue(null);
				}
			}
		}
	});
	
	pane.add(grid, BorderLayout.NORTH);
	pane.add(new JSeparator(), BorderLayout.CENTER);
	pane.add(controls, BorderLayout.SOUTH);
}

private static void loadGUI() {
	Solver frame = new Solver();
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.initializeGrid(frame.getContentPane());
	frame.pack();
	frame.setVisible(true);
}

private static int[] nextCoord(int x, int y) {
	//returns an int[] of length 2 corresponding to the next cell in puzzleGrid relative to cell x,y
	int[] coord;
	if (x==8 && y==8)
		coord = new int[]{9, 9};
	else if (x==8)
		coord = new int[]{0, y+1};
	else 
		coord = new int[]{x+1, y};
	return coord;
}

private void printGrid(){
	//prints the current state of puzzleGrid to the console
	System.out.println();
	for (int i = 0; i<9; i++) {
		for (int j = 0; j<9; j++) {
			System.out.print('|');
			System.out.print(puzzleGrid[i][j]);
		}
		System.out.println('|');
	}
	System.out.println();
}

private boolean sanityCheck(int x, int y) {
	//Checks for any obvious conflicts between currently filled cells
	int[] next = nextCoord(x,y);
	if (x==9 || y==9) {
		return true;
	}else if (puzzleGrid[x][y] != 0) {
		//this cell has a value so check for conflicts in the same row/column/box
		return verifyCol(x, puzzleGrid[x][y]) && verifyRow(y, puzzleGrid[x][y]) && verifyBox(x/3, y/3, puzzleGrid[x][y]) && sanityCheck(next[0],next[1]);
	}else {
		return sanityCheck(next[0], next[1]);
	}
}

private boolean solveGrid(int x, int y) {
	//recursively solves the sudoku puzzle via brute force
	int[] next = nextCoord(x, y);
	if (x == 9 || y == 9) {
		//all cells of puzzleGrid have been filled with valid entries; the puzzle has been solved, return true down the recursive stack
		printGrid();
		System.out.println("Solved in " + stepCount + " steps!");
		stepCount = 0;
		return true;
	}
	else if (puzzleGrid[x][y] != 0)
		//the current cell already has an initial value; skip to the next cell
		return solveGrid(next[0], next[1]);
	else {
		//the current cell is 'empty'; try each valid value for this cell sequentially
		for (int i=1;i<10;i++) {
			stepCount++;
			puzzleGrid[x][y] = i;
			if (verifyCol(x, i) && verifyRow(y, i) && verifyBox(x/3,y/3,i) && solveGrid(next[0], next[1])) {
				//the current cell value is valid and the puzzle has been solved; relay a true return value down the recursive stack 
				return true;
			}
		}
		//all possible valid values for the cell have been tried and the puzzle cannot be solved; reset the cell value to 'empty' and return false down the recursive stack to try a different value in a previous cell
		puzzleGrid[x][y] = 0;
		return false;
	}
}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				loadGUI();
			}
		});
	}
}
