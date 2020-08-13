window.chartColors = {
    red: 'rgb(255, 99, 132)',
    orange: 'rgb(255, 159, 64)',
    yellow: 'rgb(255, 205, 86)',
    green: 'rgb(75, 192, 192)',
    blue: 'rgb(54, 162, 235)',
    purple: 'rgb(153, 102, 255)',
    grey: 'rgb(201, 203, 207)'
};

let updateDataConfig = function () {
    let cashFlowInfo = new CashFlowInfo(getCashFlows());
    config.data.datasets[0].data = cashFlowInfo.irrNFVData;
    config.data.datasets[1].data = cashFlowInfo.checkPointData;
    if (document.getElementById("includeOtherChecksInput").checked) {
        config.data.datasets[2].data = cashFlowInfo.otherCheckPointData;
    } else {
        config.data.datasets[2].data = [];
    }

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
let compoundRate = function (principal, amount, term) {
    return ((amount - principal) * 100) / (principal * term);
}

let error = function (msg) {
    document.getElementById('error').innerText = msg
}

class CashFlowInfo {
    constructor(cashFlows) {
        this._cashFlows = cashFlows;
        this._totalPositiveCashFlow = 0;
        this._totalNegativeCashFlow = 0;
        for (let i = 0; i < cashFlows.length; i++) {
            let cashFlow = cashFlows[i];
            if (cashFlow > 0) {
                this._totalPositiveCashFlow += cashFlow;
            } else {
                this._totalNegativeCashFlow += -1 * cashFlow;
            }
        }
        this.setInitialMinMax()
        if (this._initialMinMax == null || this._initialMinMax.max.nfv <= 0) {
            this._isPositiveSlope = false;
        } else {
            this._isPositiveSlope = true;
        }
    }

    setInitialMinMax() {
        let negativeRate = 0;
        let positiveRate = 0;
        this._initialMinMax = null;
        if (this._totalPositiveCashFlow == this._totalNegativeCashFlow) {
            error('Net cash flow is zero, so IRR shall be zero')
        } else if (this._totalPositiveCashFlow == 0 || this._totalNegativeCashFlow == 0) {
            error('Both positive and negative cash flows are required for IRR')
        } else {
            if (this._totalPositiveCashFlow > this._totalNegativeCashFlow) {
                negativeRate = compoundRate(this._totalPositiveCashFlow, this._totalNegativeCashFlow, 1)
                positiveRate = compoundRate(this._totalNegativeCashFlow, this._totalPositiveCashFlow, 1)
            } else {
                negativeRate = compoundRate(this._totalNegativeCashFlow, this._totalPositiveCashFlow, 1)
                positiveRate = compoundRate(this._totalPositiveCashFlow, this._totalNegativeCashFlow, 1)
            }
            let min = new IRRNFV(negativeRate, getNFV(this._cashFlows, negativeRate))
            let max = new IRRNFV(positiveRate, getNFV(this._cashFlows, positiveRate))
            let minMax = new MinMax(min, max)
            this._initialMinMax = minMax;
        }
    }

    get irrNFVData() {
        let data = new Array()
        let cashFlows = this._cashFlows;
        let minMax = this._initialMinMax;
        if (minMax === null) {
            return []
        }
        let increment = (minMax.max.irr - minMax.min.irr) / 200
        let from = minMax.min.irr - increment
        let to = minMax.max.irr + increment
        for (let i = from; i <= to; i = i + increment) {
            data.push({'x': i, 'y': getNFV(cashFlows, i)})
        }
        return data;
    }

    get checkPointData() {
        let cashFlows = this._cashFlows;
        let data = new Array();
        let positiveCashFlow = this._totalPositiveCashFlow;
        let negativeCashFlow = this._totalNegativeCashFlow;
        if (positiveCashFlow > 0 && negativeCashFlow > 0) {
            let numberOfIntervals = cashFlows.length - 1
            let rate = compoundRate(positiveCashFlow, negativeCashFlow, 1)
            data.push({'x': rate, 'y': getNFV(cashFlows, rate)})
            rate = compoundRate(negativeCashFlow, positiveCashFlow, 1)
            data.push({'x': rate, 'y': getNFV(cashFlows, rate)})
        }
        return data
    }

    get otherCheckPointData() {
        let data = new Array();
        let cashFlows = this._cashFlows;
        let positiveCashFlow = this._totalPositiveCashFlow;
        let negativeCashFlow = this._totalNegativeCashFlow;
        let numberOfIntervals = this.numberOfIntervals;
        let rate = null;

        //zero
        data.push({'x': 0, 'y': getNFV(cashFlows, 0)});

        //total cash flows only in first and last interval
        rate = compoundRate(positiveCashFlow, negativeCashFlow, numberOfIntervals)
        data.push({'x': rate, 'y': getNFV(cashFlows, rate)})
        rate = compoundRate(negativeCashFlow, positiveCashFlow, numberOfIntervals)
        data.push({'x': rate, 'y': getNFV(cashFlows, rate)})
        return data;
    }

    get numberOfIntervals() {
        return this._cashFlows.length - 1
    }

    get cashFlows() {
        return this._cashFlows;
    }

    get totalPositiveCashFlow() {
        return this._totalPositiveCashFlow;
    }

    get totalNegativeCashFlow() {
        return this._totalNegativeCashFlow;
    }

    get initialMinMax() {
        return this._initialMinMax;
    }

    get isPositiveSlope() {
        return this._isPositiveSlope;
    }
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