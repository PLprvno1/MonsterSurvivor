package com.example.monstersurvivor.android;

import android.util.Log;
import com.example.monstersurvivor.debug.DiagnosticRun;
import java.io.File;
import org.json.JSONObject;

/** Debug-variant test component only. A stale file or Intent cannot authorize a new diagnostic run. */
public final class DiagnosticLauncher extends AndroidLauncher {
    @Override protected DiagnosticRun diagnosticsForLaunch() {
        String id=getIntent().getStringExtra("diagnostic_run"),token=getIntent().getStringExtra("diagnostic_token");
        getIntent().removeExtra("diagnostic_run");getIntent().removeExtra("diagnostic_token");
        try {
            if(id==null || !id.matches("[a-f0-9]{32}") || token==null || !token.matches("[a-f0-9]{32}"))throw new IllegalArgumentException("Missing explicit run/token");
            File request=new File(getFilesDir(),"diagnostic-runs/"+id+"/lease.json");
            JSONObject lease=new JSONObject(new com.badlogic.gdx.files.FileHandle(request).readString("UTF-8"));
            long now=System.currentTimeMillis(),created=lease.getLong("createdMs");
            if(!"codex-automated-tests".equals(lease.getString("owner")) || !token.equals(lease.getString("token"))
                || created>now+5000 || now-created>60000)throw new IllegalArgumentException("Invalid or expired one-use lease");
            if(!request.delete())throw new IllegalStateException("Cannot consume lease");
            Log.i("DiagnosticGate","Accepted one-use automated run="+id);
            return DiagnosticRun.automated(id);
        }catch(Exception rejected) {
            Log.w("DiagnosticGate","Rejected diagnostic launch; using normal gameplay: "+rejected.getClass().getSimpleName());
            return DiagnosticRun.NONE;
        }
    }
}
