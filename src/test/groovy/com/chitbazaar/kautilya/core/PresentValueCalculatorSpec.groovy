package com.chitbazaar.kautilya.core

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class PresentValueCalculatorSpec extends Specification {
    PresentValueCalculator sut = new PresentValueCalculator()

    def "P.V Should work #scenario"() {
        when:
        double pv = sut.presentValue(amount, ratePerInterval, numberOfIntervals, compoundingFrequency).round(2)
        then:
        pv == expected
        where:
        scenario                    || amount | ratePerInterval | numberOfIntervals | compoundingFrequency | expected
        'simple +ve'                || 112    | 12              | 1                 | 1                    | 100
        'simple -ve'                || 88     | -12             | 1                 | 1                    | 100
        '-ve principal'             || -112   | 12              | 1                 | 1                    | -100
        '-ve p -ve r'               || -88    | -12             | 1                 | 1                    | -100
        'quarterly compounded'      || 112.55 | 12              | 1                 | 4                    | 100
        'monthly compounded'        || 112.68 | 12              | 1                 | 12                   | 100
        'zero return'               || 100    | 0               | 10                | 12                   | 100
        'zero frequency'            || 100    | 20              | 10                | 0                    | 100
        'zero return and frequency' || 100    | 20              | 10                | 0                    | 100
    }

    def "P.V-Default frequency should work #scenario"() {
        when:
        double pv = sut.presentValue(principal, ratePerInterval, numberOfIntervals).round(2)
        then:
        pv == expected
        where:
        scenario     || principal | ratePerInterval | numberOfIntervals | expected
        'simple +ve' || 112       | 12              | 1                 | 100
        'simple -ve' || 88        | -12             | 1                 | 100
    }

    def "N.P.V should work #scenario"() {
        when:
        double npv = sut.netPresentValue(cashFlows, ratePerInterval).round(2)
        then:
        npv == expected
        where:
        scenario                || cashFlows                              | ratePerInterval | expected
        'All positive'          || [3.0d, 3.0d, 3.0d, 3.0d, 103.0d]       | 4               | 99.37
        'Positive And Negative' || [100.0d, -3.0d, -3.0d, -3.0d, -103.0d] | 3               | 0
        'All negative'          || [-100.0d, -3.0d, -3.0d, -3.0d, -3.0d]  | 2               | -111.42
        'With zeroes'           || [-100.0d, 0d, 0d, 0d, 0d]              | 5               | -100
    }

}
