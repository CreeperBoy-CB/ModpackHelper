package com.cb2495.modpackhelper.help;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 「哪些帮助文档已经读过」的记录。
 * <p>这份记录随帮助功能一起搬到了本模组，文件位置也迁到了本模组自己的缓存目录：
 * {@code .cache/modpack_helper/read_help_message.json}。
 * <p>为什么不再复用 VerityPlus 的那份实现：让两个模组之间只剩一条单向依赖
 * （本模组 → VerityPlus），而不是互相引用；文件格式保持一致，
 * 需要时可以手工把旧文件挪过来。
 * <p>数据只在这几个方法里读写，写盘是同步的小文件，打开帮助页才会发生一次，不必挪到后台。
 */
public final class HelpReadState {

    private HelpReadState() {}

    private static final Logger LOGGER = LogUtils.getLogger();

    /** 本模组的缓存目录。 */
    private static final String CACHE_DIR = ".cache/modpack_helper";

    private static Path readFile() {
        return FMLPaths.GAMEDIR.get().resolve(CACHE_DIR).resolve("read_help_message.json");
    }

    /** 已读主题集合；null 表示尚未从磁盘加载。 */
    private static Set<String> readTopics = null;

    private static Set<String> topics() {
        if (readTopics == null) {
            readTopics = load();
        }
        return readTopics;
    }

    private static Set<String> load() {
        Set<String> result = new LinkedHashSet<>();
        Path file = readFile();
        if (!Files.exists(file)) return result;
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) return result;
            // 只认最简单的一行一个主题的格式，避免为了一个记录文件引入完整的 json 解析
            for (String line : content.split("\\r?\\n")) {
                String topic = line.trim();
                if (!topic.isEmpty()) result.add(topic);
            }
        } catch (Exception e) {
            // 文件损坏时按「没读过」处理，而不是让界面崩溃
            LOGGER.warn("读取已读帮助记录失败，按未读处理: {}", e.getMessage());
        }
        return result;
    }

    /** 该帮助主题是否已被阅读过。 */
    public static boolean hasRead(String topic) {
        if (topic == null || topic.isEmpty()) return false;
        return topics().contains(topic);
    }

    /**
     * 标记某帮助主题为已读并写回磁盘。
     * <p>已读过时直接返回，避免每次打开教程都重写文件。
     */
    public static void markRead(String topic) {
        if (topic == null || topic.isEmpty()) return;
        if (!topics().add(topic)) return;
        save();
    }

    /** 批量标记为已读，只写一次磁盘。 */
    public static void markAllRead(Collection<String> topicList) {
        if (topicList == null || topicList.isEmpty()) return;
        boolean changed = false;
        for (String topic : topicList) {
            if (topic == null || topic.isEmpty()) continue;
            if (topics().add(topic)) changed = true;
        }
        if (changed) save();
    }

    /** 提前把记录读进内存，避免第一次打开帮助页时在渲染线程上读盘。 */
    public static void preload() {
        topics();
    }

    private static void save() {
        Path file = readFile();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, String.join("\n", topics()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("保存已读帮助记录失败: {}", e.getMessage());
        }
    }
}

