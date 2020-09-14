package red.jackf.chesttracker.memory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import red.jackf.chesttracker.ChestTracker;
import red.jackf.chesttracker.mixins.AccessorMinecraftServer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.*;

@Environment(EnvType.CLIENT)
public class MemoryDatabase {
    private final String id;
    private final Map<Identifier, BiMap<BlockPos, Memory>> locations = new HashMap<>();
    private final Map<Identifier, BiMap<BlockPos, Memory>> namedLocations = new HashMap<>();

    @Nullable
    private static MemoryDatabase currentDatabase = null;

    private MemoryDatabase(String id) {
        this.id = id;
    }

    public Set<Identifier> getDimensions() {
        return locations.keySet();
    }

    @Nullable
    public static MemoryDatabase getCurrent() {
        String id = getUsableId();
        if (id == null) return null;
        if (currentDatabase != null && currentDatabase.getId().equals(id)) return currentDatabase;
        MemoryDatabase database = new MemoryDatabase(id);
        database.load();
        currentDatabase = database;
        ChestTracker.LOGGER.info("Loaded " + id);
        return database;
    }

    public String getId() {
        return id;
    }

    @Nullable
    private static String getUsableId() {
        MinecraftClient mc = MinecraftClient.getInstance();
        String id = null;
        if (mc.isInSingleplayer() && mc.getServer() != null) {
            id = "singleplayer-" + MemoryUtils.getSingleplayerName(((AccessorMinecraftServer) mc.getServer()).getSession());
        } else if (mc.isConnectedToRealms()) {
            id = "realms-canonlysupport1rightnowsorry";
        } else {
            ClientPlayNetworkHandler cpnh = mc.getNetworkHandler();
            if (cpnh != null) {
                SocketAddress address = cpnh.getConnection().getAddress();
                if (address instanceof InetSocketAddress) {
                    InetSocketAddress inet = ((InetSocketAddress) address);
                    id = "multiplayer-" + inet.getAddress() + (inet.getPort() == 25565 ? "" : "-" + inet.getPort());
                } else {
                    id = "multiplayer-" + MemoryUtils.makeFileSafe(address.toString());
                }
            }
        }

        return id;
    }

    public void save() {
        Path savePath = getFilePath();
    }

    public void load() {
        Path loadPath = getFilePath();
    }

    @NotNull
    public Path getFilePath() {
        return FabricLoader.getInstance().getGameDir().resolve("chesttracker").resolve(id + ".json");
    }

    public List<ItemStack> getItems(Identifier worldId) {
        if (locations.containsKey(worldId)) {
            Map<LightweightStack, Integer> count = new HashMap<>();
            BiMap<BlockPos, Memory> location = locations.get(worldId);
            location.forEach((pos, memory) -> memory.getItems().forEach(stack -> {
                LightweightStack lightweightStack = new LightweightStack(stack.getItem(), stack.getTag());
                count.merge(lightweightStack, stack.getCount(), Integer::sum);
            }));
            List<ItemStack> results = new ArrayList<>();
            count.forEach(((lightweightStack, integer) -> {
                ItemStack stack = new ItemStack(lightweightStack.getItem(), integer);
                stack.setTag(lightweightStack.getTag());
                results.add(stack);
            }));
            return results;
        } else {
            return Collections.emptyList();
        }
    }

    public Collection<Memory> getNamedMemories(Identifier worldId) {
        if (namedLocations.containsKey(worldId)) {
            return namedLocations.get(worldId).values();
        } else {
            return Collections.emptyList();
        }
    }

    public void mergeItems(Identifier worldId, Memory memory) {
        BiMap<BlockPos, Memory> map;
        if (!locations.containsKey(worldId)) {
            map = HashBiMap.create();
            locations.put(worldId, map);
        } else {
            map = locations.get(worldId);
        }
        map.put(memory.getPosition(), memory);
        if (memory.getTitle() != null) {
            BiMap<BlockPos, Memory> namedMap;
            if (!namedLocations.containsKey(worldId)) {
                namedMap = HashBiMap.create();
                namedLocations.put(worldId, namedMap);
            } else {
                namedMap = namedLocations.get(worldId);
            }
            namedMap.put(memory.getPosition(), memory);
        }
    }

    public void removePos(Identifier worldId, BlockPos pos) {
        BiMap<BlockPos, Memory> location = locations.get(worldId);
        if (location != null) location.remove(pos);
        BiMap<BlockPos, Memory> namedLocation = namedLocations.get(worldId);
        if (namedLocation != null) namedLocation.remove(pos);
    }

    public List<Memory> findItems(ItemStack toFind, Identifier worldId) {
        List<Memory> found = new ArrayList<>();
        BiMap<BlockPos, Memory> location = locations.get(worldId);
        if (location != null) {
            for (Map.Entry<BlockPos, Memory> entry : location.entrySet()) {
                if (entry.getKey() != null) {
                    if (entry.getValue().getItems().stream().anyMatch(candidate -> MemoryUtils.areStacksEquivalent(toFind, candidate, !toFind.hasTag()))) {
                        found.add(entry.getValue());
                    }
                }
            }
        }
        return found;
    }

}
