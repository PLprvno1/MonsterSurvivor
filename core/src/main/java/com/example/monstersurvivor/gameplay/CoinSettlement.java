package com.example.monstersurvivor.gameplay;

import com.example.monstersurvivor.character.CharacterProgressLedger;
import com.example.monstersurvivor.config.GameMode;

/** Currency settlement snapshot; separate from official match-statistics events. */
public record CoinSettlement(String matchUuid,int amount,GameMode mode,boolean diagnostic) {
    public boolean eligible(){return CharacterProgressLedger.eligible(mode,diagnostic)
        && CharacterProgressLedger.validUuid(matchUuid) && amount>=0;}
}
