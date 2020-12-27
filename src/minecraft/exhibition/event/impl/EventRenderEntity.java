package exhibition.event.impl;

import exhibition.event.Event;
import net.minecraft.entity.EntityLivingBase;

/**
 * Created by Arithmo on 10/7/2017 at 11:42 PM.
 */
public class EventRenderEntity extends Event {

    private EntityLivingBase entity;
    private boolean pre;
    private float limbSwing;
    private float limbSwingAmount;
    private float ageInTicks;
    private float rotationYawHead;
    private float rotationPitch;
    private float chestRot;
    private float offset;

    public void fire(EntityLivingBase entity, boolean pre, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYawHead, float rotationPitch, float chestRot, float offset) {
        this.entity = entity;
        this.pre = pre;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.rotationYawHead = rotationYawHead;
        this.rotationPitch = rotationPitch;
        this.chestRot = chestRot;
        this.offset = offset;
        super.fire();
    }

    public void fire(EntityLivingBase entity, boolean pre) {
        this.entity = entity;
        this.pre = pre;
        super.fire();
    }

    public EntityLivingBase getEntity() {
        return entity;
    }

    public boolean isPre() {
        return pre;
    }

    public boolean isPost() {
        return !pre;
    }

    public float getLimbSwing()
    {
        return this.limbSwing;
    }

    public void setLimbSwing(float limbSwing)
    {
        this.limbSwing = limbSwing;
    }

    public float getLimbSwingAmount()
    {
        return this.limbSwingAmount;
    }

    public void setLimbSwingAmount(float limbSwingAmount)
    {
        this.limbSwingAmount = limbSwingAmount;
    }

    public float getAgeInTicks()
    {
        return this.ageInTicks;
    }

    public void setAgeInTicks(float ageInTicks)
    {
        this.ageInTicks = ageInTicks;
    }

    public float getRotationYawHead()
    {
        return this.rotationYawHead;
    }

    public void setRotationYawHead(float rotationYawHead)
    {
        this.rotationYawHead = rotationYawHead;
    }

    public float getRotationPitch()
    {
        return this.rotationPitch;
    }

    public void setRotationPitch(float rotationPitch)
    {
        this.rotationPitch = rotationPitch;
    }

    public float getOffset()
    {
        return this.offset;
    }

    public void setOffset(float offset)
    {
        this.offset = offset;
    }

    public float getRotationChest()
    {
        return this.chestRot;
    }

    public void setRotationChest(float rotationChest)
    {
        this.chestRot = rotationChest;
    }

}
