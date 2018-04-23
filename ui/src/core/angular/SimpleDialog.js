import SimpleController from './SimpleController.js';

export default class SimpleDialog extends SimpleController {
    static get $inject() {
        return ['$element'].concat(super.$inject);
    }

    showDialog(tagId) {
        this.$element.find('#' + tagId).modal('show');
    }
}
