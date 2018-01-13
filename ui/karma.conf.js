module.exports = (config) => {
    config.set({
        basePath: '',
        frameworks: ['jasmine'],
        files: [
            'dist/bundle.js',
            'node_modules/angular-mocks/angular-mocks.js',
            'src/**/*.test.js'],
        webpack: require("./webpack.config.js"),
        exclude: [],
        preprocessors: {
            'src/**/*.test.js': 'webpack',
            'dist/bundle.js': [/*'sourcemap',*/ 'coverage']
        },
        reporters: ['progress', 'coverage', 'kjhtml', 'mocha', 'html'],
        htmlReporter: {
            outputFile: 'tests/units.html',
            // Optional
            pageTitle: 'Unit Tests',
            subPageTitle: 'A sample project description',
            groupSuites: true,
            useCompactStyle: true,
            useLegacyStyle: true
        },

        colors: true,
        logLevel: config.LOG_INFO,
        autoWatch: false,
        browsers: ['Chromium'], // Chromium Firefox PhantomJS
        // customLaunchers: {
        //     'PhantomJS_custom': {
        //         base: 'PhantomJS',
        //         debug: true,
        //     },
        // },
        singleRun: true,
        concurrency: Infinity
    })
}
