package org.dan.ping.pong.app.match.rule.reason;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.rule.OrderRuleName;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InfoReason implements Reason {
    private OrderRuleName rule;

    @Override
    @JsonIgnore
    public Uid getUid() {
        return null;
    }

    @Override
    public int compareTo(Reason o) {
        throw new IllegalStateException();
    }

    public String toString() {
        return "skip " + rule.name();
    }
}
