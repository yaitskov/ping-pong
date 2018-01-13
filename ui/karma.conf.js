const path = require('path');

function showHelp() {
    console.log(`
Custom keys:
  --khelp       - print this message
  --kkeep       - keep browser open
  --kstest=     - single test to run
`);
    process.exit(1);
}

module.exports = (config) => {
    if (config.khelp) {
        showHelp();
    }
    config.set({
        basePath: '',
        frameworks: ['jasmine'],
        files: [
            'dist/bundle.js',
            'node_modules/angular-mocks/angular-mocks.js',
            config.kstest ?  `src/**/${config.kstest}` : 'src/**/*.test.js'],
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
        webpack: require("./webpack.config.js"),
        colors: true,
        logLevel: config.LOG_INFO,
        autoWatch: false,
        browsers: ['Chromium'], // Chromium Firefox PhantomJS
        singleRun: !config.kkeep,
        concurrency: Infinity
    })
}
