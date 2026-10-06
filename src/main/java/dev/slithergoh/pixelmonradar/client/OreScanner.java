package dev.slithergoh.pixelmonradar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import java.util.*;

public final class OreScanner {
    private OreScanner() {}

    private static volatile List<BlockPos> cached = List.of();
    private static final Map<Long, List<BlockPos>> chunkCache = new HashMap<>();
    private static final ArrayDeque<Long> scanQueue = new ArrayDeque<>();
    private static final Set<Long> queued = new HashSet<>();
    private static final Set<Block> selectedBlocks = new HashSet<>();
    private static int selectionHash = Integer.MIN_VALUE;
    private static int lastRange = -1;
    private static BlockPos lastCenter = BlockPos.ZERO;
    private static int ticks;

    public enum Category { USEFUL, ORES, LOOT, PLANTS, UTILITY, ALL }

    public static List<ResourceLocation> candidates() { return candidates(Category.ALL); }

    public static List<ResourceLocation> candidates(Category cat) {
        ArrayList<ResourceLocation> out = new ArrayList<>();
        for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet()) if (matches(id, cat)) out.add(id);
        out.sort(Comparator.comparing(ResourceLocation::toString));
        return out;
    }

    private static boolean matches(ResourceLocation id, Category c) {
        String n=id.toString().toLowerCase(), p=id.getPath().toLowerCase();
        boolean ore=p.contains("ore")||p.contains("bauxite")||p.contains("ruby")||p.contains("sapphire")||p.contains("platinum")||p.contains("silver")||p.contains("silicon")||p.contains("crystal");
        boolean loot=p.contains("pokeloot")||p.contains("poke_loot")||p.contains("pokeball")||p.contains("poke_ball")||p.contains("pokestop")||p.contains("poke_stop")||p.contains("grotto")||p.contains("loot");
        boolean plant=p.contains("apricorn")||p.contains("berry")||p.contains("plant")||p.contains("crop");
        boolean util=p.contains("healer")||p.contains("pc")||p.contains("trade")||p.contains("fossil")||p.contains("den")||p.contains("shrine")||p.contains("machine");
        return switch(c){case ORES->ore;case LOOT->loot;case PLANTS->plant;case UTILITY->util;case USEFUL->n.startsWith("pixelmon:")&&(loot||ore||util);case ALL->true;};
    }

    public static List<BlockPos> scan() { return cached; }

    public static void tick() {
        Minecraft mc=Minecraft.getInstance();
        if(mc.level==null||mc.player==null||!RadarSettings.oreEspEnabled) return;
        if(RadarSettings.selectedOres.isEmpty()){clear();return;}

        BlockPos center=mc.player.blockPosition();
        int range=RadarSettings.oreRange;
        int hash=RadarSettings.selectedOres.hashCode();

        if(hash!=selectionHash){
            selectionHash=hash;
            selectedBlocks.clear();
            for(String s:RadarSettings.selectedOres){
                ResourceLocation id=ResourceLocation.tryParse(s);
                if(id!=null) selectedBlocks.add(BuiltInRegistries.BLOCK.get(id));
            }
            chunkCache.clear();scanQueue.clear();queued.clear();cached=List.of();
            enqueueVisible(mc,center,range);
        } else if(range!=lastRange || center.distSqr(lastCenter)>64) {
            enqueueVisible(mc,center,range);
        }

        lastCenter=center;lastRange=range;

        // Refresh one loaded chunk per tick. Existing chunks remain visible while it refreshes.
        Long key=scanQueue.pollFirst();
        if(key!=null){
            queued.remove(key);
            int cx=(int)(key>>32), cz=(int)(long)key;
            if(mc.level.hasChunk(cx,cz)) scanChunk(mc,cx,cz,center,range);
            else chunkCache.remove(key);
            rebuildVisible(center,range);
        } else if(++ticks%40==0) {
            // Periodic refresh catches mined/placed blocks without clearing the display.
            enqueueVisible(mc,center,range);
        }
    }

    private static void enqueueVisible(Minecraft mc,BlockPos center,int range){
        int cr=(range+15)/16+1, pcx=center.getX()>>4, pcz=center.getZ()>>4;
        ArrayList<long[]> ordered=new ArrayList<>();
        for(int dz=-cr;dz<=cr;dz++) for(int dx=-cr;dx<=cr;dx++){
            int cx=pcx+dx,cz=pcz+dz;
            if(!mc.level.hasChunk(cx,cz)) continue;
            long key=key(cx,cz);
            ordered.add(new long[]{key,(long)dx*dx+(long)dz*dz});
        }
        ordered.sort(Comparator.comparingLong(a->a[1]));
        for(long[] e:ordered) if(queued.add(e[0])) scanQueue.addLast(e[0]);
    }

    private static void scanChunk(Minecraft mc,int cx,int cz,BlockPos center,int range){
        ArrayList<BlockPos> matches=new ArrayList<>();
        int minY=Math.max(mc.level.getMinBuildHeight(),center.getY()-range);
        int maxY=Math.min(mc.level.getMaxBuildHeight()-1,center.getY()+range);
        int minX=cx<<4,minZ=cz<<4;
        BlockPos.MutableBlockPos p=new BlockPos.MutableBlockPos();
        for(int y=minY;y<=maxY;y++) for(int z=minZ;z<minZ+16;z++) for(int x=minX;x<minX+16;x++){
            p.set(x,y,z);
            if(selectedBlocks.contains(mc.level.getBlockState(p).getBlock())) matches.add(p.immutable());
        }
        chunkCache.put(key(cx,cz),List.copyOf(matches));
    }

    private static void rebuildVisible(BlockPos center,int range){
        double rr=(double)range*range;
        ArrayList<BlockPos> out=new ArrayList<>();
        for(List<BlockPos> list:chunkCache.values()) for(BlockPos p:list) if(p.distSqr(center)<=rr) out.add(p);
        out.sort(Comparator.comparingDouble(p->p.distSqr(center)));
        if(out.size()>2500) out.subList(2500,out.size()).clear();
        cached=List.copyOf(out);
    }

    private static long key(int cx,int cz){return ((long)cx<<32)^(cz&0xffffffffL);}
    private static void clear(){cached=List.of();chunkCache.clear();scanQueue.clear();queued.clear();selectedBlocks.clear();selectionHash=Integer.MIN_VALUE;}
}