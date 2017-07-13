package org.dan.ping.pong.app.match;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

class GroupMatchListComparator implements Comparator<List<GroupMatchInfo>> {
    public static final Comparator<List<GroupMatchInfo>> COMPARATOR
            = new GroupMatchListComparator().reversed();

    @Override
    public int compare(List<GroupMatchInfo> o1, List<GroupMatchInfo> o2) {
        final int result = Integer.compare(o1.size(), o2.size());
        if (result == 0) {
            return Integer.compare(sum(o1), sum(o2));
        }
        return result;
    }

    private int sum(List<GroupMatchInfo> o1) {
        return o1.stream()
                .map(GroupMatchInfo::getSetsWon)
                .map(Optional::get)
                .mapToInt(o -> o)
                .sum();
    }
}
