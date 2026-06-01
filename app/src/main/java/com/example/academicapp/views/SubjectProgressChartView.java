package com.example.academicapp.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Vista personalizada Android que encapsula dibujo e interaccion especificos de AcademicApp.
 */
public class SubjectProgressChartView extends View {

    private static final int PERCENTILE_COLOR = Color.rgb(124, 58, 237);

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<UnitValue> units = new ArrayList<>();

    private double media = -1;
    private double percentil = -1;
    private int alumnosComparados = 0;
    private float animationProgress = 1f;
    private float percentileProgress = 1f;
    private ValueAnimator mainAnimator;
    private ValueAnimator percentileAnimator;

    public SubjectProgressChartView(Context context) {
        super(context);
        init();
    }

    public SubjectProgressChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SubjectProgressChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setMinimumHeight(dp(190));
    }

    public void setData(double media, List<UnitValue> units) {
        this.media = media;
        this.units.clear();
        if (units != null) {
            this.units.addAll(units);
        }
        requestLayout();
        startIntroAnimation();
        if (percentil >= 0) {
            startPercentileAnimation();
        }
    }

    public void setPercentile(double percentil, int alumnosComparados) {
        this.percentil = percentil;
        this.alumnosComparados = Math.max(0, alumnosComparados);
        startPercentileAnimation();
    }

    private void startIntroAnimation() {
        if (mainAnimator != null) {
            mainAnimator.cancel();
        }
        animationProgress = 0f;
        mainAnimator = ValueAnimator.ofFloat(0f, 1f);
        mainAnimator.setDuration(2000);
        mainAnimator.addUpdateListener(animation -> {
            animationProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        mainAnimator.start();
    }

    private void startPercentileAnimation() {
        if (percentileAnimator != null) {
            percentileAnimator.cancel();
        }
        percentileProgress = 0f;
        percentileAnimator = ValueAnimator.ofFloat(0f, 1f);
        percentileAnimator.setDuration(2000);
        percentileAnimator.addUpdateListener(animation -> {
            percentileProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        percentileAnimator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = dp(184) + Math.max(1, units.size()) * dp(38);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int padding = dp(4);
        int chartWidth = width - padding * 2;
        int y = dp(12);

        drawMedia(canvas, padding, y, chartWidth);
        y += dp(56);

        drawPercentile(canvas, padding, y, chartWidth);
        y += dp(98);

        drawUnitBars(canvas, padding, y, chartWidth);
    }

    private void drawMedia(Canvas canvas, int x, int y, int width) {
        drawLabel(canvas, "Media de la asignatura", x, y);
        int barTop = y + dp(18);
        drawValueBar(canvas, x, barTop, width, media, 10, getGradeColor(media));

        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setTextSize(sp(12));
        textPaint.setFakeBoldText(true);
        textPaint.setColor(Color.parseColor("#0F172A"));
        canvas.drawText(media >= 0 ? format(media) : "-", x + width, y, textPaint);
    }

    private void drawPercentile(Canvas canvas, int x, int y, int width) {
        drawLabel(canvas, "Comparativa de la asignatura", x, y);
        int barTop = y + dp(18);
        drawValueBar(canvas, x, barTop, width, percentil, 100, getPercentileColor(percentil));

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(sp(11));
        textPaint.setFakeBoldText(false);
        textPaint.setColor(Color.parseColor("#64748B"));
        String detail = percentil >= 0 ? "Percentil " + format(percentil) : "Percentil no disponible";
        if (alumnosComparados > 0) {
            detail += " · " + alumnosComparados + " alumnos";
        }
        canvas.drawText(detail, x, barTop + dp(32), textPaint);

        textPaint.setTextSize(sp(10));
        textPaint.setColor(Color.parseColor("#475569"));
        canvas.drawText(getPercentileExplanation(), x, barTop + dp(50), textPaint);
    }

    private void drawUnitBars(Canvas canvas, int x, int y, int width) {
        drawLabel(canvas, "Unidades didácticas", x, y);
        int currentY = y + dp(26);

        if (units.isEmpty()) {
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTextSize(sp(12));
            textPaint.setFakeBoldText(false);
            textPaint.setColor(Color.parseColor("#64748B"));
            canvas.drawText("Sin unidades disponibles.", x, currentY, textPaint);
            return;
        }

        for (UnitValue unit : units) {
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTextSize(sp(11));
            textPaint.setFakeBoldText(false);
            textPaint.setColor(Color.parseColor("#0F172A"));
            canvas.drawText(ellipsize(unit.name, 28), x, currentY, textPaint);

            textPaint.setTextAlign(Paint.Align.RIGHT);
            textPaint.setFakeBoldText(true);
            canvas.drawText(unit.value >= 0 ? format(unit.value) : "-", x + width, currentY, textPaint);

            drawValueBar(canvas, x, currentY + dp(8), width, unit.value, 10, getGradeColor(unit.value));
            currentY += dp(38);
        }
    }

    private void drawValueBar(Canvas canvas, int x, int y, int width, double value, double max, int color) {
        int height = dp(10);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#E2E8F0"));
        canvas.drawRoundRect(new RectF(x, y, x + width, y + height), dp(5), dp(5), paint);

        if (value < 0) {
            return;
        }

        float progress = max == 100 ? getPercentileMotionProgress() : animationProgress;
        float filled = (float) (width * clamp(value, 0, max) / max * progress);
        filled = Math.max(0, Math.min(width, filled));
        paint.setColor(color);
        canvas.drawRoundRect(new RectF(x, y, x + filled, y + height), dp(5), dp(5), paint);
    }

    private void drawLabel(Canvas canvas, String label, int x, int y) {
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(sp(13));
        textPaint.setFakeBoldText(true);
        textPaint.setColor(Color.parseColor("#0F172A"));
        canvas.drawText(label, x, y, textPaint);
    }

    private int getGradeColor(double value) {
        if (value < 0) {
            return Color.parseColor("#94A3B8");
        }
        if (value < 5) {
            return Color.parseColor("#DC2626");
        }
        if (value < 7) {
            return Color.parseColor("#FACC15");
        }
        if (value < 9) {
            return Color.parseColor("#2563EB");
        }
        return Color.parseColor("#16A34A");
    }

    private int getPercentileColor(double value) {
        if (value < 0) {
            return Color.parseColor("#94A3B8");
        }
        return PERCENTILE_COLOR;
    }

    private String getPercentileExplanation() {
        if (percentil < 0) {
            return "Se calculará cuando haya suficientes notas comparables.";
        }
        int rounded = Math.round((float) clamp(percentil, 0, 100));
        return "Tu nota media está por encima del " + rounded + "% de tus compañeros.";
    }

    private float getPercentileMotionProgress() {
        float p = percentileProgress;
        if (p < 0.38f) {
            return lerp(0f, 1.12f, easeOut(p / 0.38f));
        }
        if (p < 0.68f) {
            return lerp(1.12f, 0.92f, easeInOut((p - 0.38f) / 0.30f));
        }
        return lerp(0.92f, 1f, easeOut((p - 0.68f) / 0.32f));
    }

    private float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    private float easeOut(float value) {
        value = Math.max(0f, Math.min(1f, value));
        return 1f - (1f - value) * (1f - value);
    }

    private float easeInOut(float value) {
        value = Math.max(0f, Math.min(1f, value));
        return value < 0.5f ? 2f * value * value : 1f - (float) Math.pow(-2f * value + 2f, 2) / 2f;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String ellipsize(String value, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 1) + "...";
    }

    private String format(double value) {
        return String.format(Locale.getDefault(), "%.2f", value);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private float sp(int value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }

    public static class UnitValue {
        public final String name;
        public final double value;

        public UnitValue(String name, double value) {
            this.name = name;
            this.value = value;
        }
    }
}
