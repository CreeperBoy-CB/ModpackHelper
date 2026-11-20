package com.cb2495.modpackhelper.util;

import com.cb2495.verityplus.util.ConfigIO;
import com.cb2495.verityplus.util.ImageHeader;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.InputStream;

import java.io.InputStream;

import java.io.InputStream;

/**
 * 帮助图片的共用工具。
 * <p>“读图片文件头算尺寸”和“把解码挪到后台线程”这两件事都直接复用 VerityPlus
 * （modid {@code verityplus}）里的实现：那是与具体功能无关的通用代码，
 * 没必要在本模组里再抄一份，将来修 bug 也只需要改一处。
 * <p>本类把这几处跨模组的调用收在一处，将来若 VerityPlus 改了包名，只改这一个文件。
 */
public final class HelpImages {

    private HelpImages() {}

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 只读图片文件头取出宽高，不做完整解码。
     *
     * @param in 图片输入流，方法内部会关闭它
     * @return 宽高，格式不认识时返回 {@code null}
     */
    public static int[] readSize(InputStream in) {
        return ImageHeader.readSize(in);
    }

    /** 在 VerityPlus 的后台线程（单线程串行）上执行磁盘读写与图片解码。 */
    public static void runInBackground(Runnable task) {
        ConfigIO.run(task);
    }

    /** 把后台线程的结果交回客户端主线程执行。 */
    public static void runOnClient(Runnable task) {
        ConfigIO.backToClient(task);
    }

    /** 帮助功能的日志出口（与游戏其它日志走同一条 SLF4J 通道）。 */
    public static void warn(String message) {
        LOGGER.warn(message);
    }

    /** 帮助功能的日志出口。 */
    public static void error(String message) {
        LOGGER.error(message);
    }

    /** 调试日志；只有日志级别放开时才会输出。 */
    public static void debug(String message) {
        LOGGER.debug(message);
    }

    /** 普通信息日志。 */
    public static void info(String message) {
        LOGGER.info(message);
    }
}
