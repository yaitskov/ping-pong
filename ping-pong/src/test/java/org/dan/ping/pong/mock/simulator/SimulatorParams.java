package org.dan.ping.pong.mock.simulator;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Wither;

@Getter
@Builder
@Wither
@ToString
public class SimulatorParams {
    public static final SimulatorParams T_1_Q_2_G_8 = SimulatorParams.builder()
            .tables(1)
            .quitsFromGroup(2)
            .maxGroupSize(8)
            .build();

    public static final SimulatorParams T_1_Q_1_G_8 = SimulatorParams.builder()
            .tables(1)
            .quitsFromGroup(1)
            .maxGroupSize(8)
            .build();

    public static final SimulatorParams T_1_Q_1_G_2_3P = SimulatorParams.builder()
            .tables(1)
            .quitsFromGroup(1)
            .maxGroupSize(2)
            .thirdPlace(true)
            .build();

    public static final SimulatorParams T_3_Q_1_G_8 = SimulatorParams.builder()
            .tables(3)
            .quitsFromGroup(1)
            .maxGroupSize(8)
            .build();

    public static final SimulatorParams T_1_Q_1_G_2 = SimulatorParams.builder()
            .tables(1)
            .quitsFromGroup(1)
            .maxGroupSize(2)
            .build();

    private int quitsFromGroup;
    private int tables;
    private int maxGroupSize;
    private boolean thirdPlace;
}
