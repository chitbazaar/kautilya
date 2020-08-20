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
    config.data.datasets[1].data = cashFlowInfo.initialMinMaxData;
    config.data.datasets[2].data = cashFlowInfo.otherCheckPointData;
    config.data.datasets[3].data = cashFlowInfo.currentMinMaxData;
    config.data.datasets[4].data = cashFlowInfo.irrData;
};

let getNFV = function (cashFlows, r) {
    let nfv = 0;
    for (let i = 0; i < cashFlows.length; i++) {
        nfv += cashFlows[i] * Math.pow(1 + r / 100, cashFlows.length - i - 1);
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

let info = function (msg) {
    if (msg == null) {
        document.getElementById('info').innerText = '';
        return;
    }
    document.getElementById('info').innerText = msg + '\n' + document.getElementById('info').innerText;
}

let xAxisCut = function (x1y1, x2y2, cashFlows) {
    if ((x2y2.irr - x1y1.irr) == 0) {
        return null;
    }
    let slope = (x2y2.nfv - x1y1.nfv) / (x2y2.irr - x1y1.irr);
    let ratePerInterval = x2y2.irr - (x2y2.nfv / slope);
    let netFutureValue = getNFV(cashFlows, ratePerInterval);
    return new IRRNFV(ratePerInterval, netFutureValue);
}

class CashFlowInfo {
    constructor(cashFlows) {
        this._cashFlows = cashFlows;
        this._totalPositiveCashFlow = 0;
        this._totalNegativeCashFlow = 0;
        this._irrsWithPositiveNFV = new Array();
        this._irrsWithNegativeNFV = new Array();
        this._precision = parseFloat(document.getElementById('precisionInput').value);
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

    get precision() {
        return this._precision;
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
            let newPoint = null
            if (min.nfv > 0 && max.nfv > 0) {
                newPoint = this.tryToGetPointCheck(false);
            } else if (min.nfv < 0 && max.nfv < 0) {
                newPoint = this.tryToGetPointCheck(true);
            }
            if (newPoint != null) {
                if (newPoint.irr <= min.irr) {
                    max = min
                    min = newPoint
                }
                if (newPoint.irr >= max.irr) {
                    min = max
                    max = newPoint
                }
            }
            if ((min.nfv > 0 && max.nfv > 0) || (min.nfv < 0 && max.nfv < 0)) {
                error('Could not find initial boundaries..');
                return;
            }
            let minMax
            if (min.nfv == 0) {
                minMax = new MinMax(min, min)
            } else if (max.nfv == 0) {
                minMax = new MinMax(max, max)
            } else {
                minMax = new MinMax(min, max)
            }
            this._initialMinMax = minMax;
            this._currentMinMax = minMax;
            info(`Initial Min Max:: min:${this._initialMinMax.min.irr} max:${this._initialMinMax.max.irr}`);
        }
    }

    tryToGetPointCheck(needPositive) {
        let point = null;
        let increment = 10;
        for (let i = 0; i <= 1000; i += increment) {
            let foundResult = false;
            for (let rate = i; rate < i + increment; rate += 1) {
                point = new IRRNFV(rate, getNFV(this._cashFlows, rate))
                if (needPositive && point.nfv >= 0 || !needPositive && point.nfv <= 0) {
                    foundResult = true;
                    break;
                }
            }
            if (foundResult) {
                break;
            }
            for (let rate = -1 * i; rate > -1 * i - increment; rate -= 1) {
                point = new IRRNFV(rate, getNFV(this._cashFlows, rate))
                if (needPositive && point.nfv >= 0 || !needPositive && point.nfv <= 0) {
                    foundResult = true;
                    break;
                }
            }
            if (foundResult) {
                break;
            }
        }
        return point;
    }

    get irrNFVData() {
        let data = new Array()
        let cashFlows = this._cashFlows;
        let minMax = this._initialMinMax;
        if (minMax === null) {
            return []
        }
        let from
        let to
        if (minMax.min.irr == minMax.max.irr) {
            from = minMax.min.irr - 200
            to = minMax.max.irr + 200
        } else {
            from = minMax.min.irr - 1
            to = minMax.max.irr + 1
        }
        let increment = (to - from) / 200
        for (let i = from; i <= to; i = i + increment) {
            data.push({'x': i, 'y': getNFV(cashFlows, i)})
        }
        return data;
    }

    get initialMinMaxData() {
        let data = new Array();
        data.push({'x': this._initialMinMax.min.irr, 'y': this._initialMinMax.min.nfv});
        data.push({'x': this._initialMinMax.max.irr, 'y': this._initialMinMax.max.nfv});
        this.updateIrrsWithNFVs(data);
        return data
    }

    get currentMinMaxData() {
        let data = new Array();
        if (this._currentMinMax == undefined) {
            return data;
        }
        data.push({'x': this._currentMinMax.min.irr, 'y': this._currentMinMax.min.nfv});
        data.push({'x': this._currentMinMax.max.irr, 'y': this._currentMinMax.max.nfv});
        return data
    }

    updateIrrsWithNFVs(data) {
        data.forEach(xy => {
                if (this._initialMinMax != null && (xy.x >= this._currentMinMax.min.irr && xy.x <= this._currentMinMax.max.irr)) {
                    if (xy.y >= 0) {
                        this._irrsWithPositiveNFV.push(new IRRNFV(xy.x, xy.y))
                    } else {
                        this._irrsWithNegativeNFV.push(new IRRNFV(xy.x, xy.y))
                    }
                }
            }
        );
    }

    setCurrentMinMax() {
        let minMaxForPositiveNFVs = this.getMinMax(this._irrsWithPositiveNFV);
        let minMaxForNegativeNFVs = this.getMinMax(this._irrsWithNegativeNFV);
        if (this._isPositiveSlope) {
            this._currentMinMax = new MinMax(minMaxForNegativeNFVs.max, minMaxForPositiveNFVs.min)
        } else {
            this._currentMinMax = new MinMax(minMaxForPositiveNFVs.max, minMaxForNegativeNFVs.min)
        }
    }

    getMinMax(irrNFVs) {
        if (irrNFVs == null) {
            return null;
        }
        let min = null;
        let max = null;
        irrNFVs.forEach(irrNFV => {
                if (min == null || max == null) {
                    min = irrNFV;
                    max = irrNFV;
                } else if (irrNFV.irr < min.irr && irrNFV.irr > this._initialMinMax.min.irr) {
                    min = irrNFV;
                } else if (irrNFV.irr >= max.irr && irrNFV.irr < this._initialMinMax.max.irr) {
                    max = irrNFV;
                }
            }
        );
        return new MinMax(min, max);
    }

    get irrData() {
        let data = new Array();
        if (this._irr == undefined) {
            let startTime = performance.now()
            let increment = Math.pow(0.1, this._precision);
            let count = 1;
            if (this._currentMinMax.min.nfv == 0) {
                this._irr = this._currentMinMax.min;
            } else if (this._currentMinMax.max.nfv == 0) {
                this._irr = this._currentMinMax.min;
            } else {
                let maxIterations = Math.log2((this._currentMinMax.max.irr - this._currentMinMax.min.irr) / increment) + 1;
                info(`Max iterations: ${maxIterations}`)
                while ((this._currentMinMax.max.irr - this._currentMinMax.min.irr) > increment) {
                    this.setNewMinMax();
                    if (this._currentMinMax.min.nfv == 0 || this._currentMinMax.max.nfv == 0) {
                        break;
                    }
                    if (count > maxIterations) {
                        return NaN
                    }
                    count++;
                }
                if (Math.abs(this._currentMinMax.min.nfv) < Math.abs(this._currentMinMax.max.nfv)) {
                    this._irr = this._currentMinMax.min;
                } else {
                    this._irr = this._currentMinMax.max;
                }
            }
            this._irr.irr = this._irr.irr.toFixed(this._precision);
            let endTime = performance.now()
            info(`Time taken ${endTime - startTime} milliseconds. Number of iterations: ${count}`)
            info(`***** irr: ${this._irr.irr} ***`);
        }
        data.push({'x': this._irr.irr, 'y': this._irr.nfv})
        return data;
    }

    setNewMinMax() {
        let data = new Array();
        if (this._currentMinMax.min.nfv == 0 || this._currentMinMax.max.nfv == 0) {
            info('No additional check points required. NFV is zero for either min or max');
            return data
        }

        this._irrsWithNegativeNFV = new Array();
        this._irrsWithPositiveNFV = new Array();
        this.updateIrrsWithNFVs(this.currentMinMaxData);

        let cashFlows = this._cashFlows;
        let positiveCashFlow = this._totalPositiveCashFlow;
        let negativeCashFlow = this._totalNegativeCashFlow;
        let numberOfIntervals = this.numberOfIntervals;
        let rate = null;
        let point = {};

        //Middle
        let middle = (this._currentMinMax.min.irr + this._currentMinMax.max.irr) / 2;
        let nfvForMiddle = getNFV(cashFlows, middle)
        point = {'x': middle, 'y': nfvForMiddle}
        data.push(point)

        //X axis cut
        let xCut = xAxisCut(this._currentMinMax.min, this._currentMinMax.max, cashFlows);
        if (xCut != null) {
            let nfvForXCut = getNFV(cashFlows, xCut.irr);
            point = {'x': xCut.irr, 'y': nfvForXCut}
            data.push(point);
        }
        //Min tangential cut
        let nMinusOne = new IRRNFV(this._currentMinMax.min.irr - this._precision, getNFV(cashFlows, this._currentMinMax.min.irr - this._precision))
        let nPlusOne = new IRRNFV(this._currentMinMax.min.irr + this._precision, getNFV(cashFlows, this._currentMinMax.min.irr + this._precision))
        xCut = xAxisCut(nMinusOne, nPlusOne, cashFlows);
        if (xCut != null) {
            let nfvForXCut = getNFV(cashFlows, xCut.irr);
            point = {'x': xCut.irr, 'y': nfvForXCut}
            data.push(point);
        }

        //Max tangential cut
        nMinusOne = new IRRNFV(this._currentMinMax.max.irr - this._precision, getNFV(cashFlows, this._currentMinMax.max.irr - this._precision))
        nPlusOne = new IRRNFV(this._currentMinMax.max.irr + this._precision, getNFV(cashFlows, this._currentMinMax.max.irr + this._precision))
        xCut = xAxisCut(nMinusOne, nPlusOne, cashFlows);
        if (xCut != null) {
            let nfvForXCut = getNFV(cashFlows, xCut.irr);
            point = {'x': xCut.irr, 'y': nfvForXCut}
            data.push(point);
        }

        this.updateIrrsWithNFVs(data);
        this.setCurrentMinMax();
        return data;
    }

    get otherCheckPointData() {
        let data = new Array();
        if (this._initialMinMax.min.nfv == 0 || this._initialMinMax.max.nfv == 0) {
            info('No additional check points required. NFV is zero for either min or max');
            return data;
        }
        let cashFlows = this._cashFlows;
        let positiveCashFlow = this._totalPositiveCashFlow;
        let negativeCashFlow = this._totalNegativeCashFlow;
        let numberOfIntervals = this.numberOfIntervals;
        let rate = null;
        let point = {};
        //zero
        point = {'x': 0, 'y': getNFV(cashFlows, 0)};
        info(`For zero return x:${point.x} y:${point.y}`);
        data.push(point);

        //total cash flows only in first and last interval
        rate = compoundRate(positiveCashFlow, negativeCashFlow, numberOfIntervals)
        point = {'x': rate, 'y': getNFV(cashFlows, rate)};
        data.push(point);
        info(`PCF first NCF last x:${point.x} y:${point.y}`);

        rate = compoundRate(negativeCashFlow, positiveCashFlow, numberOfIntervals)
        point = {'x': rate, 'y': getNFV(cashFlows, rate)};
        data.push(point);
        info(`NCF first PCF last x:${point.x} y:${point.y}`);
        this.updateIrrsWithNFVs(data);
        this.setCurrentMinMax();
        info(`current min:${this._currentMinMax.min.irr}  current max: ${this._currentMinMax.max.irr}`);

        //Middle
        let middle = (this._currentMinMax.min.irr + this._currentMinMax.max.irr) / 2;
        let nfvForMiddle = getNFV(cashFlows, middle)
        point = {'x': middle, 'y': nfvForMiddle}
        data.push(point)
        info(`For middle x:${point.x} y:${point.y}`);

        //X axis cut
        let xCut = xAxisCut(this._currentMinMax.min, this._currentMinMax.max, cashFlows);
        if (xCut != null) {
            let nfvForXCut = getNFV(cashFlows, xCut.irr);
            point = {'x': xCut.irr, 'y': nfvForXCut}
            data.push(point);
            info(`X axis cut for current min&max x:${point.x} y:${point.y}`);
        }
        //Min tangential cut
        let nMinusOne = new IRRNFV(this._currentMinMax.min.irr - this._precision, getNFV(cashFlows, this._currentMinMax.min.irr - this._precision))
        let nPlusOne = new IRRNFV(this._currentMinMax.min.irr + this._precision, getNFV(cashFlows, this._currentMinMax.min.irr + this._precision))
        xCut = xAxisCut(nMinusOne, nPlusOne, cashFlows);
        if (xCut != null) {
            let nfvForXCut = getNFV(cashFlows, xCut.irr);
            point = {'x': xCut.irr, 'y': nfvForXCut}
            data.push(point);
            info(`Tangential cut for current min x:${point.x} y:${point.y}`);
        }

        //Max tangential cut
        nMinusOne = new IRRNFV(this._currentMinMax.max.irr - this._precision, getNFV(cashFlows, this._currentMinMax.max.irr - this._precision))
        nPlusOne = new IRRNFV(this._currentMinMax.max.irr + this._precision, getNFV(cashFlows, this._currentMinMax.max.irr + this._precision))
        xCut = xAxisCut(nMinusOne, nPlusOne, cashFlows);
        if (xCut != null) {
            let nfvForXCut = getNFV(cashFlows, xCut.irr);
            point = {'x': xCut.irr, 'y': nfvForXCut}
            data.push(point);
            info(`Tangential cut for current max x:${point.x} y:${point.y}`);
        }

        this.updateIrrsWithNFVs(data);
        this.setCurrentMinMax();
        info(`Final Min Max:: min:${this._currentMinMax.min.irr} max:${this._currentMinMax.max.irr}`);
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

    get currentMinMax() {
        return this._currentMinMax;
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