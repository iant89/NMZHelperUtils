package com.github.iant89.ultimatenmz.skills;

import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.iant89.ultimatenmz.skills.SkillConstants.trackedSkills;

public class SkillTracker {
    private final Map<Skill, Integer> skillBoosts = new EnumMap<>(Skill.class);

    public void onStatChanged(StatChanged event) {
        Skill skill = event.getSkill();
        if (!trackedSkills.contains(skill)) {
            return;
        }

        skillBoosts.put(skill, getBoost(event));
    }

    public Set<Skill> getUnboostedSkills(Map<Skill, Integer> minimumBoosts) {
        return trackedSkills.stream()
                .filter(minimumBoosts::containsKey)
                .filter(isBoosted(minimumBoosts).negate())
                .collect(Collectors.toSet());
    }

    private Predicate<Skill> isBoosted(Map<Skill, Integer> minimumBoosts) {
        return skill -> skillBoosts.getOrDefault(skill, 0) >= minimumBoosts.getOrDefault(skill, 0);
    }

    private static int getBoost(StatChanged event) {
        return event.getBoostedLevel() - event.getLevel();
    }
}
