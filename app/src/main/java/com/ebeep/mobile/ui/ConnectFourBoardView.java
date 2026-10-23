package com.ebeep.mobile.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.ebeep.mobile.R;
import com.ebeep.mobile.game.ConnectFourGame;

/**
 * Custom view: draws the Connect 4 board like the beeper's screen (thin black grid,
 * black discs and white discs) and tells the screen which column was tapped.
 */
public class ConnectFourBoardView extends View {

    public interface OnColumnTapListener {
        void onColumnTap(int col);
    }

    private ConnectFourGame game;
    private OnColumnTapListener listener;

    private final Paint boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Created once here, because onDraw runs many times
    private final RectF boardRect = new RectF();
    private final Path markerPath = new Path();
    private final float borderWidth;
    private final float cornerRadius;

    public ConnectFourBoardView(Context context) {
        this(context, null);
    }

    public ConnectFourBoardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConnectFourBoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = getResources().getDisplayMetrics().density;
        borderWidth = 2 * density;
        cornerRadius = 12 * density;
        int outline = ContextCompat.getColor(context, R.color.outline);
        int ink = ContextCompat.getColor(context, R.color.disc_filled);

        boardPaint.setColor(ContextCompat.getColor(context, R.color.surface));

        borderPaint.setColor(outline);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);

        gridPaint.setColor(outline);
        gridPaint.setStrokeWidth(density);

        blackPaint.setColor(ContextCompat.getColor(context, R.color.disc_filled));
        whitePaint.setColor(ContextCompat.getColor(context, R.color.disc_hollow));

        // The ink ring that makes a white disc visible
        ringPaint.setColor(ink);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(borderWidth);

        markerPaint.setColor(ink);

        if (isInEditMode()) {
            ConnectFourGame mockGame = new ConnectFourGame(true);
            mockGame.playMine(0);
            mockGame.playOpponent("1");
            mockGame.playMine(0);
            mockGame.playOpponent("2");
            setGame(mockGame);
        }
    }

    public void setGame(ConnectFourGame game) {
        this.game = game;
        invalidate();
    }

    public void setOnColumnTapListener(OnColumnTapListener listener) {
        this.listener = listener;
    }

    /** The strip above the board where the little triangle is drawn. */
    private float markerHeight() {
        return getWidth() / (float) ConnectFourGame.COLS * 0.4f;
    }

    /** The board is 7 columns by 6 rows with a strip above it, so the height follows from the width. */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        float cellSize = width / (float) ConnectFourGame.COLS;
        float height = cellSize * ConnectFourGame.ROWS + cellSize * 0.4f;
        setMeasuredDimension(width, (int) height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (game == null) {
            return;
        }

        float cellSize = getWidth() / (float) ConnectFourGame.COLS;
        float top = markerHeight();
        float half = borderWidth / 2;

        // White board with a rounded black border
        boardRect.set(half, top + half, getWidth() - half, getHeight() - half);
        canvas.drawRoundRect(boardRect, cornerRadius, cornerRadius, boardPaint);
        drawGridLines(canvas, cellSize, top);
        canvas.drawRoundRect(boardRect, cornerRadius, cornerRadius, borderPaint);

        drawDiscs(canvas, cellSize, top);
        drawMarker(canvas, cellSize);
    }

    /** Thin lines between the cells only. */
    private void drawGridLines(Canvas canvas, float cellSize, float top) {
        for (int col = 1; col < ConnectFourGame.COLS; col++) {
            canvas.drawLine(col * cellSize, top, col * cellSize, getHeight(), gridPaint);
        }
        for (int row = 1; row < ConnectFourGame.ROWS; row++) {
            canvas.drawLine(0, top + row * cellSize, getWidth(), top + row * cellSize, gridPaint);
        }
    }

    /** 'R' is a black disc, 'Y' is a white disc with a black ring, empty cells stay empty. */
    private void drawDiscs(Canvas canvas, float cellSize, float top) {
        float radius = cellSize * 0.34f;
        for (int row = 0; row < ConnectFourGame.ROWS; row++) {
            for (int col = 0; col < ConnectFourGame.COLS; col++) {
                char cell = game.getCell(row, col);
                float centerX = col * cellSize + cellSize / 2;
                float centerY = top + row * cellSize + cellSize / 2;

                if (cell == 'R') {
                    canvas.drawCircle(centerX, centerY, radius, blackPaint);
                } else if (cell == 'Y') {
                    canvas.drawCircle(centerX, centerY, radius, whitePaint);
                    canvas.drawCircle(centerX, centerY, radius, ringPaint);
                }
            }
        }
    }

    /** A small triangle pointing down at the column of the last disc, like on the beeper. */
    private void drawMarker(Canvas canvas, float cellSize) {
        if (game.getLastCol() < 0) {
            return;
        }
        float centerX = game.getLastCol() * cellSize + cellSize / 2;
        float size = markerHeight();

        markerPath.reset();
        markerPath.moveTo(centerX - size * 0.4f, size * 0.15f);
        markerPath.lineTo(centerX + size * 0.4f, size * 0.15f);
        markerPath.lineTo(centerX, size * 0.85f);
        markerPath.close();
        canvas.drawPath(markerPath, markerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && listener != null) {
            int col = (int) (event.getX() / (getWidth() / (float) ConnectFourGame.COLS));
            if (col >= 0 && col < ConnectFourGame.COLS) {
                listener.onColumnTap(col);
            }
            performClick();
        }
        return true;
    }

    // Required so accessibility services know the view can be clicked
    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
