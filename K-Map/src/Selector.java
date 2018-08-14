import java.util.ArrayList;

import javax.swing.JCheckBox;

@SuppressWarnings("rawtypes")
public class Selector extends JCheckBox implements Comparable{

	private static final long serialVersionUID = 1L;
	
	private boolean value;
	private int index;
	private int maxGroupSize;
	private ArrayList<ArrayList<Integer>> groups = new ArrayList<ArrayList<Integer>>();
	
	public Selector(int i) {
		this.index = i;
		
		this.value = false;
		this.maxGroupSize = 0;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public void clearGroups() {
		groups.clear();
	}
	
	public boolean getValue() {
		return this.value;
	}
	
	public void swapValue() {
		this.value = !this.value;
	}
	
	public void setValue(boolean newValue) {
		this.value = newValue;
	}
	
	public int getMaxGroupSize() {
		return this.maxGroupSize;
	}
	
	public void findMaxGroups(int order, Selector[] outputs) {
		findValidGroups(outputs, order);
		
		removeSmallerGroups();
		
		if(groups.size() != 0) {
			this.maxGroupSize = groups.get(0).size();
		}
		
		System.out.println(groups.toString());
	}
	
	public void removeSmallerGroups() {
		int max = 0;
		for(ArrayList<Integer> list : groups) {
			if(list.size() > max) {
				max = list.size();
			}
		}
		for(int i = 0; i < groups.size(); i++) {
			if(groups.get(i).size() < max) {
				groups.remove(i);
				i--;
			}
		}
	}
	
	public static boolean[] binaryRepresentation(int i, int order) {
		boolean[] binary = new boolean[order];
		
		for(int j = binary.length - 1; j >= 0; j--) {
			int k = (int) Math.pow(2, j);
			if(i >= k) {
				i -= k;
				binary[binary.length - j - 1] = true;
			}
		}
		return binary;
	}
	
	public void findValidGroups(Selector[] outputs, int order) {
		boolean[] binary = binaryRepresentation(this.index, order);
		ArrayList<boolean[]> g = new ArrayList<boolean[]>();
		g.add(binary);
		
		recursiveSearch(copy(g), order, 1, outputs);
		recursiveSearch(swapValue(copy(g), 0), order, 1, outputs);
	}
	
	public void recursiveSearch(ArrayList<boolean[]> indices, int order, int iterator, Selector[] outputs) {
		if(validList(indices, outputs)) {
			if(order == iterator) {
				ArrayList<Integer> validGroup = new ArrayList<Integer>();
				for(boolean[] b : indices) {
					validGroup.add(binaryArrayToInt(b));
				}
				groups.add(validGroup);
			}else {
				recursiveSearch(copy(indices), order, iterator + 1, outputs);
				recursiveSearch(swapValue(copy(indices), iterator), order, iterator + 1, outputs);
			}
		}
	}
	
	public boolean validList(ArrayList<boolean[]> list, Selector[] outputs) {
		for(boolean[] b : list) {
			if(!outputs[binaryArrayToInt(b)].getValue()) {
				return false;
			}
		}
		return true;
	}
	
	public int binaryArrayToInt(boolean[] b) {
		int total = 0;
		for(boolean i : b) {
			total *= 2;
			if(i) {
				total++;
			}
		}
		return total;
	}
	
	public ArrayList<boolean[]> copy(ArrayList<boolean[]> original){
		ArrayList<boolean[]> newArray = new ArrayList<boolean[]>();
		for(boolean[] b : original) {
			newArray.add(b);
		}
		return newArray;
	}
	
	public ArrayList<boolean[]> swapValue(ArrayList<boolean[]> b, int index) {
		for(int i = 0; i < b.size(); i += 2) {
			boolean[] newArray = new boolean[b.get(0).length];
			for(int j = 0; j < b.get(0).length; j++) {
				newArray[j] = b.get(i)[j];
			}
			newArray[index] = !newArray[index];
			b.add(i, newArray);
		}
		return b;
	}
	
	public ArrayList<ArrayList<Integer>> getGroups(){
		return this.groups;
	}
	
	public ArrayList<Integer> getNextGroup(ArrayList<Integer> covered){
		int[] ranking = new int[groups.size()];
		for(int i = 0; i < groups.size(); i++) {
			for(int j : covered) {
				if(groups.get(i).indexOf(j) != -1) {
					ranking[i]++;
				}
			}
		}
		int index = 0;
		int min = ranking[0];
		for(int i = 0; i < ranking.length; i++) {
			if(ranking[i] < min) {
				min = ranking[i];
				index = i;
			}
		}
		return this.groups.get(index);
	}
	
	public String toString() {
		String s = "";
		if(groups == null) {
			return "";
		}
		for(ArrayList<Integer> g : groups) {
			s += "[";
			for(int i : g) {
				s += i + ",";
			}
			s += "]";
		}
		return s;
	}

	public int compareTo(Object o) {
		if(o instanceof Selector) {
			Selector s = (Selector) o;
			if(this.getMaxGroupSize() == s.getMaxGroupSize()) {
				if(this.groups.size() == s.getGroups().size()) {
					return this.index - s.getIndex();
				}
				return this.groups.size() - s.getGroups().size();
			}
			return this.getMaxGroupSize() - s.getMaxGroupSize();
		}
		System.out.println("Selector was compared with a non-selector");
		return 1;
	}
}
