package com.chitbazaar.kautilya.domain;

import java.util.Objects;

public class MinMaxIRRAndNFV {
    public final IRRAndNFV min;
    public final IRRAndNFV max;

    public MinMaxIRRAndNFV() {
        this(null, null);
    }

    public MinMaxIRRAndNFV(IRRAndNFV min, IRRAndNFV max) {
        this.min = min;
        this.max = max;
    }

    public Double getIRRAbsDifference() {
        if (Objects.isNull(min) || Objects.isNull(max)) {
            return Double.NaN;
        }
        return Math.abs(max.ratePerInterval - min.ratePerInterval);
    }

    public Double getIRRForLeastAbsNFV(){
        if (Objects.isNull(min) || Objects.isNull(max)) {
            return Double.NaN;
        }
        if(Math.abs(min.nfv) < Math.abs(max.nfv)){
            return min.ratePerInterval;
        }else{
            return max.ratePerInterval;
        }
    }
}
