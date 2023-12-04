package org.processmining.slpnminer.help;

import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.S;
import org.matheclipse.core.form.output.OutputFormFactory;
import org.matheclipse.core.interfaces.IExpr;

import java.io.StringWriter;

public class IsolateVariable {

    public static void main(String[] args) {
    	try {
    		getIsolatedVar("Solve({a1==a8,a2==a10,a3==a17*t22/(t22+t29+t30+t28+t24+t27),a4==a14*t18/(t2+t3+t18+t19)+a10*t2/(t2+t3+t18+t19),a5==a2*t2/(t2+t3+t20)+a4*t20/(t2+t3+t20),a6==a13*t2/(t2+t3+t10)+a5*t10/(t2+t3+t10),a7==a8*t2/(t2+t3+t34+t35),a8==a16*t34/(t34+t35),a9==a6*t37/(t2+t3+t37)+a17*t2/(t2+t3+t37),a10==a1*t18/(t18+t19),a11==1,a12==a15*t14/(t33+t14),a13==a2,a14==a7*t15/(t2+t3+t15)+a1*t2/(t2+t3+t15),a15==a3*t2/(t2+t3+t22+t29+t30+t28+t24+t27)+a9*t22/(t2+t3+t22+t29+t30+t28+t24+t27),a0==a12,a16==a11,a17==a13},{a1,a2,a3,a4,a5,a6,a7,a8,a9,a10,a11,a12,a13,a14,a15,a0,a16,a17})");        
    	}
    	catch(Exception e) {
    		
    	}
        //        getIsolatedVar("Solve({x1==x12,x2==x4,x3==x11*t28/(t28+t27)+x7*t27/(t28+t27),x4==0,x5==1,x6==0,x7==x5,x8==x18*t28/(t28+t27)+x11*t27/(t28+t27),x9==0,x10==x17,x11==x14,x12==x2,x13==0,x14==0,x15==x1*t37/(t37+t63+t23)+x13*t63/(t37+t63+t23)+x10*t23/(t37+t63+t23),x16==x8,x17==0,x18==x6,x19==x9,x0==x15},{x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14,x15,x16,x17,x18,x19,x0})");
    }

    public static String getIsolatedVar(String equationSystems){
//        System.out.println("eq system: "+equationSystems);

        ExprEvaluator scriptEngine = new ExprEvaluator();
        String evaledResult = printResult(scriptEngine.eval(equationSystems));
        String isolatedVar = evaledResult.substring(evaledResult.indexOf(">")+1,
                evaledResult.indexOf(","));
//        System.out.println(isolatedVar);
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
        off =
                OutputFormFactory.get(relaxedSyntax, false, significantFigures - 1, significantFigures + 1);

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
        return  printResult(result, true);
    }

}
