package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.RegisterSlot;
import github.saukiya.sxattribute.data.condition.SXConditionReturnType;
import github.saukiya.sxattribute.data.condition.SXConditionType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * 手持免疫
 *
 * @author Saukiya
 */
public class HandCondition extends SubCondition {

    public HandCondition() {
        super("Hand", SXConditionType.HAND);
    }

    @Override
    public SXConditionReturnType determine(LivingEntity entity, ItemStack item, String lore) {
        if (lore == null || lore.isEmpty()) {
            return SXConditionReturnType.NULL;
        }

        for (String armor : Config.getConfig().getStringList(Config.NAME_ARMOR)) {
            if (lore.contains(armor)) {
                return SXConditionReturnType.ITEM;
            }
        }

        for (Map.Entry<Integer, RegisterSlot> entry : SXAttribute.getApi().getRegisterSlotMapEntrySet()) {
            if (lore.contains(entry.getValue().getName())) {
                return SXConditionReturnType.ITEM;
            }
        }

        return SXConditionReturnType.NULL;
    }

    /*@Override
    public SXConditionReturnType determine(LivingEntity entity, ItemStack item, String lore) {
        return (Config.getConfig().getStringList(Config.NAME_ARMOR).stream().anyMatch(lore::contains) || SXAttribute.getApi().getRegisterSlotMapEntrySet().stream().anyMatch(entry -> lore.contains(entry.getValue().getName()))) ? SXConditionReturnType.ITEM : SXConditionReturnType.NULL;
    }*/
}
