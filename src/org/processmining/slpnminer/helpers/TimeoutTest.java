package org.processmining.slpnminer.helpers;

import java.util.concurrent.*;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.S;
import org.matheclipse.core.form.output.OutputFormFactory;
import org.matheclipse.core.interfaces.IExpr;


public class TimeoutTest {
	
	public static void main(String[] args) {
		for(int i=0;i<5;i++) {
			String rst =  getIsoRes();
			if (rst==null) {
			    System.out.println("执行超时！");
			}
			else {
			    System.out.println("get result: "+rst);
			}
		}
	}
	
	public static String getIsoRes() {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		String rst = null;
		Future<String> future = executorService.submit(() ->  getIsolatedVar("Solve({a1==a7,a2==a28*t5/(t5+t4+t9+t8+t3)+a27*t9/(t5+t4+t9+t8+t3),a3==a28*t6/(t9+t8+t6)+a11*t9/(t9+t8+t6),a4==a11,a5==a30*t5/(t5+t4+t3+t14+t13)+a27*t14/(t5+t4+t3+t14+t13),a6==1,a7==a9*t5/(t5+t4+t9+t8+t3+t14+t13)+a21*t9/(t5+t4+t9+t8+t3+t14+t13)+a19*t14/(t5+t4+t9+t8+t3+t14+t13)+a17*t3/(t5+t4+t9+t8+t3+t14+t13),a8==a3*t3/(t9+t8+t3)+a4*t9/(t9+t8+t3),a9==a24*t9/(t9+t8+t3+t14+t13)+a8*t14/(t9+t8+t3+t14+t13)+a20*t3/(t9+t8+t3+t14+t13),a10==a13*t12/(t12+t6),a11==a22*t6/(t12+t6),a12==a30*t9/(t9+t8+t3+t14+t13)+a28*t14/(t9+t8+t3+t14+t13),a13==a6*t0/(t0+t1),a14==a1*t19/(t19+t18+t17),a15==a14,a16==a27*t6/(t5+t4+t6)+a11*t5/(t5+t4+t6),a0==a15,a17==a29*t9/(t5+t4+t9+t8+t6+t14+t13)+a18*t6/(t5+t4+t9+t8+t6+t14+t13)+a20*t5/(t5+t4+t9+t8+t6+t14+t13)+a25*t14/(t5+t4+t9+t8+t6+t14+t13),a18==a12*t5/(t5+t4+t9+t8+t3+t14+t13)+a5*t9/(t5+t4+t9+t8+t3+t14+t13)+a2*t14/(t5+t4+t9+t8+t3+t14+t13),a19==a8*t5/(t5+t4+t9+t8+t3)+a23*t9/(t5+t4+t9+t8+t3)+a25*t3/(t5+t4+t9+t8+t3),a20==a12*t6/(t9+t8+t6+t14+t13)+a26*t9/(t9+t8+t6+t14+t13)+a3*t14/(t9+t8+t6+t14+t13),a21==a23*t14/(t5+t4+t3+t14+t13)+a24*t5/(t5+t4+t3+t14+t13)+a29*t3/(t5+t4+t3+t14+t13),a22==a10,a23==a16*t3/(t5+t4+t3)+a4*t5/(t5+t4+t3),a24==a4*t14/(t3+t14+t13)+a26*t3/(t3+t14+t13),a25==a2*t6/(t5+t4+t9+t8+t6)+a3*t5/(t5+t4+t9+t8+t6)+a16*t9/(t5+t4+t9+t8+t6),a26==a30*t6/(t6+t14+t13)+a11*t14/(t6+t14+t13),a27==a22*t5/(t5+t4+t3),a28==a22*t9/(t9+t8+t3),a29==a5*t6/(t5+t4+t6+t14+t13)+a26*t5/(t5+t4+t6+t14+t13)+a16*t14/(t5+t4+t6+t14+t13),a30==a22*t14/(t3+t14+t13)},{a1,a2,a3,a4,a5,a6,a7,a8,a9,a10,a11,a12,a13,a14,a15,a16,a0,a17,a18,a19,a20,a21,a22,a23,a24,a25,a26,a27,a28,a29,a30})"));
		
			try {
		    //设置超时时间
		    rst = future.get(5, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
		} catch(Exception e){
		}finally {
		    executorService.shutdown();
		}
		return rst;
	}
	
	public static String getIsolatedVar(String equationSystems){
	   System.out.println("eq system: "+equationSystems);
	   ExprEvaluator scriptEngine = new ExprEvaluator();
       String evaledResult = printResult(scriptEngine.eval(equationSystems));
       String isolatedVar = evaledResult.substring(evaledResult.indexOf(">")+1,
               evaledResult.indexOf(","));
//       System.out.println(isolatedVar);
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
