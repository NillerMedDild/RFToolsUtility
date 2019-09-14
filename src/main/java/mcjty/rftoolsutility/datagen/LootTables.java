package mcjty.rftoolsutility.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mcjty.rftoolsutility.modules.crafter.CrafterSetup;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.functions.CopyName;
import net.minecraft.world.storage.loot.functions.CopyNbt;
import net.minecraft.world.storage.loot.functions.SetContents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class LootTables extends LootTableProvider {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Map<Block, LootTable.Builder> lootTables = new HashMap<>();
    private final DataGenerator generator;

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
        this.generator = dataGeneratorIn;
    }

    @Override
    public void act(DirectoryCache cache) {
        LootPool.Builder builder = LootPool.builder()
                .name("crafter1")
                .rolls(ConstantRange.of(1))
                .addEntry(ItemLootEntry.builder(CrafterSetup.BLOCK_CRAFTER1)
                        .acceptFunction(CopyName.builder(CopyName.Source.BLOCK_ENTITY))
                        .acceptFunction(CopyNbt.func_215881_a(CopyNbt.Source.BLOCK_ENTITY)
                                .func_216055_a("Info", "BlockEntityTag.Info", CopyNbt.Action.REPLACE)
                                .func_216055_a("Items", "BlockEntityTag.Items", CopyNbt.Action.REPLACE)
                                .func_216055_a("Energy", "BlockEntityTag.Energy", CopyNbt.Action.REPLACE))
                        .acceptFunction(SetContents.func_215920_b()
                                .func_216075_a(DynamicLootEntry.func_216162_a(new ResourceLocation("minecraft", "contents"))))
                );
        LootTable.Builder tableBuilder = LootTable.builder().addLootPool(builder);
        lootTables.put(CrafterSetup.BLOCK_CRAFTER1, tableBuilder);

        Map<ResourceLocation, LootTable> tables = new HashMap<>();
        for (Map.Entry<Block, LootTable.Builder> entry : lootTables.entrySet()) {
            tables.put(entry.getKey().getLootTable(), entry.getValue().setParameterSet(LootParameterSets.BLOCK).build());
        }
        writeTables(cache, tables);
    }

    private void writeTables(DirectoryCache cache, Map<ResourceLocation, LootTable> tables) {
        Path outputFolder = this.generator.getOutputFolder();
        tables.forEach((key, lootTable) -> {
            Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
            try {
                IDataProvider.save(GSON, cache, LootTableManager.toJson(lootTable), path);
            } catch (IOException e) {
                LOGGER.error("Couldn't write loot table {}", path, e);
            }
        });
    }

    @Override
    public String getName() {
        return "RFToolsUtility LootTables";
    }
}
