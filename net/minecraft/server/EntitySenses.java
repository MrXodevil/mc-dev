package net.minecraft.server;

import java.util.ArrayList;
import java.util.List;

public class EntitySenses {

    EntityLiving entity;
    List seenEntities = new ArrayList();
    List unseenEntities = new ArrayList();

    public EntitySenses(EntityLiving entityliving) {
        this.entity = entityliving;
    }

    public void a() {
        this.seenEntities.clear();
        this.unseenEntities.clear();
    }

    public boolean canSee(Entity entity) {
        if (this.seenEntities.contains(entity)) {
            return true;
        } else if (this.unseenEntities.contains(entity)) {
            return false;
        } else {
            this.entity.world.methodProfiler.a("canSee");
            boolean flag = this.entity.n(entity);

            this.entity.world.methodProfiler.b();
            if (flag) {
                this.seenEntities.add(entity);
            } else {
                this.unseenEntities.add(entity);
            }

            return flag;
        }
    }
}
