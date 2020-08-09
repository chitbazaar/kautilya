package com.chitbazaar.kautilya.util

import spock.lang.Specification

class NumberUtilsSpec extends Specification{
    def 'Should round to given precision'(){
        when:
        double result = NumberUtils.round(7.6789234, 2)
        then:
        result == 7.68
    }
    def 'Should handle null safe'(){
        when:
        double result = NumberUtils.round(2323.343, null)
        then:
        noExceptionThrown()
    }
}
