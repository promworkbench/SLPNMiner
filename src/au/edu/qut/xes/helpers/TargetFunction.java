package au.edu.qut.xes.helpers;

import cern.jet.random.engine.MersenneTwister;
import de.congrace.exp4j.Calculable;

import org.apache.commons.math4.legacy.analysis.MultivariateFunction;
import org.apache.commons.math4.legacy.optim.InitialGuess;
import org.apache.commons.math4.legacy.optim.MaxEval;
import org.apache.commons.math4.legacy.optim.SimpleBounds;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.PopulationSize;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.Sigma;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.apache.commons.math3.analysis.UnivariateFunction;

import java.util.*;


public class TargetFunction {

    public static double getUEMSC(double[] x){

        String prob1 = "(t11*t12*t8)/(t11*t12*t7+t11*t7*t7+t11*t12*t8+t11*t7*t8+t12*t7*t8+t7*t7*t8+t12*t8*t8+t7*t8*t8)";
        String prob2 = "(t11*t12*t8)/(t11*t11*t12+t11*t12*t12+t11*t11*t7+t11*t12*t7+t11*t12*t8+t12*t12*t8+t11*t7*t8+t12*t7*t8)";
        String prob3 = "(t12*t8)/(t11*t11+t11*t12+t11*t8+t12*t8)";
        String prob4 = "(t11*t7*t8)/(t11*t11*t12+t11*t12*t12+t11*t11*t7+t11*t12*t7+t11*t12*t8+t12*t12*t8+t11*t7*t8+t12*t7*t8)";
        String prob5 = "(t11*t7)/(t11*t7+t11*t8+t7*t8+t8*t8)";
        String prob6 = "(t11*t7*t8)/(t11*t12*t7+t11*t7*t7+t11*t12*t8+t11*t7*t8+t12*t7*t8+t7*t7*t8+t12*t8*t8+t7*t8*t8)";

        HashMap<String, Double> strToDouble = new HashMap<>();
        strToDouble.put("t12", x[0]);
        strToDouble.put("t11", x[1]);
        strToDouble.put("t8", x[2]);
        strToDouble.put("t7", x[3]);
        for(String s:strToDouble.keySet()) {
	        System.out.println(s+" "+strToDouble.get(s));

        }
//        System.out.println(converStringToMathExp(prob1, strToDouble));
//        System.out.println(converStringToMathExp(prob2, strToDouble));
//        System.out.println(converStringToMathExp(prob3, strToDouble));
//        System.out.println(converStringToMathExp(prob4, strToDouble));

        return 1 - Math.max(0.00625 - converStringToMathExp(prob1, strToDouble), 0)
                 - Math.max(0.0375 - converStringToMathExp(prob2, strToDouble), 0)
                 - Math.max(0.375 - converStringToMathExp(prob3, strToDouble), 0)
                 - Math.max(0.3375- converStringToMathExp(prob4, strToDouble), 0)
                 - Math.max(0.1875 - converStringToMathExp(prob5, strToDouble), 0)
                 - Math.max(0.05625 - converStringToMathExp(prob6, strToDouble), 0);
    }

//    public static double getEntropy(double[] x){
//        return -0.3 * (Math.log(x[0]/(x[0]+x[1]+x[4])) / Math.log(2))
//                -0.3 * (Math.log(x[1] * x[2]/((x[0]+x[1]+x[4])*(x[2]+x[3]))) / Math.log(2))
//                -0.3 * (Math.log(x[1] * x[3]/((x[0]+x[1]+x[4])*(x[2]+x[3]))) / Math.log(2))
//                -0.1 * (Math.log(x[4] /(x[0]+x[1]+x[4])) / Math.log(2));
//    }
//
//    public static double getJS(double[] x){

//        double p1 = 0.3;
//        double p2 = 0.3;
//        double p3 = 0.3;
//        double p4 = 0.1;
//        double q1 = x[0]/(x[0]+x[1]+x[4]);
//        double q2 = (x[1] * x[2])/((x[0]+x[1]+x[4])*(x[2]+x[3]));
//        double q3 = (x[1] * x[3])/((x[0]+x[1]+x[4])*(x[2]+x[3]));
//        double q4 = x[4]/(x[0]+x[1]+x[4]);
//        double m1 = 0.5*(p1+q1);
//        double m2 = 0.5*(p2+q2);
//        double m3 = 0.5*(p3+q3);
//        double m4 = 0.5*(p4+q4);
//
//
//        return
//        0.3 * Math.log(0.3/m1) + q1 * Math.log(q1 /m1)+
//        0.3 * Math.log(0.3/m2) + q2 * Math.log(q2 /m2)+
//        0.3 * Math.log(0.3/m3) + q3 * Math.log(q3 /m3)+
//        0.1 * Math.log(0.1/m4) + q4 * Math.log(q4 /m4);
//
//    }


    public static void main(String[] args) {

//        numerator += x[0];
//        denominator += x[0];

        MultivariateFunction fUEMSC = new MultivariateFunction() {
            public double value(double[] x) {
                return getUEMSC(x);
            }
        };
//
//        MultivariateFunction fEntropy = new MultivariateFunction() {
//            public double value(double[] x) {
//                return getEntropy(x);
//            }
//        };

//        MultivariateFunction fJS = new MultivariateFunction() {
//            public double value(double[] x) {
//                return getJS(x);
//            }
//        };
//
//        double[] lowerBound = new double[]{0, 0, 0, 0};
//        double[] upperBound = new double[]{1, 1, 1, 1};
//
//        System.out.println("\nuEMSC");
//        BOBYQAOptimizer optim1 = new BOBYQAOptimizer(10);
//        PointValuePair result2 = optim1.optimize(
//                new MaxEval(100000),
//                new ObjectiveFunction(fUEMSC),
//                GoalType.MAXIMIZE,
//                new SimpleBounds(lowerBound, upperBound),
//                new InitialGuess(new double[] {0.1,0.1,0.1,0.1})
//        );
//        System.out.println(result2.getPoint()[0]);
//        System.out.println(result2.getPoint()[1]);
//        System.out.println(result2.getPoint()[2]);
//        System.out.println(result2.getPoint()[3]);

        
        int modelTransitionNum = 4;
        double[] lowerBound = new double[modelTransitionNum];
        double[] upperBound = new double[modelTransitionNum];
        double[] initGuess = new double[modelTransitionNum];
		double[] sigma = new double[modelTransitionNum];
        Arrays.fill(lowerBound, 0.0001);
        Arrays.fill(upperBound, 1.0000);
        Arrays.fill(initGuess, 0.0001);
		Arrays.fill(sigma, 0.1);
		UniformRandomProvider rngG = RandomSource.MT_64.create();
        CMAESOptimizer optimizer = new CMAESOptimizer(
        		1000000, 
        		0, 
        		true, 
        		modelTransitionNum,
                100, 
                rngG,
                true, 
                null);

		double[] result2 = optimizer.optimize(
				new MaxEval(1000000),
		   new ObjectiveFunction(fUEMSC),
		   GoalType.MAXIMIZE,
		   new PopulationSize((int) (4+3*Math.log(modelTransitionNum))),
		   new Sigma(sigma),
		   new InitialGuess(initGuess),
		   new SimpleBounds(lowerBound, upperBound)).getPoint();
		
          System.out.println(result2[0]);
	      System.out.println(result2[1]);
	      System.out.println(result2[2]);
	      System.out.println(result2[3]);
//        BOBYQAOptimizer optim2 = new BOBYQAOptimizer(11);
//        PointValuePair result2 = optim2.optimize(
//                new MaxEval(100000),
//                new ObjectiveFunction(fEntropy),
//                GoalType.MINIMIZE,
//                new SimpleBounds(lowerBound, upperBound),
//                new InitialGuess(new double[] {1,1,1,1})
//        );
//        System.out.println(result2.getPoint()[0]);
//        System.out.println(result2.getPoint()[1]);
//        System.out.println(result2.getPoint()[2]);
//        System.out.println(result2.getPoint()[3]);
//
//        System.out.println("\njs");
//        BOBYQAOptimizer optim3 = new BOBYQAOptimizer(9);
//        PointValuePair result3 = optim3.optimize(
//                new MaxEval(100000),
//                new ObjectiveFunction(fJS),
//                GoalType.MINIMIZE,
//                new SimpleBounds(lowerBound, upperBound),
//                new InitialGuess(new double[] {1,1,1,1})
//        );
//        System.out.println(result3.getPoint()[0]);
//        System.out.println(result3.getPoint()[1]);
//        System.out.println(result3.getPoint()[2]);
//        System.out.println(result3.getPoint()[3]);


//        int maxIterations = 200000;
//        double stopFitness = 0; //Double.NEGATIVE_INFINITY;
//        boolean isActiveCMA = true;
//        int diagonalOnly = 0;
//        int checkFeasableCount = 1;
//        RandomGenerator random = new Well19937c();
//        boolean generateStatistics = false;//
//        OptimizationData sigma = new CMAESOptimizer.Sigma(new double[] {
//                (upperBound[0] - lowerBound[0]),
//                (upperBound[0] - lowerBound[0]),
//                (upperBound[0] - lowerBound[0]),
//                (upperBound[0] - lowerBound[0]),
//                (upperBound[0] - lowerBound[0])});
//        OptimizationData popSize = new CMAESOptimizer.PopulationSize((int) (4 + Math.floor(3 * Math.log(2))));
//
//        // construct solver
//        ConvergenceChecker<PointValuePair> checker = new SimpleValueChecker(1e-6, 1e-10);
//
//        CMAESOptimizer opt = new CMAESOptimizer(maxIterations, stopFitness, isActiveCMA, diagonalOnly,
//                checkFeasableCount, random, generateStatistics, checker);
//        PointValuePair pair = opt.optimize(new InitialGuess(
//                new double[] {1,1,1,1,1}),
//                new ObjectiveFunction(fEntropy),
//                GoalType.MINIMIZE, new SimpleBounds(lowerBound, upperBound),
//                sigma, popSize,
//                new MaxIter(maxIterations), new MaxEval(maxIterations * 2));
//        System.out.println(pair.getPoint()[0]);
//        System.out.println(pair.getPoint()[1]);
//        System.out.println(pair.getPoint()[2]);
//        System.out.println(pair.getPoint()[3]);
//        System.out.println(pair.getPoint()[4]);

    }

    static public double converStringToMathExp(String calculateString, HashMap<String, Double> strToDouble) {
        return calculateInversePolandExpression(getInversePolandExpression(calculateString),strToDouble);
    }

    static public double converStringToMathExp(String calculateString) {
        return calculateInversePolandExpression(getInversePolandExpression2(calculateString));
    }

    static public double converStringToMathExp(
            Map<String, String> constantMap, String calculateString) {
        double result = 0;
        for (String str : constantMap.keySet()) {
            calculateString = calculateString.replaceAll(str,
                    constantMap.get(str));
        }
        result = calculateInversePolandExpression(getInversePolandExpression(calculateString));
        return result;
    }


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
//                System.out.println("get the str:" + str);
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

    static private List<String> getInversePolandExpression(
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
        String tempVar ="";
        boolean isInInteger = true;

        for (int i = 0; i < normalChararray.length; i++) {

            char tempChar = normalChararray[i];
            //
            if (tempChar == 't') {
                tempVar = "t";
                for (int j = i+1; j < normalChararray.length; j++) {
                    if (normalChararray[j] >= '0' && normalChararray[j] <= '9'){
                        tempVar = tempVar.concat(String.valueOf(normalChararray[j]));
                        continue;
                    }
                    inversePolandExpression.add(tempVar);
                    i = j-1;
                    break;
                }
            }
            else if (tempChar >= '0' && tempChar <= '9') {
                if (isInInteger) {
                    tempNumber = tempNumber * 10 + (int) (tempChar - 48);
                }
                // ?
                else {
                    tempNumber += (double) (tempChar - 48)
                            * Math.pow(0.1, i - pointPosition);
                }
                tempVar = tempVar.concat(String.valueOf(tempChar));
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
