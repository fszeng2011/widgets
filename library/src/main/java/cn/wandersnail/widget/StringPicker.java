package cn.wandersnail.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 文本滚动选择器
 * <p>
 * date: 2019/8/22 12:41
 * author: zengfansheng
 */
public class StringPicker extends View {
    private static final float SPEED = 2f;
    private List<String> dataList = new ArrayList<>();
    /** 选中的位置，这个位置是dataList的中心位置，一直不变  */
    private int currentSelected;
    private Paint paint;
    private float selectedTextSize = -1f;
    private float unselectedTextSize = -1f;
    private float textSpace = -1f;
    private int selectedTextColor = -0xafafb0;
    private int unselectedTextColor = 0x11505050;
    private float lastDownY;
    /** 滑动的距离  */
    private float moveLen = 0f;
    private boolean isInit;
    private OnSelectListener selectListener;
    private Timer timer;
    private MyTimerTask task;
    private GestureDetector gestureDetector;
    private Scroller scroller;
    private boolean isFling;
    private boolean enableLoop;
    private boolean isEdge;
    private MyHandler updateHandler = new MyHandler(this);

    public StringPicker(Context context) {
        super(context);
        init(context);
    }

    public StringPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StringPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        timer = new Timer();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                isFling = true;
                scroller.fling((int) e2.getX(), (int) e2.getY(), 0, (int) velocityY, 0, 0, -2000, 2000);
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
        scroller = new Scroller(context);
    }

    private class MyTimerTask extends TimerTask {        
        @Override
        public void run() {
            updateHandler.sendMessage(updateHandler.obtainMessage());
        }
    }
    
    public interface OnSelectListener {
        void onSelect(@NonNull String text);
    }
    
    private static class MyHandler extends Handler {
        private WeakReference<StringPicker> weakRef;

        MyHandler(StringPicker picker) {
            weakRef = new WeakReference<>(picker);
        }

        @Override
        public void handleMessage(Message msg) {
            StringPicker picker = weakRef.get();
            if (picker != null) {
                if (Math.abs(picker.moveLen) < SPEED) {
                    picker.moveLen = 0f;
                    if (picker.task != null) {
                        picker.task.cancel();
                        picker.task = null;
                        picker.performSelect();
                    }
                } else {
                    // 这里mMoveLen / Math.abs(mMoveLen)是为了保有mMoveLen的正负号，以实现上滚或下滚
                    picker.moveLen = picker.moveLen - picker.moveLen / Math.abs(picker.moveLen) * SPEED;
                }
                picker.invalidate();
            }
        }
    }
    
    private void doUp() {
        // 抬起手后mCurrentSelected的位置由当前位置move到中间选中位置
        if (Math.abs(moveLen) < 0.0001) {
            moveLen = 0f;
            performSelect();
            return;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
        task = new MyTimerTask();
        timer.schedule(task, 0, 10);
    }
    
    private void processMove(float currY) {
        moveLen += currY - lastDownY;
        if (moveLen > textSpace / 2) {
            // 往下滑超过离开距离
            moveTailToHead();
            moveLen -= textSpace;
        } else if (moveLen < -textSpace / 2) {
            // 往上滑超过离开距离
            moveHeadToTail();
            moveLen += textSpace;
        }
    }
    
    private void doMove(MotionEvent event) {
        float currY = event == null ? scroller.getCurrY() : event.getY();
        if ((currentSelected >= dataList.size() - 1 && currY - lastDownY < 0) ||
                (currentSelected <= 0 && currY - lastDownY > 0)) {
            if (!enableLoop) {
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                if (isEdge) {
                    moveLen = 0f;
                } else {
                    moveLen += currY - lastDownY;
                    if (moveLen > textSpace / 2) {
                        moveLen -= textSpace;
                    } else if (moveLen < -textSpace / 2) {
                        moveLen += textSpace;
                    }
                }
                isEdge = true;
            } else {
                processMove(currY);
            }
        } else {
            isEdge = false;
            processMove(currY);
        }
        lastDownY = currY;
        invalidate();
    }
    
    private void doDown(MotionEvent event) {
        if (!scroller.isFinished()) {
            scroller.abortAnimation();
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
        lastDownY = event.getY();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            gestureDetector.onTouchEvent(event);
            switch(event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    doDown(event);
            		break;
                case MotionEvent.ACTION_MOVE:
                    doMove(event);
            		break;
                case MotionEvent.ACTION_UP:
                    doUp();
                    break;
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void moveTailToHead() {
        if (enableLoop) {
            dataList.add(0, dataList.remove(dataList.size() - 1));
        } else {
            currentSelected--;
        }
    }

    private void moveHeadToTail() {
        if (enableLoop) {
            dataList.add(dataList.remove(0));
        } else {
            currentSelected++;
        }
    }

    /**
     * @param x 偏移量
     */
    private float getScale(float x) {
        float d = 1 - Math.abs(x) * 3 / textSpace;
        float percent = 1 - Math.abs(x) * (1 + (d < 0 ? 0f : d)) / (3 * textSpace);
        return percent < 0 ? 0f : percent;
    }

    private int getColor(float scale) {
        int aa = unselectedTextColor & 0x20000000 >> 24 & 0xff;
        int ra = unselectedTextColor & 0x20000000 >> 16 & 0xff;
        int ga = unselectedTextColor & 0x20000000 >> 8 & 0xff;
        int ba = unselectedTextColor & 0x20000000 & 0xff;
        int ab = selectedTextColor >> 24 & 0xff;
        int rb = selectedTextColor >> 16 & 0xff;
        int gb = selectedTextColor >> 8 & 0xff;
        int bb = selectedTextColor & 0xff;
        int a = (int) (aa + (ab - aa) * scale);
        int r = (int) (ra + (rb - ra) * scale);
        int g = (int) (ga + (gb - ga) * scale);
        int b = (int) (ba + (bb - ba) * scale);
        return Color.argb(a, r, g, b);
    }
    
    private void drawOtherText(Canvas canvas, int position, int type) {
        float d = textSpace * position + type * moveLen;
        float scale = getScale(type * d);
        paint.setTextSize((selectedTextSize - unselectedTextSize) * scale + unselectedTextSize);
        paint.setColor(getColor(scale));
        float y = getHeight() / 2f + type * d;
        Paint.FontMetricsInt fmi = paint.getFontMetricsInt();
        float baseline = y - (fmi.bottom / 2f + fmi.top / 2f);
        int indexs = currentSelected + type * position;
        String textData = dataList.get(indexs);
        canvas.drawText(textData, getWidth() / 2f, baseline, paint);
    }
    
    private void drawData(Canvas canvas) {
        if (dataList.isEmpty()) return;
                // 先绘制选中的text再往上往下绘制其余的text
                float scale = getScale(moveLen);
        paint.setTextSize((selectedTextSize - unselectedTextSize) * scale + unselectedTextSize);
        paint.setColor(getColor(scale));
        // text居中绘制，注意baseline的计算才能达到居中，y值是text中心坐标
        float x = getWidth() / 2f;
        float y = getHeight() / 2f + moveLen;
        Paint.FontMetricsInt fmi = paint.getFontMetricsInt();
        float baseline = y - (fmi.bottom / 2f + fmi.top / 2f);
        int indexs = currentSelected;
        String textData = dataList.get(indexs);
        canvas.drawText(textData, x, baseline, paint);

        // 绘制上方data
        int i = 1;
        while (currentSelected - i >= 0) {
            drawOtherText(canvas, i, -1);
            i++;
        }
        // 绘制下方data
        while (currentSelected + i < dataList.size()) {
            drawOtherText(canvas, i, 1);
            i++;
        }
    }
    
    public void select(String item) {
        if (!scroller.isFinished()) {
            scroller.abortAnimation();
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).equals(item)) {
                currentSelected = i;
                break;
            }
        }
        if (enableLoop) {
            int distance = dataList.size() / 2 - currentSelected;
            if (distance < 0) {
                for (int i = 0; i < -distance; i++) {
                    moveHeadToTail();
                    currentSelected--;
                }
            } else if (distance > 0) {
                for (int i = 0; i < distance; i++) {
                    moveTailToHead();
                    currentSelected++;
                }
            }
        }
        performSelect();
        invalidate();
    }
    
    public void setData(List<String> data) {
        if (dataList == null || dataList.size() == 0) return;
        dataList = data;
        currentSelected = data.size() / 2;
        invalidate();
    }
    
    public void setLoopEnable(boolean enable) {
        enableLoop = enable;
    }

    private void performSelect() {
        if (selectListener != null)
            selectListener.onSelect(dataList.get(currentSelected));
    }
    
    public void setTypeface(Typeface typeface) {
        paint.setTypeface(typeface);
        invalidate();
    }

    /**
     * 字体间距，指未选中字体间距
     */
    public void setTextSpace(float space) {
        textSpace = space + unselectedTextSize;
    }

    public void setOnSelectListener(OnSelectListener listener) {
        selectListener = listener;
    }

    public void setTextColor(int unselectedTextColor, int selectedTextColor) {
        this.selectedTextColor = selectedTextColor;
        this.unselectedTextColor = unselectedTextColor;
        invalidate();
    }

    public void setTextSize(float unselectedTextSize, float selectedTextSize) {
        this.selectedTextSize = selectedTextSize;
        this.unselectedTextSize = unselectedTextSize;
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            if (isFling) doMove(null);
        } else {
            if (isFling) {
                doUp();
                isFling = false;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (selectedTextSize == -1f || unselectedTextSize == -1f) {
            selectedTextSize = getHeight() / 4f;
            unselectedTextSize = selectedTextSize / 3f;
        }
        if (textSpace == -1f) textSpace = unselectedTextSize * 2.4f;
        isInit = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 根据index绘制view
        if (isInit) {
            drawData(canvas);
        }
    }

    /**
     * @return 返回选中的文本，未选中返回null
     */
    public String getSelected() {
        return dataList.isEmpty() ? null : dataList.get(currentSelected);
    }

    /**
     * @return 未选中返回-1，否则返回对应索引
     */
    public int getSelectedIndex() {
        return dataList.isEmpty() ? -1 : currentSelected;
    }
}
