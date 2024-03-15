package org.processmining.slpnminer.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class StrToExp {
	
	static public double converStringToMathExp(String calculateString, HashMap<String, Double> strToDouble) {
        return calculateInversePolandExpression(getInversePolandExpression(calculateString),strToDouble);
    }

//    static public double converStringToMathExp(String calculateString) {
//        return calculateInversePolandExpression(getInversePolandExpression2(calculateString));
//    }
//
//    static public double converStringToMathExp(
//            Map<String, String> constantMap, String calculateString) {
//        double result = 0;
//        for (String str : constantMap.keySet()) {
//            calculateString = calculateString.replaceAll(str,
//                    constantMap.get(str));
//        }
//        result = calculateInversePolandExpression(getInversePolandExpression(calculateString));
//        return result;
//    }


    static private double calculateInversePolandExpression(
            List<String> inversePolandExpression) {
        double result = 0;
        Stack<Double> calculateStack = new Stack<Double>();
        for (String str : inversePolandExpression) {
            if (str.equals("+") || str.equals("-") || str.equals("*")
                    || str.equals("/")) {

                double t1 = Double.valueOf(calculateStack.pop());
                double t2 = Double.valueOf(calculateStack.pop());
                result = simpleCalculate(t2, t1, str);
                calculateStack.push(result);
            } else {
                calculateStack.push(Double.valueOf(str));
            }
        }
//        System.out.println(String.valueOf(result));
        return result;
    }

    static private double calculateInversePolandExpression(
            List<String> inversePolandExpression,
            HashMap<String, Double> strToDouble) {
    	
        double result = 0;
        Stack<Double> calculateStack = new Stack<Double>();
        for (String str : inversePolandExpression) {

            if (str.equals("+") || str.equals("-") || str.equals("*")
                    || str.equals("/")) {
                // do the calculation for two variables.
                double p1 = calculateStack.pop();
                double p2 = calculateStack.pop();
                result = simpleCalculate(p2,p1,str);
                calculateStack.push(result);
            } else {
                if(strToDouble.containsKey(str)){
                    calculateStack.push(strToDouble.get(str));
                }
                else{
                    calculateStack.push(Double.valueOf(str));
                }
            }
        }

        return result;
    }
    
    public static List<String> getInversePolandExpression(String exp) {
    	
    	if (exp == null)
    		return null;
    	List<String> result2 = new ArrayList<>();
    	int len = exp.length();
    	Stack<Character> operator = new Stack<Character>(); 
    	Stack<String> reversePolish = new Stack<String>();
    	//avoid checking empty
    	operator.push('#');
    	for (int i = 0; i < len;) {
    		//deal with space
    		while (i < len && exp.charAt(i) == ' ')
    			i++;
    		if (i == len)
    			break;
    		//if is number
    		
    		if (isVar(exp.charAt(i))) {
    			String num = "t";
    			i++;
    			while (i < len && isNum(exp.charAt(i))) 
        			num += exp.charAt(i++);
    			reversePolish.push(num);
    		} 
    		else if (isNum(exp.charAt(i))) {
    			String num = "";
    			while (i < len && isNum(exp.charAt(i))) 
        			num += exp.charAt(i++);
    			reversePolish.push(num);
    		} 
    		
    		else if (isOperator(exp.charAt(i))) {
    			char op = exp.charAt(i);
    			switch (op) {
    			case '(':
    				operator.push(op);
    				break;
    			case ')':
    				while (operator.peek() != '(')
    					reversePolish.push(Character.toString(operator.pop()));
    				operator.pop();
    				break;
				case '+':
				case '-':
					if (operator.peek() == '(')
						operator.push(op);
					else {
						while (operator.peek() != '#' && operator.peek() != '(')
							reversePolish.push(Character.toString(operator.pop()));
						operator.push(op);
					}
					break;
				case '*':
				case '/':
					if (operator.peek() == '(')
						operator.push(op);
					else {
						while (operator.peek() != '#' && operator.peek() != '+' &&
								operator.peek() != '-' && operator.peek() != '(')
							reversePolish.push(Character.toString(operator.pop()));
						operator.push(op);
					}
					break;
				}
    			i++;
    		}
    	}
    	while (operator.peek() != '#')
    		reversePolish.push(Character.toString(operator.pop()));
    	while (!reversePolish.isEmpty()) {
    			String temp1 = reversePolish.pop();
    			result2.add(0,temp1);
    		}

    	return result2;
    }
    
    public static boolean isOperator(char c) {
    	return c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')';
    }
    
    public static boolean isNum(char c) {
    	return c - '0' >= 0 && c - '0' <= 9;
    }
    
    public static boolean isVar(char c) {
    	return c == 't';
    }

  
 
    static private List<String> getInversePolandExpression2(
            String normalExpression) {
        List<String> inversePolandExpression = new ArrayList<String>();
        char[] normalChararray = (normalExpression + "$").toCharArray();
        //
        Stack<String> signStack = new Stack<String>();
        List<Stack<String>> signStackList = new ArrayList<Stack<String>>();
        signStackList.add(signStack);
        //
        int level = 0;

        int pointPosition = 0;
        double tempNumber = 0;
        boolean isInInteger = true;

        for (int i = 0; i < normalChararray.length; i++) {
            char tempChar = normalChararray[i];
            //
            if (tempChar >= '0' && tempChar <= '9') {
                //
                if (isInInteger) {
                    tempNumber = tempNumber * 10 + (int) (tempChar - 48);
                }
                // ?
                else {
                    tempNumber += (double) (tempChar - 48)
                            * Math.pow(0.1, i - pointPosition);
                }

            }
            // ?
            else if (tempChar == '.') {
                isInInteger = false;
                pointPosition = i;
            }
            //
            else if (tempChar == '+' || tempChar == '-' || tempChar == '*'
                    || tempChar == '/' || tempChar == '$') {
                //
                isInInteger = true;
                // ?
                if (tempNumber > 0) {
                    inversePolandExpression.add(String.valueOf(tempNumber));
                }
                // 0
                tempNumber = 0;
                // ???
                if ((tempChar == '+') || (tempChar == '-')
                        || tempChar == '$') {

                    while (!signStackList.get(level).isEmpty()) {
                        //
                        inversePolandExpression.add(signStackList
                                .get(level).pop());
                    }
                }
                // ?

                signStackList.get(level).push(tempChar + "");

            } else if (tempChar == '(') {
                signStack = new Stack<String>();
                signStackList.add(signStack);
                level++;
            } else if (tempChar == ')') {
                //
                isInInteger = true;
                // ?
                if (tempNumber > 0) {
                    inversePolandExpression.add(String.valueOf(tempNumber));
                }

                // 0
                tempNumber = 0;
                // ???

                while (!signStackList.get(level).isEmpty()) {
                    //
                    inversePolandExpression.add(signStackList.get(level)
                            .pop());

                }
                level--;
            }
        }
//        System.out.println(inversePolandExpression);
        return inversePolandExpression;
    }


    static private double simpleCalculate(double x, double y, String sign) {
        double result = 0;
        if (sign.equals("+")) {
            result = x + y;
        } else if (sign.equals("-")) {
            result = x - y;
        } else if (sign.equals("*")) {
            result = x * y;
        } else if (sign.equals("/")) {
            result = x / y;
        }
        return result;

    }
	
}
