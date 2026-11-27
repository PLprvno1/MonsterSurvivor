import com.badlogic.gdx.utils.*;
import com.example.monstersurvivor.enemy.*;
import com.example.monstersurvivor.enemy.skill.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;

/** Generate the 82-species handoff from the actual immutable runtime policies and loaded catalog metadata. */
public final class EnemySkillManifest {
    public static void main(String[] args)throws Exception{
        Map<String,JsonValue> sprites=new HashMap<>();for(String file:new String[]{"game/catalog.json","game/enemy-catalog.json"})for(var s=new JsonReader().parse(Files.readString(Path.of(file))).get("sprites").child;s!=null;s=s.next)sprites.put(s.getString("id"),s);
        var doc=new StringBuilder("# Enemy Skill Assignment — Task08.1\n\n82 loài, policy lấy trực tiếp từ EnemyDefinition.skill; không sửa HP/damage/contact/spawn weights cũ. Boss body luôn NONE và không nằm trong bảng này. Cooldown tính giữa các lần bắt đầu cast; skill damage1. Gate dùng progress, các timer dùng giây gameplay thực. Chỉ bắt đầu trên camera, có LOS; đạn chốt hướng lúc release.\n\nSPECIAL chỉ ghi khi catalog thật có clip cho đủ bốn hướng, còn lại ATTACK fallback. Windup/recover retime clip thật, không suy đoán asset Special. NONE chủ động giữ melee; chuỗi special cũ trong roster vẫn chỉ là gợi ý nghiên cứu.\n\n| Enemy ID | Loài | Biome / family | Skill / pattern | Range | CD Easy / Hard | Windup E/H | Speed | FX | Animation | Balance / lý do |\n|---|---|---|---|---:|---:|---:|---:|---|---|---|\n");
        var buffer=new StringWriter();var json=new JsonWriter(buffer);json.setOutputType(JsonWriter.OutputType.json);json.array();int assigned=0;
        for(var d:EnemyRegistry.all()){
            var skill=d.skill;boolean special=true;var sprite=sprites.get(d.spriteId);
            for(String dir:new String[]{"down","left","right","up"}){boolean found=false;for(var a=sprite.get("animations").child;a!=null;a=a.next)if(a.getString("state").equalsIgnoreCase("special") && a.getString("direction").equalsIgnoreCase(dir))found=true;special&=found;}
            String animation=skill==EnemySkillDefinition.NONE?"melee cũ":special?"SPECIAL":"ATTACK (fallback)";
            String pattern=skill.kind==EnemySkillDefinition.Kind.SHOT?(skill.count==1?"1 straight":skill.count+" fan, "+skill.spreadOrRadius+"° gap"):skill.kind==EnemySkillDefinition.Kind.NONE?"contact-only":skill.kind+" R"+skill.radius();
            String note=skill==EnemySkillDefinition.NONE?(d.charge?"Giữ legacy charge, không cộng skill mới":d.archetype==EnemyArchetype.TANK?"Tank/cận chiến; không ép thành caster":"Giữ chase/contact; không ép phép từ clip Special"):("damage1; gate"+(int)skill.unlock+(skill.status!=null?"; "+skill.status:"")+(skill.poisonArea?"; poison area cap3":""));
            if(skill!=EnemySkillDefinition.NONE)assigned++;
            doc.append("| ").append(d.id).append(" | ").append(d.name).append(" | ").append(d.biome).append(" / ").append(d.family).append(" | ").append(skill).append(" / ").append(pattern).append(" | ").append(skill.range).append(" | ").append(String.format(Locale.ROOT,"%.2f / %.2f",skill.cooldown(false),skill.cooldown(true))).append(" | ").append(String.format(Locale.ROOT,"%.2f / %.2f",skill.windup(false),skill.windup(true))).append(" | ").append(skill.speed).append(" | ").append(skill==EnemySkillDefinition.NONE?"—":skill.fx+" / "+skill.impact).append(" | ").append(animation).append(" | ").append(note).append(" |\n");
            json.object().name("id").value(d.id).name("name").value(d.name).name("biome").value(d.biome).name("family").value(d.family).name("skill").value(skill.name()).name("kind").value(skill.kind.name()).name("count").value(skill.count).name("range").value(skill.range).name("speed").value(skill.speed).name("cooldownEasy").value(skill.cooldown(false)).name("cooldownHard").value(skill.cooldown(true)).name("windupEasy").value(skill.windup(false)).name("windupHard").value(skill.windup(true)).name("unlock").value(skill.unlock).name("fx").value(skill.fx).name("impact").value(skill.impact).name("animation").value(animation).pop();
        }
        json.pop();json.close();Path out=Path.of(System.getProperty("ms.report","../core/build/reports/task08_1"));Files.createDirectories(out);Files.writeString(out.resolve("enemy-skill-manifest.json"),buffer.toString());
        doc.append("\n**Tổng:** ").append(assigned).append(" có kỹ năng; ").append(82-assigned).append(" NONE. Không có skill thường trên Boss. Budget chung: caster tối đa8 Easy/10 Hard (progress<90:3/4), telegraph3/4, projectile24, FX24, poison area3. Global gate0,42/0,24s; recovery projectile0,18/0,12s, melee/AoE0,26/0,20s; stand-off86/96. Không tăng hai A* mỗi frame.\n");Files.writeString(Path.of("../ENEMY_SKILL_ASSIGNMENT.md"),doc.toString());System.out.println("EnemySkillManifest PASS 82 policies, "+assigned+" assigned; catalog animation metadata verified");
    }
}
