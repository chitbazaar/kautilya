package com.chitbazaar.kautilya.core;

import com.chitbazaar.kautilya.domain.CashFlowInfo;
import com.chitbazaar.kautilya.domain.MinMaxIRRAndNFV;
import com.chitbazaar.kautilya.util.NumberUtils;

import java.util.List;

public class IRRCalculator {
    public static final Integer MIN_PRECISION = 0;
    public static final Integer MAX_PRECISION = 14;
    private final int defaultPrecision;
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

        if (precision < MIN_PRECISION || precision > MAX_PRECISION) {
            throw new RuntimeException(String.format("Precision supported %s to %s inclusive", MIN_PRECISION, MAX_PRECISION));
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
        return NumberUtils.round(minMaxIRRAndNFV.getIRRForLeastAbsNFV(), cashFlowInfo.precision);
    }

}
