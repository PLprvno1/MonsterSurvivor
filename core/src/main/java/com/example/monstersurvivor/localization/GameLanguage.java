package com.example.monstersurvivor.localization;

/** Persistent language codes; independent of Android and the device locale. */
public enum GameLanguage {
    VI("vi"), EN("en");
    public final String code;
    GameLanguage(String code) {this.code=code;}
    public static GameLanguage parse(String code) {
        if(code!=null)for(var language:values())if(language.code.equalsIgnoreCase(code.trim()))return language;
        return VI;
    }
}
