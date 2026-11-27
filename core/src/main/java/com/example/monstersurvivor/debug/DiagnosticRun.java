package com.example.monstersurvivor.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;

/** Explicit process/session capability. Normal constructors never inspect legacy fixture files. */
public final class DiagnosticRun {
    public static final DiagnosticRun NONE=new DiagnosticRun(null);
    private final String id;
    private DiagnosticRun(String id) {this.id=id;}
    /** Android callers must first consume a fresh one-use lease in the debug-only test launcher. */
    public static DiagnosticRun automated(String id) {
        if(id==null || !id.matches("[a-f0-9]{32}"))throw new IllegalArgumentException("Diagnostic run ID");
        return new DiagnosticRun(id);
    }
    public boolean enabled() {return id!=null;}
    public String id() {return id==null?"none":id;}
    public FileHandle file(String name) {
        if(!enabled() || !name.matches("[a-zA-Z0-9_.-]+"))throw new IllegalArgumentException("Diagnostic file");
        return Gdx.files.local("diagnostic-runs/"+id+"/"+name);
    }
    public JsonValue object(String name) {
        if(!enabled())return null;
        var f=file(name);if(!f.exists())return null;
        try {
            var value=new JsonReader().parse(f);
            return value!=null && value.isObject()?value:null;
        }catch(SerializationException incomplete) {return null;}
    }
}
