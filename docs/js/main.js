window.chartColors = {
    red: 'rgb(255, 99, 132)',
    orange: 'rgb(255, 159, 64)',
    yellow: 'rgb(255, 205, 86)',
    green: 'rgb(75, 192, 192)',
    blue: 'rgb(54, 162, 235)',
    purple: 'rgb(153, 102, 255)',
    grey: 'rgb(201, 203, 207)'
};

let getCheckPointData = function () {
    let cashFlows = getCashFlows();
    let data = new Array()

    data.push({'x': 0, 'y': getNFV(cashFlows, 0)});
    data.push({'x': -100, 'y': getNFV(cashFlows, -100)});

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
    if (positiveCashFlow > 0 && negativeCashFlow > 0) {
        let numberOfIntervals = cashFlows.length - 1
        let rate = cagr(positiveCashFlow, negativeCashFlow, 1)
        data.push({'x': rate, 'y': getNFV(cashFlows, rate)})
        rate = cagr(negativeCashFlow, positiveCashFlow, 1)
        data.push({'x': rate, 'y': getNFV(cashFlows, rate)})
        rate = cagr(positiveCashFlow, negativeCashFlow, numberOfIntervals)
        data.push({'x': rate, 'y': getNFV(cashFlows, rate)})
        rate = cagr(negativeCashFlow, positiveCashFlow, numberOfIntervals)
        data.push({'x': rate, 'y': getNFV(cashFlows, rate)})
    }

    return data
};

let getIRRNFVData = function () {
    let data = new Array()
    let cashFlows = getCashFlows();
    let minMax = getMinMax(cashFlows);
    if (minMax === null) {
        return []
    }
    let increment = (minMax.max.irr - minMax.min.irr) / 200
    for (let i = minMax.min.irr; i <= minMax.max.irr; i = i + increment) {
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
let cagr = function (principal, amount, term) {
    return ((amount - principal) * 100) / (principal * term);
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
    if (positiveCashFlow == negativeCashFlow) {
        error('Net cash flow is zero, so IRR shall be zero')
    } else if (positiveCashFlow == 0 || negativeCashFlow == 0) {
        error('Both positive and negative cash flows are required for IRR')
    } else {
        if (positiveCashFlow > negativeCashFlow) {
            negativeRate = cagr(positiveCashFlow, negativeCashFlow, 1)
            positiveRate = cagr(negativeCashFlow, positiveCashFlow, 1)
        } else {
            negativeRate = cagr(negativeCashFlow, positiveCashFlow, 1)
            positiveRate = cagr(positiveCashFlow, negativeCashFlow, 1)
        }
        let min = new IRRNFV(negativeRate, getNFV(cashFlows, negativeRate))
        let max = new IRRNFV(positiveRate, getNFV(cashFlows, positiveRate))
        let minMax = new MinMax(min, max)
        return minMax;
    }
    return null;
}

let error = function (msg) {
    document.getElementById('error').innerText = msg
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