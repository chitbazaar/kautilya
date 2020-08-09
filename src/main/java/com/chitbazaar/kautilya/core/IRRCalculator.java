package com.chitbazaar.kautilya.core;

import com.chitbazaar.kautilya.util.NumberUtils;

import java.util.List;
import java.util.TreeMap;

public class IRRCalculator {
    private final int precision;
    private final Double increment;
    private FutureValueCalculator futureValueCalculator = new FutureValueCalculator();
    private CompoundingCalculator compoundingCalculator = new CompoundingCalculator();

    public IRRCalculator() {
        this(10);
    }

    public IRRCalculator(int precision) {
        if (precision < 0 || precision > 10) {
            throw new RuntimeException("Precision supported 0 to 10 inclusive");
        }
        this.precision = precision;
        increment = Math.pow(0.1, precision);
    }

    public Double irr(List<Double> cashFlows) {
        CashFlowInfo cashFlowInfo = new CashFlowInfo(cashFlows);
        Double result = irr(cashFlowInfo);
        return NumberUtils.round(result, precision);
    }

    Double irr(CashFlowInfo cashFlowInfo) {
        if (cashFlowInfo.getNetCashFlow() == 0) {
            return 0.0d;
        }
        if (cashFlowInfo.getNegativeCashFLow() == 0) {
            return Double.POSITIVE_INFINITY;
        }
        if (cashFlowInfo.getPositiveCashFlow() == 0) {
            return -100.0d;
        }
        if (cashFlowInfo.isOnlyEndCashFlows()) {
            Double first = Math.abs(cashFlowInfo.getCashFlows().get(0));
            Double last = Math.abs(cashFlowInfo.getCashFlows().get(cashFlowInfo.getCashFlows().size() - 1));
            return compoundingCalculator.compoundRate(first, last, cashFlowInfo.getNumberOfIntervals().doubleValue());
        }
        Double min = NumberUtils.round(cashFlowInfo.getMinReturnToCheck().ratePerInterval, precision);
        Double max = NumberUtils.round(cashFlowInfo.getMaxReturnToCheck().ratePerInterval, precision);
        Double mid = NumberUtils.round(((min + max) / 2), precision);
        Double maxDiff = increment + increment;
        Double nfvForMid;
        Double nfvForMin;
        Double nfvForMax;
        while (true) {
            nfvForMid = futureValueCalculator.netFutureValue(cashFlowInfo.getCashFlows(), mid);
            if (nfvForMid == 0) {
                return mid;
            }
            nfvForMin = futureValueCalculator.netFutureValue(cashFlowInfo.getCashFlows(), min);
            if (nfvForMin == 0) {
                return min;
            }
            nfvForMax = futureValueCalculator.netFutureValue(cashFlowInfo.getCashFlows(), max);
            if (nfvForMax == 0) {
                return max;
            }
            if (Math.abs(max - min) <= maxDiff) {
                break;
            }
            if (nfvForMin < 0 && nfvForMid < 0) {
                min = mid;
            } else if (nfvForMin > 0 && nfvForMid > 0) {
                min = mid;
            } else if (nfvForMax < 0 && nfvForMid < 0) {
                max = mid;
            } else if (nfvForMax > 0 && nfvForMid > 0) {
                max = mid;
            }
            mid = NumberUtils.round(((min + max) / 2), precision);
        }
        TreeMap<Double, Double> nfvMap = new TreeMap<>();
        nfvMap.put(Math.abs(nfvForMin), min);
        nfvMap.put(Math.abs(nfvForMax), max);
        nfvMap.put(Math.abs(nfvForMid), mid);
        return nfvMap.firstEntry().getValue();
    }
}
