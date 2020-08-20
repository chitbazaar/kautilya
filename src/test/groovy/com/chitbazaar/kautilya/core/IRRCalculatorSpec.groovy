package com.chitbazaar.kautilya.core

import com.chitbazaar.kautilya.domain.CashFlowInfo
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class IRRCalculatorSpec extends Specification {
    IRRCalculator sut
    FutureValueCalculator futureValueCalculator
    Integer precision

    def setup() {
        precision = IRRCalculator.MAX_PRECISION
        sut = new IRRCalculator(precision)
        futureValueCalculator = new FutureValueCalculator()
    }

    def "Should return IRR #scenario"() {
        setup:
        CashFlowInfo cashFlowInfo = new CashFlowInfo(cashFlows, precision)
        when:
        Double increment = Math.pow(0.1, precision);
        Double irr = sut.irr(cashFlows)
        Double nfvBefore = futureValueCalculator.netFutureValue(cashFlows, (irr - increment).round(precision)).abs()
        Double nfv = futureValueCalculator.netFutureValue(cashFlows, irr).abs()
        Double nfvAfter = futureValueCalculator.netFutureValue(cashFlows, (irr + increment).round(precision)).abs()
        println "***** irr: ${irr}  ******"
        then:
        (nfvBefore >= nfv) && (nfv <= nfvAfter)
        where:
        scenario                || cashFlows
        'simple +ve return'     || [-100d, 112d]
        'simple +ve return - 2' || [100d, -112d]
        'simple -ve return'     || [-100d, 88d]
        'simple -ve return - 2' || [88d, -100d]
        'Positive And Negative' || [-100.0d, 3.0d, 3.0d, 3.0d, 103.0d]
        'Small cash flows'      || [0.001d, 0.1d, 1d, 1d, 1d, 0d, 0d, 0d, 0d, -10d]
        'Some big flows'        || [-6000000d, -3000000d, -3000000d, -3000000d,
                                    5000000d, 5000000d, 5000000d, 5000000d, 5000000d, 5000000d,
                                    6600000d, 6600000d, 6600000d, 6600000d,
                                    10600000d, 10600000d, 10600000d, 10600000d, 10600000d, 10600000d,
                                    19000000d, 19000000d, 19000000d, 19000000d, 19000000d,
                                    31000000d, 31000000d, 31000000d, 31000000d, 31000000d]
        'Some big flows 2'      || [100000.0d, 100000.0d, 100000.0d, 100000.0d, 100000.0d, -100000.0d, 100000.0d, 100000.0d, 100000.0d, 100000.0d, -91965802.7616772739d]
        'Multiple zero cuts'    || [-10, -10, 100]
        'Multiple zero cuts 2'  || [0.5, -4, -4, -4, -4, 10, 0.4]
        'Interesting'           || [5, -4, -4, -4, -4, 17]
        'Interesting 2'         || [17, -4, -4, -4, -4, 5]
        'Extreme 1'             || [-1, -1, 0, 10000000]
        'Extreme 2'             || [-10000000, 0, 1, 1]
    }

    def 'Check precision'() {
        when:
        IRRCalculator irrCalculator = new IRRCalculator(20)
        then:
        thrown(RuntimeException)
    }

    def 'Edge cases'() {
        when: 'net cash flow 0'
        Double irr = sut.irr([-100d, 50d, 50d])
        then:
        irr == 0
        when: 'No negative cash flow'
        irr = sut.irr([100d, 50d, 50d])
        then:
        irr == Double.POSITIVE_INFINITY
        when: 'No positive cash flow'
        irr = sut.irr([-100d, -50d, -50d])
        then:
        irr == -100d
    }
}
