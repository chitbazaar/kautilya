package com.chitbazaar.kautilya.core;

import com.chitbazaar.kautilya.domain.CashFlowInfo;
import com.chitbazaar.kautilya.domain.IRRAndNFV;
import com.chitbazaar.kautilya.domain.MinMaxIRRAndNFV;

import java.util.*;

public class IRRHelper {

    private final FutureValueCalculator futureValueCalculator = new FutureValueCalculator();
    private final CompoundingCalculator compoundingCalculator = new CompoundingCalculator();

    public MinMaxIRRAndNFV getInitialBounderies(CashFlowInfo cashFlowInfo) {
        MinMaxIRRAndNFV minMaxIRRAndNFV = new MinMaxIRRAndNFV();
        Set<IRRAndNFV> positiveNFVToReturnSet = new TreeSet<>();
        Set<IRRAndNFV> negativeNFVToReturnSet = new TreeSet<>();

        IRRAndNFV irrAndNFV = null;
        //near intervals
        irrAndNFV = getIRRandNFV(cashFlowInfo.negativeCashFLow, cashFlowInfo.positiveCashFlow, 1, cashFlowInfo.cashFlows);
        setNFVAndReturn(irrAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        irrAndNFV = getIRRandNFV(cashFlowInfo.positiveCashFlow, cashFlowInfo.negativeCashFLow, 1, cashFlowInfo.cashFlows);
        setNFVAndReturn(irrAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        //far intervals
        irrAndNFV = getIRRandNFV(cashFlowInfo.negativeCashFLow, cashFlowInfo.positiveCashFlow, cashFlowInfo.numberOfIntervals, cashFlowInfo.cashFlows);
        setNFVAndReturn(irrAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        irrAndNFV = getIRRandNFV(cashFlowInfo.positiveCashFlow, cashFlowInfo.negativeCashFLow, cashFlowInfo.numberOfIntervals, cashFlowInfo.cashFlows);
        setNFVAndReturn(irrAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        minMaxIRRAndNFV = getNewMinMaxIRRAndNFV(minMaxIRRAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        //zero irr
        irrAndNFV = getIRRandNFV(0.0, cashFlowInfo.cashFlows);
        setNFVAndReturn(irrAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);
        minMaxIRRAndNFV = getNewMinMaxIRRAndNFV(minMaxIRRAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        return getNewMinMaxIRRAndNFV(minMaxIRRAndNFV, cashFlowInfo);
    }

    private IRRAndNFV getIRRandNFV(Double principal, Double amount, Integer numberOfIntervals, List<Number> cashFlows) {
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

    public MinMaxIRRAndNFV getNewMinMaxIRRAndNFV(MinMaxIRRAndNFV minMaxIRRAndNFV, CashFlowInfo cashFlowInfo) {
        Set<IRRAndNFV> positiveNFVToReturnSet = new TreeSet<>();
        Set<IRRAndNFV> negativeNFVToReturnSet = new TreeSet<>();
        setNFVAndReturn(minMaxIRRAndNFV.min, positiveNFVToReturnSet, negativeNFVToReturnSet);
        setNFVAndReturn(minMaxIRRAndNFV.max, positiveNFVToReturnSet, negativeNFVToReturnSet);

        MinMaxIRRAndNFV newMinMaxIRRAndNFV = null;

        //x axis cut
        IRRAndNFV xAxisCut = getXAxisCut(minMaxIRRAndNFV.min, minMaxIRRAndNFV.max, cashFlowInfo.cashFlows);
        setNFVAndReturn(xAxisCut, positiveNFVToReturnSet, negativeNFVToReturnSet);
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
        setNFVAndReturn(mid, positiveNFVToReturnSet, negativeNFVToReturnSet);

        setTangentialCuts(mid, cashFlowInfo, positiveNFVToReturnSet, negativeNFVToReturnSet);
        newMinMaxIRRAndNFV = getNewMinMaxIRRAndNFV(newMinMaxIRRAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        return newMinMaxIRRAndNFV;
    }

    private void setTangentialCuts(IRRAndNFV n, CashFlowInfo cashFlowInfo, Set<IRRAndNFV> positiveNFVToReturnSet, Set<IRRAndNFV> negativeNFVToReturnSet) {
        Double nMinusOnePrecision = n.ratePerInterval - cashFlowInfo.precision;
        Double nPlusOnePrecision = n.ratePerInterval + cashFlowInfo.precision;
        IRRAndNFV nMinusOne = new IRRAndNFV(nMinusOnePrecision, futureValueCalculator.netFutureValue(cashFlowInfo.cashFlows, nMinusOnePrecision));
        IRRAndNFV nPlusOne = new IRRAndNFV(nPlusOnePrecision, futureValueCalculator.netFutureValue(cashFlowInfo.cashFlows, nPlusOnePrecision));
        IRRAndNFV xAxisCut = null;
        xAxisCut = getXAxisCut(nMinusOne, nPlusOne, cashFlowInfo.cashFlows);
        setNFVAndReturn(xAxisCut, positiveNFVToReturnSet, negativeNFVToReturnSet);
        xAxisCut = getXAxisCut(n, nPlusOne, cashFlowInfo.cashFlows);
        setNFVAndReturn(xAxisCut, positiveNFVToReturnSet, negativeNFVToReturnSet);
        xAxisCut = getXAxisCut(nMinusOne, n, cashFlowInfo.cashFlows);
        setNFVAndReturn(xAxisCut, positiveNFVToReturnSet, negativeNFVToReturnSet);
    }

    public void setNFVAndReturn(IRRAndNFV irrAndNFV, Set<IRRAndNFV> positiveNFVToReturnSet, Set<IRRAndNFV> negativeNFVToReturnSet) {
        if (Objects.isNull(irrAndNFV) || irrAndNFV.nfv.isNaN() || irrAndNFV.ratePerInterval.isNaN()) {
            return;
        }
        if (irrAndNFV.nfv > 0) {
            positiveNFVToReturnSet.add(irrAndNFV);
        } else {
            negativeNFVToReturnSet.add(irrAndNFV);
        }
    }


    public MinMaxIRRAndNFV getNewMinMaxIRRAndNFV(MinMaxIRRAndNFV minMaxIRRAndNFV, Set<IRRAndNFV> positiveNFVToReturnSet, Set<IRRAndNFV> negativeNFVToReturnSet) {
        IRRAndNFV positiveMin = Collections.min(positiveNFVToReturnSet);
        IRRAndNFV positiveMax = Collections.max(positiveNFVToReturnSet);
        IRRAndNFV negativeMin = Collections.min(negativeNFVToReturnSet);
        IRRAndNFV negativeMax = Collections.max(negativeNFVToReturnSet);

        MinMaxIRRAndNFV newMinMaxIRRAndNFV = null;

        if (minMaxIRRAndNFV.max == null) {
            newMinMaxIRRAndNFV = new MinMaxIRRAndNFV(min(positiveMin, negativeMin), max(positiveMax, negativeMax));
        } else if (minMaxIRRAndNFV.max.nfv >= 0) {
            newMinMaxIRRAndNFV = new MinMaxIRRAndNFV(min(negativeMax, positiveMin), max(negativeMax, positiveMin));
        } else {
            newMinMaxIRRAndNFV = new MinMaxIRRAndNFV(min(negativeMin, positiveMax), max(negativeMin, positiveMax));
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
