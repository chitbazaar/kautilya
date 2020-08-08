package com.chitbazaar.kautilya.core;

import java.util.*;

public class CashFlowInfo {
    private final List<Double> cashFlows;
    private final Integer positiveCashFlowCount;
    private final Integer negativeCashFlowCount;
    private final Integer zeroCashFlowCount;
    private final Integer numberOfIntervals;
    private final Double netCashFlow;
    private final Double positiveCashFlow;
    private final Double negativeCashFLow;
    private final boolean onlyEndCashFlows;
    private IRRAndNFV minReturnToCheck;
    private IRRAndNFV maxReturnToCheck;
    private CompoundingCalculator compoundingCalculator = new CompoundingCalculator();
    private FutureValueCalculator futureValueCalculator = new FutureValueCalculator();
    private Set<IRRAndNFV> positiveNFVToReturnSet = new TreeSet();
    private Set<IRRAndNFV> negativeNFVToReturnSet = new TreeSet();

    CashFlowInfo(List<Double> cashFlows) {
        List<Double> cleanedUpCashFlows = new ArrayList();
        Double netCashFlow = 0d;
        Double positiveCashFlow = 0d;
        Double negativeCashFLow = 0d;
        Integer negativeCashFlowCount = 0;
        Integer positiveCashFlowCount = 0;
        Integer zeroCashFlowCount = 0;
        boolean onlyEndCashFlows = true;
        for (Integer index = 0; index < cashFlows.size(); index++) {
            Double cashFlow = cashFlows.get(index);
            if (Objects.isNull(cashFlow)) {
                cleanedUpCashFlows.add(0d);
                zeroCashFlowCount++;
            } else if (cashFlow < 0) {
                cleanedUpCashFlows.add(cashFlow);
                negativeCashFlowCount++;
                negativeCashFLow += -1 * cashFlow;
            } else {
                cleanedUpCashFlows.add(cashFlow);
                positiveCashFlowCount++;
                positiveCashFlow += cashFlow;
            }
            netCashFlow += cashFlow;
            if (index != 0 && index != cashFlows.size() - 1 && cashFlow != 0) {
                onlyEndCashFlows = false;
            }
        }

        this.onlyEndCashFlows = onlyEndCashFlows;
        this.cashFlows = Collections.unmodifiableList(cleanedUpCashFlows);
        this.netCashFlow = netCashFlow;
        this.positiveCashFlow = positiveCashFlow;
        this.negativeCashFLow = negativeCashFLow;
        this.negativeCashFlowCount = negativeCashFlowCount;
        this.positiveCashFlowCount = positiveCashFlowCount;
        this.zeroCashFlowCount = zeroCashFlowCount;
        numberOfIntervals = cashFlows.size() - 1;
        if (!onlyEndCashFlows && positiveCashFlow > 0 && negativeCashFLow > 0 && netCashFlow != 0) {
            setMinMaxChecks();
        }
    }

    private void setMinMaxChecks() {
        // zero return
        IRRAndNFV zeroIRRNFV = setAndGetIRRandNFV(0.0);

        setAndGetIRRandNFV(this.negativeCashFLow, this.positiveCashFlow, this.numberOfIntervals);
        setAndGetIRRandNFV(this.negativeCashFLow, this.positiveCashFlow, 1);
        setAndGetIRRandNFV(this.positiveCashFlow, this.negativeCashFLow, this.numberOfIntervals);
        setAndGetIRRandNFV(this.positiveCashFlow, this.negativeCashFLow, 1);
        setMinMaxIRRAndNFV();

        setXAxisCut(minReturnToCheck, maxReturnToCheck);
        setMinMaxIRRAndNFV();

        setXAxisCut(minReturnToCheck, zeroIRRNFV);
        setXAxisCut(zeroIRRNFV, maxReturnToCheck);
        setMinMaxIRRAndNFV();
    }

    private void setMinMaxIRRAndNFV() {
        IRRAndNFV positiveMin = Collections.min(positiveNFVToReturnSet);
        IRRAndNFV positiveMax = Collections.max(positiveNFVToReturnSet);
        IRRAndNFV negativeMin = Collections.min(negativeNFVToReturnSet);
        IRRAndNFV negativeMax = Collections.max(negativeNFVToReturnSet);

        if (maxReturnToCheck == null) {
            minReturnToCheck = min(positiveMin, negativeMin);
            maxReturnToCheck = max(positiveMax, negativeMax);
        } else if (maxReturnToCheck.nfv >= 0) {
            minReturnToCheck = min(negativeMax, positiveMin);
            maxReturnToCheck = max(negativeMax, positiveMin);
        } else {
            minReturnToCheck = min(negativeMin, positiveMax);
            maxReturnToCheck = max(negativeMin, positiveMax);
        }
    }

    private IRRAndNFV min(IRRAndNFV first, IRRAndNFV second) {
        if (first.compareTo(second) <= 0) {
            return first;
        } else {
            return second;
        }
    }

    private IRRAndNFV max(IRRAndNFV first, IRRAndNFV second) {
        if (first.compareTo(second) >= 0) {
            return first;
        } else {
            return second;
        }
    }

    private void setXAxisCut(IRRAndNFV x1y1, IRRAndNFV x2y2) {
        Double slope = (x2y2.nfv - x1y1.nfv) / (x2y2.ratePerInterval - x1y1.ratePerInterval);
        if (slope == 0) {
            return;
        }
        Double ratePerInterval = x2y2.ratePerInterval - x2y2.nfv / slope;
        Double netFutureValue = futureValueCalculator.netFutureValue(cashFlows, ratePerInterval);
        IRRAndNFV irrAndNFV = new IRRAndNFV(ratePerInterval, netFutureValue);
        setNFVAndReturn(irrAndNFV);
    }

    private IRRAndNFV setAndGetIRRandNFV(Double ratePerInterval) {
        Double netFutureValue = futureValueCalculator.netFutureValue(cashFlows, ratePerInterval);
        IRRAndNFV irrAndNFV = new IRRAndNFV(ratePerInterval, netFutureValue);
        setNFVAndReturn(irrAndNFV);
        return irrAndNFV;
    }

    private IRRAndNFV setAndGetIRRandNFV(Double principal, Double amount, Integer numberOfIntervals) {
        Double ratePerInterval = compoundingCalculator.compoundRate(principal, amount, numberOfIntervals.doubleValue());
        Double netFutureValue = futureValueCalculator.netFutureValue(cashFlows, ratePerInterval);
        IRRAndNFV irrAndNFV = new IRRAndNFV(ratePerInterval, netFutureValue);
        setNFVAndReturn(irrAndNFV);
        return irrAndNFV;
    }

    private void setNFVAndReturn(IRRAndNFV irrAndNFV) {
        if (irrAndNFV.nfv > 0) {
            positiveNFVToReturnSet.add(irrAndNFV);
        } else {
            negativeNFVToReturnSet.add(irrAndNFV);
        }
    }

    static class IRRAndNFV implements Comparable<IRRAndNFV> {
        Double ratePerInterval;
        Double nfv;

        public IRRAndNFV(Double ratePerInterval, Double nfv) {
            this.ratePerInterval = ratePerInterval;
            this.nfv = nfv;
        }

        @Override
        public String toString() {
            return String.format("irr:%s nfv:%s", ratePerInterval, nfv);
        }

        @Override
        public int compareTo(IRRAndNFV o) {
            if (ratePerInterval == o.ratePerInterval) {
                return Double.compare(Math.abs(nfv), Math.abs(o.nfv));
            }
            return Double.compare(ratePerInterval, o.ratePerInterval);
        }
    }

    public List<Double> getCashFlows() {
        return cashFlows;
    }

    public Integer getPositiveCashFlowCount() {
        return positiveCashFlowCount;
    }

    public Integer getNegativeCashFlowCount() {
        return negativeCashFlowCount;
    }

    public Integer getZeroCashFlowCount() {
        return zeroCashFlowCount;
    }

    public Integer getNumberOfIntervals() {
        return numberOfIntervals;
    }

    public Double getNetCashFlow() {
        return netCashFlow;
    }

    public Double getPositiveCashFlow() {
        return positiveCashFlow;
    }

    public Double getNegativeCashFLow() {
        return negativeCashFLow;
    }

    public boolean isOnlyEndCashFlows() {
        return onlyEndCashFlows;
    }

    public IRRAndNFV getMinReturnToCheck() {
        return minReturnToCheck;
    }

    public IRRAndNFV getMaxReturnToCheck() {
        return maxReturnToCheck;
    }

    public Set<IRRAndNFV> getPositiveNFVToReturnSet() {
        return positiveNFVToReturnSet;
    }

    public Set<IRRAndNFV> getNegativeNFVToReturnSet() {
        return negativeNFVToReturnSet;
    }
}
