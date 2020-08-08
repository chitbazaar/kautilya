package com.chitbazaar.kautilya.core

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class FutureValueCalculatorSpec extends Specification {
    FutureValueCalculator sut = new FutureValueCalculator()

    def "F.V Should work #scenario"() {
        when:
        double fv = sut.futureValue(principal, ratePerInterval, numberOfIntervals, compoundingFrequency).round(2)
        then:
        fv == expected
        where:
        scenario                    || principal | ratePerInterval | numberOfIntervals | compoundingFrequency | expected
        'simple +ve'                || 100       | 12              | 1                 | 1                    | 100 + 12.0
        'simple -ve'                || 100       | -12             | 1                 | 1                    | 100 - 12.0
        '-ve principal'             || -100      | 12              | 1                 | 1                    | -100 - 12.0
        '-ve p -ve r'               || -100      | -12             | 1                 | 1                    | -100 + 12.0
        'quarterly compounded'      || 100       | 12              | 1                 | 4                    | 100 + 12.55
        'monthly compounded'        || 100       | 12              | 1                 | 12                   | 100 + 12.68
        'zero return'               || 100       | 0               | 10                | 12                   | 100 + 0
        'zero frequency'            || 100       | 20              | 10                | 0                    | 100 + 0
        'zero return and frequency' || 100       | 20              | 10                | 0                    | 100 + 0
    }

    def "N.F.V should work #scenario"() {
        when:
        double nfv = sut.netFutureValue(cashFlows, ratePerInterval).round(2)
        then:
        nfv == expected
        where:
        scenario                || cashFlows                             | ratePerInterval | expected
        'All positive'          || [3.0d, 3.0d, 3.0d, 3.0d, 103.0d]      | 4               | 116.25
        'Positive And Negative' || [-100.0d, 3.0d, 3.0d, 3.0d, 103.0d]   | 3               | 0
        'All negative'          || [-100.0d, -3.0d, -3.0d, -3.0d, -3.0d] | 2               | -120.61
        'With zeroes'           || [-100.0d, 0d, 0d, 0d, 0d]             | 5               | -121.55
    }

}
