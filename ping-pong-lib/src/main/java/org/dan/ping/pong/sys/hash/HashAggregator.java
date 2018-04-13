package org.dan.ping.pong.sys.hash;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class HashAggregator {
    private final Hasher hasher;
    private boolean hasSomething;

    public static HashAggregator createHashAggregator() {
        return new HashAggregator(Hashing.md5().newHasher());
    }

    public Optional<String> currentHash() {
        if (hasSomething) {
            Optional.of(hasher.hash().toString());
        }
        return Optional.empty();
    }

    public HashAggregator section(String section) {
        hasher.putString(section, UTF_8);
        return this;
    }

    public HashAggregator hash(int i) {
        hasSomething = true;
        hasher.putInt(i);
        return this;
    }

    public HashAggregator hash(Hashable hashable) {
        hashable.hashTo(this);
        return this;
    }
}
