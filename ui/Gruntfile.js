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

const webpackConfig = require('./webpack.config');

module.exports = function(grunt) {
    grunt.loadNpmTasks('grunt-install-dependencies');
    grunt.loadNpmTasks('grunt-webpack');
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json')
    });
    grunt.initConfig({
        webpack: {
            options: {
                stats: !process.env.NODE_ENV || process.env.NODE_ENV === 'development'
            },
            prod: webpackConfig,
            dev: Object.assign({ watch: false }, webpackConfig)
        }
    });
    grunt.registerTask('all', ['install-dependencies', 'webpack']);
    grunt.registerTask('default', ['all']);
};
