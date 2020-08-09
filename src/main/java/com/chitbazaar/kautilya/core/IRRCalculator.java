package com.chitbazaar.kautilya.core;

import com.chitbazaar.kautilya.domain.CashFlowInfo;
import com.chitbazaar.kautilya.domain.MinMaxIRRAndNFV;
import com.chitbazaar.kautilya.util.NumberUtils;

import java.util.List;

public class IRRCalculator {
    private final int precision;
    private final Double increment;
    private FutureValueCalculator futureValueCalculator = new FutureValueCalculator();
    private CompoundingCalculator compoundingCalculator = new CompoundingCalculator();
    private IRRHelper irrHelper = new IRRHelper();

    public IRRCalculator() {
        this(10);
    }

    public IRRCalculator(int precision) {
//        Somehow in java following gives different values so precision is restricted to 14
//        double value = ((25.759135891535635 + 25.75913589153564) / 2)
//        println value
//        println((25.759135891535635 + 25.75913589153564) / 2)

        if (precision < 0 || precision > 14) {
            throw new RuntimeException("Precision supported 0 to 10 inclusive");
        }
        this.precision = precision;
        increment = Math.pow(0.1, precision);
    }

    public Double irr(List<Double> cashFlows) {
        CashFlowInfo cashFlowInfo = new CashFlowInfo(cashFlows, precision);
        Double result = irr(cashFlowInfo);
        return NumberUtils.round(result, precision);
    }

    Double irr(CashFlowInfo cashFlowInfo) {
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
            Double first = Math.abs(cashFlowInfo.cashFlows.get(0));
            Double last = Math.abs(cashFlowInfo.cashFlows.get(cashFlowInfo.cashFlows.size() - 1));
            return compoundingCalculator.compoundRate(first, last, cashFlowInfo.numberOfIntervals.doubleValue());
        }
        MinMaxIRRAndNFV minMaxIRRAndNFV = irrHelper.getInitialBounderies(cashFlowInfo);
        Double maxDiff = increment + increment;
        while (true) {
            if (minMaxIRRAndNFV.min.nfv == 0) {
                return minMaxIRRAndNFV.min.ratePerInterval;
            }
            if (minMaxIRRAndNFV.max.nfv == 0) {
                return minMaxIRRAndNFV.max.ratePerInterval;
            }
            if (minMaxIRRAndNFV.min.ratePerInterval.isNaN() || minMaxIRRAndNFV.max.ratePerInterval.isNaN()) {
                throw new RuntimeException("Unexpected NAN");
            }
//            System.out.printf("min:%s\tmax:%s\n", minMaxIRRAndNFV.min.ratePerInterval, minMaxIRRAndNFV.max.ratePerInterval);
            if (minMaxIRRAndNFV.getIRRAbsDifference() <= maxDiff) {
                break;
            }
            minMaxIRRAndNFV = irrHelper.getNewMinMaxIRRAndNFV(minMaxIRRAndNFV, cashFlowInfo);
        }
        return minMaxIRRAndNFV.getIRRForLeastAbsNFV();
    }

}
