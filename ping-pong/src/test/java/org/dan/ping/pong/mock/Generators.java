package org.dan.ping.pong.mock;

import com.github.javafaker.Faker;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;

public class Generators {
    public static String genStr() {
        return UUID.randomUUID().toString();
    }

    public static String genFirstLastName() {
        return genStr();
    }

    public static String genPlaceLocation() {
        return new Faker().address().fullAddress();
    }

    public static String genPlaceName() {
        Faker faker = new Faker();
        return faker.address().cityName() + " "
                + faker.ancient().hero() + " " + genOrder();
    }

    public static String genTournamentName() {
        Faker faker = new Faker();
        return faker.address().city()
                + " " + faker.ancient().god()
                + " " + genOrder();
    }

    public static String genEmail() {
        return genFirstLastName().replaceAll(" ", ".") + "@gmail.com";
    }

    public static String genPhone() {
        final String result = genStr().replaceAll("[^0-9]+", "");
        if (result.length() > 10) {
            return result.substring(0, 9);
        }
        return result;
    }

    public static int genOrder() {
        return new Random().nextInt(100);
    }

    public static String genStr(int l) {
        return UUID.randomUUID().toString().substring(0, l);
    }

    public static Instant genFutureTime() {
        return Instant.now().plus(new Random().nextInt(10) + 1, ChronoUnit.DAYS);
    }

    public static String genCategoryName() {
        return new Faker().color().name();
    }
}
