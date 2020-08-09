package com.chitbazaar.kautilya.domain;

public enum CashFlowInterval {
    Daily(1 / 365d),
    Monthly(1 / 12d),
    Quarterly(1 / 4d),
    HalfYearly(1 / 2d),
    Annually(1d);
    public final Double numberOfYears;

    CashFlowInterval(Double numberOfYears) {
        this.numberOfYears = numberOfYears;
    }
}
