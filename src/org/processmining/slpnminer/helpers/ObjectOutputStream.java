//package org.processmining.slpnminer.helpers;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.util.HashMap;
//
//import org.deckfour.xes.model.XLog;
//import org.processmining.framework.plugin.PluginContext;
//import org.processmining.statisticaltests.test.FakeContext;
//import org.processmining.xeslite.plugin.OpenLogFileLiteImplPlugin;
//
//import dk.brics.automaton.Automaton;
//import entropic.Utils;
//import gnu.trove.map.TObjectShortMap;
//import gnu.trove.map.custom_hash.TObjectShortCustomHashMap;
//import gnu.trove.strategy.HashingStrategy;
//
//public class ObjectOutputStream {
//
//	public static void main(String[] args) {
//		
//		HashingStrategy<String> strategy = new HashingStrategy<String>() {
//
//			public int computeHashCode(String object) {
//				return object.hashCode();
//			}
//
//			public boolean equals(String o1, String o2) {
//				return o1.equals(o2);
//			}
//		};
//		
//		TObjectShortMap<String> activity2short = new TObjectShortCustomHashMap<String>(strategy, 10, 0.5f, (short) -1);
//		
//		PluginContext context = new FakeContext();
//
//		XLog log1;
//		try {
//			log1 = (XLog) new OpenLogFileLiteImplPlugin().importFile(context, new File("/Applications/Programming/xes/er.xes"));
//			Automaton at1 = Utils.constructAutomatonFromLog(log1, activity2short);
//			System.out.println(at1);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		//		HashMap<Integer, String> hmap = new HashMap<Integer, String>();
////        //Adding elements to HashMap
////        hmap.put(11, "AB");
////        hmap.put(2, "CD");
////        hmap.put(33, "EF");
////        hmap.put(9, "GH");
////        hmap.put(3, "IJ");
////        try
////        {
////               FileOutputStream fos =
////                  new FileOutputStream("hashmap.ser");
////               ObjectOutputStream oos = new ObjectOutputStream(fos);
////               oos.writeObject(hmap);
////               oos.close();
////               fos.close();
////               System.out.printf("Serialized HashMap data is saved in hashmap.ser");
////        }catch(IOException ioe)
////         {
////               ioe.printStackTrace();
////         }
//	}
//	
//	
//	
//}
