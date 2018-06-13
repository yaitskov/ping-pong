package org.dan.ping.pong.app.group;

import static java.lang.Integer.compare;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class HisIntPair implements Comparable<HisIntPair> {
    private int his;
    private int enemy;

    @Override
    public int compareTo(HisIntPair o) {
        if (isWon()) {
            if (o.isWon()) {
                return compare(o.diff(), diff());
            } else {
                return -1;
            }
        } else if (isLost()) {
            if (o.isWon()) {
                return 1;
            } else if (o.isLost()) {
                return compare(o.diff(), diff());
            } else {
                return 1;
            }
        } else {
            if (o.isWon()) {
                return 1;
            } else if (o.isLost()) {
                return -1;
            } else {
                return compare(o.his, his);
            }
        }
    }

    private boolean isWon() {
        return his > enemy;
    }

    private boolean isLost() {
        return his < enemy;
    }

    private int diff() {
        return his - enemy;
    }
}
