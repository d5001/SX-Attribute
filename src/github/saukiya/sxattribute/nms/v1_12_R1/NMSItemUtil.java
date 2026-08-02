package github.saukiya.sxattribute.nms.v1_12_R1;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.ItemUtil;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class NMSItemUtil extends ItemUtil {
    /**
     * 加载NBT反射类
     * 此类所附加的nbt均带有插件名
     *
     * @param plugin SXAttribute
     * @throws NoSuchMethodException  NoSuchMethodException
     * @throws ClassNotFoundException ClassNotFoundException
     */
    public NMSItemUtil(SXAttribute plugin) throws NoSuchMethodException, ClassNotFoundException {
        super(plugin, false);
    }

    @Override
    public ItemStack setAttackSpeed(ItemStack item, double... speed) {
        if (item == null || item.getType() == Material.AIR) return item;
        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsItem.getTag();
        if (compound == null) compound = new NBTTagCompound();

        NBTTagList modifiers = new NBTTagList();

        NBTTagCompound attackSpeed = new NBTTagCompound();

        attackSpeed.set("AttributeName", new NBTTagString("generic.attackSpeed"));
        attackSpeed.set("Name", new NBTTagString("AttackSpeed"));
        attackSpeed.set("Amount", new NBTTagDouble(getAttackSpeed(item, speed)));
        attackSpeed.set("Operation", new NBTTagInt(1)); // 1 = 百分比加成
        attackSpeed.set("UUIDLeast", new NBTTagInt(20000));
        attackSpeed.set("UUIDMost", new NBTTagInt(1000));
        attackSpeed.set("Slot", new NBTTagString("mainhand"));

        modifiers.add(attackSpeed);

        compound.set("AttributeModifiers", modifiers);

        nmsItem.setTag(compound);

        ItemStack bukkitItem = CraftItemStack.asBukkitCopy(nmsItem);
        ItemMeta meta = bukkitItem.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void clearAttribute(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;

        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsItem.getTag();
        if (compound == null) compound = new NBTTagCompound();

        String key = plugin.getName() + "-Clear";
        String expectedValue = "yes" + Config.isDamageGauges();
        if (!compound.hasKey(key) || !compound.getString(key).equals(expectedValue)) {
            compound.setString(key, expectedValue);

            NBTTagList modifiers = new NBTTagList();

            compound.set("AttributeModifiers", modifiers);

            nmsItem.setTag(compound);

            ItemStack bukkitItem = CraftItemStack.asBukkitCopy(nmsItem);
            ItemMeta meta = bukkitItem.getItemMeta();

            if (Config.isDamageGauges()) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }

            item.setItemMeta(meta);
        }
    }

    @Override
    public void removeAttribute(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;

        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsItem.getTag();
        if (compound == null) compound = new NBTTagCompound();

        String key = plugin.getName() + "-Clear";
        if (!compound.hasKey(key) || !compound.getString(key).equals("no")) {
            compound.set(key, new NBTTagString("no"));
            compound.remove("AttributeModifiers");

            nmsItem.setTag(compound);

            ItemStack result = CraftItemStack.asBukkitCopy(nmsItem);
            ItemMeta meta = result.getItemMeta();
            meta.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
    }

    @Override
    public boolean isNBT(ItemStack item, String key) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        NBTTagCompound compound = nmsItem.getTag();
        if (compound == null) return false;

        return compound.hasKey(plugin.getName() + "-" + key);
    }

    @Override
    public String getNBT(ItemStack item, String key) {
        if (item == null || item.getType() == Material.AIR) return null;

        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        NBTTagCompound compound = nmsItem.getTag();
        if (compound == null) return null;

        String fullKey = plugin.getName() + "-" + key;

        if (compound.hasKey(fullKey)) {
            return compound.getString(fullKey);
        }

        return null;
    }

    @Override
    public ItemStack setNBT(ItemStack item, String key, String value) {
        if (item == null || item.getType() == Material.AIR) return null;

        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        NBTTagCompound compound = nmsItem.getTag();
        if (compound == null) compound = new NBTTagCompound();

        compound.set(plugin.getName() + "-" + key, new NBTTagString(value));

        nmsItem.setTag(compound);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public ItemStack setNBTList(ItemStack item, String key, List<String> list) {
        if (item == null || item.getType() == Material.AIR) return null;

        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsItem.getTag();
        if (compound == null) compound = new NBTTagCompound();

        NBTTagList tagList = new NBTTagList();
        for (String str : list) {
            tagList.add(new NBTTagString(str));
        }

        compound.set(plugin.getName() + "-" + key, tagList);
        nmsItem.setTag(compound);

        ItemStack result = CraftItemStack.asBukkitCopy(nmsItem);
        item.setItemMeta(result.getItemMeta());
        return item;
    }

    @Override
    public List<String> getNBTList(ItemStack item, String key) {
        List<String> list = new ArrayList<>();
        if (item == null || item.getType() == Material.AIR) return list;

        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsItem.getTag();
        if (compound == null) return list;

        String fullKey = plugin.getName() + "-" + key;
        if (!compound.hasKey(fullKey)) return list;

        NBTBase nbtBase = compound.get(fullKey);
        if (!(nbtBase instanceof NBTTagList)) return list;

        NBTTagList tagList = (NBTTagList) nbtBase;
        for (int i = 0; i < tagList.size(); i++) {
            list.add(tagList.getString(i));
        }

        return list;
    }

    @Override
    public boolean removeNBT(ItemStack item, String key) {
        if (item == null || item.getType() == Material.AIR) return false;

        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsItem.getTag();
        if (compound == null) return false;

        String fullKey = plugin.getName() + "-" + key;
        if (compound.hasKey(fullKey)) {
            compound.remove(fullKey);
            nmsItem.setTag(compound);

            ItemStack result = CraftItemStack.asBukkitCopy(nmsItem);
            item.setItemMeta(result.getItemMeta());
            return true;
        }

        return false;
    }

    @Override
    public String getAllNBT(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;

        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsItem.getTag();
        if (compound == null) compound = new NBTTagCompound();

        return "[" + item.getTypeId() + ":" + item.getDurability() + "-" + item.hashCode() + "]>" + compound;
    }
}
