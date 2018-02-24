export default function () {
    return {
        autoResize: true,
        height: '100%',
        width: '100%',
        manipulation: false,
        nodes: {
            font: {
                face: 'mono',
                size: 20,
                color: 'black',
                align: 'left'
            },
            color: {
                background: '#bbb',
                border: 'black'
            },
            margin: {
                right: 10
            }
        },
        edges: {
            arrows: 'to',
            color: {
                color: 'green',
                inherit: false
            }
        },
        layout: {
            hierarchical: {
                direction: 'LR',
                enabled: true,
                levelSeparation: 400,
                sortMethod: "directed"
            }
        },
        physics: {
            hierarchicalRepulsion: {
                nodeDistance: 80
            }
        }
    };
}
