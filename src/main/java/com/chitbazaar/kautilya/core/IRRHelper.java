package com.chitbazaar.kautilya.core;

import com.chitbazaar.kautilya.domain.CashFlowInfo;
import com.chitbazaar.kautilya.domain.IRRAndNFV;
import com.chitbazaar.kautilya.domain.MinMaxIRRAndNFV;

import java.util.*;

public class IRRHelper {

    private final FutureValueCalculator futureValueCalculator = new FutureValueCalculator();
    private final CompoundingCalculator compoundingCalculator = new CompoundingCalculator();

    public MinMaxIRRAndNFV getInitialBounderies(CashFlowInfo cashFlowInfo) {
        MinMaxIRRAndNFV minMaxIRRAndNFV = null;
        Set<IRRAndNFV> positiveNFVToReturnSet = new TreeSet<>();
        Set<IRRAndNFV> negativeNFVToReturnSet = new TreeSet<>();

        IRRAndNFV irrAndNFV = null;
        //near intervals
        irrAndNFV = getIRRandNFV(cashFlowInfo.negativeCashFLow, cashFlowInfo.positiveCashFlow, 1, cashFlowInfo.cashFlows);
        setNFVAndReturn(cashFlowInfo, irrAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        irrAndNFV = getIRRandNFV(cashFlowInfo.positiveCashFlow, cashFlowInfo.negativeCashFLow, 1, cashFlowInfo.cashFlows);
        setNFVAndReturn(cashFlowInfo, irrAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        if (positiveNFVToReturnSet.size() > 0 && negativeNFVToReturnSet.size() > 0) {
            minMaxIRRAndNFV = getNewMinMaxIRRAndNFV(null, positiveNFVToReturnSet, negativeNFVToReturnSet);
            if (minMaxIRRAndNFV != null) {
                cashFlowInfo.setCurrentMinMax(minMaxIRRAndNFV);
            }
        }

        if (positiveNFVToReturnSet.size() > 0 && negativeNFVToReturnSet.size() > 0) {
            minMaxIRRAndNFV = getNewMinMaxIRRAndNFV(minMaxIRRAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);
            cashFlowInfo.setCurrentMinMax(minMaxIRRAndNFV);
        } else {
            positiveNFVToReturnSet.clear();
            negativeNFVToReturnSet.clear();
            tryOtherInitialCheckPoints(cashFlowInfo, positiveNFVToReturnSet, negativeNFVToReturnSet);
            if (positiveNFVToReturnSet.size() == 0 || negativeNFVToReturnSet.size() == 0) {
                throw new IRRException("Cannot determine minmax boundaries for cashFlows.");
            } else {
                minMaxIRRAndNFV = getNewMinMaxIRRAndNFV(minMaxIRRAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);
                cashFlowInfo.setCurrentMinMax(minMaxIRRAndNFV);
            }
        }

        return setAndGetNewMinMaxIRRAndNFV(minMaxIRRAndNFV, cashFlowInfo);
    }

    private void tryOtherInitialCheckPoints(CashFlowInfo cashFlowInfo, Set<IRRAndNFV> positiveNFVToReturnSet, Set<IRRAndNFV> negativeNFVToReturnSet) {
        IRRAndNFV zeroIrrAndNFV;
        //zero irr
        zeroIrrAndNFV = getIRRandNFV(0.0, cashFlowInfo.cashFlows);
        setNFVAndReturn(cashFlowInfo, zeroIrrAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        IRRAndNFV irrAndNFV;
        int increment = 10;
        //Try practical returns
        for (int i = 0; i <= 10000; i += increment) {
            boolean foundResult = false;
            for (double rate = i; rate < i + increment; rate += 1) {
                irrAndNFV = getIRRandNFV(rate, cashFlowInfo.cashFlows);
                setNFVAndReturn(cashFlowInfo, irrAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);
                if (positiveNFVToReturnSet.size() > 0 && negativeNFVToReturnSet.size() > 0) {
                    foundResult = true;
                    break;
                }
            }
            if(foundResult){
                break;
            }
            for (double rate = -1 * i; rate > -1 * i - increment; rate -= 1) {
                irrAndNFV = getIRRandNFV(rate, cashFlowInfo.cashFlows);
                setNFVAndReturn(cashFlowInfo, irrAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);
                if (positiveNFVToReturnSet.size() > 0 && negativeNFVToReturnSet.size() > 0) {
                    foundResult = true;
                    break;
                }
            }
            if (foundResult) {
                break;
            }
        }
    }

    public IRRAndNFV getIRRandNFV(Double principal, Double amount, Integer numberOfIntervals, List<Number> cashFlows) {
        Double ratePerInterval = compoundingCalculator.compoundRate(principal, amount, numberOfIntervals.doubleValue());
        Double netFutureValue = futureValueCalculator.netFutureValue(cashFlows, ratePerInterval);
        IRRAndNFV irrAndNFV = new IRRAndNFV(ratePerInterval, netFutureValue);
        return irrAndNFV;
    }

    private IRRAndNFV getIRRandNFV(Double ratePerInterval, List<Number> cashFlows) {
        Double netFutureValue = futureValueCalculator.netFutureValue(cashFlows, ratePerInterval);
        IRRAndNFV irrAndNFV = new IRRAndNFV(ratePerInterval, netFutureValue);
        return irrAndNFV;
    }

    public MinMaxIRRAndNFV setAndGetNewMinMaxIRRAndNFV(MinMaxIRRAndNFV minMaxIRRAndNFV, CashFlowInfo cashFlowInfo) {
        Set<IRRAndNFV> positiveNFVToReturnSet = new TreeSet<>();
        Set<IRRAndNFV> negativeNFVToReturnSet = new TreeSet<>();
        setNFVAndReturn(cashFlowInfo, minMaxIRRAndNFV.min, positiveNFVToReturnSet, negativeNFVToReturnSet);
        setNFVAndReturn(cashFlowInfo, minMaxIRRAndNFV.max, positiveNFVToReturnSet, negativeNFVToReturnSet);

        MinMaxIRRAndNFV newMinMaxIRRAndNFV = null;

        //x axis cut
        IRRAndNFV xAxisCut = getXAxisCut(minMaxIRRAndNFV.min, minMaxIRRAndNFV.max, cashFlowInfo.cashFlows);
        setNFVAndReturn(cashFlowInfo, xAxisCut, positiveNFVToReturnSet, negativeNFVToReturnSet);
        newMinMaxIRRAndNFV = getNewMinMaxIRRAndNFV(minMaxIRRAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        //Min tangential cut
        setTangentialCuts(newMinMaxIRRAndNFV.min, cashFlowInfo, positiveNFVToReturnSet, negativeNFVToReturnSet);
        newMinMaxIRRAndNFV = getNewMinMaxIRRAndNFV(newMinMaxIRRAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        //Max tangential cut
        setTangentialCuts(newMinMaxIRRAndNFV.max, cashFlowInfo, positiveNFVToReturnSet, negativeNFVToReturnSet);
        newMinMaxIRRAndNFV = getNewMinMaxIRRAndNFV(newMinMaxIRRAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        //Mid tangential cut
        Double midRate = (newMinMaxIRRAndNFV.min.ratePerInterval + newMinMaxIRRAndNFV.max.ratePerInterval) / 2d;
        IRRAndNFV mid = new IRRAndNFV(midRate, futureValueCalculator.netFutureValue(cashFlowInfo.cashFlows, midRate));
        setNFVAndReturn(cashFlowInfo, mid, positiveNFVToReturnSet, negativeNFVToReturnSet);

        setTangentialCuts(mid, cashFlowInfo, positiveNFVToReturnSet, negativeNFVToReturnSet);
        newMinMaxIRRAndNFV = getNewMinMaxIRRAndNFV(newMinMaxIRRAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        cashFlowInfo.setCurrentMinMax(newMinMaxIRRAndNFV);
        return newMinMaxIRRAndNFV;
    }

    private void setTangentialCuts(IRRAndNFV n, CashFlowInfo cashFlowInfo, Set<IRRAndNFV> positiveNFVToReturnSet, Set<IRRAndNFV> negativeNFVToReturnSet) {
        Double nMinusOnePrecision = n.ratePerInterval - cashFlowInfo.precision;
        Double nPlusOnePrecision = n.ratePerInterval + cashFlowInfo.precision;
        IRRAndNFV nMinusOne = new IRRAndNFV(nMinusOnePrecision, futureValueCalculator.netFutureValue(cashFlowInfo.cashFlows, nMinusOnePrecision));
        IRRAndNFV nPlusOne = new IRRAndNFV(nPlusOnePrecision, futureValueCalculator.netFutureValue(cashFlowInfo.cashFlows, nPlusOnePrecision));
        IRRAndNFV xAxisCut = null;
        xAxisCut = getXAxisCut(nMinusOne, nPlusOne, cashFlowInfo.cashFlows);
        setNFVAndReturn(cashFlowInfo, xAxisCut, positiveNFVToReturnSet, negativeNFVToReturnSet);
        xAxisCut = getXAxisCut(n, nPlusOne, cashFlowInfo.cashFlows);
        setNFVAndReturn(cashFlowInfo, xAxisCut, positiveNFVToReturnSet, negativeNFVToReturnSet);
        xAxisCut = getXAxisCut(nMinusOne, n, cashFlowInfo.cashFlows);
        setNFVAndReturn(cashFlowInfo, xAxisCut, positiveNFVToReturnSet, negativeNFVToReturnSet);
    }

    public void setNFVAndReturn(CashFlowInfo cashFlowInfo, IRRAndNFV irrAndNFV, Set<IRRAndNFV> positiveNFVToReturnSet, Set<IRRAndNFV> negativeNFVToReturnSet) {
        if (Objects.isNull(irrAndNFV) || irrAndNFV.nfv.isNaN() || irrAndNFV.ratePerInterval.isNaN()) {
            return;
        }
        if (irrAndNFV.nfv == 0) {
            cashFlowInfo.setCurrentMinMax(new MinMaxIRRAndNFV(irrAndNFV, irrAndNFV));
            positiveNFVToReturnSet.add(irrAndNFV);
            negativeNFVToReturnSet.add(irrAndNFV);
            return;
        }

        //Check for current boundaries and ignore if going out of boundary
        MinMaxIRRAndNFV currentMinMax = cashFlowInfo.getCurrentMinMax();
        if (Objects.nonNull(currentMinMax) && Objects.nonNull(currentMinMax.max) && Objects.nonNull(currentMinMax.min)
                && (irrAndNFV.ratePerInterval < currentMinMax.min.ratePerInterval || irrAndNFV.ratePerInterval > currentMinMax.max.ratePerInterval)
        ) {
            return;
        }

        if (irrAndNFV.nfv > 0) {
            positiveNFVToReturnSet.add(irrAndNFV);
        } else {
            negativeNFVToReturnSet.add(irrAndNFV);
        }

    }

    //check whether both numbers are both positive or both negative
    private boolean areSameSign(Number n1, Number n2) {
        return (n1.doubleValue() > 0 && n2.doubleValue() > 0) || (n1.doubleValue() < 0 && n2.doubleValue() < 0) || (n1.doubleValue() == 0 && n2.doubleValue() == 0);
    }

    public MinMaxIRRAndNFV getNewMinMaxIRRAndNFV(MinMaxIRRAndNFV minMaxIRRAndNFV, Set<IRRAndNFV> positiveNFVToReturnSet, Set<IRRAndNFV> negativeNFVToReturnSet) {
        if (positiveNFVToReturnSet.size() == 0 || negativeNFVToReturnSet.size() == 0) {
            throw new IRRException("Both positive and negative cash flows are required to determine min max boundaries");
        }
        IRRAndNFV positiveMin = Collections.min(positiveNFVToReturnSet);
        IRRAndNFV positiveMax = Collections.max(positiveNFVToReturnSet);
        IRRAndNFV negativeMin = Collections.min(negativeNFVToReturnSet);
        IRRAndNFV negativeMax = Collections.max(negativeNFVToReturnSet);

        MinMaxIRRAndNFV givenMinMaxIRRAndNFV = minMaxIRRAndNFV;
        MinMaxIRRAndNFV newMinMaxIRRAndNFV;
        if (givenMinMaxIRRAndNFV == null) {
            givenMinMaxIRRAndNFV = new MinMaxIRRAndNFV(min(positiveMin, negativeMin), max(positiveMax, negativeMax));
            return givenMinMaxIRRAndNFV;
        }
        if (givenMinMaxIRRAndNFV.max.nfv >= 0) {
            newMinMaxIRRAndNFV = new MinMaxIRRAndNFV(negativeMax, positiveMin);
        } else {
            newMinMaxIRRAndNFV = new MinMaxIRRAndNFV(positiveMax, negativeMin);
        }
        return newMinMaxIRRAndNFV;
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

    private IRRAndNFV getXAxisCut(IRRAndNFV x1y1, IRRAndNFV x2y2, List<Number> cashFlows) {
        Double slope = (x2y2.nfv - x1y1.nfv) / (x2y2.ratePerInterval - x1y1.ratePerInterval);
        if (slope == 0 || slope.isNaN()) {
            return null;
        }
        Double ratePerInterval = null;
        if (slope.isInfinite()) {
            ratePerInterval = 0d;
        } else {
            ratePerInterval = x2y2.ratePerInterval - (x2y2.nfv / slope);
        }
        Double netFutureValue = futureValueCalculator.netFutureValue(cashFlows, ratePerInterval);
        IRRAndNFV irrAndNFV = new IRRAndNFV(ratePerInterval, netFutureValue);
        return irrAndNFV;
    }
}
