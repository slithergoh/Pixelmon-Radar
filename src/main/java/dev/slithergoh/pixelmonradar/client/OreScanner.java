package dev.slithergoh.pixelmonradar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class OreScanner {
    private OreScanner() {}
    private static List<BlockPos> cached = List.of();
    private static long lastScan = 0;

    public static List<ResourceLocation> candidates() {
        ArrayList<ResourceLocation> out = new ArrayList<>();
        for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet()) {
            String p=id.getPath();
            if (p.contains("ore") || p.contains("bauxite") || p.contains("ruby") || p.contains("sapphire") || p.contains("platinum") || p.contains("silver") || p.contains("silicon") || p.contains("crystal")) out.add(id);
        }
        out.sort(Comparator.comparing(ResourceLocation::toString));
        return out;
    }

    public static List<BlockPos> scan() {
        Minecraft mc=Minecraft.getInstance();
        if(mc.level==null||mc.player==null||RadarSettings.selectedOres.isEmpty()) return List.of();
        long now=System.currentTimeMillis();
        if(now-lastScan<750) return cached;
        lastScan=now;
        int r=RadarSettings.oreRange;
        BlockPos c=mc.player.blockPosition();
        ArrayList<BlockPos> found=new ArrayList<>();
        for(BlockPos p:BlockPos.betweenClosed(c.offset(-r,-r,-r),c.offset(r,r,r))) {
            Block b=mc.level.getBlockState(p).getBlock();
            ResourceLocation id=BuiltInRegistries.BLOCK.getKey(b);
            if(RadarSettings.selectedOres.contains(id.toString())) found.add(p.immutable());
            if(found.size()>=1500) break;
        }
        cached=List.copyOf(found); return cached;
    }
}
