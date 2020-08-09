package com.chitbazaar.kautilya.practical


import spock.lang.Specification
import spock.lang.Unroll

class PIRRCalculatorSpec extends Specification {

    PIRRCalculator sut

    def setup() {
        sut = new PIRRCalculator();
    }

    @Unroll
    def 'Annualised should work for default interval #cashFlows'() {
        when:
        Double annualized = sut.pirrAnnualized(cashFlows)
        then:
        annualized == expected
        where:
        cashFlows     | expected
        [-100d, 101d] | 12.6825
        [101d, -100d] | 12.55
        [100d, -101d] | -11.3615
        [-101d, 100d] | -11.2551
    }

    @Unroll
    def 'Practical IRR should work #cashFlows'() {
        when:
        Double pirr = sut.pirr(cashFlows, 2)
        then:
        pirr == expected
        cashFlows.size() == 5
        where:
        cashFlows                    | expected
        [-100d, 1d, 1d, 1d, 101d]    | 1
        [101d, 1d, 1d, 1d, -100d]    | 0.99
        [100d, -1d, -1d, -1d, -101d] | -1
        [-101d, -1d, -1d, -1d, 100d] | -0.99
        [-100d, -1d, -1d, -1d, 101d] | -0.5
    }

    def 'zero net cash flow'(){
        when:
        Double pirr = sut.pirr([-100d,100d])
        then:
        pirr == 0
        when:
        Double pirra = sut.pirrAnnualized([-100d,100d],0)
        then:
        pirra == 0
    }
}
