package entropic;

import java.util.Optional;
import org.deckfour.xes.model.XTrace;

public interface ReplayInformationGatherer {
    public void openTrace(XTrace var1);

    public void closeTrace(XTrace var1, boolean var2, Optional<Double> var3);

    public void processEvent(String var1, double var2);
}
