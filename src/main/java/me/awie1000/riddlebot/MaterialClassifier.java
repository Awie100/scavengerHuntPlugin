package me.awie1000.riddlebot;

import org.bukkit.Material;

import java.util.*;
import java.util.stream.Collectors;

public class MaterialClassifier {
    final Set<Material> survivalMaterials, creativeMaterials;

    public MaterialClassifier() {
        List<Material> creativeList = new ArrayList<>(Arrays.asList(
                Material.BARRIER,
                Material.STRUCTURE_VOID,
                Material.LIGHT,
                Material.COMMAND_BLOCK,
                Material.CHAIN_COMMAND_BLOCK,
                Material.REPEATING_COMMAND_BLOCK,
                Material.STRUCTURE_BLOCK,
                Material.JIGSAW,
                Material.PETRIFIED_OAK_SLAB,
                Material.PLAYER_HEAD,
                Material.KNOWLEDGE_BOOK,
                Material.DEBUG_STICK,
                Material.BEDROCK,
                Material.REINFORCED_DEEPSLATE,
                Material.BUDDING_AMETHYST,
                Material.CHORUS_PLANT,
                Material.DIRT_PATH,
                Material.END_PORTAL_FRAME,
                Material.FARMLAND,
                Material.SPAWNER,
                Material.BUNDLE,
                Material.COMMAND_BLOCK_MINECART
        ));

        creativeList.addAll(Arrays.stream(Material.values())
                .filter(material -> material.toString().startsWith("INFESTED") || material.toString().endsWith("SPAWN_EGG"))
                .collect(Collectors.toList())
        );

        creativeMaterials = new HashSet<>(creativeList);
        survivalMaterials = Arrays.stream(Material.values())
                .filter(material -> !creativeMaterials.contains(material) && material.isItem())
                .collect(Collectors.toSet());
    }

    public List<Material> randomSurvivalList(int amount) {
        Random rand = new Random();
        List<Material> survivalList = new ArrayList<>(survivalMaterials);
        if(amount >= survivalList.size()) return survivalList;
        for(int i = 0; i < amount; i++) {
            Collections.swap(survivalList, i, rand.nextInt(survivalList.size() - i) + i);
        }
        return survivalList.subList(0, amount);
    }

    public static String matToName(Material mat) {
        return mat.toString().toLowerCase().replace('_', ' ');
    }
}
