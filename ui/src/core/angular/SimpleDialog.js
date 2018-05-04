import SimpleController from './SimpleController.js';

export default class SimpleDialog extends SimpleController {
    static get $inject() {
        return ['$element'].concat(super.$inject);
    }

    showDialog(tagId) {
        this.$element.find('#' + (tagId || this.tagId)).modal('show');
    }

    hideDialog(tagId) {
        this.$element.find('#' + (tagId || this.tagId)).modal('hide');
    }

    onHide(cb, tagId) {
        this.$element.find('#' + (tagId || this.tagId)).on('hidden.bs.modal', cb);
    }

    get tagId() {
        return null;
    }
}
