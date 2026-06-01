package com.example.academicapp.activities;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.academicapp.R;

/**
 * Ayudante visual que muestra un estado de error recuperable cuando la app no puede contactar con el backend.
 */
final class ConnectionErrorViewHelper {

    private static final String TAG_CONNECTION_ERROR = "academic_app_connection_error";

    private ConnectionErrorViewHelper() {
    }

    static void show(Activity activity, Runnable retryAction) {
        if (activity == null) {
            return;
        }

        FrameLayout content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }

        hide(activity);

        FrameLayout overlay = new FrameLayout(activity);
        overlay.setTag(TAG_CONNECTION_ERROR);
        overlay.setClickable(true);
        overlay.setBackgroundColor(ContextCompat.getColor(activity, R.color.backgroundColor));
        overlay.setPadding(dp(activity, 24), dp(activity, 24), dp(activity, 24), dp(activity, 24));

        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(activity, 24), dp(activity, 24), dp(activity, 24), dp(activity, 24));
        card.setBackground(createCardBackground(activity));

        TextView title = new TextView(activity);
        title.setText("No se puede conectar con el servidor");
        title.setTextColor(ContextCompat.getColor(activity, R.color.textColorPrimary));
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        TextView message = new TextView(activity);
        message.setText("Comprueba que la API está encendida y que el móvil está conectado a la misma red WiFi.");
        message.setTextColor(ContextCompat.getColor(activity, R.color.textColorSecondary));
        message.setTextSize(14);
        message.setGravity(Gravity.CENTER);
        message.setLineSpacing(dp(activity, 2), 1f);

        Button retry = new Button(activity);
        retry.setText("Reintentar");
        retry.setTextColor(Color.WHITE);
        retry.setAllCaps(false);
        retry.setBackgroundTintList(ContextCompat.getColorStateList(activity, R.color.primaryColor));
        retry.setOnClickListener(v -> {
            hide(activity);
            if (retryAction != null) {
                retryAction.run();
            }
        });

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        card.addView(title, titleParams);

        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        messageParams.topMargin = dp(activity, 10);
        card.addView(message, messageParams);

        LinearLayout.LayoutParams retryParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(activity, 48)
        );
        retryParams.topMargin = dp(activity, 18);
        card.addView(retry, retryParams);

        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.gravity = Gravity.CENTER;
        overlay.addView(card, cardParams);

        content.addView(overlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    static void hide(Activity activity) {
        if (activity == null) {
            return;
        }

        FrameLayout content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }

        for (int i = content.getChildCount() - 1; i >= 0; i--) {
            View child = content.getChildAt(i);
            if (TAG_CONNECTION_ERROR.equals(child.getTag())) {
                content.removeViewAt(i);
            }
        }
    }

    private static GradientDrawable createCardBackground(Activity activity) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(activity, 8));
        background.setStroke(dp(activity, 1), ContextCompat.getColor(activity, R.color.surfaceTintColor));
        return background;
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
