import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import javax.swing.*;
import javax.swing.event.*;

public class MapGUI extends JFrame{

	private static final long serialVersionUID = 1L;
	
	private int size = 5;
	private final int MAX = 9;
	private final char START = 'A';
	
	private char[] inputs = new char[MAX];
	
	private Selector[] outputs;
	
	private JPanel main = new JPanel(new BorderLayout());
	private JPanel north = new JPanel(new BorderLayout());
	private JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
	private JPanel middle = new JPanel(new BorderLayout());
	private JPanel gridAndTop = new JPanel(new BorderLayout());
	private JPanel level1 = new JPanel(new FlowLayout());
	private JPanel level2 = new JPanel();
	private JPanel topLabels = new JPanel();
	private JPanel sideLabels = new JPanel();
	private JPanel middleWrapper = new JPanel(new FlowLayout());
	private JPanel topLabelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
	private JPanel sideLabelWrapper = new JPanel(new FlowLayout(FlowLayout.TRAILING));
	private JPanel formulaInput = new JPanel(new FlowLayout(FlowLayout.LEFT));
	
	private JTextArea mapSolution = new JTextArea(1, 60);
	
	private JTextField kMapInput = new JTextField(60);
	
	private JButton findSolution = new JButton("Solve K-Map");
	private JButton makeMap = new JButton("Draw K-Map");
	
	private JSlider order = new JSlider(1, MAX, size); //Need to set initial display!
	
	class SliderListener implements ChangeListener{
		public void stateChanged(ChangeEvent e) {
			JSlider source = (JSlider) e.getSource();
			if(!source.getValueIsAdjusting()) {
				int s = source.getValue();
				if(s != size) {
					size = s;
					setTable(size);
				}
			}
		}
	}
	
	class BoxListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			outputs[Integer.parseInt(e.getActionCommand())].swapValue();
		}
	}
	
	class ButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand().equals("solveKMap")) {
				assignMaxGroups();
			}else if(e.getActionCommand().equals("drawKMap")) {
				Boolean b = false;
				for(Selector s : outputs) {
					b = ContextFreeGrammar.memberOfSet(s.getIndex(), kMapInput.getText(), size, START);
					if(b == null) {
						System.out.println("Error, null returned.");
					}else if(b){
						s.setValue(b);
						s.setSelected(true);
					}else {
						s.setValue(b);
						s.setSelected(false);
					}
				}
				assignMaxGroups();
			}
		}
	}
	
	public void assignMaxGroups() {
		PriorityQueue<Selector> toAssign = new PriorityQueue<Selector>();
		
		for(Selector s : outputs) {
			s.clearGroups();
			if(s.getValue()) {
				s.findMaxGroups(size, outputs);
			
				toAssign.add(s);
			}
		}
		
		ArrayList<ArrayList<Integer>> allSolutions = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> covered = new ArrayList<Integer>();
		Selector current;
		
		while(!toAssign.isEmpty()) {
			current = toAssign.remove();
			allSolutions.add(current.getNextGroup(covered));
			for(int i : current.getNextGroup(covered)) {
				covered.add(i);
				toAssign.remove(outputs[i]);
			}
		}
		
		translate(allSolutions);
	}
	
	public void translate(ArrayList<ArrayList<Integer>> input) {
		String output = "";
		ArrayList<Integer> differences = new ArrayList<Integer>();
		int iterations = 0;
		
		if(input.size() == 0) {
			output = "False";
		}else if(input.get(0).size() == Math.pow(2, size)){
			output = "True";
		}else {			
			for(ArrayList<Integer> list : input) {
				differences.clear();
				
				Collections.sort(list);
				
				for(int i = 1; i < list.size(); i *= 2) {
					differences.add(log2(list.get(i) - list.get(0)));
				}
				
				boolean[] onOrOff = Selector.binaryRepresentation(list.get(0), size);
				
				output += "(";
				
				for(int i = 0; i < onOrOff.length; i++) {
					if(differences.indexOf(i) == -1) {
						if(!onOrOff[size - i - 1]) {
							output += "!";
						}
						output += inputs[i] + " AND ";
					}
				}
				
				output = output.substring(0, output.length() - 5) + ") OR ";
			}
			output = output.substring(0, output.length() - 4);
		}
		mapSolution.setText(output);
	}
	
	public int log2(int input) {
		int counter = 0;
		while(input > 1) {
			input /= 2;
			counter++;
		}
		return counter;
	}
	
	public void setTable(int s) {
		int topX = (int) Math.pow(2, (s + 1) / 2);
		int topY = (s + 1) / 2;
		int sideY = (int) Math.pow(2, s / 2) + topY;
		int gap = 0;
		int gap2 = 0;
		int skip = topY;
		
		topLabels.removeAll();
		sideLabels.removeAll();
		
		switch(s) {
		case 6:
		case 7: gap2 = 1;
		break;
		case 8:
		case 9:
		case 10: gap2 = 2;
		gap = 1;
		sideY--;
		skip--;
		break;
		}
		
		topLabels.setLayout(new GridLayout(topY, topX, 10 + gap, 0));
		sideLabels.setLayout(new GridLayout(sideY, 1, 0, 4 + gap2));
		
		outputs = new Selector[(int) Math.pow(2, s)];
		
		level2.removeAll();
		level1.remove(level2);
		
		fillGrid(s);
		fillTopLabels(topX, topY);
		fillSideLabels(sideY, skip);
		
		level1.add(level2);
		revalidate();
	}
	
	public void fillTopLabels(int columns, int rows) {
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j++) {
				String s = "";
				if((j / (int) (Math.pow(2, i))) % 2 == 0) {
					s += "!";
				}
				s += inputs[i];
				JTextArea t = new JTextArea(s);
				topLabels.add(t);
			}
		}
	}
	public void fillSideLabels(int rows, int gap) {
		String s = "";
		for(int i = 0; i < gap; i++) {
			JPanel t = new JPanel();
			sideLabels.add(t);
		}
		for(int i = 0; i < rows - gap; i++) {
			boolean[] b = Selector.binaryRepresentation(i, size);
			s = "";
			for(int j = 0; j < b.length / 2; j++) {
				if(!b[b.length - j - 1]) {
					s += "!";
				}
				s += inputs[j + (size + 1) / 2] + " ";
			}
			JTextArea t = new JTextArea(s);
			sideLabels.add(t);
		}
	}
	
	public void fillGrid(int s) {		
		if(s <= 2) {
			level2.setLayout(new GridLayout(1, 2, 0, 0));
			
			level2.add(addSelector(0));
			level2.add(addSelector(1));
			
			if(s == 2) {
				level2.setLayout(new GridLayout(2, 2, 0, 0));
				
				level2.add(addSelector(2));
				level2.add(addSelector(3));
			}
		}else {
			int length = (int) Math.pow(2, (s + 1) / 2);
			
			if(s % 2 == 1) {
				level2.setLayout(new GridLayout(1, 2, 2 * (s - 3), 2 * (s - 3)));
				
				level2.add(fillGridRecurse(s - 1, length, 0));
				level2.add(fillGridRecurse(s - 1, length, length / 2));
			}else {
				level2.setLayout(new GridLayout(2, 2, 2 * (s - 4), 2 * (s - 4)));
				
				level2.add(fillGridRecurse(s - 2, length, 0));
				level2.add(fillGridRecurse(s - 2, length, length / 2));
				level2.add(fillGridRecurse(s - 2, length, length * length / 2));
				level2.add(fillGridRecurse(s - 2, length, length / 2 * (length + 1)));
			}
		}
	}
	
	public Selector addSelector(int num) {
		Selector x = new Selector(num);
		x.addActionListener(new BoxListener());
		x.setActionCommand("" + num);
		
		boolean[] sequence = Selector.binaryRepresentation(num, size);
		String toolTip = num + " (";
		
		for(int i = sequence.length - 1; i >= 0; i--) {
			if(!sequence[i]) {
				toolTip += "!";
			}
			toolTip += inputs[sequence.length - 1 - i] + " ";
		}
		
		toolTip = toolTip.substring(0, toolTip.length() - 1) +  ")";
		
		x.setToolTipText(toolTip);
		
		outputs[num] = x;
		
		return x;
	}
	
	public JPanel fillGridRecurse(int s, int length, int start) {
		JPanel p;
		if(s <= 4) {
			p = new JPanel(new GridLayout(2, 2, 0, 0));
		}else {
			p = new JPanel(new GridLayout(2, 2, 2 * (s - 4), 2 * (s - 4)));
		}
		if(s == 2) {
			p.add(addSelector(start));
			p.add(addSelector(start + 1));
			p.add(addSelector(start + length));
			p.add(addSelector(start + length + 1));
		}else {
			int a = (int) Math.pow(2, (s + 1) / 2);
			
			p.add(fillGridRecurse(s - 2, length, start));
			p.add(fillGridRecurse(s - 2, length, start + a / 2));
			p.add(fillGridRecurse(s - 2, length, start + length * a / 2));
			p.add(fillGridRecurse(s - 2, length, start + (length + 1) * (a / 2)));
		}
		return p;
	}
	
	public MapGUI() {
		for(int i = 0; i < MAX; i++) {
			inputs[i] = (char) (START + i);
		}
		
		mapSolution.setLineWrap(true);
		mapSolution.setWrapStyleWord(true);
		
		order.setMajorTickSpacing(1);
		order.setPaintTicks(true);
		order.setPaintLabels(true);
		order.addChangeListener(new SliderListener());
		
		findSolution.addActionListener(new ButtonListener());
		findSolution.setActionCommand("solveKMap");
		
		makeMap.addActionListener(new ButtonListener());
		makeMap.setActionCommand("drawKMap");
		
		formulaInput.add(makeMap);
		formulaInput.add(kMapInput);
		
		north.add(formulaInput, BorderLayout.NORTH);
		north.add(order, BorderLayout.SOUTH);
		
		topLabelWrapper.add(topLabels);
		sideLabelWrapper.add(sideLabels);
		
		gridAndTop.add(topLabelWrapper, BorderLayout.NORTH);
		gridAndTop.add(level1, BorderLayout.CENTER);
		
		middle.add(gridAndTop, BorderLayout.CENTER);
		middle.add(sideLabelWrapper, BorderLayout.WEST);
		
		south.add(findSolution);
		south.add(mapSolution);
		
		middleWrapper.add(middle);
		
		main.add(north, BorderLayout.NORTH);
		main.add(middleWrapper, BorderLayout.CENTER);
		main.add(south, BorderLayout.SOUTH);
		
		setTable(size);
	}
	
	public JPanel getPane() {
		return this.main;
	}
	
	public static void main(String[] args) {
		MapGUI gui = new MapGUI();
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setSize(825, 725);
		gui.setTitle("K-Map Generator and Solver");
		gui.setVisible(true);
		gui.setContentPane(gui.getPane());
	}
}
