package com.chitbazaar.kautilya.core

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class CompoundingCalculatorSpec extends Specification {
    CompoundingCalculator sut = new CompoundingCalculator()

    def "C.Interest Should work #scenario"() {
        when:
        double ci = sut.compoundInterest(principal, ratePerInterval, numberOfIntervals, compoundingFrequency).round(2)
        then:
        ci == expected
        where:
        scenario                    || principal | ratePerInterval | numberOfIntervals | compoundingFrequency | expected
        'simple +ve'                || 100       | 12              | 1                 | 1                    | 12.0
        'simple -ve'                || 100       | -12             | 1                 | 1                    | -12.0
        '-ve principal'             || -100      | 12              | 1                 | 1                    | -12.0
        '-ve p -ve r'               || -100      | -12             | 1                 | 1                    | 12.0
        'quarterly compounded'      || 100       | 12              | 1                 | 4                    | 12.55
        'monthly compounded'        || 100       | 12              | 1                 | 12                   | 12.68
        'zero return'               || 100       | 0               | 10                | 12                   | 0
        'zero frequency'            || 100       | 20              | 10                | 0                    | 0
        'zero return and frequency' || 100       | 20              | 10                | 0                    | 0
    }

    def "C.Amount Should work #scenario"() {
        when:
        double ci = sut.compoundAmount(principal, ratePerInterval, numberOfIntervals, compoundingFrequency).round(2)
        then:
        ci == principal + expected
        where:
        scenario                    || principal | ratePerInterval | numberOfIntervals | compoundingFrequency | expected
        'simple +ve'                || 100       | 12              | 1                 | 1                    | 12.0
        'simple -ve'                || 100       | -12             | 1                 | 1                    | -12.0
        '-ve principal'             || -100      | 12              | 1                 | 1                    | -12.0
        '-ve p -ve r'               || -100      | -12             | 1                 | 1                    | 12.0
        'quarterly compounded'      || 100       | 12              | 1                 | 4                    | 12.55
        'monthly compounded'        || 100       | 12              | 1                 | 12                   | 12.68
        'zero return'               || 100       | 0               | 10                | 12                   | 0
        'zero frequency'            || 100       | 20              | 10                | 0                    | 0
        'zero return and frequency' || 100       | 20              | 10                | 0                    | 0
    }

    def "C.Principal Should work #scenario"() {
        when:
        double ci = sut.compoundPrincipal(amount, ratePerInterval, numberOfIntervals, compoundingFrequency).round(2)
        then:
        ci == expected
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

    def "C.Rate Should work #scenario"() {
        when:
        double ci = sut.compoundRate(principal, amount, numberOfIntervals, compoundingFrequency).round(2)
        then:
        ci == expected
        where:
        scenario                    || amount | principal | numberOfIntervals | compoundingFrequency | expected
        'simple +ve'                || 112    | 100       | 1                 | 1                    | 12
        'simple -ve'                || 88     | 100       | 1                 | 1                    | -12
        '-ve principal'             || -112   | -100      | 1                 | 1                    | 12
        '-ve p -ve r'               || -88    | -100      | 1                 | 1                    | -12
        'quarterly compounded'      || 112.55 | 100       | 1                 | 4                    | 12
        'monthly compounded'        || 112.68 | 100       | 1                 | 12                   | 12
        'zero return'               || 100    | 100       | 10                | 12                   | 0
        'zero frequency'            || 100    | 100       | 10                | 0                    | Double.NaN
        'zero return and frequency' || 100    | 100       | 10                | 0                    | Double.NaN
    }
}
