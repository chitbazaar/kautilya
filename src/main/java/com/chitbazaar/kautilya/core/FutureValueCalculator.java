package com.chitbazaar.kautilya.core;

import java.util.List;

public class FutureValueCalculator {
    private final CompoundingCalculator compoundingCalculator = new CompoundingCalculator();

    public Double futureValue(Double cashFlow, Double ratePerInterval, Double numberOfIntervals, Double compoundingFrequency) {
        return compoundingCalculator.compoundAmount(cashFlow, ratePerInterval, numberOfIntervals, compoundingFrequency);
    }

    public Double futureValue(Double cashFlow, Double ratePerInterval, Double numberOfIntervals) {
        return futureValue(cashFlow, ratePerInterval, numberOfIntervals, 1.0);
    }

    public Double netFutureValue(List<Number> cashFlows, Double ratePerInterval, Double compoundingFrequency) {
        Double nfv = 0.0;
        for (int i = 0; i < cashFlows.size(); i++) {
            Double cashFlow = cashFlows.get(i) == null ? 0d : cashFlows.get(i).doubleValue();
            Double numberIntervals = (cashFlows.size() - i - 1.0);
            nfv += compoundingCalculator.compoundAmount(cashFlow, ratePerInterval, numberIntervals, compoundingFrequency);
        }
        return nfv;
    }

    public Double netFutureValue(List<Number> cashFlows, Double ratePerInterval) {
        return netFutureValue(cashFlows, ratePerInterval, 1.0);
    }
}
