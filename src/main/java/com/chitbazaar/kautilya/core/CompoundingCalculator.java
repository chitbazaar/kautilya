package com.chitbazaar.kautilya.core;

public class CompoundingCalculator {

    public Double compoundAmount(Double principal, Double ratePerInterval, Double numberOfIntervals, Double compoundingFrequency) {
        Double compoundAmount = principal * Math.pow((1 + ratePerInterval / (100 * compoundingFrequency)), compoundingFrequency * numberOfIntervals);
        return compoundAmount;
    }

    public Double compoundAmount(Double principal, Double ratePerInterval, Double numberOfIntervals) {
        return compoundAmount(principal, ratePerInterval, numberOfIntervals, 1.0);
    }

    public Double compoundPrincipal(Double amount, Double ratePerInterval, Double numberOfIntervals, Double compoundingFrequency) {
        Double compoundPrincipal = amount / Math.pow((1 + ratePerInterval / (100 * compoundingFrequency)), compoundingFrequency * numberOfIntervals);
        return compoundPrincipal;
    }

    public Double compoundPrincipal(Double amount, Double ratePerInterval, Double numberOfIntervals) {
        return compoundPrincipal(amount, ratePerInterval, numberOfIntervals, 1.0);
    }

    public Double compoundInterest(Double principal, Double ratePerInterval, Double numberOfIntervals, Double compoundingFrequency) {
        return compoundAmount(principal, ratePerInterval, numberOfIntervals, compoundingFrequency) - principal;
    }

    public Double compoundInterest(Double principal, Double ratePerInterval, Double numberOfIntervals) {
        return compoundInterest(principal, ratePerInterval, numberOfIntervals, 1.0);
    }

    public Double compoundRate(Double principal, Double amount, Double numberOfIntervals, Double compoundingFrequency) {
        return (Math.pow(amount / principal, 1 / (numberOfIntervals * compoundingFrequency)) - 1) * 100 * compoundingFrequency;
    }

    public Double compoundRate(Double principal, Double amount, Double numberOfIntervals) {
        return compoundRate(principal, amount, numberOfIntervals, 1.0);
    }
}
