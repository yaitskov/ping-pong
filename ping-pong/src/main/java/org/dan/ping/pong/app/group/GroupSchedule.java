package org.dan.ping.pong.app.group;

import static com.google.common.primitives.Ints.asList;
import static java.util.stream.Collectors.toList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupSchedule {
    public static final GroupSchedule DEFAULT_SCHEDULE = GroupSchedule.builder()
            .size2Schedule(generateDefaultSchedules(20)).build();

    public static List<Integer> oneBased(int... ns) {
        return asList(ns).stream().map(n -> n - 1).collect(toList());
    }

    public static Map<Integer, List<Integer>> generateDefaultSchedules(int maxGroupSize) {
        final Map<Integer, List<Integer>> result = new HashMap<>();
        result.put(2, asList(0, 1));
        result.put(3, asList(0, 2, 0, 1, 1, 2));
        result.put(4, oneBased(1, 3, 2, 4, 1, 2, 3, 4, 1, 4, 2, 3));
        result.put(5, oneBased(2, 5, 3, 4, 1, 4, 3, 5, 1, 3, 2, 4, 1, 2, 4, 5, 1, 5, 2, 3));
        for (int i = 6; i < maxGroupSize; ++i) {
            result.put(i, generateDefaultSchedule(i));
        }
        return result;
    }

    public static List<Integer> generateDefaultSchedule(int groupSize) {
        final List<Integer> result = new ArrayList<>();
        for (int i = 0; i < groupSize; ++i) {
            for (int j = i + 1; j < groupSize; ++j) {
                result.add(i);
                result.add(j);
            }
        }
        return result;
    }

    private Map<Integer, List<Integer>> size2Schedule;
}
