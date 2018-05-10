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


const GitRevisionPlugin = require('git-revision-webpack-plugin');
const fs = require('fs');
const path = require('path');

const gitHash = new GitRevisionPlugin().commithash();
const jsCssInFix = gitHash + "-" + Date.now();
const distPath = path.resolve(__dirname, 'dist');

if (!fs.existsSync(distPath)) {
    console.log(`Create ${distPath}`);
    fs.mkdirSync(distPath);
}

fs.writeFileSync(path.resolve(distPath, 'jsCssInFix'), jsCssInFix);

const webpackConfig = require('./webpack.config.js');

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
            prod: webpackConfig,
            dev: Object.assign({ watch: false }, webpackConfig)
        },
        exec: {
            remove_cache: 'sed -i "/^CACHE:/d" dist/appcache/manifest.appcache'
        }
    });
    grunt.registerTask('all', ['install-dependencies',
                               'webpack',
                               'karma:unit:start',
                               'exec']);
    grunt.registerTask('default', ['all']);
};
