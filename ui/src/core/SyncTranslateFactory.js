import AngularBean from 'core/angular/AngularBean.js';
import SyncTranslate from './SyncTranslate.js';

export default class SyncTranslateFactory extends AngularBean {
    static get $inject() {
        return ['$translate', '$q'];
    }

    create() {
        return new SyncTranslate(this.$translate, this.$q);
    }
}
