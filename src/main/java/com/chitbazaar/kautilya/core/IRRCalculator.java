package com.chitbazaar.kautilya.core;

import com.chitbazaar.kautilya.domain.CashFlowInfo;
import com.chitbazaar.kautilya.domain.IRRAndNFV;
import com.chitbazaar.kautilya.domain.MinMaxIRRAndNFV;
import com.chitbazaar.kautilya.util.NumberUtils;

import java.util.List;

public class IRRCalculator {
    public static final Integer MIN_PRECISION = 0;
    public static final Integer MAX_PRECISION = 13;
    private final int defaultPrecision;
    private FutureValueCalculator futureValueCalculator = new FutureValueCalculator();
    private CompoundingCalculator compoundingCalculator = new CompoundingCalculator();
    private IRRHelper irrHelper = new IRRHelper();

    public IRRCalculator() {
        this(10);
    }

    public IRRCalculator(int precision) {
//        Somehow in java following gives different values so precision is restricted to 13
//        double value = ((25.759135891535635 + 25.75913589153564) / 2)
//        println value
//        println((25.759135891535635 + 25.75913589153564) / 2)

        if (precision < MIN_PRECISION || precision > MAX_PRECISION) {
            throw new IRRException(String.format("Precision supported %s to %s inclusive", MIN_PRECISION, MAX_PRECISION));
        }
        this.defaultPrecision = precision;
    }

    public Double irr(List<Number> cashFlows) {
        CashFlowInfo cashFlowInfo = new CashFlowInfo(cashFlows, defaultPrecision);
        Double result = irr(cashFlowInfo);
        return NumberUtils.round(result, defaultPrecision);
    }

    public Double irr(CashFlowInfo cashFlowInfo) {
        if (cashFlowInfo.netCashFlow == 0) {
            return 0.0d;
        }
        if (cashFlowInfo.negativeCashFLow == 0) {
            return Double.POSITIVE_INFINITY;
        }
        if (cashFlowInfo.positiveCashFlow == 0) {
            return -100.0d;
        }
        if (cashFlowInfo.onlyEndCashFlows) {
            Double first = Math.abs(cashFlowInfo.cashFlows.get(0).doubleValue());
            Double last = Math.abs(cashFlowInfo.cashFlows.get(cashFlowInfo.cashFlows.size() - 1).doubleValue());
            return NumberUtils.round(compoundingCalculator.compoundRate(first, last, cashFlowInfo.numberOfIntervals.doubleValue()), cashFlowInfo.precision);
        }
        MinMaxIRRAndNFV minMaxIRRAndNFV = irrHelper.getInitialBounderies(cashFlowInfo);
        Double increment = cashFlowInfo.increment;
        Double maxDiff = increment + increment;
        Double maxIterations = NumberUtils.log2((minMaxIRRAndNFV.max.ratePerInterval - minMaxIRRAndNFV.min.ratePerInterval) / increment) + 1;
        Double count = 0d;
        while (true) {
            if (minMaxIRRAndNFV.min.nfv == 0) {
                return minMaxIRRAndNFV.min.ratePerInterval;
            }
            if (minMaxIRRAndNFV.max.nfv == 0) {
                return minMaxIRRAndNFV.max.ratePerInterval;
            }
            if (minMaxIRRAndNFV.min.ratePerInterval.isNaN() || minMaxIRRAndNFV.max.ratePerInterval.isNaN()) {
                throw new IRRException("Unexpected NAN");
            }
//            System.out.printf("min:%s\tmax:%s\n", minMaxIRRAndNFV.min.ratePerInterval, minMaxIRRAndNFV.max.ratePerInterval);
            minMaxIRRAndNFV = irrHelper.setAndGetNewMinMaxIRRAndNFV(minMaxIRRAndNFV, cashFlowInfo);
            if (minMaxIRRAndNFV.getIRRAbsDifference() <= maxDiff) {
                break;
            }
            if (count > maxIterations) {
                throw new IRRException("Something unexpected.. could not find IRR.");
            }
            count++;
        }
        Double closeIRR = minMaxIRRAndNFV.getIRRForLeastAbsNFV();
        Double floorIRR = NumberUtils.floor(closeIRR, cashFlowInfo.precision);
        Double nfvForFloorIRR = futureValueCalculator.netFutureValue(cashFlowInfo.cashFlows, floorIRR);
        Double ceilIRR = NumberUtils.ceil(closeIRR, cashFlowInfo.precision);
        Double nfvForCeilIRR = futureValueCalculator.netFutureValue(cashFlowInfo.cashFlows, ceilIRR);
        minMaxIRRAndNFV = new MinMaxIRRAndNFV(new IRRAndNFV(floorIRR, nfvForFloorIRR), new IRRAndNFV(ceilIRR, nfvForCeilIRR));
        return NumberUtils.round(minMaxIRRAndNFV.getIRRForLeastAbsNFV(), cashFlowInfo.precision);
    }

}
