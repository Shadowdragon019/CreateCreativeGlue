package com.roxxane.creative_glue;

import com.mojang.logging.LogUtils;
import com.roxxane.creative_glue.mixin_interfacese.SuperGlueEntityInterface;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.glue.SuperGlueItem;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Cg.id)
public class Cg {
    public static final String id = "creative_glue";
    @SuppressWarnings("unused")
    public static final Logger logger = LogUtils.getLogger();
    public static final Registrate registrate = Registrate.create(id);

    @SuppressWarnings("unused")
    public static final ItemEntry<CgItem> creative_glue_item =
        registrate.item("creative_glue", CgItem::new).register();

    public static final TagKey<Block> creative_glue_push_anyways =
        BlockTags.create(new ResourceLocation(id, "creative_glue_push_anyways"));

    public Cg() {
    }

    public static void set_is_creative(SuperGlueEntity entity, boolean value) {
        ((SuperGlueEntityInterface) entity).cg$setIsCreative(value);
    }

    public static boolean is_creative(Object object) {
        return (object instanceof SuperGlueEntity glue_entity && ((SuperGlueEntityInterface) glue_entity).cg$isCreative()) ||
            (object instanceof Player player && player.getMainHandItem().getItem() instanceof CgItem) ||
            (object instanceof ItemStack stack && stack.getItem() instanceof CgItem) ||
            (object instanceof CgItem);
    }

    public static int passive_color(Entity entity) {
        if (is_creative(entity)) return 0x634d91;
        else return 0x4D9162;
    }

    public static int highlight_color(Entity entity) {
        if (is_creative(entity)) return 0x8568c4;
        else return 0x68c586;
    }

    public static int fail_color(Entity entity) {
        if (is_creative(entity)) return 0x49c4b4;
        else return 0xc5b548;
    }

    public static int placement_color(Entity entity) {
        if (is_creative(entity)) return 0xc2afed;
        else return 0xB5F2C6;
    }

    @SuppressWarnings({"unchecked", "RedundantCast", "unused"})
    public static <T> T mixin_cast(Class<T> clazz, Object object) {
        return (T) (Object) object;
    }

    /** Checks the direction too */
    public static boolean is_creative_glued(LevelAccessor level, BlockPos blocks_pos, Direction direction) {
        BlockPos target_pos = blocks_pos.relative(direction);
        for (SuperGlueEntity glue : level.getEntitiesOfClass(SuperGlueEntity.class, SuperGlueEntity.span(blocks_pos, target_pos).inflate(16))) {
            if (!glue.contains(blocks_pos) || !glue.contains(target_pos)) continue;
            return is_creative(glue);
        }
        return false;
    }

    public static boolean is_creative_glued(LevelAccessor level, BlockPos blocks_pos) {
        for (SuperGlueEntity glue : level.getEntitiesOfClass(SuperGlueEntity.class, SuperGlueEntity.span(blocks_pos, blocks_pos).inflate(16))) {
            if (!glue.contains(blocks_pos)) continue;
            return is_creative(glue);
        }
        return false;
    }

    public static boolean can_use_glue(Player player, Item item) {
        if (player.isCreative()) return item instanceof SuperGlueItem;
        else return item instanceof SuperGlueItem && !Cg.is_creative(item);
    }
}