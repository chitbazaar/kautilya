window.chartColors = {
    red: 'rgb(255, 99, 132)',
    orange: 'rgb(255, 159, 64)',
    yellow: 'rgb(255, 205, 86)',
    green: 'rgb(75, 192, 192)',
    blue: 'rgb(54, 162, 235)',
    purple: 'rgb(153, 102, 255)',
    grey: 'rgb(201, 203, 207)'
};
let getIRRNFVData = function () {
    let data = new Array()
    let cashFlows = getCashFlows();
    let minMax = getMinMax(cashFlows);
    for (let i = minMax.min.irr; i <= minMax.max.irr; i++) {
        data.push({'x': i, 'y': getNFV(cashFlows, i)})
    }
    return data;
};

let getNFV = function (cashFlows, r) {
    let nfv = 0;
    for (let i = 0; i < cashFlows.length; i++) {
        nfv += cashFlows[i] * Math.pow(1 + r / 100, cashFlows.length - i);
    }
    return nfv;
}

let getCashFlows = function () {
    let cashFlowText = document.getElementById('cashFlowsInput').value;
    let cashFlows = new Array();
    cashFlowText.split(',').forEach(function (item) {
        cashFlows.push(parseFloat(item));
    });
    return cashFlows;
}
let cagr = function(principal, amount, term){
    return ((amount-principal)*100)/(principal*term);
}
let getMinMax = function (cashFlows) {
    let positiveCashFlow = 0;
    let negativeCashFlow = 0;
    for (let i = 0; i < cashFlows.length; i++) {
        let cashFlow = cashFlows[i];
        if (cashFlow > 0) {
            positiveCashFlow += cashFlow;
        } else {
            negativeCashFlow += -1 * cashFlow;
        }
    }
    let negativeRate
    let positiveRate
    if(positiveCashFlow > negativeCashFlow){
        negativeRate = cagr(positiveCashFlow, negativeCashFlow, 1)
        positiveRate = cagr(negativeCashFlow, positiveCashFlow, 1)
    }else{
        negativeRate = cagr(negativeCashFlow, positiveCashFlow, 1)
        positiveRate = cagr(positiveCashFlow, negativeCashFlow, 1)
    }
    let min = new IRRNFV(negativeRate, getNFV(cashFlows, negativeRate))
    let max = new IRRNFV(positiveRate, getNFV(cashFlows, positiveRate))
    let minMax = new MinMax(min, max)
    return minMax;
}

class MinMax {
    constructor(min, max) {
        this._min = min;
        this._max = max;
    }

    get min() {
        return this._min;
    }

    set min(value) {
        this._min = value;
    }

    get max() {
        return this._max;
    }

    set max(value) {
        this._max = value;
    }
}

class IRRNFV {
    constructor(irr, nfv) {
        this._irr = irr;
        this._nfv = nfv;
    }

    get irr() {
        return this._irr;
    }

    set irr(value) {
        this._irr = value;
    }

    get nfv() {
        return this._nfv;
    }

    set nfv(value) {
        this._nfv = value;
    }
}