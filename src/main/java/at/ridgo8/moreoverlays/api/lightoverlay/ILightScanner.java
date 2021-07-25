package at.ridgo8.moreoverlays.api.lightoverlay;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface ILightScanner {

    void update(Player player);

    void clear();

    List<Pair<BlockPos, Byte>> getLightModes();
}
