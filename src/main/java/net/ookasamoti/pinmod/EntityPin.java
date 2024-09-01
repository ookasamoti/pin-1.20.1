package net.ookasamoti.pinmod;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class EntityPin extends Pin {
    private Entity entity;

    public EntityPin(Entity entity, UUID playerUUID, ResourceKey<Level> dimension) {
        super(entity.getX(), entity.getY() + entity.getBbHeight() + 1, entity.getZ(), true, playerUUID, dimension);
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public void updatePosition(Entity entity) {
        this.x = entity.getX();
        this.y = entity.getY() + entity.getBbHeight() + 1;
        this.z = entity.getZ();
        this.entity = entity;
    }

    public boolean shouldRemove() {
        if (entity == null || !entity.isAlive()) return true;
        assert Minecraft.getInstance().player != null;
        return entity.distanceTo(Minecraft.getInstance().player) > 512;
    }
}



