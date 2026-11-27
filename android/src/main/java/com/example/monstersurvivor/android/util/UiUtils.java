package com.example.monstersurvivor.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.monstersurvivor.R;
import com.example.monstersurvivor.android.model.GamePreferences;

import java.util.Locale;

/**
 * Utility helpers for UI immersive mode, theme management, localization wrapper, and animations.
 */
public final class UiUtils {

    private UiUtils() {}

    /**
     * Wraps the context with the user's preferred in-app language (VI or EN) without
     * modifying the host Android OS system locale.
     */
    public static Context wrapLocale(Context context) {
        String lang = GamePreferences.getInstance(context).getGameLanguage();
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }

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

    private static long sLastClickTime = 0;
    private static final long DEBOUNCE_INTERVAL_MS = 500;

    /**
     * Plays a tactile bounce animation on button click with debounce protection.
     */
    public static void animateClick(View view, Runnable afterAnimation) {
        animateClick(view, com.example.monstersurvivor.audio.AudioEvent.UI_TAP, afterAnimation);
    }

    public static void animateClick(View view, com.example.monstersurvivor.audio.AudioEvent event, Runnable afterAnimation) {
        if (view == null || !view.isEnabled()) return;
        long now = android.os.SystemClock.elapsedRealtime();
        if (now - sLastClickTime < DEBOUNCE_INTERVAL_MS) {
            return;
        }
        sLastClickTime = now;
        view.setEnabled(false);
        Animation anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.btn_press_scale);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                view.postDelayed(() -> view.setEnabled(true), 300);
                com.example.monstersurvivor.android.audio.NativeAudio.play(view.getContext(), event);
                if (afterAnimation != null) afterAnimation.run();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        view.startAnimation(anim);
    }

    /**
     * Performs standard animated back navigation with sound and reverse slide.
     */
    public static void navigateBack(Activity activity) {
        if (activity == null) return;
        com.example.monstersurvivor.android.audio.NativeAudio.play(activity, com.example.monstersurvivor.audio.AudioEvent.UI_CANCEL);
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Styles and constrains an AlertDialog for clean, single-frame Fantasy Pixel Art presentation.
     * Prevents oversized stretching in landscape, removes extra background overlays,
     * and guarantees high-contrast button styling.
     */
    public static void styleDialog(androidx.appcompat.app.AlertDialog dialog) {
        if (dialog == null) return;
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.dialog_fantasy_window_bg);
            android.util.DisplayMetrics dm = dialog.getContext().getResources().getDisplayMetrics();
            int maxWidth = Math.min((int) (dm.widthPixels * 0.70f), (int) (480 * dm.density));
            if (maxWidth > 0) {
                window.setLayout(maxWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
        android.widget.Button pos = dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
        if (pos != null) {
            pos.setTextColor(androidx.core.content.ContextCompat.getColor(dialog.getContext(), R.color.gold_bright));
            pos.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        }
        android.widget.Button neg = dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE);
        if (neg != null) {
            neg.setTextColor(androidx.core.content.ContextCompat.getColor(dialog.getContext(), R.color.text_secondary));
            neg.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        }
        android.widget.Button neu = dialog.getButton(android.content.DialogInterface.BUTTON_NEUTRAL);
        if (neu != null) {
            neu.setTextColor(androidx.core.content.ContextCompat.getColor(dialog.getContext(), R.color.text_muted));
        }
    }
}
