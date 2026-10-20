package com.ebeep.mobile.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.ebeep.mobile.R;
import com.ebeep.mobile.game.ConnectFourGame;

/** Custom view: draws the Connect 4 board and tells the screen which column was tapped. */
public class ConnectFourBoardView extends View {

    public interface OnColumnTapListener {
        void onColumnTap(int col);
    }

    private ConnectFourGame game;
    private OnColumnTapListener listener;

    private final Paint boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint yellowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ConnectFourBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        boardPaint.setColor(ContextCompat.getColor(context, R.color.board));
        emptyPaint.setColor(ContextCompat.getColor(context, R.color.board_hole));
        redPaint.setColor(ContextCompat.getColor(context, R.color.disc_red));
        yellowPaint.setColor(ContextCompat.getColor(context, R.color.disc_yellow));

        // A ring around the disc that was dropped last
        ringPaint.setColor(ContextCompat.getColor(context, R.color.text_primary));
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(6f);
    }

    public void setGame(ConnectFourGame game) {
        this.game = game;
        invalidate();
    }

    public void setOnColumnTapListener(OnColumnTapListener listener) {
        this.listener = listener;
    }

    /** The board is 7 columns by 6 rows, so the height follows from the width. */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, width * ConnectFourGame.ROWS / ConnectFourGame.COLS);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (game == null) {
            return;
        }

        float cellSize = getWidth() / (float) ConnectFourGame.COLS;
        float radius = cellSize * 0.4f;
        canvas.drawRect(0, 0, getWidth(), getHeight(), boardPaint);

        for (int row = 0; row < ConnectFourGame.ROWS; row++) {
            for (int col = 0; col < ConnectFourGame.COLS; col++) {
                float centerX = col * cellSize + cellSize / 2;
                float centerY = row * cellSize + cellSize / 2;
                canvas.drawCircle(centerX, centerY, radius, paintFor(game.getCell(row, col)));

                if (row == game.getLastRow() && col == game.getLastCol()) {
                    canvas.drawCircle(centerX, centerY, radius, ringPaint);
                }
            }
        }
    }

    private Paint paintFor(char cell) {
        if (cell == 'R') {
            return redPaint;
        }
        return cell == 'Y' ? yellowPaint : emptyPaint;
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
