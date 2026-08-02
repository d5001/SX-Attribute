package github.saukiya.sxattribute.data.condition;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxlevel.SXLevel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 条件抽象类
 *
 * @author Saukiya
 */
public abstract class SubCondition {

    static final ConditionMap conditionMap = new ConditionMap();

    private final String name;

    private JavaPlugin plugin = null;

    private SXConditionType[] updateTypes = new SXConditionType[]{SXConditionType.ALL};

    /**
     * 实现一个条件类
     *
     * @param name String 条件名
     * @param type SXConditionType[] 条件类型
     */
    public SubCondition(String name, SXConditionType... type) {
        this.name = name;
        this.updateTypes = type;
    }

    /**
     * 实现一个条件类
     *
     * @param name String 条件名
     */
    public SubCondition(String name) {
        this.name = name;
    }

    /**
     * 获取物品名称
     *
     * @param item ItemStack
     * @return String
     */
    public static String getItemName(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return "N/A";
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) {
                return meta.getDisplayName();
            }
        }
        return item.getType().name();
    }

    /**
     * 获取物品等级
     *
     * @param item ItemStack
     * @return int 没有则为-1
     */
    public static int getItemLevel(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return -1;
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasLore()) {
                List<String> loreList = meta.getLore();
                String target = Config.getConfig().getString(Config.NAME_LIMIT_LEVEL);
                for (String lore : loreList) {
                    if (lore.contains(target)) {
                        try {
                            return Integer.parseInt(getNumber(lore).replace(".", ""));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 获取生物等级
     *
     * @param entity LivingEntity
     * @return int 非玩家则为10000
     */
    public static int getLevel(LivingEntity entity) {
        return entity instanceof Player ? SXAttribute.isSxLevel() ? SXLevel.getApi().getPlayerData((Player) entity).getLevel() : ((Player) entity).getLevel() : 10000;
    }


    private static final Pattern CLEAN_PATTERN = Pattern.compile("§+[a-z0-9]|[^-0-9.]");

    /**
     * 获取属性值
     *
     * @param lore String
     * @return String
     */
    public static String getNumber(String lore) {
        if (lore == null || lore.isEmpty()) {
            return "0";
        }

        String str = CLEAN_PATTERN.matcher(lore).replaceAll("");
        return str.isEmpty() ? "0" : str;
    }
    /*public static String getNumber(String lore) {
        String str = lore.replaceAll("§+[a-z0-9]", "").replaceAll("[^-0-9.]", "");
        return str.isEmpty() ? "0" : str;
    }*/


    /**
     * 获取当前耐久值
     *
     * @param lore String
     * @return int
     */
    public static int getDurability(String lore) {
        return getDurabilityValue(lore, 0);
    }
    /*public static int getDurability(String lore) {
        return lore.contains("/") && lore.split("/").length > 1 ? Integer.valueOf(lore.replaceAll("§+[0-9]", "").split("/")[0].replaceAll("[^0-9]", "")) : 0;
    }*/

    /**
     * 获取最大耐久值
     *
     * @param lore String
     * @return int
     */
    public static int getMaxDurability(String lore) {
        return getDurabilityValue(lore, 1);
    }
    /*public static int getMaxDurability(String lore) {
        return lore.contains("/") && lore.split("/").length > 1 ? Integer.valueOf(lore.replaceAll("§+[0-9]", "").split("/")[1].replaceAll("[^0-9]", "")) : 0;
    }*/

    private static final Pattern NON_DIGIT = Pattern.compile("[^0-9]");

    /**
     * 获取耐久值（当前/最大）
     * @param lore 耐久字符串，格式如 "§a50/100" 或 "50/100"
     * @param index 0=当前耐久，1=最大耐久
     * @return 耐久值，无效时返回0
     */
    private static int getDurabilityValue(String lore, int index) {
        if (lore == null || !lore.contains("/")) {
            return 0;
        }

        // 使用预编译正则移除颜色代码
        String cleaned = ChatColor.stripColor(lore);
        String[] parts = cleaned.split("/");

        if (parts.length <= index) {
            return 0;
        }

        // 使用预编译正则提取数字
        String value = NON_DIGIT.matcher(parts[index]).replaceAll("");
        return value.isEmpty() ? 0 : Integer.parseInt(value);
    }

    /**
     * 获取物品是否为无限耐久
     *
     * @param meta ItemMeta
     * @return boolean
     */
    public static boolean getUnbreakable(ItemMeta meta) {
        return SXAttribute.getVersionSplit()[1] >= 11 ? meta.isUnbreakable() : meta.spigot().isUnbreakable();
    }

    /**
     * 获取条件名
     *
     * @return String
     */
    public final String getName() {
        return name;
    }

    /**
     * 获取类型
     *
     * @return SXConditionType[]
     */
    public final SXConditionType[] getType() {
        return updateTypes.clone();
    }

    /**
     * 判断物品是否符合条件
     *
     * @param entity LivingEntity
     * @param item   ItemStack
     * @param lore   String
     * @return SXConditionReturnType
     */
    public abstract SXConditionReturnType determine(LivingEntity entity, ItemStack item, String lore);

    /**
     * 判断条件类型
     *
     * @param type        目标类型
     * @param strContains 是否判断字符串
     * @return boolean
     */
    public final boolean containsType(SXConditionType type, boolean strContains) {
        if (type == null) {
            return false;
        }

        for (SXConditionType updateType : updateTypes) {
            if (updateType == SXConditionType.ALL) {
                return true;
            }

            if (strContains) {
                if (type.getName().contains(updateType.getName())) {
                    return true;
                }
            } else {
                if (updateType.equals(type)) {
                    return true;
                }
            }
        }
        return false;
    }
    /*public final boolean containsType(SXConditionType type, boolean strContains) {
        return type != null && Arrays.stream(this.updateTypes).anyMatch(updateType -> updateType.equals(SXConditionType.ALL) || (strContains ? type.getName().contains(updateType.getName()) : updateType.equals(type)));
    }*/

    /**
     * 注册条件方法
     * 优先级需在SX-Attribute/Config.yml 设定
     *
     * @param plugin JavaPlugin
     */
    public final void registerCondition(JavaPlugin plugin) {
        if (plugin == null) {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cCondition >> §4" + this.getName() + " §cNull Plugin!");
        } else if (this.getPriority() < 0) {
            Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] §8Condition >> Disable §4" + this.getName() + " §8!");
        } else if (SXAttribute.isPluginEnabled()) {
            Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] §cCondition >> §cSXAttribute is Enabled §4" + this.getName() + "§r !");
        } else {
            this.plugin = plugin;
            SubCondition condition = conditionMap.put(this.getPriority(), this);
            if (condition == null) {
                Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] Condition >> Register §c" + this.getName() + " §rTo Priority §c" + this.getPriority() + " §r!");
            } else {
                Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] Condition >> §cThe §4" + this.getName() + " §cCover To §4" + condition.getName() + " §7[§c" + condition.getPlugin() + "§7]§r !");
            }
        }
    }

    /**
     * 条件注册成功后启动时执行的方法
     */
    public void onEnable() {

    }

    /**
     * 条件注册成功后关闭时执行的方法
     */
    public void onDisable() {

    }

    /**
     * 为条件添加介绍说明
     *
     * @return List
     */
    public List<String> introduction() {
        return new ArrayList<>();
    }

    /**
     * 获取优先级
     *
     * @return int 优先值 -1为关闭
     */
    public final int getPriority() {
        return Config.getConfig().getInt("ConditionPriority." + getName(), -1);
    }

    /**
     * 获取注册该条件的插件
     *
     * @return JavaPlugin
     */
    public final JavaPlugin getPlugin() {
        return plugin;
    }
}
