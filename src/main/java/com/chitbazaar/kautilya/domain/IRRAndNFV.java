package com.chitbazaar.kautilya.domain;

public class IRRAndNFV implements Comparable<IRRAndNFV> {
    public final Double ratePerInterval;
    public final Double nfv;

    public IRRAndNFV(Double ratePerInterval, Double nfv) {
        this.ratePerInterval = ratePerInterval;
        this.nfv = nfv;
    }

    @Override
    public String toString() {
        return String.format("irr:%s nfv:%s", ratePerInterval, nfv);
    }

    @Override
    public int compareTo(IRRAndNFV o) {
        if (ratePerInterval.equals(o.ratePerInterval)) {
            return Double.compare(Math.abs(nfv), Math.abs(o.nfv));
        }
        return Double.compare(ratePerInterval, o.ratePerInterval);
    }
}