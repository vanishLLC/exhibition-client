package exhibition.management.waypoints;

import net.minecraft.util.Vec3;

/**
 * Created by Arithmo on 5/8/2017 at 2:47 PM.
 */
public class Waypoint {

    private final String name;
    private final Vec3 vec3;
    private final int color;
    private final String address;
    private final boolean temp;

    public Waypoint(String name, Vec3 vec3, int color, String address) {
        this(name, vec3, color, address, false);
    }

    public Waypoint(String name, Vec3 vec3, int color, String address, boolean temp) {
        this.name = name;
        this.vec3 = vec3;
        this.color = color;
        this.address = address;
        this.temp = temp;
    }

    public String getName() {
        return name;
    }

    public Vec3 getVec3() {
        return vec3;
    }

//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public void setVec3(Vec3 vec3) {
//        this.vec3 = vec3;
//    }
//
//    public void setVec3(double x, double y, double z) {
//        this.vec3 = new Vec3(x,y,z);
//    }

    public int getColor() {
        return color;
    }

    public String getAddress() {
        return address;
    }

    public boolean isTemp() {
        return temp;
    }

}
