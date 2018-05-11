// rm -rf node_modules && npm cache clean && npm i
// npm install grunt-dependency-installer --save-dev
// npm install grunt-install-dependencies --save-dev
// npm install ajv --save-dev
// npm install webpack --save-dev
// npm install grunt-webpack --save-dev
// npm install --save-dev style-loader css-loader
// npm install --save-dev file-loader
// npm install clean-webpack-plugin --save-dev
// npm install --save-dev html-webpack-plugin
// npm install sass-loader node-sass webpack --save-dev
// npm i -D html-loader
// npm install offline-plugin --save-dev
// npm install grunt-exec --save-dev

const path = require('path');
const fs = require('fs');
const GitRevisionPlugin = require('git-revision-webpack-plugin');
const webpackConfig = require('./webpack.config.js');
const distDir = path.resolve(__dirname, 'dist');
const webpack = require('webpack');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');
const OptimizeCssAssetsPlugin = require('optimize-css-assets-webpack-plugin');

function generateBuildInfo() {
    const gitHash = new GitRevisionPlugin().commithash();
    fs.writeFileSync(
        `${__dirname}/src/AppBuildInfo.js`,
        `export default class AppBuildInfo {
             constructor() {
                 this.lastCommitHash = '${gitHash}';
                 this.buildTime = new Date(${new Date().getTime()});
             }
        }`);
}

const wpProd = Object.assign(
    {},
    webpackConfig,
    {
        plugins: [...webpackConfig.plugins.slice(0, -1),
                  new OptimizeCssAssetsPlugin({
                      cssProcessor: require('cssnano'),
                      cssProcessorOptions: {
                          discardComments: {
                              removeAll: true
                          }
                      },
                      canPrint: true
                  }),
                  new UglifyJsPlugin({
                      parallel: 4,
                      sourceMap: true
                  }),
                  ...webpackConfig.plugins.slice(-1)]
    }
);

module.exports = function(grunt) {
    grunt.loadNpmTasks('grunt-install-dependencies');
    grunt.loadNpmTasks('grunt-webpack');
    grunt.loadNpmTasks('grunt-exec');
    grunt.loadNpmTasks('grunt-karma');
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        karma: {
            unit: {
                configFile: 'karma.conf.js'
            }
        },
        webpack: {
            options: {
                stats: !process.env.NODE_ENV || process.env.NODE_ENV === 'development'
            },
            prod: wpProd,
            dev: Object.assign({ watch: false }, webpackConfig)
        },
        exec: {
            clean: `rm -rf ${__dirname}/dist ${__dirname}/node_modules/.cache/hard-source && mkdir ${__dirname}/dist`,
            karman: 'karma start',
            fixAppCacheManifest: `sed -i "/^CACHE:/d" ${__dirname}/dist/appcache/manifest.appcache`
        }
    });

    grunt.task.registerTask('build-info', 'generates AppBuildInfo.js',
                            generateBuildInfo);
    grunt.registerTask('wdbuild', ['build-info', 'webpack:dev', 'exec:fixAppCacheManifest']);
    grunt.registerTask('wrbuild', ['build-info', 'webpack:prod', 'exec:fixAppCacheManifest']);

    grunt.registerTask('rerelease', ['wrbuild', 'exec:karman']);
    grunt.registerTask('release', ['exec:clean', 'install-dependencies',
                                    'wrbuild', 'exec:karman']);
    grunt.registerTask('default', ['release']);
};
