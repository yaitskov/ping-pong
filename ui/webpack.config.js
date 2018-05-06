const HardSourceWebpackPlugin = require('hard-source-webpack-plugin');
const path = require('path');
const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const ExtractTextPlugin = require("extract-text-webpack-plugin");
const OfflinePlugin = require('offline-plugin');
const WebpackSourceMapSupport = require("webpack-source-map-support");
const FaviconsWebpackPlugin = require('favicons-webpack-plugin');

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
            {
                test: /\.(scss|css)$/,
                loader: ExtractTextPlugin.extract({
                    fallback: "style-loader",
                    use: [
                        {
                            loader: 'css-loader',
                        },
                        {
                            loader: 'sass-loader',
                            // options: {
                            //     includePaths: ["absolute/path/a", "absolute/path/b"]
                            // }
                        }
                    ]
                }),
            },
            {
                test: /\.(png|woff|woff2|eot|ttf|svg)$/,
                loader: 'url-loader?limit=1000'
            },
            {   test: /\.html$/,
                loader: 'file-loader'
            },
            {
                test: /\.js$/,
                exclude: /(node_modules)/,
                loader: 'babel-loader',
                query: {
                    presets: ["es2015", "es2016", 'es2017'],
                    plugins: [ 'source-map-support' //'transform-runtime'
                        //"transform-es2015-for-of",
                        //"transform-es2015-block-scoping", "transform-strict-mode",
                               ,"transform-object-rest-spread"
                    ]
                }
            }
        ]
    },
    devtool: 'source-map',
    plugins:[
        new HardSourceWebpackPlugin(),
        //new WebpackSourceMapSupport(),
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
        new FaviconsWebpackPlugin({
            logo: 'logo.png',
            persistentCache: true,
            inject: true,
            title: 'Cloud-Sport',
            icons: {
                android: false,
                appleIcon: false,
                appleStartup: false,
                coast: false,
                favicons: true,
                firefox: true,
                opengraph: false,
                twitter: false,
                yandex: false,
                windows: false
            }
        }),
        new HtmlWebpackPlugin({
            template: 'src/index-template.ejs'
        }),
        new ExtractTextPlugin("styles.css"),
        new webpack.ProvidePlugin({
            $: 'jquery',
            jQuery: 'jquery',
            FlatpickrInstance: 'flatpickr',
            "window.jQuery": "jquery"
        }),
        new OfflinePlugin({
            ServiceWorker: false,
            //AppCache: false,
            externals: [
                'version.txt'
            ]
        })
    ]
};
