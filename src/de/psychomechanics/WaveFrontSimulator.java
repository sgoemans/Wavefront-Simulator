package de.psychomechanics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import de.psychomechanics.Wavefront.EndlessLoopException;
import de.psychomechanics.Wavefront.NoPathFoundException;

public class WaveFrontSimulator extends JFrame implements IUICallback, ActionListener{

	private final JPanel gui = new JPanel(new BorderLayout(3, 3));
	private JPanel floorgrid;
	private final JLabel message = new JLabel("Wavefront Algorithm Simulator");

	private JButton btnNew = new JButton("New");
	private JButton btnNext = new JButton("Next");
	private JButton btnQuit = new JButton("Quit");

	private boolean simStart = false;
	private boolean lockFloor = false;

	private static final int GRIDSIZEX= 13;
	private static final int GRIDSIZEY = 13;
	private JButton[][] cell = new JButton[GRIDSIZEX][GRIDSIZEY];

	private String GOAL = "3";
	private String START = "2";
	private String FREE = "";
	private String WALL = "1";

	private Wavefront wf;
	private int[][] grid = new int[GRIDSIZEY][GRIDSIZEX];

	WaveFrontSimulator() {
		initializeGui();
	}

	public final void initializeGui() {
		// set up the main GUI
		gui.setBorder(new EmptyBorder(15, 15, 15, 15));
		JToolBar tools = new JToolBar();
		tools.setFloatable(false);
		gui.add(tools, BorderLayout.PAGE_START);
		tools.add(btnNew);
		tools.add(btnNext);
		tools.addSeparator();
		tools.add(btnQuit);
		tools.addSeparator();
		tools.add(message);

		floorgrid = new JPanel(new GridLayout(0, GRIDSIZEX));
		floorgrid.setBorder(new LineBorder(Color.BLACK));
		gui.add(floorgrid);

		btnNew.addActionListener(this);
		btnNext.addActionListener(this);
		btnQuit.addActionListener(this);

		btnNext.setEnabled(false);

		// Create the JButton Array which represents the tiles in the floorplan
		//
		//		Insets buttonMargin = new Insets(0,0,0,0);
		Dimension d = new Dimension(50, 50);
		for (int row = 0; row < cell.length; row++) {
			for (int col = 0; col < cell[row].length; col++) {
				JButton b = new JButton();
				b.addActionListener(this);
				cell[row][col] = b;
				cell[row][col].setBackground(Color.WHITE);
				cell[row][col].setPreferredSize(d);
			}
		}
		// Fill the floorplan with the corresponding JButtons
		for (int row = 0; row < GRIDSIZEY; row++) {
			for (int col = 0; col < GRIDSIZEX; col++) {
				floorgrid.add(cell[row][col]);
			}
		}
	}

	public final JComponent getGui() {
		return gui;
	}

	public static void main(String[] args) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				WaveFrontSimulator wf =	new WaveFrontSimulator();

				JFrame f = new JFrame("WaveFront");
				f.add(wf.getGui());
				f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				f.setLocationByPlatform(true);

				// Ensures the frame has the minimum required size
				// in order display the components within it
				f.pack();
				// Ensures the minimum size is enforced.
				f.setMinimumSize(f.getSize());
				f.setVisible(true);
			}
		};
		SwingUtilities.invokeLater(r);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {	
		JButton j = (JButton) ae.getSource();
		if(j.getText().equals("Quit")) {
			System.exit(0);
		}
		if(j.getText().equals("New")) {
			resetGrid();
			btnNext.setEnabled(true);
			simStart = true;
			lockFloor = false;
			return;
		}
		if(simStart) {
			if(j.getText().equals("Next")) {
				if(checkGoalAndCarExists()) {				
					if(!lockFloor) {
						lockFloor = true;
						// Create a two-dimensional array which reflects the current state of the grid in the UI.
						for (int row = 0; row < GRIDSIZEY; row++) {
							for (int col = 0; col < GRIDSIZEX; col++) {
								if(cell[row][col].getText().equals("")) {
									grid[row][col] = 0;
								} else {
									grid[row][col] = Integer.parseInt(cell[row][col].getText());
								}
							}
						}
						wf = new Wavefront(grid, this);
					}

					try {
						if(!wf.wavefrontPass()) {
						} else {
							ArrayList<Dimension> path = wf.getPath();
							JOptionPane.showMessageDialog(null, "Found!");
						}
					} catch (HeadlessException | EndlessLoopException | NoPathFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					JOptionPane.showMessageDialog(null, "Select location for start (2) and goal (3)");
				}
			}
		}
		if(simStart && !lockFloor) {
			if(j.getText().equals(START)) {
				j.setText("3");
			} else if(j.getText().equals(WALL)) {
				j.setText("2");
			} else if(j.getText().equals(FREE)) {
				j.setText("1");
			} else if(j.getText().equals(GOAL)) {
				j.setText("");
			}
		}
	}

	private boolean checkGoalAndCarExists() {
		boolean car = false;
		boolean goal = false;

		for (int row = 0; row < cell.length; row++) {
			for (int col = 0; col < cell[row].length; col++) {
				if(cell[row][col].getText().equals(GOAL)) goal = true;
				if(cell[row][col].getText().equals(START)) car = true;
			}
		}
		return(goal && car);
	}

	private void resetGrid() {
		for (int row = 0; row < cell.length; row++) {
			for (int col = 0; col < cell[row].length; col++) {
				if(col == 0 || row == 0 || col == GRIDSIZEX-1 || row == GRIDSIZEY-1) {
					cell[row][col].setText(WALL);
				} else {
					cell[row][col].setText("");
				}
				cell[row][col].setName("" + ((row * GRIDSIZEX ) + col));
				cell[row][col].setBackground(Color.WHITE);
			}
		}
	}

	@Override
	public void updateTileLabel(int row, int col, int value) {
		cell[row][col].setText(String.valueOf(value));		
	}

	@Override
	public void paintTile(int row, int col, int value) {
		cell[row][col].setBackground(Color.CYAN);

	}
}