module.exports = (config) => {
    config.set({
        basePath: '',
        frameworks: ['jasmine'],
        files: [
            'dist/bundle.js',
            'src/**/*.test.js'],
        exclude: [],
        preprocessors: {
            'src/**/*.test.js': 'webpack',
            'dist/bundle.js': 'coverage'
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
        webpack: require("./webpack.config.js"),
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
        singleRun: false,
        concurrency: Infinity
    })
}
