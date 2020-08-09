package script;

import com.chitbazaar.kautilya.core.IRRCalculator;
import org.apache.poi.ss.formula.functions.Irr;

import java.util.ArrayList;
import java.util.List;

public class IRRPerformanceCheck {
    Double numberOfTries = 1000d;

    public static void main(String[] args) {
        IRRPerformanceCheck irrPerformanceCheck = new IRRPerformanceCheck();
        double[][] inputs = {
                {-100000.0d, -100000.0d, -100000.0d, -100000.0d, -100000.0d, -100000.0d, -100000.0d, 478263.3456652112d},
                {-100.0d, -100.0d, -100.0d, -100.0d, -100.0d, -100.0d, -100.0d, -100.0d, -100.0d, -100.0d, -100.0d, -100.0d, -100.0d, -100.0d, -100.0d, -100.0d, -100.0d, -100.0d, -100.0d, 1195.882185377919d},
                {-100000.0d, -100000.0d, -100000.0d, -100000.0d, -100000.0d, -100000.0d, -100000.0d, -100000.0d, -100000.0d, -100000.0d, 665802.7616772739d},
                {100000.0d, 100000.0d, 100000.0d, 100000.0d, 100000.0d, -100000.0d, 100000.0d, 100000.0d, 100000.0d, 100000.0d, -91965802.7616772739d},
                {-6000000d, -3000000d, -3000000d, -3000000d, 5000000d, 5000000d, 5000000d, 5000000d, 5000000d, 5000000d, 6600000d, 6600000d, 6600000d, 6600000d, 10600000d, 10600000d, 10600000d, 10600000d, 10600000d, 10600000d, 19000000d, 19000000d, 19000000d, 19000000d, 19000000d, 31000000d, 31000000d, 31000000d, 31000000d, 31000000d}
        };
        List<PerformanceInfo> poiPerformanceList = new ArrayList<>();
        StringBuilder poiReport = new StringBuilder("===========POI Report======\n");
        for (double[] cashFlows : inputs) {
            poiReport.append("\nCash flows: ").append(irrPerformanceCheck.toList(cashFlows).toString()).append("\n");
            PerformanceInfo performanceInfo = irrPerformanceCheck.checkPOIIRR(cashFlows);
            irrPerformanceCheck.updateReport(poiReport, performanceInfo);
            poiPerformanceList.add(performanceInfo);
        }
        System.out.println(poiReport);
        StringBuilder myIRRReport = new StringBuilder("===========Durga's IRR Report======\n");
        List<PerformanceInfo> myIRRPerformanceList = new ArrayList<>();
        for (double[] cashFlowsArray : inputs) {
            List<Double> cashFlows = irrPerformanceCheck.toList(cashFlowsArray);
            myIRRReport.append("\nCash flows: ").append(cashFlows.toString()).append("\n");
            List<PerformanceInfo> performanceInfos = irrPerformanceCheck.checkMyIRR(cashFlows);
            irrPerformanceCheck.updateReport(myIRRReport, performanceInfos);
            myIRRPerformanceList.addAll(performanceInfos);
        }
        System.out.println(myIRRReport);
    }

    private void updateReport(StringBuilder report, PerformanceInfo performanceInfo){
        report.append(performanceInfo.toString()).append("\n");
    }
    private void updateReport(StringBuilder report, List<PerformanceInfo> performanceInfos) {
        for (PerformanceInfo performanceInfo : performanceInfos) {
            updateReport(report, performanceInfo);
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
//            System.out.printf("Checking for precision %s\n", precision);
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
            if (precision == null) {
                return String.format("precision:N/A\t\t\t\t\tirr:%s\t\t\t\t\t\ttimeTakenInMillis:%s", irr, timeTakenInMillis);
            }
            return String.format("precision:%s\t\t\t\t\tirr:%s\t\t\t\t\t\ttimeTakenInMillis:%s", precision, irr, timeTakenInMillis);
        }
    }
}
