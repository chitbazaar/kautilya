# Kautilya
Calculators and Utilities for Personal finance

Version 1.0.0

## IRR - Durga's Pragmatic approach
IRR calculator provided in this library is first of its kind. It is based on Durga's pragmatic approach. 
* It works based on the boundaries of IRR. So initial guess value not required for calculating IRR.
* It supports custom precision. Lowering precision shall improve performance.
* It shall not fail with any valid cash flows. 

`Refer: irr method in IRRCalculator.`
### PIRR (Practical IRR)
Assume negative cash flow means 'out flow' and positive cash flow means 'inflow'.
If we consider cash flows #1 [-100,90], #2 [90,-100], in both scenarios net cash flow is negative. 
That means cash flows result in loss, but IRR value is positive for one and negative for other.
* IRR for [-100,90] is -10
* IRR for [90,-100] is 11.11
PIRR takes care of this based on net cash flow. It gives negative value for both cash flows. 

`Refer: pirr method in PIRRCalculator.`

This version also has CompoundingCalculator and FutureValueCalculator.

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