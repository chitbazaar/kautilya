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

    private IRRAndNFV getIRRandNFV(Double principal, Double amount, Integer numberOfIntervals, List<Double> cashFlows) {
        Double ratePerInterval = compoundingCalculator.compoundRate(principal, amount, numberOfIntervals.doubleValue());
        Double netFutureValue = futureValueCalculator.netFutureValue(cashFlows, ratePerInterval);
        IRRAndNFV irrAndNFV = new IRRAndNFV(ratePerInterval, netFutureValue);
        return irrAndNFV;
    }

    private IRRAndNFV getIRRandNFV(Double ratePerInterval, List<Double> cashFlows) {
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
        Double minMinusOnePrecision = newMinMaxIRRAndNFV.min.ratePerInterval - cashFlowInfo.precision;
        IRRAndNFV nMinusOne = new IRRAndNFV(minMinusOnePrecision, futureValueCalculator.netFutureValue(cashFlowInfo.cashFlows, minMinusOnePrecision));
        Double minPlusOnePrecision = newMinMaxIRRAndNFV.min.ratePerInterval + cashFlowInfo.precision;
        IRRAndNFV nPlusOne = new IRRAndNFV(minPlusOnePrecision, futureValueCalculator.netFutureValue(cashFlowInfo.cashFlows, minPlusOnePrecision));
        xAxisCut = getXAxisCut(nMinusOne, nPlusOne, cashFlowInfo.cashFlows);
        setNFVAndReturn(xAxisCut, positiveNFVToReturnSet, negativeNFVToReturnSet);
        newMinMaxIRRAndNFV = getNewMinMaxIRRAndNFV(newMinMaxIRRAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        //Max tangential cut
        Double maxMinusOnePrecision = newMinMaxIRRAndNFV.max.ratePerInterval - cashFlowInfo.precision;
        nMinusOne = new IRRAndNFV(maxMinusOnePrecision, futureValueCalculator.netFutureValue(cashFlowInfo.cashFlows, maxMinusOnePrecision));
        Double maxPlusOnePrecision = newMinMaxIRRAndNFV.max.ratePerInterval + cashFlowInfo.precision;
        nPlusOne = new IRRAndNFV(maxPlusOnePrecision, futureValueCalculator.netFutureValue(cashFlowInfo.cashFlows, maxPlusOnePrecision));
        xAxisCut = getXAxisCut(nMinusOne, nPlusOne, cashFlowInfo.cashFlows);
        setNFVAndReturn(xAxisCut, positiveNFVToReturnSet, negativeNFVToReturnSet);
        newMinMaxIRRAndNFV = getNewMinMaxIRRAndNFV(newMinMaxIRRAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        //Middle
        Double mid = (newMinMaxIRRAndNFV.min.ratePerInterval + newMinMaxIRRAndNFV.max.ratePerInterval) / 2d;
        IRRAndNFV midIRRNV = new IRRAndNFV(mid, futureValueCalculator.netFutureValue(cashFlowInfo.cashFlows, mid));
        setNFVAndReturn(midIRRNV, positiveNFVToReturnSet, negativeNFVToReturnSet);
        newMinMaxIRRAndNFV = getNewMinMaxIRRAndNFV(newMinMaxIRRAndNFV, positiveNFVToReturnSet, negativeNFVToReturnSet);

        return newMinMaxIRRAndNFV;
    }

    public void setNFVAndReturn(IRRAndNFV irrAndNFV, Set<IRRAndNFV> positiveNFVToReturnSet, Set<IRRAndNFV> negativeNFVToReturnSet) {
        if (Objects.isNull(irrAndNFV)) {
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

    private IRRAndNFV getXAxisCut(IRRAndNFV x1y1, IRRAndNFV x2y2, List<Double> cashFlows) {
        Double slope = (x2y2.nfv - x1y1.nfv) / (x2y2.ratePerInterval - x1y1.ratePerInterval);
        if (slope == 0) {
            return null;
        }
        Double ratePerInterval = x2y2.ratePerInterval - x2y2.nfv / slope;
        Double netFutureValue = futureValueCalculator.netFutureValue(cashFlows, ratePerInterval);
        IRRAndNFV irrAndNFV = new IRRAndNFV(ratePerInterval, netFutureValue);
        return irrAndNFV;
    }
}
