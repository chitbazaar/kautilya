package com.chitbazaar.kautilya.util;

public class NumberUtils {
    public static Double round(Double value, Integer precision) {
        if (value == null || precision == null) {
            return value;
        }
        return (Math.floor(value * Math.pow(10, precision) + 0.5) / Math.pow(10, precision));
    }

    public static Double ceil(Double value, Integer precision) {
        if (value == null || precision == null) {
            return value;
        }
        return (Math.ceil(value * Math.pow(10, precision)) / Math.pow(10, precision));
    }

    public static Double floor(Double value, Integer precision) {
        if (value == null || precision == null) {
            return value;
        }
        return (Math.floor(value * Math.pow(10, precision)) / Math.pow(10, precision));
    }
}
