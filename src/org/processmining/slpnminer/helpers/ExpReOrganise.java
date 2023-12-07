package org.processmining.slpnminer.helpers;

import java.util.regex.*;

public class ExpReOrganise {

	public static void main(String[] args) {
        String expression = "(-t2^2*t4)/(-t1^3-3*t3^14*t4)";

        getTransformedString(expression);

    }
	
	private static String getTransformedString(String expression) {
		 // Define the pattern for detecting variables with exponentiation
        Pattern pattern = Pattern.compile("([a-zA-Z]\\d*)\\^\\d+");

        // Create a matcher with the given expression
        Matcher matcher = pattern.matcher(expression);

        String expression2 = expression;
        // Find and print all matches
        while (matcher.find()) {
//            System.out.println("Variable with exponentiation: " + transformPowerExpression(matcher.group()));
            expression2 = expression2.replace(matcher.group(), transformPowerExpression(matcher.group()));
        }
        return expression2;
	}
	
    private static String transformPowerExpression(String powerExpression) {
        // Split the expression into variable and exponent parts
        String[] parts = powerExpression.split("\\^");

        // Extract variable and exponent
        String variable = parts[0];
        int exponent = Integer.parseInt(parts[1]);

        // Create the transformed expression
        StringBuilder transformedExpression = new StringBuilder();
        for (int i = 0; i < exponent; i++) {
            transformedExpression.append(variable);
            if (i < exponent - 1) {
                transformedExpression.append("*");
            }
        }

        return transformedExpression.toString();
    }
	
}