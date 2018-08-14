
public class ContextFreeGrammar {
	private static final String[] keyWords = new String[] {"XNOR", "XOR", "NOR", "OR", "NAND", "AND"};
	
	public static void main(String[] args) {
		memberOfSet(4, "(A or B) or !C", 3, 'A');
	}
	
	public static Boolean memberOfSet(int possibleMember, String conditions, int order, char startingChar) {
		boolean[] binary = Selector.binaryRepresentation(possibleMember, order);
		
		binary = reverseOrder(binary);
		conditions = conditions.replaceAll(" ", "");
		conditions = conditions.toUpperCase();
		
		return recursiveGrammar(binary, conditions, order, startingChar);
	}
	
	public static boolean[] reverseOrder(boolean[] input) {
		boolean b = false;
		
		for(int i = 0; i < input.length / 2; i++) {
			b = input[i];
			input[i] = input[input.length - i - 1];
			input[input.length - i - 1] = b;
		}
		
		return input;
	}
	
	public static Boolean recursiveGrammar(boolean[] binary, String conditions, int order, char startingChar) {
		//if there is just a character (or !A), then return its evaluation
		if(conditions.length() == 2) {
			if(conditions.charAt(0) != '!') {
				return null;
			}
			if(conditions.charAt(1) - startingChar < order) {
				return !binary[conditions.charAt(1) - startingChar];
			}
		}else if(conditions.length() == 1) {
			return binary[conditions.charAt(0) - startingChar];
		}else if(conditions.length() == 0) {
			System.out.println("Error! Empty statement!");
			return null;
		}
		
		//if there is only ONE group, then remove outer parenthesis and continue
		if(onlyOneGroup(conditions)) {
			if(conditions.charAt(0) == '!') {
				return !recursiveGrammar(binary, conditions.substring(2, conditions.length() - 1), order, startingChar);
			}else {
				return recursiveGrammar(binary, conditions.substring(1, conditions.length() - 1), order, startingChar);
			}
		}
		
		//otherwise, determine last group and continue. Let remainder be first group and continue that as well. Then perform and return their operation.
		int lastGroupIndex = getLastGroup(conditions);
		String command = "";
		String firstGroup = conditions.substring(0, lastGroupIndex);
		boolean first, last;
		
		if(lastGroupIndex >= 4) {
			command = conditions.substring(lastGroupIndex - 4, lastGroupIndex);
		}else {
			command = conditions.substring(0, lastGroupIndex);
		}
		
		//XNOR
		if(command.indexOf("XNOR") != -1) {
			firstGroup = firstGroup.substring(0, firstGroup.length() - 4);
			first = recursiveGrammar(binary, firstGroup, order, startingChar);
			last = recursiveGrammar(binary, conditions.substring(lastGroupIndex), order, startingChar);
			
			return !((first && !last) || (!first && last));
		}
		
		//XOR
		if(command.indexOf("XOR") != -1) {
			firstGroup = firstGroup.substring(0, firstGroup.length() - 3);
			first = recursiveGrammar(binary, firstGroup, order, startingChar);
			last = recursiveGrammar(binary, conditions.substring(lastGroupIndex), order, startingChar);
			
			return (first && !last) || (!first && last);
		}
		
		//NOR
		if(command.indexOf("NOR") != -1) {
			firstGroup = firstGroup.substring(0, firstGroup.length() - 3);
			first = recursiveGrammar(binary, firstGroup, order, startingChar);
			last = recursiveGrammar(binary, conditions.substring(lastGroupIndex), order, startingChar);
			
			return !first && !last;
		}
		
		//OR
		if(command.indexOf("OR") != -1) {
			firstGroup = firstGroup.substring(0, firstGroup.length() - 2);
			first = recursiveGrammar(binary, firstGroup, order, startingChar);
			last = recursiveGrammar(binary, conditions.substring(lastGroupIndex), order, startingChar);
			
			return first || last;
		}
		
		//NAND
		if(command.indexOf("NAND") != -1) {
			firstGroup = firstGroup.substring(0, firstGroup.length() - 4);
			first = recursiveGrammar(binary, firstGroup, order, startingChar);
			last = recursiveGrammar(binary, conditions.substring(lastGroupIndex), order, startingChar);
			
			return !(first && last);
		}
		
		//AND
		if(command.indexOf("AND") != -1) {
			firstGroup = firstGroup.substring(0, firstGroup.length() - 3);
			first = recursiveGrammar(binary, firstGroup, order, startingChar);
			last = recursiveGrammar(binary, conditions.substring(lastGroupIndex), order, startingChar);
			
			return first && last;
		}
		
		//if all fails, print an error and return null
		System.out.println("Error! No group or condition detected!");
		return null;
	}
	
	public static int getLastGroup(String conditions) {
		int index = 0;
		
		if(conditions.charAt(conditions.length() - 1) == ')') {
			int counter = -1;
			index = conditions.length() - 2;
			while(counter < 0 && index >= 0) {
				if(conditions.charAt(index) == '(') {
					counter++;
				}else if(conditions.charAt(index) == ')') {
					counter--;
				}
				index--;
			}
			return index + 1;
		}
		
		for(String s : keyWords) {
			if(conditions.lastIndexOf(s) + s.length() > index) {
				index = conditions.lastIndexOf(s) + s.length();
			}
		}
		
		return index;		
	}
	
	public static boolean onlyOneGroup(String conditions) {
		if(conditions.charAt(0) != '!' && conditions.charAt(0) != '(') {
			return false;
		}
		String copy = conditions;
		int count = 0;
		
		if(copy.indexOf(0) == '!') {
			copy = copy.substring(1);
		}
		for(int i = 0; i < conditions.length(); i++) {
			if(conditions.charAt(i) == '(') {
				count++;
			}
			if(conditions.charAt(i) == ')') {
				count--;
			}
			if(count == 0 && i != conditions.length() - 1) {
				return false;
			}
		}
		if(count != 0) {
			System.out.println("Error! Unequal number of parentheses!");
			return false;
		}
		return true;
	}
}