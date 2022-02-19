package pro.mikey.xray.xray;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.tuple.Pair;
import pro.mikey.xray.utils.BlockData;
import pro.mikey.xray.utils.RenderBlockProps;

import java.util.*;

public class RenderEnqueue {
	/**
	 * Use Controller.requestBlockFinder() to trigger a scan.
	 */
	public static Set<RenderBlockProps> blockFinder() {
        HashMap<UUID, BlockData> blocks = Controller.getBlockStore().getStore();
        if ( blocks.isEmpty() ) {
            return new HashSet<>(); // no need to scan the region if there's nothing to find
        }

		final Level world = Minecraft.getInstance().level;
        final Player player = Minecraft.getInstance().player;

		// Something is fatally wrong
        if( world == null || player == null ) {
			return new HashSet<>();
		}

		final Set<RenderBlockProps> renderQueue = new HashSet<>();

		int range = Controller.getHalfRange();

		int cX = player.chunkPosition().x;
		int cZ = player.chunkPosition().z;

		BlockState currentState;
		FluidState currentFluid;

		Pair<BlockData, UUID> dataWithUUID;
		ResourceLocation block;

		for (int i = cX - range; i <= cX + range; i++) {
			int chunkStartX = i << 4;
			for (int j = cZ - range; j <= cZ + range; j++) {
				int chunkStartZ = j << 4;

				int height =
						Arrays.stream(world.getChunk(i, j).getSections())
								.filter(Objects::nonNull)
								.mapToInt(LevelChunkSection::bottomBlockY)
								.max()
								.orElse(0);

				for (int k = chunkStartX; k < chunkStartX + 16; k++) {
					for (int l = chunkStartZ; l < chunkStartZ + 16; l++) {
						for (int m = world.getMinBuildHeight(); m < height + (1 << 4); m++) {
							BlockPos pos = new BlockPos(k, m, l);

							currentState = world.getBlockState(pos);
							currentFluid = currentState.getFluidState();

							if( (currentFluid.getType() == Fluids.LAVA || currentFluid.getType() == Fluids.FLOWING_LAVA) && Controller.isLavaActive() ) {
								renderQueue.add(new RenderBlockProps(pos.getX(), pos.getY(), pos.getZ(), 0xff0000));
								continue;
							}

							// Reject blacklisted blocks
							if( Controller.blackList.contains(currentState.getBlock()) )
								continue;

							block = currentState.getBlock().getRegistryName();
							if( block == null )
								continue;

							dataWithUUID = Controller.getBlockStore().getStoreByReference(block.toString());
							if( dataWithUUID == null )
								continue;

							if( dataWithUUID.getKey() == null || !dataWithUUID.getKey().isDrawing() ) // fail safe
								continue;

							// Push the block to the render queue
							renderQueue.add(new RenderBlockProps(pos.getX(), pos.getY(), pos.getZ(), dataWithUUID.getKey().getColor()));
						}
					}
				}
			}
		}

		return renderQueue;
//
////		final List<RenderBlockProps> renderQueue = new ArrayList<>();
//		int lowBoundX, highBoundX, lowBoundY, highBoundY, lowBoundZ, highBoundZ;
//
//		// Used for cleaning up the searching process
////		BlockState currentState;
////		FluidState currentFluid;
////
////		ResourceLocation block;
////		Pair<BlockData, UUID> dataWithUUID;
////
////		// Loop on chunks (x, z)
////		for ( int chunkX = box.minChunkX; chunkX <= box.maxChunkX; chunkX++ )
////		{
////			// Pre-compute the extend bounds on X
////			int x = chunkX << 4; // lowest x coord of the chunk in block/world coordinates
////			lowBoundX = (x < box.minX) ? box.minX - x : 0; // lower bound for x within the extend
////			highBoundX = (x + 15 > box.maxX) ? box.maxX - x : 15;// and higher bound. Basically, we clamp it to fit the radius.
////
////			for ( int chunkZ = box.minChunkZ; chunkZ <= box.maxChunkZ; chunkZ++ )
////			{
////				// Time to getStore the chunk (16x256x16) and split it into 16 vertical extends (16x16x16)
////				if (!world.hasChunk(chunkX, chunkZ)) {
////					continue; // We won't find anything interesting in unloaded chunks
////				}
////
////				LevelChunk chunk = world.getChunk( chunkX, chunkZ );
////				LevelChunkSection[] extendsList = chunk.getSections();
////
////				// Pre-compute the extend bounds on Z
////				int z = chunkZ << 4;
////				lowBoundZ = (z < box.minZ) ? box.minZ - z : 0;
////				highBoundZ = (z + 15 > box.maxZ) ? box.maxZ - z : 15;
////
////				// Loop on the extends around the player's layer (6 down, 2 up)
////				for ( int curExtend = box.minChunkY; curExtend <= box.maxChunkY; curExtend++ )
////				{
////					LevelChunkSection ebs = extendsList[curExtend];
////					if (ebs == null) // happens quite often!
////						continue;
////
////					// Pre-compute the extend bounds on Y
////					int y = curExtend << 4;
////					lowBoundY = (y < box.minY) ? box.minY - y : 0;
////					highBoundY = (y + 15 > box.maxY) ? box.maxY - y : 15;
////
////					// Now that we have an extend, let's check all its blocks
////					for ( int i = lowBoundX; i <= highBoundX; i++ ) {
////						for ( int j = lowBoundY; j <= highBoundY; j++ ) {
////							for ( int k = lowBoundZ; k <= highBoundZ; k++ ) {
////								currentState = ebs.getBlockState(i, j, k);
////								currentFluid = currentState.getFluidState();
////
////								if( (currentFluid.getType() == Fluids.LAVA || currentFluid.getType() == Fluids.FLOWING_LAVA) && Controller.isLavaActive() ) {
////									renderQueue.add(new RenderBlockProps(x + i, y + j, z + k, 0xff0000));
////									continue;
////								}
////
////								// Reject blacklisted blocks
////								if( Controller.blackList.contains(currentState.getBlock()) )
////									continue;
////
////								block = currentState.getBlock().getRegistryName();
////								if( block == null )
////									continue;
////
////								dataWithUUID = Controller.getBlockStore().getStoreByReference(block.toString());
////								if( dataWithUUID == null )
////									continue;
////
////								if( dataWithUUID.getKey() == null || !dataWithUUID.getKey().isDrawing() ) // fail safe
////									continue;
////
////								System.out.println(new BlockPos(x + i, y + j, z + k));
////								System.out.println(new BlockPos(x, y, z));
////								System.out.println(new BlockPos(i, j, k));
////
////								System.out.printf("Expected: %d %d %d%n", 31, 80, 16);
////								System.out.printf("Got:      %d %d %d%n", x + i, y + j, z + k);
////
////								// Push the block to the render queue
////								renderQueue.add(new RenderBlockProps(x + i, y + j, z + k, dataWithUUID.getKey().getColor()));
////							}
////						}
////					}
////				}
////			}
////		}
////		final BlockPos playerPos = player.blockPosition();
////		renderQueue.sort((t, t1) -> Double.compare(t1.getPos().distSqr(playerPos), t.getPos().distSqr((playerPos))));
////		Render.syncRenderList.clear();
////		Render.syncRenderList.addAll( renderQueue ); // Add all our found blocks to the Render.syncRenderList list. To be use by Render when drawing.
	}

	/**
	 * Single-block version of blockFinder. Can safely be called directly
	 * for quick block check.
	 * @param pos the BlockPos to check
	 * @param state the current state of the block
	 * @param add true if the block was added to world, false if it was removed
	 */
	public static void checkBlock(BlockPos pos, BlockState state, boolean add )
	{
		if ( !Controller.isXRayActive() || Controller.getBlockStore().getStore().isEmpty() )
		    return; // just pass

		// If we're removing then remove :D
		if( !add ) {
			Controller.syncRenderList.remove( new RenderBlockProps(pos,0) );
			return;
		}

		ResourceLocation block = state.getBlock().getRegistryName();
		if( block == null )
			return;

		Pair<BlockData, UUID> dataWithUUID = Controller.getBlockStore().getStoreByReference(block.toString());
		if( dataWithUUID == null || dataWithUUID.getKey() == null || !dataWithUUID.getKey().isDrawing() )
			return;

		// the block was added to the world, let's add it to the drawing buffer
		Controller.syncRenderList.add(new RenderBlockProps(pos, dataWithUUID.getKey().getColor()) );
		Render.requestedRefresh = true;
	}
}
