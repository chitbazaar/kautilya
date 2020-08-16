package com.chitbazaar.kautilya.domain;

import com.chitbazaar.kautilya.core.IRRCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CashFlowInfo {
    public final List<Number> cashFlows;
    public final Integer positiveCashFlowCount;
    public final Integer negativeCashFlowCount;
    public final Integer zeroCashFlowCount;
    public final Integer numberOfIntervals;
    public final Double netCashFlow;
    public final Double positiveCashFlow;
    public final Double negativeCashFLow;
    public final boolean onlyEndCashFlows;
    public final Integer precision;
    public final Double increment;
    public final Double irrLowerLimit;
    public final Double irrUpperLimit;
    public MinMaxIRRAndNFV currentMinMax;

    public CashFlowInfo(List<Number> cashFlows, Integer precision) {
        if (precision < IRRCalculator.MIN_PRECISION || precision > IRRCalculator.MAX_PRECISION) {
            throw new RuntimeException(String.format("Precision supported %s to %s inclusive", IRRCalculator.MIN_PRECISION, IRRCalculator.MAX_PRECISION));
        }
        List<Double> cleanedUpCashFlows = new ArrayList();
        Double netCashFlow = 0d;
        Double positiveCashFlow = 0d;
        Double negativeCashFLow = 0d;
        Integer negativeCashFlowCount = 0;
        Integer positiveCashFlowCount = 0;
        Integer zeroCashFlowCount = 0;
        boolean onlyEndCashFlows = true;
        for (Integer index = 0; index < cashFlows.size(); index++) {
            Double cashFlow = null;
            if (Objects.isNull(cashFlows.get(index))) {
                cashFlow = 0d;
            } else {
                cashFlow = (cashFlows.get(index)).doubleValue();
            }
            if (cashFlow < 0) {
                cleanedUpCashFlows.add(cashFlow);
                negativeCashFlowCount++;
                negativeCashFLow +=  cashFlow;
            } else if (cashFlow > 0) {
                cleanedUpCashFlows.add(cashFlow);
                positiveCashFlowCount++;
                positiveCashFlow += cashFlow;
            } else {
                cleanedUpCashFlows.add(cashFlow);
                zeroCashFlowCount++;
            }
            netCashFlow += cashFlow;
            if (index != 0 && index != cashFlows.size() - 1 && cashFlow != 0) {
                onlyEndCashFlows = false;
            }
        }
        this.precision = precision;
        this.increment = Math.pow(0.1, precision);
        this.onlyEndCashFlows = onlyEndCashFlows;
        this.cashFlows = Collections.unmodifiableList(cleanedUpCashFlows);
        this.netCashFlow = netCashFlow;
        this.positiveCashFlow = positiveCashFlow;
        this.negativeCashFLow = negativeCashFLow;
        this.negativeCashFlowCount = negativeCashFlowCount;
        this.positiveCashFlowCount = positiveCashFlowCount;
        this.zeroCashFlowCount = zeroCashFlowCount;
        this.numberOfIntervals = cashFlows.size() - 1;
        if (this.positiveCashFlow == this.negativeCashFLow) {
            this.irrLowerLimit = 0d;
            this.irrUpperLimit = 0d;
        } else {
            Double biggerCashFlow = this.positiveCashFlow > this.negativeCashFLow ? this.positiveCashFlow : this.negativeCashFLow;
            Double smallerCashFlow = this.positiveCashFlow > this.negativeCashFLow ? this.negativeCashFLow : this.positiveCashFlow;
            this.irrLowerLimit = ((smallerCashFlow / biggerCashFlow - 1) * 100) - this.increment;
            this.irrUpperLimit = ((biggerCashFlow / smallerCashFlow - 1) * 100) + this.increment;
        }
    }

    public MinMaxIRRAndNFV getCurrentMinMax() {
        return currentMinMax;
    }

    public void setCurrentMinMax(MinMaxIRRAndNFV currentMinMax) {
        this.currentMinMax = currentMinMax;
    }
}
