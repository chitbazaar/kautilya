package script

import com.chitbazaar.kautilya.core.FutureValueCalculator

class IRR2NFVPlotter {

    static FutureValueCalculator futureValueCalculator = new FutureValueCalculator();
    public static void main(String[] args) {
        IRR2NFVPlotter irr2NFV = new IRR2NFVPlotter();
        List cashFlows = [-1000.0d,1.0d]
        StringBuilder report = new StringBuilder('irr,nfv\n')
        Double from = -1000
        Double to = 1000
        Double increment = (to-from)/100;
        for(double d=from; d<to ; d = d+ increment){
            double nfv = futureValueCalculator.netFutureValue(cashFlows, d)
            report.append("${d}, ${nfv}\n")
        }
        println report.toString()
    }
}
