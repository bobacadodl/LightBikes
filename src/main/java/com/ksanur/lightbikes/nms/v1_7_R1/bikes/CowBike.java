package com.ksanur.lightbikes.nms.v1_7_R1.bikes;

import net.minecraft.server.v1_7_R1.World;

/**
 * User: bobacadodl
 * Date: 1/22/14
 * Time: 10:05 PM
 */
public class CowBike extends AgeableBike implements com.ksanur.lightbikes.nms.bikes.Bike {
    public CowBike(World world) {
        super(world);
    }

    protected void makeStepSound() {
        this.makeSound("mob.cow.step", 0.15F, 1.0F);
    }
}
