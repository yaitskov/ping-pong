import AngularBean from 'core/angular/AngularBean.js';

export default class RequestStatusService extends AngularBean {
    static get $inject() {
        return ['$rootScope' /* $sce */];
    }

    startLoading(msg, meta) {
        this.$rootScope.$broadcast('event.request.started', msg || 'Loading', meta);
    }

    failed(response, meta) {
        console.log(`req failed response = [${JSON.stringify(response)}]`);
        this.$rootScope.$broadcast('event.request.failed', response, meta);
    }

    validationFailed(msg) {
        this.$rootScope.$broadcast('event.request.validation', msg);
    }

    complete(response) {
        this.$rootScope.$broadcast('event.request.complete', response);
    }

    convertMsg(msg) {
        if (typeof msg == 'string') {
            return {message: msg, params: {}};
        } else if (msg instanceof Array) {
            return {message: msg[0], params: msg[1]};
        } else {
            if (!msg.params) {
                msg.params = {};
            }
            return msg;
        }
    }

    responseToErr(responseData, prefix) {
        prefix = this.convertMsg(prefix);
        if (typeof responseData == 'object') {
            if (typeof responseData.message == 'string') {
                var result = {};
                result.message = responseData.message;
                result.params = responseData.params || {};
                result.causes = [];
                if (responseData.field2Errors instanceof Object) {
                    for(var key in responseData.field2Errors) {
                        var list = responseData.field2Errors[key];
                        for (var k2 in list) {
                            result.causes.push(this.convertMsg(list[k2]));
                        }
                    }
                }
                return result;
            } else {
                return Object.assign(prefix, {verb: JSON.stringify(responseData)});
            }
        } else {
            return Object.assign(prefix, {verb: /*this.$sce.trustAsHtml*/(responseData)});
        }
    }

    responseStatusToError(response) {
        if (response.status == 502 || response.status == -1) {
            return this.responseToErr(response.data, "Server is not available");
        } else if (response.status == 401) {
            return this.responseToErr(response.data, 'authentication-error');
        } else if (response.status == 403) {
            return this.responseToErr(response.data, 'authorization-error');
        } else if (response.status == 404) {
            return this.responseToErr(response.data, 'entity-not-found');
        } else if (response.status == 400) {
            return this.responseToErr(response.data, 'bad-request');
        } else if (response.status == 500) {
            return this.responseToErr(response.data, 'application-error');
        } else if (response.status < 299) {
            return this.responseToErr(response.data,
                                            ['no-error-but-failed',
                                             {status: response.status}]);
        } else if (!response.status) {
            console.log("no status message: " + response.message);
            return this.responseToErr(response.data, 'status-is-missing');
        } else {
            return this.responseToErr(response.data,
                                      ['unexpected-status', {status: response.status}]);
        }
    }
}
