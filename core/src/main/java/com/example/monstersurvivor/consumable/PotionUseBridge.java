package com.example.monstersurvivor.consumable;

/** Platform boundary that durably consumes one potion before gameplay applies it. */
@FunctionalInterface
public interface PotionUseBridge {
    void consume(PotionType type,java.util.function.Consumer<Boolean> completion);
    PotionUseBridge NONE=(type,completion)->completion.accept(false);
}
