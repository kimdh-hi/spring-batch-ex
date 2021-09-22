package com.spingbatchex.lab7.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Enumerated(EnumType.STRING)
    private Level level  = Level.NORMAL;

    private int totalAmount;

    private LocalDate updatedAt;

    @Builder
    private User(String username, int totalAmount) {
        this.username = username;
        this.totalAmount = totalAmount;
    }

    public boolean isValidToLevelup() {
        return Level.availableLevelUp(this.getLevel(), this.getTotalAmount());
    }

    public Level levelUp() {
        Level nextLevel = this.getNextLevel(this.getTotalAmount());
        this.level = nextLevel;
        this.updatedAt = LocalDate.now();

        return nextLevel;
    }

    private Level getNextLevel(int totalAmount) {
        if (totalAmount >= Level.VIP.nextAmount) return Level.VIP;
        if (totalAmount >= Level.GOLD.nextAmount) return Level.GOLD.nextLevel;
        if (totalAmount >= Level.SILVER.nextAmount) return Level.SILVER.nextLevel;
        if (totalAmount >= Level.NORMAL.nextAmount) return Level.NORMAL.nextLevel;

        return this.level;
    }

    public enum Level {
        VIP(500_000, null),
        GOLD(500_000, VIP), // 구매 금액이 500,000만원이 되면 VIP 등급으로 등급상승
        SILVER(300_000, GOLD),
        NORMAL(200_000, SILVER);

        private final int nextAmount;
        private final Level nextLevel;

        Level(int nextAmount, Level nextLevel) {
            this.nextAmount = nextAmount;
            this.nextLevel = nextLevel;
        }

        public static boolean availableLevelUp(Level level, int totalAmount) {
            if (Objects.isNull(level)) return false; // VIP
            if (Objects.isNull(level.nextLevel)) return false; // VIP

            return totalAmount >= level.nextAmount;
        }
    }
}
