package com.chitbazaar.kautilya.core

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class IRRCalculatorSpec extends Specification {
    IRRCalculator sut
    FutureValueCalculator futureValueCalculator

    def setup() {
        sut = new IRRCalculator()
        futureValueCalculator = new FutureValueCalculator()
    }

    def "Should return IRR #scenario"() {
        when:
        Double irr = sut.irr(cashFlows)
        Double nfvBefore = futureValueCalculator.netFutureValue(cashFlows, (irr - sut.increment).round(sut.precision)).abs()
        Double nfv = futureValueCalculator.netFutureValue(cashFlows, irr).abs()
        Double nfvAfter = futureValueCalculator.netFutureValue(cashFlows, (irr + sut.increment).round(sut.precision)).abs()
        println nfv
        then:
        (nfvBefore >= nfv) && (nfv <= nfvAfter)
        where:
        scenario                || cashFlows
        'simple +ve return'     || [-100d, 112d]
        'simple +ve return - 2' || [100d, -112d]
        'simple -ve return'     || [-100d, 88d]
        'simple -ve return - 2' || [88d, -100d]
        'Positive And Negative' || [-100.0d, 3.0d, 3.0d, 3.0d, 103.0d]
    }
}
