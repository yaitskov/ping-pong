package org.dan.ping.pong.sys.ctx;

import static java.lang.Integer.compare;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@Builder
@EqualsAndHashCode(of = "priority")
public class PropertyFileRef implements Comparable<PropertyFileRef> {
    private final int priority;
    private final Resource resource;

    @Override
    public int compareTo(PropertyFileRef o) {
        return compare(priority, o.priority);
    }

    public String toString() {
        return resource.toString();
    }
}
