const path = require('path');
const webpack = require('webpack');

module.exports = {
    cache: true,
    entry: ['core-js', './src/cloud-sport.js'],
    output: {
        filename: 'bundle.js',
        path: path.resolve(__dirname, 'dist')
    },
    target: 'web',
    resolve: {
        extensions: ['.js'],
        modules: ['src', 'node_modules']
    },
    module: {
        rules: [
            {   test: /\.html$/,
                loader: 'file-loader'
            },
            {
                test: /\.js$/,
                exclude: /(node_modules)/,
                loader: 'babel-loader',
                query: {
                    presets: ["es2015", "es2016", 'es2017'],
                    plugins: [//'transform-runtime'
                        //"transform-es2015-for-of",
                        //"transform-es2015-block-scoping", "transform-strict-mode",
                        //"transform-object-rest-spread"
                    ]
                }
            }
        ]
    },
    plugins:[

        // new webpack.DefinePlugin({
        //     'process.env': {
        //         'NODE_ENV': JSON.stringify('production'),
        //     }
        // }),
        // new webpack.optimize.UglifyJsPlugin({
        //     output: {comments: false},
        //     sourceMap: true
        // }),
        // new CleanWebpackPlugin(['dist']),
        new webpack.ProvidePlugin({
            $: 'jquery',
            jQuery: 'jquery',
            FlatpickrInstance: 'flatpickr',
            "window.jQuery": "jquery"
        })
    ]
};
