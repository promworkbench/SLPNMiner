package entropic;

import java.io.File;
import org.deckfour.xes.in.XMxmlGZIPParser;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;

public class XLogReader {
    public static XLog openLog(String inputLogFileName) throws Exception {
        XParser parser;
        XLog log = null;
        if (inputLogFileName.toLowerCase().contains("mxml.gz")) {
            parser = new XMxmlGZIPParser();
            if (((XMxmlGZIPParser)parser).canParse(new File(inputLogFileName))) {
                try {
                    log = parser.parse(new File(inputLogFileName)).get(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if ((inputLogFileName.toLowerCase().contains("mxml") || inputLogFileName.toLowerCase().contains("xml")) && ((XMxmlParser)(parser = new XMxmlParser())).canParse(new File(inputLogFileName))) {
            try {
                log = parser.parse(new File(inputLogFileName)).get(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (inputLogFileName.toLowerCase().contains("xes.gz")) {
            parser = new XesXmlGZIPParser();
            if (((XesXmlGZIPParser)parser).canParse(new File(inputLogFileName))) {
                try {
                    log = parser.parse(new File(inputLogFileName)).get(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (inputLogFileName.toLowerCase().contains("xes") && ((XesXmlParser)(parser = new XesXmlParser())).canParse(new File(inputLogFileName))) {
            try {
                log = parser.parse(new File(inputLogFileName)).get(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (log == null) {
            throw new Exception("Oops ...");
        }
        return log;
    }
}

    