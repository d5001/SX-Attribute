package github.saukiya.sxattribute.data.attribute.sub.other;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.util.Message;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractItemStack;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicDropLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.drops.Drop;
import io.lumine.xikage.mythicmobs.drops.DropMetadata;
import io.lumine.xikage.mythicmobs.drops.IItemDrop;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Saukiya
 */
public class MythicmobsDropAttribute extends SubAttribute {

    /*private static boolean sxSeal = false;*/

    public MythicmobsDropAttribute() {
        super("MythicmobsDrop", 0, new SXAttributeType(SXAttributeType.Type.OTHER, "Drops"));
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            /*if (Bukkit.getPluginManager().getPlugin("SX-Seal") != null) {
                sxSeal = true;
            }*/
            try {
                Class.forName("io.lumine.xikage.mythicmobs.api.bukkit.events.MythicDropLoadEvent");
                Bukkit.getPluginManager().registerEvents(new Major(), getPlugin());
            } catch (ClassNotFoundException ignored) {
                Bukkit.getPluginManager().registerEvents(new General(), getPlugin());
            }
        }
    }

    @Override
    public void eventMethod(EventData eventData) {
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return null;
    }

    @Override
    public List<String> getPlaceholders() {
        return new ArrayList<>();
    }

    @Override
    public boolean loadAttribute(String lore) {
        return false;
    }

    @Override
    public double getValue() {
        return 0;
    }

    public static class Major implements Listener {
        @EventHandler
        public void onDrop(MythicDropLoadEvent e) {
            if (e.getDropName().equalsIgnoreCase("sx")) {
                String line = e.getContainer().getLine();
                int index = line.indexOf(" ");
                if (index != -1) {
                    e.register(new SXDrop(line.substring(index + 1), e.getConfig()));
                } else {
                    e.register(new SXDrop(e.getConfig()));
                }
            }
        }

        public static class SXDrop extends Drop implements IItemDrop {

            private boolean valid = false;
            private String name;

            public SXDrop(String line, MythicLineConfig config) {
                super(line, config);
                valid = true;
                int index = line.indexOf(" ");
                name = index == -1 ? line : line.substring(0, index);
            }

            public SXDrop(MythicLineConfig config) {
                super("sx", config);
            }

            @Override
            public AbstractItemStack getDrop(DropMetadata dropMetadata) {
                if (valid) {
                    ItemStack item;
                    AbstractEntity trigger = dropMetadata.getTrigger();
                    if (trigger != null && trigger.isPlayer()) {
                        item = SXAttribute.getApi().getItem(name, (Player) trigger.getBukkitEntity());
                    } else {
                        item = SXAttribute.getApi().getItem(name, null);
                    }
                    if (item != null) {
                        item.setAmount((int) getAmount());
                        return BukkitAdapter.adapt(item);
                    }
                    Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cMythicmobs怪物: §4" + dropMetadata.getCaster().getEntity().getCustomName() + "§c 不存在这个掉落物品: §4" + name);
                }
                return null;
            }
        }
    }

    public static class General implements Listener {
        @EventHandler
        private static void onMythicMobDeathEvent(MythicMobDeathEvent event) {
            MythicMob mm = event.getMobType();
            List<String> dropList = mm.getConfig().getStringList("drops");
            List<ItemStack> drops = event.getDrops();
            if (event.getKiller() instanceof Player) {
                for (String str : dropList) {
                    if (str.contains(" ")) {
                        String[] args = str.split(" ");
                        if (args.length > 1 && args[0].equalsIgnoreCase("sx")) {
                            int amount = 1;
                            if (args.length > 3 && !args[3].isEmpty() && SXAttribute.getRandom().nextDouble() > Double.parseDouble(args[3].replaceAll("[^0-9.]", ""))) {// 几率判断
                                continue;
                            }
                            if (args.length > 2 && !args[2].isEmpty()) {// 数量判断
                                String[] split = args[2].split("-");
                                if (split.length > 1) {
                                    int i1 = Integer.parseInt(split[0].replaceAll("[^0-9]", ""));
                                    int i2 = Integer.parseInt(split[1].replaceAll("[^0-9]", ""));
                                    if (i1 > i2) {
                                        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§c随机数大小不正确!: §4" + str);
                                    } else {
                                        amount = SXAttribute.getRandom().nextInt(i2 - i1 + 1) + i1;
                                    }
                                } else {
                                    amount = Integer.parseInt(args[2].replaceAll("[^0-9]", ""));
                                }
                            }
                            ItemStack item = SXAttribute.getApi().getItem(args[1], (Player) event.getKiller());
                            if (item != null) {
                                item.setAmount(amount);
                                drops.add(item.clone());
                            } else {
                                String customName = event.getEntity().getCustomName();
                                if (customName == null) customName = event.getEntity().getName();
                                Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cMythicmobs怪物: §4" + customName + "§c 不存在这个掉落物品: §4" + args[1]);
                            }
                        }
                    }
                }
            }
        }
    }
}
