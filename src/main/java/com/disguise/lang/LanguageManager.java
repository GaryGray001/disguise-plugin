package com.disguise.lang;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多语言管理器：
 * - 首次启动将 jar 内 lang/*.yml 复制到插件数据目录 lang/（已存在不覆盖，用户可自由修改）
 * - config.yml 的 language 键选择语言（取 lang 文件夹中的语言文件名，如 zh_cn / en_us）
 * - 用户可自行新建语言文件：复制任意语言文件改名（如 fr_fr.yml）并在 config 中填写即可
 * - 当前语言缺失的 key 回退内置中文（jar 内 zh_cn.yml），再缺失返回 key 本身
 */
public final class LanguageManager {

    private static JavaPlugin plugin;
    private static String language = "zh_cn";
    private static final Map<String, String> messages = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> lists = new ConcurrentHashMap<>();
    private static final Map<String, String> fallbackMessages = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> fallbackLists = new ConcurrentHashMap<>();

    private LanguageManager() {}

    public static void init(JavaPlugin p) {
        plugin = p;
        // 1. 复制 jar 内默认语言文件到 dataFolder/lang（不存在才复制，不覆盖用户修改）
        File langDir = new File(p.getDataFolder(), "lang");
        if (!langDir.exists()) langDir.mkdirs();
        copyDefaultLang(langDir, "zh_cn.yml");
        copyDefaultLang(langDir, "en_us.yml");

        // 2. 内置中文兜底（从 jar 资源读，保证即使文件被删也有中文）
        loadResourceInto("zh_cn.yml", fallbackMessages, fallbackLists);

        // 3. 加载 config 选中的语言
        language = p.getConfig().getString("language", "zh_cn");
        load(language);
    }

    /** 重载 config + 语言（/dp reload 用） */
    public static void reload() {
        if (plugin != null) init(plugin);
    }

    /** 当前语言代码（如 zh_cn / en_us） */
    public static String getLanguage() {
        return language;
    }

    private static void copyDefaultLang(File langDir, String name) {
        File target = new File(langDir, name);
        if (target.exists()) return;
        try (InputStream in = plugin.getResource("lang/" + name)) {
            if (in != null) Files.copy(in, target.toPath());
        } catch (IOException e) {
            plugin.getLogger().warning("语言文件复制失败: " + name + " " + e.getMessage());
        }
    }

    private static void load(String lang) {
        messages.clear();
        lists.clear();
        // 先以内置中文为 base（默认语言也有完整数据），再被选中语言文件覆盖
        messages.putAll(fallbackMessages);
        lists.putAll(fallbackLists);
        File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
        if (langFile.exists()) {
            try {
                YamlConfiguration yml = YamlConfiguration.loadConfiguration(langFile);
                for (String key : yml.getKeys(true)) {
                    if (yml.isList(key)) lists.put(key, yml.getStringList(key));
                    else messages.put(key, yml.getString(key, ""));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("语言文件加载失败 " + langFile.getName() + ": " + e.getMessage());
            }
        } else {
            plugin.getLogger().warning("语言文件不存在 " + langFile.getName() + "，使用中文（zh_cn）");
        }
    }

    /** 从 jar 资源读取语言数据（fallback 用） */
    private static void loadResourceInto(String resource, Map<String, String> msgMap, Map<String, List<String>> listMap) {
        msgMap.clear();
        listMap.clear();
        try (InputStream in = plugin.getResource("lang/" + resource)) {
            if (in != null) {
                YamlConfiguration yml = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
                for (String key : yml.getKeys(true)) {
                    if (yml.isList(key)) listMap.put(key, yml.getStringList(key));
                    else {
                        String v = yml.getString(key);
                        if (v != null) msgMap.put(key, v);
                    }
                }
            }
        } catch (Exception e) {
            // 资源缺失时 fallback 为空，get 会返回 key 本身
        }
    }

    /** 取当前语言文本（缺 key 回退中文，再缺返回 key 本身） */
    public static String get(String key) {
        String v = messages.get(key);
        if (v != null && !v.isEmpty()) return v;
        String fb = fallbackMessages.get(key);
        return fb != null ? fb : key;
    }

    /** 带 {0} {1} 占位符替换 */
    public static String get(String key, Object... args) {
        String v = get(key);
        for (int i = 0; i < args.length; i++) {
            v = v.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return v;
    }

    /** 取当前语言文本列表（lore 用） */
    public static List<String> getList(String key) {
        List<String> v = lists.get(key);
        if (v != null) return v;
        List<String> fb = fallbackLists.get(key);
        return fb != null ? fb : new ArrayList<>();
    }

    /** 带默认值（LanguageManager 未初始化或 key 缺失时用默认） */
    public static String getOrDefault(String key, String def) {
        if (messages.isEmpty() && fallbackMessages.isEmpty()) return def;
        String v = messages.get(key);
        if (v != null && !v.isEmpty()) return v;
        String fb = fallbackMessages.get(key);
        return fb != null ? fb : def;
    }
}
