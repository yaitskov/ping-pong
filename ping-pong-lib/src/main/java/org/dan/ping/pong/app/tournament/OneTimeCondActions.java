package org.dan.ping.pong.app.tournament;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
@Builder
public class OneTimeCondActions {
    private List<OneTimeCondAction> onScheduleTables;

    public void runSchedule(Tid tid) {
        log.info("Run {} one time schedule actions for {}",
                onScheduleTables.size(), tid);
        if (onScheduleTables.isEmpty()) {
            return;
        }
        onScheduleTables.forEach(OneTimeCondAction::fire);
        onScheduleTables.clear();
    }
}
