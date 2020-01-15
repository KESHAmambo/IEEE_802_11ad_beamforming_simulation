Highcharts.chart('container', {
    yAxis: {
        title: {
            text: 'Время бимформинга, мс'
        }
    },
    xAxis: {
        title: {
            text: 'Количество слотов'
        }
    },

    series: [{
        name: '25 stations',
        data: [
            [3, 227602],
            [4, 17586],
            [6, 2111],
            [8, 822],
            [10, 453],
        ]
    }, {
        name: '20 stations',
        data: [
            [3, 35607],
            [4, 5354],
            [6, 989],
            [8, 487],
            [10, 311],
        ]
    }, {
        name: '18 stations',
        data: [
            [3, 18146],
            [4, 3157],
            [6, 754],
            [8, 385],
            [10, 265],
        ]
    }],
});