package org.dan.ping.pong.app.tournament.console;

import org.springframework.context.annotation.Import;


@Import({ConsoleStrategyDispatcher.class, NoConsoleStrategy.class, ConsoleStrategyImpl.class})
public class ConsoleCtx {
}
