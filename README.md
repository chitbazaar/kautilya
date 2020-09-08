# Kautilya
Calculators and Utilities for Personal finance

Version 1.0.0

## IRR
IRR calculator provided in this library is first of its kind. It is based on Durga's pragmatic approach. 
* It works based on the boundaries of IRR. So initial guess value not required for calculating IRR.
* It supports custom precision. Lowering precision shall improve performance.
* It shall not fail most of practical cash flows. 
```
Java:
IRRCalculator irrCalculator = new IRRCalculator();
Number[] cashFlowsArray = {-10000,-10000,-1000,-10000,40000};
List<Number> cashFlows = Arrays.asList(cashFlowsArray);
System.out.println(irrCalculator.irr(cashFlows));

#Custom precision
IRRCalculator irrCalculator = new IRRCalculator(6);
Number[] cashFlowsArray = {-10000,-10000,-1000,-10000,40000};
List<Number> cashFlows = Arrays.asList(cashFlowsArray);
System.out.println(irrCalculator.irr(cashFlows));```
```

```
Groovy:
IRRCalculator irrCalculator = new IRRCalculator()
List<Double> cashFlows = [-10000,-10000,-1000,-10000,40000]
println irrCalculator.irr(cashFlows)

#Custom precision
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
compile group: 'com.chitbazaar', name: 'kautilya', version: '1.0.1'
```
#### Maven dependency
```
<dependency>
    <groupId>com.chitbazaar</groupId>
    <artifactId>kautilya</artifactId>
    <version>1.0.1</version>
</dependency>
```