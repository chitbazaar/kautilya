package com.chitbazaar.kautilya.core;

import java.util.List;

public class PresentValueCalculator {
    private final CompoundingCalculator compoundingCalculator = new CompoundingCalculator();

    public Double presentValue(Double cashFlow, Double ratePerInterval, Double numberOfIntervals, Double compoundingFrequency) {
        return compoundingCalculator.compoundPrincipal(cashFlow, ratePerInterval, numberOfIntervals, compoundingFrequency);
    }

    public Double presentValue(Double cashFlow, Double ratePerInterval, Double numberOfIntervals) {
        return presentValue(cashFlow, ratePerInterval, numberOfIntervals, 1.0);
    }

    public Double netPresentValue(List<Number> cashFlows, Double ratePerInterval, Double compoundingFrequency) {
        Double nfv = 0.0;
        for (int i = 0; i < cashFlows.size(); i++) {
            Double cashFlow = cashFlows.get(i) == null ? 0d : cashFlows.get(i).doubleValue();
            Double numberIntervals = (double) i;
            nfv += compoundingCalculator.compoundPrincipal(cashFlow, ratePerInterval, numberIntervals, compoundingFrequency);
        }
        return nfv;
    }

    public Double netPresentValue(List<Number> cashFlows, Double ratePerInterval) {
        return netPresentValue(cashFlows, ratePerInterval, 1.0);
    }
}
