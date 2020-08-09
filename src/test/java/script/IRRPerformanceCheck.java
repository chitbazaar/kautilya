package script;

import com.chitbazaar.kautilya.core.IRRCalculator;
import org.apache.poi.ss.formula.functions.Irr;

import java.util.ArrayList;
import java.util.List;

public class IRRPerformanceCheck {
    Double numberOfTries = 1000d;
    public static void main(String[] args) {
        IRRPerformanceCheck irrPerformanceCheck = new IRRPerformanceCheck();
        double[] input = {
                -6000000d, -3000000d, -3000000d, -3000000d,
                5000000d, 5000000d, 5000000d, 5000000d, 5000000d, 5000000d,
                6600000d, 6600000d, 6600000d, 6600000d,
                10600000d, 10600000d, 10600000d, 10600000d, 10600000d, 10600000d,
                19000000d, 19000000d, 19000000d, 19000000d, 19000000d,
                31000000d, 31000000d, 31000000d, 31000000d, 31000000d
        };
        PerformanceInfo poiIRRPerformance = irrPerformanceCheck.checkPOIIRR(input);
        System.out.println(poiIRRPerformance.toString());

//        List<Double> cashFlows = new ArrayList<>();
//        for (double cashFlow : input) {
//            cashFlows.add(cashFlow);
//        }
//
//        List<PerformanceInfo> myIRRPerformance = irrPerformanceCheck.checkMyIRR(cashFlows);
//
//        for (PerformanceInfo performanceInfo : myIRRPerformance) {
//            System.out.println(performanceInfo.toString());
//        }
    }

    List<PerformanceInfo> checkMyIRR(List<Double> cashFlows) {

        List<PerformanceInfo> infos = new ArrayList<>();
        for (int precision = 14; precision <= 14; precision += 1) {
            System.out.printf("Checking for precision %s\n", precision);
            IRRCalculator irrCalculator = new IRRCalculator(precision);
            Double averageTime = 0d;
            Double irr = null;
            long startTime = System.currentTimeMillis();
            for (int j = 0; j < numberOfTries; j++) {
                irr = irrCalculator.irr(cashFlows);
            }
            long endTime = System.currentTimeMillis();
            averageTime = (endTime - startTime) / numberOfTries;
            PerformanceInfo performanceInfo = new PerformanceInfo();
            performanceInfo.setPrecision(precision);
            performanceInfo.setIrr(irr);
            performanceInfo.setTimeTakenInMillis(averageTime);
            infos.add(performanceInfo);
        }
        return infos;
    }

    PerformanceInfo checkPOIIRR(double[] cashFlows) {
        Double averageTime = 0d;
        Double irr = null;
        long startTime = System.currentTimeMillis();
        for (int j = 0; j < numberOfTries; j++) {
            irr = Irr.irr(cashFlows);
        }
        long endTime = System.currentTimeMillis();
        averageTime = (endTime - startTime) / numberOfTries;
        PerformanceInfo performanceInfo = new PerformanceInfo();
        performanceInfo.setPrecision(null);
        performanceInfo.setIrr(irr * 100);
        performanceInfo.setTimeTakenInMillis(averageTime);
        return performanceInfo;
    }

    static class PerformanceInfo {
        private Integer precision;
        private Double irr;
        private Double timeTakenInMillis;

        public Integer getPrecision() {
            return precision;
        }

        public void setPrecision(Integer precision) {
            this.precision = precision;
        }

        public Double getIrr() {
            return irr;
        }

        public void setIrr(Double irr) {
            this.irr = irr;
        }

        public Double getTimeTakenInMillis() {
            return timeTakenInMillis;
        }

        public void setTimeTakenInMillis(Double timeTakenInMillis) {
            this.timeTakenInMillis = timeTakenInMillis;
        }

        @Override
        public String toString() {
            return String.format("%s,%s,%s", precision, irr, timeTakenInMillis);
        }
    }
}
