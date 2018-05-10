import SimpleController from 'core/angular/SimpleController.js';
import AppBuildInfo from 'AppBuildInfo.js';

export default class Footer extends SimpleController {
    static get $inject() {
        return [];
    }

    $onInit() {
        this.buildInfo = new AppBuildInfo();
    }
}
