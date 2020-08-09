package script;

import com.chitbazaar.kautilya.core.IRRCalculator;
import org.apache.poi.ss.formula.functions.Irr;

import java.util.ArrayList;
import java.util.List;

public class IRRPerformanceCheck {
    Double numberOfTries = 1000d;

    public static void main(String[] args) {
        IRRPerformanceCheck irrPerformanceCheck = new IRRPerformanceCheck();
        List<double[]> listOfInputs = new ArrayList<>();
        double[] input = {
                -6000000d, -3000000d, -3000000d, -3000000d,
                5000000d, 5000000d, 5000000d, 5000000d, 5000000d, 5000000d,
                6600000d, 6600000d, 6600000d, 6600000d,
                10600000d, 10600000d, 10600000d, 10600000d, 10600000d, 10600000d,
                19000000d, 19000000d, 19000000d, 19000000d, 19000000d,
                31000000d, 31000000d, 31000000d, 31000000d, 31000000d
        };
        listOfInputs.add(input);
        double[] input2 = {
                -100000.0d, -100000.0d, -100000.0d, -100000.0d, -100000.0d, -100000.0d, -100000.0d, -100000.0d, -100000.0d, -100000.0d, 665802.7616772739d
        };
        listOfInputs.add(input2);
        double[] input3 = {
                100000.0d, 100000.0d, 100000.0d, 100000.0d, 100000.0d, -100000.0d, 100000.0d, 100000.0d, 100000.0d, 100000.0d, -91965802.7616772739d
        };
        listOfInputs.add(input3);
        List<PerformanceInfo> poiPerformanceList = new ArrayList<>();
        for (double[] cashFlows : listOfInputs) {
            poiPerformanceList.add(irrPerformanceCheck.checkPOIIRR(cashFlows));
        }
        irrPerformanceCheck.print(poiPerformanceList);
        List<PerformanceInfo> myIRRPerformanceList = new ArrayList<>();
        for (double[] cashFlowsArray : listOfInputs) {
            List<Double> cashFlows = irrPerformanceCheck.toList(cashFlowsArray);
            myIRRPerformanceList.addAll(irrPerformanceCheck.checkMyIRR(cashFlows));
        }
        irrPerformanceCheck.print(myIRRPerformanceList);

    }

    private void print(List<PerformanceInfo> performanceInfos) {
        for (PerformanceInfo performanceInfo : performanceInfos) {
            System.out.println(performanceInfo.toString());
        }
    }

    private List<Double> toList(double[] input) {
        List<Double> list = new ArrayList<>();
        for (double cashFlow : input) {
            list.add(cashFlow);
        }
        return list;
    }

    List<PerformanceInfo> checkMyIRR(List<Double> cashFlows) {

        List<PerformanceInfo> infos = new ArrayList<>();
        for (int precision = 0; precision <= 14; precision += 2) {
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
