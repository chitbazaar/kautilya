package com.chitbazaar.kautilya.core

import com.chitbazaar.kautilya.domain.CashFlowInfo
import spock.lang.Specification
import spock.lang.Unroll

class CashFlowInfoSpec extends Specification {
    def 'Should populate all relevant info'() {
        setup:
        List<Double> cashFLows = [-100d, 7d, 0d, 7d, null, 0d, -6d, 100d]
        when:
        CashFlowInfo cashFlowInfo = new CashFlowInfo(cashFLows, 1)
        then:
        cashFlowInfo.positiveCashFlowCount == 3
        cashFlowInfo.negativeCashFlowCount == 2
        cashFlowInfo.zeroCashFlowCount == 3
        cashFlowInfo.cashFlows.size() == 8
    }

    @Unroll
    def 'negative cash flow scenarios'() {
        when:
        CashFlowInfo cashFlowInfo = new CashFlowInfo(cashFLows, IRRCalculator.MAX_PRECISION)
        then:
        cashFlowInfo.positiveCashFlowCount == 2
        cashFlowInfo.negativeCashFlowCount == 1
        cashFlowInfo.zeroCashFlowCount == 1
        where:
        scenario       || cashFLows
        'Positive IRR' || [89d, 1d, 0d, -100d]
        'Negative IRR' || [-100d, 0d, 1d, 89d]
    }

}
