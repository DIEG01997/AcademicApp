package com.example.academicapp.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
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
public class ProgressChartView extends View {

    private static final int PERCENTILE_COLOR = Color.rgb(124, 58, 237);

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<TermValue> terms = new ArrayList<>();

    private double mediaGlobal = -1;
    private double percentilGlobal = -1;
    private int alumnosComparados = 0;
    private int aprobadas = 0;
    private int suspensas = 0;
    private float animationProgress = 1f;
    private float percentileProgress = 1f;
    private ValueAnimator mainAnimator;
    private ValueAnimator percentileAnimator;

    public ProgressChartView(Context context) {
        super(context);
        init();
    }

    public ProgressChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPaint.setColor(Color.parseColor("#0F172A"));
        textPaint.setTextSize(sp(13));
        setMinimumHeight(dp(452));
    }

    public void setData(double mediaGlobal, int aprobadas, int suspensas,
                        double percentilGlobal, int alumnosComparados, List<TermValue> terms) {
        this.mediaGlobal = mediaGlobal;
        this.aprobadas = Math.max(0, aprobadas);
        this.suspensas = Math.max(0, suspensas);
        this.percentilGlobal = percentilGlobal;
        this.alumnosComparados = Math.max(0, alumnosComparados);
        this.terms.clear();
        if (terms != null) {
            this.terms.addAll(terms);
        }
        startIntroAnimation();
        if (percentilGlobal >= 0) {
            startPercentileAnimation();
        }
    }

    public void setPercentile(double percentilGlobal, int alumnosComparados) {
        this.percentilGlobal = percentilGlobal;
        this.alumnosComparados = Math.max(0, alumnosComparados);
        startPercentileAnimation();
    }

    public void clearData() {
        mediaGlobal = -1;
        percentilGlobal = -1;
        alumnosComparados = 0;
        aprobadas = 0;
        suspensas = 0;
        terms.clear();
        invalidate();
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int padding = dp(18);
        int y = padding;

        drawHeader(canvas, padding, y, width - padding * 2);
        y += dp(126);

        drawPercentile(canvas, padding, y, width - padding * 2);
        y += dp(104);

        drawPassFailBar(canvas, padding, y, width - padding * 2);
        y += dp(78);

        drawTimeline(canvas, padding, y, width - padding * 2);
    }

    private void drawHeader(Canvas canvas, int x, int y, int width) {
        int size = dp(96);
        RectF arcBounds = new RectF(x, y + dp(8), x + size, y + dp(8) + size);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(11));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.parseColor("#E2E8F0"));
        canvas.drawArc(arcBounds, 135, 270, false, paint);

        if (mediaGlobal >= 0) {
            paint.setColor(getGradeColor(mediaGlobal));
            canvas.drawArc(arcBounds, 135, (float) (270 * clamp(mediaGlobal, 0, 10) / 10 * animationProgress), false, paint);
        }

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.parseColor("#0F172A"));
        textPaint.setTextSize(sp(24));
        textPaint.setFakeBoldText(true);
        double animatedMedia = mediaGlobal >= 0 ? mediaGlobal * animationProgress : -1;
        canvas.drawText(animatedMedia >= 0 ? format(animatedMedia) : "-", x + size / 2f, y + dp(66), textPaint);

        textPaint.setTextSize(sp(11));
        textPaint.setFakeBoldText(false);
        textPaint.setColor(Color.parseColor("#64748B"));
        canvas.drawText("media", x + size / 2f, y + dp(84), textPaint);

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(Color.parseColor("#0F172A"));
        textPaint.setTextSize(sp(17));
        textPaint.setFakeBoldText(true);
        canvas.drawText("Resumen visual global", x + size + dp(22), y + dp(42), textPaint);

        textPaint.setTextSize(sp(12));
        textPaint.setFakeBoldText(false);
        textPaint.setColor(Color.parseColor("#64748B"));
        canvas.drawText("Rendimiento, comparativa y evolución", x + size + dp(22), y + dp(64), textPaint);

        int legendY = y + dp(88);
        String active = getQualitativeLabel(mediaGlobal);
        int legendX = x + size + dp(22);
        drawLegendDot(canvas, legendX, legendY, Color.parseColor("#DC2626"), "riesgo", "riesgo".equals(active));
        drawLegendDot(canvas, legendX + dp(72), legendY, Color.parseColor("#FACC15"), "medio", "medio".equals(active));
        drawLegendDot(canvas, legendX, legendY + dp(22), Color.parseColor("#2563EB"), "solido", "solido".equals(active));
        drawLegendDot(canvas, legendX + dp(72), legendY + dp(22), Color.parseColor("#16A34A"), "excelente", "excelente".equals(active));
    }

    private void drawPercentile(Canvas canvas, int x, int y, int width) {
        drawSectionLabel(canvas, "Comparativa con compañeros", x, y);

        int barTop = y + dp(22);
        int barHeight = dp(16);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#E2E8F0"));
        canvas.drawRoundRect(new RectF(x, barTop, x + width, barTop + barHeight), dp(8), dp(8), paint);

        if (percentilGlobal >= 0) {
            float motion = getPercentileMotionProgress();
            float filled = (float) (width * clamp(percentilGlobal, 0, 100) / 100 * motion);
            filled = Math.max(0, Math.min(width, filled));
            paint.setColor(getPercentileColor(percentilGlobal));
            canvas.drawRoundRect(new RectF(x, barTop, x + filled, barTop + barHeight), dp(8), dp(8), paint);
            paint.setColor(Color.parseColor("#0F172A"));
            canvas.drawCircle(x + filled, barTop + barHeight / 2f, dp(6), paint);
        }

        textPaint.setTextSize(sp(11));
        textPaint.setFakeBoldText(false);
        textPaint.setColor(Color.parseColor("#64748B"));
        textPaint.setTextAlign(Paint.Align.LEFT);
        String detail = percentilGlobal >= 0
                ? "Percentil " + format(percentilGlobal) + " de 100"
                : "Percentil pendiente de cálculo";
        if (alumnosComparados > 0) {
            detail += " · " + alumnosComparados + " alumnos";
        }
        canvas.drawText(detail, x, barTop + dp(38), textPaint);

        textPaint.setTextSize(sp(10));
        textPaint.setColor(Color.parseColor("#475569"));
        canvas.drawText(getPercentileExplanation(percentilGlobal), x, barTop + dp(56), textPaint);
    }

    private void drawPassFailBar(Canvas canvas, int x, int y, int width) {
        drawSectionLabel(canvas, "Balance de asignaturas", x, y);

        int total = aprobadas + suspensas;
        int barTop = y + dp(22);
        int barHeight = dp(16);
        float approvedWidth = total == 0 ? 0 : width * (aprobadas / (float) total) * animationProgress;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#E2E8F0"));
        canvas.drawRoundRect(new RectF(x, barTop, x + width, barTop + barHeight), dp(8), dp(8), paint);

        paint.setColor(Color.parseColor("#16A34A"));
        canvas.drawRoundRect(new RectF(x, barTop, x + approvedWidth, barTop + barHeight), dp(8), dp(8), paint);

        if (suspensas > 0) {
            paint.setColor(Color.parseColor("#DC2626"));
            float riskEnd = width * animationProgress;
            canvas.drawRoundRect(new RectF(x + approvedWidth, barTop, x + riskEnd, barTop + barHeight), dp(8), dp(8), paint);
        }

        textPaint.setTextSize(sp(11));
        textPaint.setFakeBoldText(false);
        textPaint.setColor(Color.parseColor("#64748B"));
        canvas.drawText(aprobadas + " aprobadas · " + suspensas + " en riesgo", x, barTop + dp(38), textPaint);
    }

    private void drawTimeline(Canvas canvas, int x, int y, int width) {
        drawSectionLabel(canvas, "Evolucion media-trimestre", x, y);

        int chartTop = y + dp(28);
        int chartHeight = dp(58);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.parseColor("#E2E8F0"));
        canvas.drawLine(x, chartTop + chartHeight, x + width, chartTop + chartHeight, paint);
        canvas.drawLine(x, chartTop, x, chartTop + chartHeight, paint);
        paint.setStrokeWidth(dp(2));
        paint.setColor(Color.parseColor("#94A3B8"));
        canvas.drawLine(x, chartTop + chartHeight, x + width, chartTop + chartHeight, paint);

        List<TermValue> validTerms = new ArrayList<>();
        for (TermValue term : terms) {
            if (term.value >= 0) {
                validTerms.add(term);
            }
        }

        if (validTerms.isEmpty()) {
            textPaint.setTextSize(sp(12));
            textPaint.setFakeBoldText(false);
            textPaint.setColor(Color.parseColor("#64748B"));
            canvas.drawText("Aún no hay datos suficientes por trimestre.", x, chartTop + dp(30), textPaint);
            return;
        }

        Path path = new Path();
        List<Float> pointsX = new ArrayList<>();
        List<Float> pointsY = new ArrayList<>();
        for (int i = 0; i < validTerms.size(); i++) {
            float px = validTerms.size() == 1 ? x + width / 2f : x + (width * i / (float) (validTerms.size() - 1));
            float py = chartTop + chartHeight - (float) (chartHeight * clamp(validTerms.get(i).value, 0, 10) / 10);
            pointsX.add(px);
            pointsY.add(py);
            if (i == 0) {
                path.moveTo(px, py);
            } else {
                path.lineTo(px, py);
            }
        }

        int visiblePoints = Math.min(validTerms.size(), (int) Math.ceil(validTerms.size() * Math.min(1f, animationProgress * 1.4f)));
        for (int i = 0; i < visiblePoints; i++) {
            float px = pointsX.get(i);
            float py = pointsY.get(i);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getGradeColor(validTerms.get(i).value));
            canvas.drawCircle(px, py, dp(5) * Math.min(1f, animationProgress * 2f), paint);

            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(sp(10));
            textPaint.setFakeBoldText(true);
            textPaint.setColor(Color.parseColor("#0F172A"));
            canvas.drawText(format(validTerms.get(i).value), px, py - dp(10), textPaint);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.parseColor("#CBD5E1"));
        for (int i = 0; i < validTerms.size(); i++) {
            float px = pointsX.get(i);
            canvas.drawLine(px, chartTop + chartHeight, px, chartTop + chartHeight + dp(5), paint);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(sp(10));
            textPaint.setFakeBoldText(false);
            textPaint.setColor(Color.parseColor("#64748B"));
            canvas.drawText(validTerms.get(i).label, px, chartTop + chartHeight + dp(18), textPaint);
        }

        Path visiblePath = new Path();
        PathMeasure measure = new PathMeasure(path, false);
        measure.getSegment(0, measure.getLength() * animationProgress, visiblePath, true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(getGradeColor(mediaGlobal >= 0 ? mediaGlobal : 8));
        canvas.drawPath(visiblePath, paint);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawSectionLabel(Canvas canvas, String label, int x, int y) {
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(sp(14));
        textPaint.setFakeBoldText(true);
        textPaint.setColor(Color.parseColor("#0F172A"));
        canvas.drawText(label, x, y, textPaint);
    }

    private void drawLegendDot(Canvas canvas, int x, int y, int color, String label, boolean active) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        float boost = active && animationProgress >= 1f ? 1.35f : 1f;
        canvas.drawCircle(x, y - dp(4), dp(4) * boost, paint);

        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(active && animationProgress >= 1f ? sp(12) : sp(10));
        textPaint.setColor(Color.parseColor("#64748B"));
        textPaint.setFakeBoldText(active && animationProgress >= 1f);
        canvas.drawText(label, x + dp(8), y, textPaint);
    }

    private String getQualitativeLabel(double value) {
        if (value < 0) {
            return "";
        }
        if (value < 5) {
            return "riesgo";
        }
        if (value < 7) {
            return "medio";
        }
        if (value < 9) {
            return "solido";
        }
        return "excelente";
    }

    private String getPercentileExplanation(double value) {
        if (value < 0) {
            return "Se calculará cuando haya suficientes notas comparables.";
        }
        int rounded = Math.round((float) clamp(value, 0, 100));
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

    private int getGradeColor(double value) {
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
        return PERCENTILE_COLOR;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
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

    public static class TermValue {
        public final String label;
        public final double value;

        public TermValue(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }
}
