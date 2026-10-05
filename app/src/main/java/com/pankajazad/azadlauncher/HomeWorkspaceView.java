package com.pankajazad.azadlauncher;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public final class HomeWorkspaceView extends FrameLayout {
    interface Listener {
        void onHomeAppClick(AppEntry app);
        void onAppMoved(String appId, int cellX, int cellY);
        void onEmptyLongPress();
    }

    private final Listener listener;
    private final Map<String, View> appViews = new HashMap<>();
    private final Map<View, String> viewAppIds = new HashMap<>();
    private int columns = 5;
    private int rows = 8;

    public HomeWorkspaceView(Context context, Listener listener) {
        super(context);
        this.listener = listener;
        setClipChildren(false);
        setClipToPadding(false);
        setBackgroundColor(Color.TRANSPARENT);
        setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                View dragged = (View) event.getLocalState();
                String appId = viewAppIds.get(dragged);
                if (appId != null) {
                    int[] cell = pointToCell(event.getX(), event.getY());
                    positionView(dragged, cell[0], cell[1], 1, 1);
                    listener.onAppMoved(appId, cell[0], cell[1]);
                    return true;
                }
            }
            return event.getAction() == DragEvent.ACTION_DRAG_STARTED;
        });
        setOnLongClickListener(v -> {
            listener.onEmptyLongPress();
            return true;
        });
    }

    void setGrid(int columns, int rows) {
        this.columns = Math.max(1, columns);
        this.rows = Math.max(1, rows);
        requestLayout();
        for (View view : appViews.values()) {
            HomeTag tag = (HomeTag) view.getTag();
            if (tag != null) {
                positionView(view, tag.cellX, tag.cellY, 1, 1);
            }
        }
    }

    void addApp(AppEntry app, int cellX, int cellY) {
        removeApp(app.getId());
        LinearLayout item = new LinearLayout(getContext());
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setClickable(true);
        item.setLongClickable(true);
        item.setContentDescription(app.getLabel());

        ImageView icon = new ImageView(getContext());
        icon.setImageDrawable(app.getIcon());
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        item.addView(icon, new LinearLayout.LayoutParams(
                dp(52), dp(52)));

        TextView label = new TextView(getContext());
        label.setText(app.getLabel());
        label.setTextSize(12);
        label.setGravity(Gravity.CENTER);
        label.setMaxLines(1);
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        item.addView(label, new LinearLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, dp(22)));

        item.setOnClickListener(v -> listener.onHomeAppClick(app));
        item.setOnLongClickListener(v -> {
            ClipData data = ClipData.newPlainText("azad-home-app", app.getId());
            v.startDragAndDrop(data, new View.DragShadowBuilder(v), v, 0);
            return true;
        });
        item.setTag(new HomeTag(cellX, cellY));
        addView(item);
        appViews.put(app.getId(), item);
        viewAppIds.put(item, app.getId());
        positionView(item, cellX, cellY, 1, 1);
    }

    void removeApp(String appId) {
        View view = appViews.remove(appId);
        if (view != null) {
            viewAppIds.remove(view);
            removeView(view);
        }
    }

    private void positionView(View view, int cellX, int cellY, int spanX, int spanY) {
        int cellWidth = getWidth() > 0 ? getWidth() / columns : 0;
        int cellHeight = getHeight() > 0 ? getHeight() / rows : 0;
        if (cellWidth <= 0 || cellHeight <= 0) {
            return;
        }
        cellX = Math.max(0, Math.min(columns - spanX, cellX));
        cellY = Math.max(0, Math.min(rows - spanY, cellY));
        HomeTag tag = (HomeTag) view.getTag();
        if (tag != null) {
            tag.cellX = cellX;
            tag.cellY = cellY;
        }
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                cellWidth * spanX, cellHeight * spanY);
        lp.leftMargin = cellX * cellWidth;
        lp.topMargin = cellY * cellHeight;
        view.setLayoutParams(lp);
    }

    private int[] pointToCell(float x, float y) {
        int cellWidth = Math.max(1, getWidth() / columns);
        int cellHeight = Math.max(1, getHeight() / rows);
        return new int[] {
                Math.max(0, Math.min(columns - 1, (int) (x / cellWidth))),
                Math.max(0, Math.min(rows - 1, (int) (y / cellHeight)))
        };
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        for (View view : appViews.values()) {
            HomeTag tag = (HomeTag) view.getTag();
            if (tag != null) {
                positionView(view, tag.cellX, tag.cellY, 1, 1);
            }
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class HomeTag {
        int cellX;
        int cellY;
        HomeTag(int cellX, int cellY) {
            this.cellX = cellX;
            this.cellY = cellY;
        }
    }
}
