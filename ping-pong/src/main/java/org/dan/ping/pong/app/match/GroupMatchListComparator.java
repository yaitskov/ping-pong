package org.dan.ping.pong.app.match;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class GroupMatchListComparator implements Comparator<List<MatchInfo>> {
    public static final Comparator<List<MatchInfo>> COMPARATOR
            = new GroupMatchListComparator().reversed();

    @Override
    public int compare(List<MatchInfo> o1, List<MatchInfo> o2) {
        final int result = Integer.compare(o1.size(), o2.size());
        if (result == 0) {
            return Integer.compare(sum(o1), sum(o2));
        }
        return result;
    }

    private int sum(List<MatchInfo> o1) {
        return o1.stream()
                .map(GroupMatchInfo::getSetsWon)
                .map(Optional::get)
                .mapToInt(o -> o)
                .sum();
    }
}
