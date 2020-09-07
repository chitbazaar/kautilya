# Kautilya
Calculators and Utilities for Personal finance

Version 1.0.0

## IRR
IRR calculator provided in this library is first of its kind. It is based on Durga's pragmatic approach. 
* It works based on the boundaries of IRR. So initial guess value not required for calculating IRR.
* It supports custom precision. Lowering precision shall improve performance.
* It shall not fail with any valid cash flows. 
```
Groovy:
IRRCalculator irrCalculator = new IRRCalculator();
List<Double> cashFlows = [-10000,-10000,-1000,-10000,40000]
println irrCalculator.irr(cashFlows)

IRRCalculator irrCalculator = new IRRCalculator(6); //custom precision
List<Double> cashFlows = [-10000,-10000,-1000,-10000,40000]
println irrCalculator.irr(cashFlows)
```

## Other
Also refer CompoundingCalculatorm, FutureValueCalculator, PresentValueCalculator 

###Artifacts
https://mvnrepository.com/artifact/com.chitbazaar/kautilya
#### Gradle dependency
```
compile group: 'com.chitbazaar', name: 'kautilya', version: '1.0.0'
```
#### Maven dependency
```
<dependency>
    <groupId>com.chitbazaar</groupId>
    <artifactId>kautilya</artifactId>
    <version>1.0.0</version>
</dependency>
```