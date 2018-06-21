package org.dan.ping.pong.sys.db.converters.num;

import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.jooq.Converter;

public abstract class SmallNumConverter
        <DN extends Number, JN extends Number>
        implements Converter<DN, JN> {

    protected abstract JN fromNonNull(DN dn);

    @Override
    public JN from(DN c) {
        if (c == null || c.intValue() == 0) {
            return null;
        }
        return fromNonNull(c);
    }

    protected abstract DN toSafe(JN u);

    @Override
    public DN to(JN u) {
        if (u == null) {
            return null;
        }
        validateMidLimits(u.intValue());
        return toSafe(u);
    }

    protected void validateMidLimits(int u) {
        if (u < 0 || u > maxValue()) {
            throw internalError(overflowMessage());
        }
    }

    protected abstract int maxValue();

    public abstract String overflowMessage();
}
