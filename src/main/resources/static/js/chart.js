function paintChart(element, title, type, dataSet) {
    var chart = echarts.init(document.getElementById(element));
    var option = {
        title: {
            text: title
        },
        tooltip: {
            show: true
        },
        xAxis: {
            data: dataSet.map(function (data) {
                return data.x;
            })
        },
        yAxis: {
            name: "人数"
        },
        series: [{
            type: type,
            data: dataSet.map(function (data) {
                return data.y;
            }),
            tooltip: {
                formatter: '{c}人'
            }
        }]
    };
    chart.setOption(option);
    var pre = window.onresize;
    if (typeof pre !== 'function') {
        window.onresize = function () {
            chart.resize();
        };
    } else {
        window.onresize = function () {
            chart.resize();
            pre();
        };
    }
}

