package com.example.academicapp.views;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.academicapp.R;

public class TutorialOverlayView extends FrameLayout {

    public interface Action {
        void run();
    }

    private final Paint scrimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF targetRect = new RectF();
    private float pulse = 0f;
    private ValueAnimator animator;

    public TutorialOverlayView(Activity activity, View target, String title, String message,
                               int index, int total, Action onBack, Action onNext, Action onSkip) {
        super(activity);
        setWillNotDraw(false);
        setClickable(true);
        setFocusable(true);

        scrimPaint.setColor(Color.parseColor("#990B1220"));
        glowPaint.setStyle(Paint.Style.FILL);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(dp(2));
        strokePaint.setColor(Color.WHITE);
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeWidth(dp(4));
        arrowPaint.setStrokeCap(Paint.Cap.ROUND);
        arrowPaint.setStrokeJoin(Paint.Join.ROUND);
        arrowPaint.setColor(ContextCompat.getColor(activity, R.color.secondaryColor));

        addView(crearBurbuja(activity, title, message, index, total, onBack, onNext, onSkip));
        post(() -> {
            calcularTarget(target);
            ubicarBurbuja();
            startAnimation();
        });
    }

    public static void show(Activity activity, View target, String title, String message,
                            int index, int total, Action onBack, Action onNext, Action onSkip) {
        ViewGroup decor = activity.findViewById(android.R.id.content);
        TutorialOverlayView overlay = new TutorialOverlayView(activity, target, title, message, index, total, onBack, onNext, onSkip);
        decor.addView(overlay, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void dismiss() {
        if (animator != null) {
            animator.cancel();
        }
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), scrimPaint);

        if (!targetRect.isEmpty()) {
            float radius = Math.max(dp(18), Math.min(targetRect.width(), targetRect.height()) / 2f);
            glowPaint.setColor(Color.argb(62 + (int) (pulse * 42), 37, 99, 235));
            canvas.drawRoundRect(expand(targetRect, dp(16 + (int) (pulse * 10))), radius, radius, glowPaint);

            glowPaint.setColor(Color.argb(42 + (int) (pulse * 38), 96, 165, 250));
            canvas.drawRoundRect(expand(targetRect, dp(7)), radius, radius, glowPaint);
            canvas.drawRoundRect(expand(targetRect, dp(6)), radius, radius, strokePaint);

            dibujarFlecha(canvas);
        }
    }

    private void dibujarFlecha(Canvas canvas) {
        View bubble = getChildAt(0);
        if (bubble == null) {
            return;
        }

        float startX = targetRect.centerX();
        float startY = targetRect.bottom + dp(10);
        float endX = bubble.getX() + bubble.getWidth() / 2f;
        float endY = bubble.getY() + dp(12);

        if (targetRect.centerY() > getHeight() / 2f) {
            startY = targetRect.top - dp(10);
            endY = bubble.getY() + bubble.getHeight() - dp(12);
        }

        Path path = new Path();
        path.moveTo(startX, startY);
        path.cubicTo(startX, (startY + endY) / 2f, endX, (startY + endY) / 2f, endX, endY);
        canvas.drawPath(path, arrowPaint);

        float arrowSize = dp(9 + (int) (pulse * 3));
        canvas.drawLine(startX, startY, startX - arrowSize, startY + (startY < endY ? arrowSize : -arrowSize), arrowPaint);
        canvas.drawLine(startX, startY, startX + arrowSize, startY + (startY < endY ? arrowSize : -arrowSize), arrowPaint);
    }

    private RectF expand(RectF rect, int amount) {
        return new RectF(rect.left - amount, rect.top - amount, rect.right + amount, rect.bottom + amount);
    }

    private LinearLayout crearBurbuja(Activity activity, String title, String message,
                                      int index, int total, Action onBack, Action onNext, Action onSkip) {
        LinearLayout bubble = new LinearLayout(activity);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setPadding(dp(20), dp(18), dp(20), dp(16));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(28));
        background.setStroke(dp(1), ContextCompat.getColor(activity, R.color.surfaceTintColor));
        bubble.setBackground(background);
        bubble.setElevation(dp(12));

        TextView counter = new TextView(activity);
        counter.setText((index + 1) + " de " + total);
        counter.setTextColor(ContextCompat.getColor(activity, R.color.primaryColor));
        counter.setTextSize(12);
        counter.setTypeface(null, Typeface.BOLD);
        counter.setGravity(Gravity.CENTER);
        bubble.addView(counter);

        TextView titleView = new TextView(activity);
        titleView.setText(title);
        titleView.setTextColor(ContextCompat.getColor(activity, R.color.textColorPrimary));
        titleView.setTextSize(21);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, dp(8), 0, dp(8));
        bubble.addView(titleView);

        TextView messageView = new TextView(activity);
        messageView.setText(message);
        messageView.setTextColor(ContextCompat.getColor(activity, R.color.textColorSecondary));
        messageView.setTextSize(15);
        messageView.setGravity(Gravity.CENTER);
        messageView.setLineSpacing(0, 1.15f);
        bubble.addView(messageView);

        bubble.addView(crearDots(activity, index, total));
        bubble.addView(crearAcciones(activity, onBack, onNext, onSkip, index, total));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(dp(18), 0, dp(18), dp(20));
        bubble.setLayoutParams(params);
        return bubble;
    }

    private LinearLayout crearDots(Activity activity, int index, int total) {
        LinearLayout dots = new LinearLayout(activity);
        dots.setGravity(Gravity.CENTER);
        dots.setPadding(0, dp(14), 0, dp(2));
        for (int i = 0; i < total; i++) {
            View dot = new View(activity);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(i == index ? ContextCompat.getColor(activity, R.color.primaryColor) : ContextCompat.getColor(activity, R.color.surfaceTintColor));
            dot.setBackground(bg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(i == index ? 18 : 8), dp(8));
            lp.setMargins(dp(4), 0, dp(4), 0);
            dots.addView(dot, lp);
        }
        return dots;
    }

    private LinearLayout crearAcciones(Activity activity, Action onBack, Action onNext, Action onSkip, int index, int total) {
        LinearLayout actions = new LinearLayout(activity);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(14), 0, 0);

        Button skip = crearBoton(activity, "Saltar", false);
        skip.setOnClickListener(v -> {
            dismiss();
            onSkip.run();
        });
        actions.addView(skip, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        Button back = crearBoton(activity, "Atrás", false);
        back.setEnabled(index > 0);
        back.setOnClickListener(v -> {
            dismiss();
            onBack.run();
        });
        actions.addView(back, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        Button next = crearBoton(activity, index == total - 1 ? "Terminar" : "Siguiente", true);
        next.setOnClickListener(v -> {
            dismiss();
            onNext.run();
        });
        actions.addView(next, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        return actions;
    }

    private Button crearBoton(Activity activity, String text, boolean primary) {
        Button button = new Button(activity);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(primary ? Color.WHITE : ContextCompat.getColor(activity, R.color.textColorPrimary));
        if (primary) {
            button.setBackgroundColor(ContextCompat.getColor(activity, R.color.primaryColor));
        }
        return button;
    }

    private void calcularTarget(View target) {
        if (target == null || target.getWidth() == 0 || target.getHeight() == 0) {
            targetRect.set(getWidth() / 2f - dp(42), getHeight() / 2f - dp(42), getWidth() / 2f + dp(42), getHeight() / 2f + dp(42));
            return;
        }

        int[] targetLocation = new int[2];
        int[] selfLocation = new int[2];
        target.getLocationOnScreen(targetLocation);
        getLocationOnScreen(selfLocation);
        float left = targetLocation[0] - selfLocation[0];
        float top = targetLocation[1] - selfLocation[1];
        targetRect.set(left, top, left + target.getWidth(), top + target.getHeight());
    }

    private void ubicarBurbuja() {
        View bubble = getChildAt(0);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bubble.getLayoutParams();
        params.gravity = targetRect.centerY() < getHeight() / 2f ? Gravity.BOTTOM : Gravity.TOP;
        params.topMargin = dp(24);
        params.bottomMargin = dp(24);
        bubble.setLayoutParams(params);
        bubble.setAlpha(0f);
        bubble.setTranslationY(dp(16));
        bubble.animate().alpha(1f).translationY(0).setDuration(260).start();
    }

    private void startAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1050);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            pulse = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
