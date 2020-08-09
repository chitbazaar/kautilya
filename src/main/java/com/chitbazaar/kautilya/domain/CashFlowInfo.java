package com.chitbazaar.kautilya.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CashFlowInfo {
    public final List<Double> cashFlows;
    public final Integer positiveCashFlowCount;
    public final Integer negativeCashFlowCount;
    public final Integer zeroCashFlowCount;
    public final Integer numberOfIntervals;
    public final Double netCashFlow;
    public final Double positiveCashFlow;
    public final Double negativeCashFLow;
    public final boolean onlyEndCashFlows;
    public final Integer precision;

    public CashFlowInfo(List<Double> cashFlows, Integer precision) {
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
                cashFlow = 0d;
                cleanedUpCashFlows.add(cashFlow);
                zeroCashFlowCount++;
            } else if (cashFlow < 0) {
                cleanedUpCashFlows.add(cashFlow);
                negativeCashFlowCount++;
                negativeCashFLow += -1 * cashFlow;
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
        this.onlyEndCashFlows = onlyEndCashFlows;
        this.cashFlows = Collections.unmodifiableList(cleanedUpCashFlows);
        this.netCashFlow = netCashFlow;
        this.positiveCashFlow = positiveCashFlow;
        this.negativeCashFLow = negativeCashFLow;
        this.negativeCashFlowCount = negativeCashFlowCount;
        this.positiveCashFlowCount = positiveCashFlowCount;
        this.zeroCashFlowCount = zeroCashFlowCount;
        this.numberOfIntervals = cashFlows.size() - 1;
    }
}
