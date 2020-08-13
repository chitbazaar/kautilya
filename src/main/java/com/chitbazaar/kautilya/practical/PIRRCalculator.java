package com.chitbazaar.kautilya.practical;

import com.chitbazaar.kautilya.core.CompoundingCalculator;
import com.chitbazaar.kautilya.core.IRRCalculator;
import com.chitbazaar.kautilya.domain.CashFlowInfo;
import com.chitbazaar.kautilya.domain.CashFlowInterval;
import com.chitbazaar.kautilya.util.NumberUtils;

import java.util.List;

public class PIRRCalculator {
    private final Integer precision;
    private final IRRCalculator irrCalculator;
    private final CompoundingCalculator compoundingCalculator;

    public PIRRCalculator() {
        this(4);
    }

    public PIRRCalculator(Integer precision) {
        this.precision = precision;
        this.irrCalculator = new IRRCalculator(precision);
        this.compoundingCalculator = new CompoundingCalculator();
    }

    public Double pirr(List<Number> cashFlows, Integer precision) {
        CashFlowInfo cashFlowInfo = new CashFlowInfo(cashFlows, precision);
        double irr = irrCalculator.irr(cashFlowInfo);
        if (irr == 0 || irr == Double.POSITIVE_INFINITY || irr == Double.NEGATIVE_INFINITY) {
            return irr;
        }
        if (cashFlowInfo.netCashFlow >= 0) {
            return Math.abs(irr);
        } else {
            return -1 * Math.abs(irr);
        }
    }

    public Double pirr(List<Number> cashFlows) {
        return pirr(cashFlows, precision);
    }

    public Double pirrAnnualized(List<Number> cashFlows) {
        return pirrAnnualized(cashFlows, CashFlowInterval.Monthly, precision);
    }

    public Double pirrAnnualized(List<Number> cashFlows, Integer precision) {
        return pirrAnnualized(cashFlows, CashFlowInterval.Monthly, precision);
    }

    public Double pirrAnnualized(List<Number> cashFlows, CashFlowInterval interval, Integer precision) {
        CashFlowInfo cashFlowInfo = new CashFlowInfo(cashFlows, precision);
        Double principal = 100d;
        Double pirr = pirr(cashFlows, cashFlowInfo.precision);
        Double pirrAnnualized = compoundingCalculator.compoundInterest(principal, pirr, 1 / interval.numberOfYears, 1d);
        return NumberUtils.round(pirrAnnualized, cashFlowInfo.precision);
    }
}
