package com.cb2495.modpackhelper.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 界面顶部那条「左按钮 + 居中标题 + 右按钮」的工具栏，用原版 {@link Button} 实现。
 * <p>按钮外观（深浅灰、悬停高亮、点击音效）全部由原版控件负责，这里只管三件事：
 * 布局、遮罩上下的绘制顺序、以及把点击转发给原版按钮。
 * <p>为什么要在 {@code super.render} 之后单独再画一次：{@code Screen.render} 在这一帧
 * 末尾会按控件表把按钮重画一遍，而那时顶部遮罩已经盖上了，按钮会被压暗成「鬼影」。
 * 所以顶栏的按钮不加入 {@code renderables}，而是由本类在遮罩之后手工调用
 * {@code renderWidget}，这样既保留了原版外观，又不会被遮罩盖住。
 * <p>按钮只在 {@link #clear()} 时创建：{@code Screen.init()} 在窗口缩放时会被重跑，
 * 每次重跑都重新建按钮会不断堆积，表现为「缩放之后冒出一大堆重复按钮」。
 */
public final class ScreenTopBar {

    /** 顶部遮罩高度（含底部那条分隔线）。 */
    public static final int HEIGHT = 26;

    /** 按钮尺寸与位置：与原版标题栏按钮一致（高 20，距顶 3）。 */
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_Y = 3;
    /** 左右两组按钮各自距离屏幕边缘的留白与相邻间隙。 */
    private static final int MARGIN = 5;
    private static final int GAP = 2;

    /** 一个按钮：左侧还是右侧分组 + 原版控件 + 是否需要切换选中状态。 */
    private static final class Entry {
        final Button button;
        final boolean leftSide;
        /** 页签类按钮：选中时白字、未选中灰字；普通按钮为 null。 */
        ChatFormatting selectedColor = null;
        boolean selected = false;

        Entry(Button button, boolean leftSide) {
            this.button = button;
            this.leftSide = leftSide;
        }
    }

    private final Screen screen;
    private final List<Entry> entries = new ArrayList<>();

    public ScreenTopBar(Screen screen) {
        this.screen = screen;
    }

    /**
     * 清空并重建。{@code Screen.init()} 每次都会调用它，保证缩放窗口后
     * 按钮数量不累积。
     * <p>本类不持有 {@code renderables}，所以清空只是丢掉自己的列表，
     * 不会影响 Screen 自己的控件。
     */
    public void clear() {
        entries.clear();
    }

    /** 在左侧追加一个普通按钮（原版外观、点击音效）。 */
    public Button addButton(String label, Runnable action) {
        return add(label, action, true, false);
    }

    /** 在右侧追加一个普通按钮。 */
    public Button addRightButton(String label, Runnable action) {
        return add(label, action, false, false);
    }

    /**
     * 在左侧追加一个「页签式」按钮：选中时白色文字，未选中灰色。
     * <p>返回的 {@link Button} 可以直接 {@code setMessage} 改文字，
     * 颜色会按当前的选中状态自动重算。
     */
    public Button addTab(String label, Runnable action) {
        return add(label, action, true, true);
    }

    private Button add(String label, Runnable action, boolean leftSide, boolean tab) {
        Button button = Button.builder(Component.literal(label), b -> action.run()).build();
        Entry entry = new Entry(button, leftSide);
        if (tab) {
            entry.selectedColor = ChatFormatting.WHITE;
        }
        entries.add(entry);
        return button;
    }

    /**
     * 更新某个按钮的选中状态。
     * <p>只对 {@code addTab} 创建的按钮有效；颜色取自原版
     * {@link ChatFormatting#WHITE} / {@link ChatFormatting#GRAY}，与创造模式物品栏的页签一致。
     */
    public void setSelected(Button button, boolean selected) {
        for (Entry entry : entries) {
            if (entry.button != button) continue;
            entry.selected = selected;
            applyTabColor(entry);
            return;
        }
    }

    /** 把选中状态换算成文字颜色；普通按钮不做任何处理。 */
    private static void applyTabColor(Entry entry) {
        if (entry.selectedColor == null) return;
        ChatFormatting color = entry.selected ? ChatFormatting.WHITE : ChatFormatting.GRAY;
        // 只替换颜色，保留调用方可能设置的其它样式（例如粗体）
        entry.button.setMessage(entry.button.getMessage().copy().withStyle(color));
    }

    /**
     * 按当前控件文字重算坐标。
     * <p>宽度用 {@code font.width} 计算加左右内边距，与原版按钮的观感一致；
     * 左右两组分别靠边排列，标题居中由调用方负责。
     */
    private void layout(Font font) {
        int leftX = MARGIN;
        int rightTotal = 0;
        for (Entry entry : entries) {
            int width = font.width(entry.button.getMessage()) + 12;
            entry.button.setWidth(width);
            if (!entry.leftSide) {
                rightTotal += width + GAP;
            }
        }
        int rightX = screen.width - MARGIN - rightTotal;
        for (Entry entry : entries) {
            entry.button.setY(BUTTON_Y);
            if (entry.leftSide) {
                entry.button.setX(leftX);
                leftX += entry.button.getWidth() + GAP;
            } else {
                entry.button.setX(rightX);
                rightX += entry.button.getWidth() + GAP;
            }
        }
    }

    /**
     * 画遮罩、分隔线、按钮与居中标题。
     * <p>必须在正文与 {@code Screen.render} 之后调用：遮罩是半透明的，
     * 先画的按钮会被压暗。
     */
    public void render(GuiGraphics graphics, Font font, String title, int mouseX, int mouseY) {
        layout(font);

        // 遮罩与分隔线。用 RenderSystem 保证盖在正文之上，与其它界面一致
        RenderSystem.enableBlend();
        graphics.fill(0, 0, screen.width, HEIGHT - 1, 0x80000000);
        graphics.fill(0, HEIGHT - 1, screen.width, HEIGHT, 0xFFAAAAAA);
        RenderSystem.disableBlend();
        graphics.drawCenteredString(font, Component.literal(title), screen.width / 2, 8, 0xFFFFFF);

        // 手动绘制按钮：这些按钮不在 Screen.renderables 里，所以只有这一次绘制
        for (Entry entry : entries) {
            entry.button.render(graphics, mouseX, mouseY, 0f);
        }
    }

    /**
     * 命中检测：先重算布局再判断，避免缩放后的第一帧用到旧坐标。
     *
     * @return 是否吃掉了这次点击（命中任一按钮）
     */
    public boolean mouseClicked(double mouseX, double mouseY) {
        layout(Minecraft.getInstance().font);
        for (Entry entry : entries) {
            if (!entry.button.visible || !entry.button.active) continue;
            if (entry.button.isMouseOver(mouseX, mouseY)) {
                entry.button.mouseClicked(mouseX, mouseY, 0);
                return true;
            }
        }
        return false;
    }

    /** 鼠标是否落在顶栏区域内。 */
    public static boolean isOverBar(double mouseY) {
        return mouseY < HEIGHT;
    }
}
