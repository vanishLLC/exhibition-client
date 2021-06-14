package exhibition.util;

import net.minecraft.entity.EntityLivingBase;

public class EntityUtil implements MinecraftUtil {

    public static float getMaxHealth(EntityLivingBase entityLivingBase) {
        if (entityLivingBase.isDead || Float.isNaN(entityLivingBase.getHealth())) {
            return 20;
        }

        float health = Float.isNaN(entityLivingBase.getHealth()) ? 0 : entityLivingBase.getHealth();

        return Math.max(health, Math.max(entityLivingBase.getMaxHealth(), 1));
    }

    public static float getHealthPercent(EntityLivingBase entityLivingBase) {
        if (entityLivingBase.isDead || Float.isNaN(entityLivingBase.getHealth())) {
            return 0;
        }

        float health = Float.isNaN(entityLivingBase.getHealth()) ? 0 : entityLivingBase.getHealth();

        return Math.min(1, Math.max(0, health / getMaxHealth(entityLivingBase)));
    }

}
