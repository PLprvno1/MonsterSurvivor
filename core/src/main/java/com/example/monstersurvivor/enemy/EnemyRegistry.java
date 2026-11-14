package com.example.monstersurvivor.enemy;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.example.monstersurvivor.entity.EnemyType;
import java.util.*;

/** Loaded from generated tuning data without Gdx.app/files; safe before Android's GL Activity starts. */
public final class EnemyRegistry {
    private static final List<EnemyDefinition> ALL;
    private static final Map<String,EnemyDefinition> BY_ID;
    public static final int FAMILY_COUNT;
    static {
        List<EnemyDefinition> all=new ArrayList<>();Map<String,EnemyDefinition> ids=new LinkedHashMap<>();Map<String,Integer> families=new LinkedHashMap<>();
        JsonValue root=new JsonReader().parse(EnemyCatalogData.json());
        for(JsonValue v=root.child;v!=null;v=v.next) {
            String family=v.getString("family");if(!families.containsKey(family))families.put(family,families.size());
            EnemyDefinition d=new EnemyDefinition(all.size(),families.get(family),v);
            if(ids.put(d.id,d)!=null)throw new IllegalArgumentException("Duplicate enemy id "+d.id);all.add(d);
        }
        ALL=Collections.unmodifiableList(all);BY_ID=Collections.unmodifiableMap(ids);FAMILY_COUNT=families.size();
    }
    public static List<EnemyDefinition> all() {return ALL;}
    public static EnemyDefinition get(String id) {
        EnemyDefinition d=BY_ID.get(id);if(d==null)throw new IllegalArgumentException("Unknown enemy id "+id);return d;
    }
    public static EnemyDefinition legacy(EnemyType type) {return get(type.spriteId);}
    private EnemyRegistry() {}
}
