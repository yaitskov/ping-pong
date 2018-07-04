package org.dan.ping.pong.sys.ctx;

import java.beans.PropertyEditorSupport;

public abstract class TypeSafePropertyEditor<T> extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) {
        setValue(getValueFromString(text));
    }

    protected abstract T getValueFromString(String text);
}
