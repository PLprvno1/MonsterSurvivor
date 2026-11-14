package com.example.monstersurvivor.android.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.monstersurvivor.R;
import com.example.monstersurvivor.android.model.GamePreferences;

/**
 * Utility helpers for UI immersive mode, theme management, and animations.
 */
public final class UiUtils {

    private UiUtils() {}

    /**
     * Applies full screen immersive sticky mode to match the fantasy game experience.
     */
    public static void applyImmersiveMode(Activity activity) {
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    /**
     * Applies the user's preferred theme (Forest or Dark Magic).
     */
    public static void applyAppTheme(Context context) {
        GamePreferences prefs = GamePreferences.getInstance(context);
        if (GamePreferences.THEME_DARK.equals(prefs.getTheme())) {
            context.setTheme(R.style.Theme_MonsterSurvivor_Dark);
        } else {
            context.setTheme(R.style.Theme_MonsterSurvivor_Forest);
        }
    }

    /**
     * Plays a tactile bounce animation on button click.
     */
    public static void animateClick(View view, Runnable afterAnimation) {
        Animation anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.btn_press_scale);
        if (afterAnimation != null) {
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    afterAnimation.run();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }
        view.startAnimation(anim);
    }
}
