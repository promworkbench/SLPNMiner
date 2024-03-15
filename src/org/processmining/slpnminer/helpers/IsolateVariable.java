package org.processmining.slpnminer.helpers;

import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.S;
import org.matheclipse.core.form.output.OutputFormFactory;
import org.matheclipse.core.interfaces.IExpr;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class IsolateVariable {

	static ExecutorService executorService;
	
    public static void main(String[] args) {
    	 String res = "";
    	 String res2 = "";
    	try {
             res = getIsolatedVar("Solve({a1==a3,a2==a4,a3==a5*t1/(t0+t2+t1),a4==a1*t0/(t0+t2+t1),a5==1,a0==a2},{a1,a2,a3,a4,a5,a0})");
             res2 = getIsolatedVar("Solve({a1==a4*t10/(t10+t9),a2==a1*t7/(t7+t4),a4==1,a5==a11*t4/(t4+t1+t2+t0)+a12*t1/(t4+t1+t2+t0),a6==a10*t3/(t12+t3),a10==a12*t1/(t6+t1+t2+t0)+a5*t6/(t6+t1+t2+t0),a11==a2*t1/(t6+t1+t2+t0),a12==a2*t4/(t7+t4),a0==a6},{a1,a2,a4,a5,a6,a10,a11,a12,a0})");
             }
    	catch(Exception e) {
    		
    	}
    	finally {
    		System.out.println("eq system 1: "+res);
    		System.out.println("eq system 2: "+res2);

    	}
        //        getIsolatedVar("Solve({x1==x12,x2==x4,x3==x11*t28/(t28+t27)+x7*t27/(t28+t27),x4==0,x5==1,x6==0,x7==x5,x8==x18*t28/(t28+t27)+x11*t27/(t28+t27),x9==0,x10==x17,x11==x14,x12==x2,x13==0,x14==0,x15==x1*t37/(t37+t63+t23)+x13*t63/(t37+t63+t23)+x10*t23/(t37+t63+t23),x16==x8,x17==0,x18==x6,x19==x9,x0==x15},{x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14,x15,x16,x17,x18,x19,x0})");
    }
    
    public static String getIsoRes(String equationSystems) {
    		executorService = Executors.newSingleThreadExecutor();
    		String rst = null;
    		Future<String> future = executorService.submit(() ->  getIsolatedVar(equationSystems));
    		try {
    		    //set timeout
    		    rst = future.get(100, TimeUnit.SECONDS);
    		} catch (TimeoutException e) {
    			System.out.println("Timeout!");
    		} catch(Exception e){
    			
    		}finally {
    		    executorService.shutdown();
    		}
    		return rst;
    }

    public static String getIsolatedVar(String equationSystems){
    	   ExprEvaluator scriptEngine = new ExprEvaluator();
           String evaledResult = printResult(scriptEngine.eval(equationSystems));
           String isolatedVar = evaledResult.substring(evaledResult.indexOf(">")+1,
                   evaledResult.indexOf(","));
           return isolatedVar;
      }

    private static String printResult(IExpr result, boolean relaxedSyntax) {
        if (result.equals(S.Null)) {
            return "";
        }
        final StringWriter buf = new StringWriter();
        EvalEngine engine = EvalEngine.get();
      
        OutputFormFactory off;
        int significantFigures = engine.getSignificantFigures();
        
        off = OutputFormFactory.get(relaxedSyntax, false, significantFigures - 1, significantFigures + 1);
        
        if (off.convert(buf, result)) {
            // print the result in the console
            return buf.toString();
        }
        if (Config.FUZZ_TESTING) {
            throw new NullPointerException();
        }
        return "ScriptEngine: ERROR-IN-OUTPUTFORM";
    }

    private static String printResult(IExpr result) {
        return printResult(result, true);
    }

}
