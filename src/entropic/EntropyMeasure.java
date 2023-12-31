package entropic;

import org.processmining.eigenvalue.MetricsCalculator;

import dk.brics.automaton.Automaton;

public class EntropyMeasure extends AbstractEntropyMeasure {

	int numberOfSkips = 0;
	
	public EntropyMeasure(Object model, int numberOfSkips) {
		
		super(model);
		this.numberOfSkips = numberOfSkips;
	}

	@Override
	protected void initializeLimitations() {
		this.limitations.add(EntropyMeasureLimitation.BOUNDED);
	}

	@Override
	public double computeMeasureValue() {
		System.out.println();
		System.out.println("===================Calculating entropy=============================");
		System.out.println();
		
		if (model instanceof Automaton) {
			double value = MetricsCalculator.calculateEntropy((Automaton)model, "model", false, false, numberOfSkips);
			return value;
		}
		return 0.0;
	}
}
