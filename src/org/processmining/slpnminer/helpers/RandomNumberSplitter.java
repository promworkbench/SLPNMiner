//package org.processmining.slpnminer.helpers;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Random;
//
//import org.deckfour.xes.factory.XFactory;
//import org.deckfour.xes.factory.XFactoryRegistry;
//import org.deckfour.xes.model.XAttributeMap;
//import org.deckfour.xes.model.XEvent;
//import org.deckfour.xes.model.XLog;
//import org.deckfour.xes.model.XTrace;
//import org.deckfour.xes.out.XSerializer;
//import org.deckfour.xes.out.XesXmlSerializer;
//import org.processmining.framework.plugin.PluginContext;
//import org.processmining.log.utils.XUtils;
//import org.processmining.statisticaltests.test.FakeContext;
//import org.processmining.xeslite.plugin.OpenLogFileLiteImplPlugin;
//
//public class RandomNumberSplitter {
//
//    public static void main(String[] args) {
//
//		PluginContext context = new FakeContext();
//
//		try {
//			XLog log = (XLog) new OpenLogFileLiteImplPlugin().importFile(context, new File("/Applications/Programming/dataset/Road_Traffic_Fine_Management_Process.xes"));
//		
//			
//			
//			List<List<Integer>> traceIdxGroups = getTracesIndexFromLog(log);
//						
//			for (int i=0;i<5;i++) {
//				XLog[] testTrainLog = extractTrainAndTestByIndex(log, traceIdxGroups.get(i),XFactoryRegistry.instance().currentDefault());
//				XLog testLog = testTrainLog[0];
//				XLog trainLog = testTrainLog[1];
//				XSerializer logSerializer = new XesXmlSerializer();
//
//				String file = "/Applications/Programming/dataset/RTF/" + "train"+ i+".xes"; 
//				FileOutputStream out = new FileOutputStream(file);
//				logSerializer.serialize(trainLog, out);
//				out.close();
//				
//				String file2 = "/Applications/Programming/dataset/RTF/" +"test"+ i+".xes"; 
//				FileOutputStream out2 = new FileOutputStream(file2);
//				logSerializer.serialize(testLog, out2);
//				out2.close();
//			}
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
//		
//      
//    
//    // Generate ten index sets for splitting log
//    public static List<List<Integer>> getTracesIndexFromLog(XLog log){
//    	int logSize = log.size();
//    	int groupSize = log.size()/10;
//    	
//        // Generate a list of numbers from 1 to 1000
//        List<Integer> numbers = new ArrayList<>();
//        for (int i = 1; i <= logSize; i++) {
//            numbers.add(i);
//        }
//
//        // Shuffle the list randomly
//        Collections.shuffle(numbers, new Random());
//
//        // Split the shuffled list into 10 groups of 100
//        List<List<Integer>> traceIdxGroups = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            int fromIndex = i * groupSize;
//            int toIndex = fromIndex + groupSize;
//            List<Integer> group = numbers.subList(fromIndex, toIndex);
//            traceIdxGroups.add(group);
//        }
//        
//        return traceIdxGroups;
//    }
//    
//    
//    // Given an index set, extract sub-event logs accordingly
//	public static XLog [] extractTrainAndTestByIndex(
//			XLog log,
//			List<Integer> traceIndexToKeep, 
//			XFactory factory) {
//
//		XLog testLog = XUtils.createLogFrom(log, factory);
//		XLog trainLog = XUtils.createLogFrom(log, factory);
//
//		int i = 0;
//		for (Iterator<XTrace> iterator = log.iterator(); iterator.hasNext();) {
//			XTrace t = iterator.next();
//			if (traceIndexToKeep.contains(i)) {
//				XTrace newTrace = factory.createTrace((XAttributeMap) t.getAttributes().clone());
//				for (XEvent e : t) {
//					newTrace.add(factory.createEvent((XAttributeMap) e.getAttributes().clone()));
//				}
//				testLog.add(newTrace);
//			}
//			else {
//				XTrace newTrace = factory.createTrace((XAttributeMap) t.getAttributes().clone());
//				for (XEvent e : t) {
//					newTrace.add(factory.createEvent((XAttributeMap) e.getAttributes().clone()));
//				}
//				trainLog.add(newTrace);
//			}
//			i++;
//		}
//		
//		XLog[] logObj = new XLog[2];
//		logObj[0] = testLog;
//		logObj[1] = trainLog;
//
//		return logObj;
//		}
//	
//    }
//
