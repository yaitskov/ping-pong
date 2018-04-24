const path = require('path');

function showHelp() {
    console.log(`
Custom keys:
  --khelp       - print this message
  --kkeep       - keep browser open
  --kstest=     - single test to run
  --srcmap      - generate source map
  --cover       - generate coverage report
  --fan         - use phantom js runner
  --chrom       - use chrome js runner
  --debugfan    - launch phantom js
  --ll=         - log level
`);
    process.exit(1);
}

module.exports = (config) => {
    if (config.khelp) {
        showHelp();
    }
    const jsPreprocessors = [];
    const reporters = ['progress'];
    const browsers = ['PhantomJS']; // Chromium Firefox ];

    if (config.cover) {
        for (let reporter of ['coverage', 'kjhtml', 'mocha', 'html']) {
            reporters.push(reporter);
        }
        jsPreprocessors.push('coverage');
    }
    if (config.srcmap) {
        jsPreprocessors.push('sourcemap');
    }
    if (config.fan) {
        browsers.length = 0;
        browsers.push('PhantomJS');
    }
    if (config.chrom) {
        browsers.length = 0;
        browsers.push('Chromium');
    }
    if (config.debugfan) {
        browsers.length = 0;
        browsers.push('PhantomJS_debug');
    }

    config.set({
        basePath: '',
        frameworks: ['jasmine', 'jasmine-matchers'],
        // plugins: [
        //     'karma-jasmine',
        //     'webpack',
        //     'ng-html2js',
        //     'karma-jasmine-matchers'
        // ],
        browserConsoleLogOptions: {
            level: config.ll ? config.ll : 'error',
            format: '%b %T: %m',
            terminal: true
        },
        files: [
            'dist/bundle.js',
            'node_modules/angular-mocks/angular-mocks.js',
            //'node_modules/jasmine-expect/dist/jasmine-matchers.js',
            'dist/*.html',
            config.kstest ?  `src/**/${config.kstest}` : 'src/**/*.test.js'],
        exclude: [],
        preprocessors: {
            'dist/*.html': ['ng-html2js'],
            'src/**/*.test.js': 'webpack',
            'dist/bundle.js': jsPreprocessors
        },
        reporters: reporters,

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
        browsers: browsers,
        singleRun: !config.kkeep,
        concurrency: Infinity,

        ngHtml2JsPreprocessor: {
            moduleName: 'cloudSportE2e.templates',
            cacheIdFromPath: function(filepath) {
                // console.log("cacheIdFromPath " + filepath);
                return filepath.substr(filepath.lastIndexOf('/') + 1);
            },
        },
        customLaunchers: {
            'PhantomJS_debug': {
                base: 'PhantomJS',
                debug: true
            }
        }
    })
}
