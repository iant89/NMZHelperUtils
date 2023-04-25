package com.github.iant89.ultimatenmz.skills;

import com.github.iant89.ultimatenmz.notifications.VisualNotificationType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SkillConstants {
    public static final Set<Integer> MELEE_POTIONS = ImmutableSet.of(
            ItemID.COMBAT_POTION4, ItemID.COMBAT_POTION3,ItemID.COMBAT_POTION2,ItemID.COMBAT_POTION1,
            ItemID.SUPER_COMBAT_POTION4, ItemID.SUPER_COMBAT_POTION3, ItemID.SUPER_COMBAT_POTION2, ItemID.SUPER_COMBAT_POTION1,
            ItemID.DIVINE_SUPER_COMBAT_POTION4, ItemID.DIVINE_SUPER_COMBAT_POTION3, ItemID.DIVINE_SUPER_COMBAT_POTION2, ItemID.DIVINE_SUPER_COMBAT_POTION1,
            ItemID.ZAMORAK_BREW4, ItemID.ZAMORAK_BREW3, ItemID.ZAMORAK_BREW2, ItemID.ZAMORAK_BREW1
    );

    public static final Set<Integer> ATTACK_POTIONS = ImmutableSet.<Integer>builder().addAll(MELEE_POTIONS).add(
            ItemID.ATTACK_POTION4, ItemID.ATTACK_POTION3,ItemID.ATTACK_POTION2,ItemID.ATTACK_POTION1,
            ItemID.SUPER_ATTACK4, ItemID.SUPER_ATTACK3, ItemID.SUPER_ATTACK2, ItemID.SUPER_ATTACK1,
            ItemID.DIVINE_SUPER_ATTACK_POTION4, ItemID.DIVINE_SUPER_ATTACK_POTION3, ItemID.DIVINE_SUPER_ATTACK_POTION2, ItemID.DIVINE_SUPER_ATTACK_POTION1
    ).build();

    public static final Set<Integer> STRENGTH_POTIONS = ImmutableSet.<Integer>builder().addAll(MELEE_POTIONS).add(
            ItemID.STRENGTH_POTION4, ItemID.STRENGTH_POTION3,ItemID.STRENGTH_POTION2,ItemID.STRENGTH_POTION1,
            ItemID.SUPER_STRENGTH4, ItemID.SUPER_STRENGTH3, ItemID.SUPER_STRENGTH2, ItemID.SUPER_STRENGTH1,
            ItemID.DIVINE_SUPER_STRENGTH_POTION4, ItemID.DIVINE_SUPER_STRENGTH_POTION3, ItemID.DIVINE_SUPER_STRENGTH_POTION2, ItemID.DIVINE_SUPER_STRENGTH_POTION1
    ).build();

    public static final Set<Integer> RANGED_POTIONS = ImmutableSet.of(
            ItemID.RANGING_POTION4, ItemID.RANGING_POTION3, ItemID.RANGING_POTION2, ItemID.RANGING_POTION1,
            ItemID.DIVINE_RANGING_POTION4, ItemID.DIVINE_RANGING_POTION3, ItemID.DIVINE_RANGING_POTION2, ItemID.DIVINE_RANGING_POTION1,
            ItemID.BASTION_POTION4, ItemID.BASTION_POTION3, ItemID.BASTION_POTION2, ItemID.BASTION_POTION1,
            ItemID.DIVINE_BASTION_POTION4, ItemID.DIVINE_BASTION_POTION3, ItemID.DIVINE_BASTION_POTION2, ItemID.DIVINE_BASTION_POTION1,
            ItemID.SUPER_RANGING_4, ItemID.SUPER_RANGING_3, ItemID.SUPER_RANGING_2, ItemID.SUPER_RANGING_1
    );

    public static final Set<Integer> MAGIC_POTIONS = ImmutableSet.of(
            ItemID.MAGIC_POTION4, ItemID.MAGIC_POTION3, ItemID.MAGIC_POTION4,
            ItemID.DIVINE_MAGIC_POTION4, ItemID.DIVINE_MAGIC_POTION3, ItemID.DIVINE_MAGIC_POTION2, ItemID.DIVINE_MAGIC_POTION1,
            ItemID.BATTLEMAGE_POTION4, ItemID.BATTLEMAGE_POTION3, ItemID.BATTLEMAGE_POTION2, ItemID.BATTLEMAGE_POTION1,
            ItemID.DIVINE_BATTLEMAGE_POTION4, ItemID.DIVINE_BATTLEMAGE_POTION3, ItemID.DIVINE_BATTLEMAGE_POTION2, ItemID.DIVINE_BATTLEMAGE_POTION1,
            ItemID.SUPER_MAGIC_POTION_4, ItemID.SUPER_MAGIC_POTION_3, ItemID.SUPER_MAGIC_POTION_2, ItemID.SUPER_MAGIC_POTION_1,
            ItemID.ANCIENT_BREW4, ItemID.ANCIENT_BREW3, ItemID.ANCIENT_BREW2, ItemID.ANCIENT_BREW1,
            ItemID.FORGOTTEN_BREW4, ItemID.FORGOTTEN_BREW3, ItemID.FORGOTTEN_BREW2, ItemID.FORGOTTEN_BREW1
    );

    public static final Map<Skill, int[]> skillPotions = ImmutableMap.<Skill, int[]>builder()
            .put(Skill.ATTACK, toIntArray(ATTACK_POTIONS))
            .put(Skill.STRENGTH, toIntArray(STRENGTH_POTIONS))
            .put(Skill.RANGED, toIntArray(RANGED_POTIONS))
            .put(Skill.MAGIC, toIntArray(MAGIC_POTIONS))
            .build();

    public static final Map<Skill, VisualNotificationType> skillNotificationType = ImmutableMap.<Skill, VisualNotificationType>builder()
            .put(Skill.ATTACK, VisualNotificationType.ATTACK_BELOW_THRESHOLD)
            .put(Skill.STRENGTH, VisualNotificationType.STRENGTH_BELOW_THRESHOLD)
            .put(Skill.RANGED, VisualNotificationType.RANGED_BELOW_THRESHOLD)
            .put(Skill.MAGIC, VisualNotificationType.MAGIC_BELOW_THRESHOLD)
            .build();

    public static final Set<Skill> trackedSkills = ImmutableSet.copyOf(skillPotions.keySet());

    private static final int[] toIntArray(Collection<Integer> collection) {
        return collection.stream().mapToInt(Integer::intValue).toArray();
    }
}
