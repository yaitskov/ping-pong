package org.dan.ping.pong.app.castinglots;

import static com.google.common.collect.ImmutableList.copyOf;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.match.MatchType;

import java.util.Optional;

@RequiredArgsConstructor
public class TypeChain {
    @Getter
    private final MatchType type;
    private final Optional<TypeChain> next;

    public TypeChain next() {
        return next.orElse(this);
    }

    public static TypeChain of(MatchType... types) {
        Optional<TypeChain> head = Optional.empty();
        for (MatchType type : copyOf(types).reverse()) {
            head = Optional.of(new TypeChain(type, head));
        }
        return head.get();
    }
}
