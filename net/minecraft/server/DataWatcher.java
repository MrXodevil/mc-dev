package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataWatcher {

    private boolean a = true;
    private static final HashMap b = new HashMap();
    private final Map c = new HashMap();
    private boolean d;
    private ReadWriteLock e = new ReentrantReadWriteLock();

    public DataWatcher() {}

    public void a(int i, Object object) {
        Integer integer = (Integer) b.get(object.getClass());

        if (integer == null) {
            throw new IllegalArgumentException("Unknown data type: " + object.getClass());
        } else if (i > 31) {
            throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + 31 + ")");
        } else if (this.c.containsKey(Integer.valueOf(i))) {
            throw new IllegalArgumentException("Duplicate id value for " + i + "!");
        } else {
            WatchableObject watchableobject = new WatchableObject(integer.intValue(), i, object);

            this.e.writeLock().lock();
            this.c.put(Integer.valueOf(i), watchableobject);
            this.e.writeLock().unlock();
            this.a = false;
        }
    }

    public void a(int i, int j) {
        WatchableObject watchableobject = new WatchableObject(j, i, null);

        this.e.writeLock().lock();
        this.c.put(Integer.valueOf(i), watchableobject);
        this.e.writeLock().unlock();
        this.a = false;
    }

    public byte getByte(int i) {
        return ((Byte) this.i(i).b()).byteValue();
    }

    public short getShort(int i) {
        return ((Short) this.i(i).b()).shortValue();
    }

    public int getInt(int i) {
        return ((Integer) this.i(i).b()).intValue();
    }

    public String getString(int i) {
        return (String) this.i(i).b();
    }

    public ItemStack getItemStack(int i) {
        return (ItemStack) this.i(i).b();
    }

    private WatchableObject i(int i) {
        this.e.readLock().lock();

        WatchableObject watchableobject;

        try {
            watchableobject = (WatchableObject) this.c.get(Integer.valueOf(i));
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Getting synched entity data");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Synched entity data");

            crashreportsystemdetails.a("Data ID", Integer.valueOf(i));
            throw new ReportedException(crashreport);
        }

        this.e.readLock().unlock();
        return watchableobject;
    }

    public void watch(int i, Object object) {
        WatchableObject watchableobject = this.i(i);

        if (!object.equals(watchableobject.b())) {
            watchableobject.a(object);
            watchableobject.a(true);
            this.d = true;
        }
    }

    public void h(int i) {
        WatchableObject.a(this.i(i), true);
        this.d = true;
    }

    public boolean a() {
        return this.d;
    }

    public static void a(List list, DataOutputStream dataoutputstream) {
        if (list != null) {
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                WatchableObject watchableobject = (WatchableObject) iterator.next();

                a(dataoutputstream, watchableobject);
            }
        }

        dataoutputstream.writeByte(127);
    }

    public List b() {
        ArrayList arraylist = null;

        if (this.d) {
            this.e.readLock().lock();
            Iterator iterator = this.c.values().iterator();

            while (iterator.hasNext()) {
                WatchableObject watchableobject = (WatchableObject) iterator.next();

                if (watchableobject.d()) {
                    watchableobject.a(false);
                    if (arraylist == null) {
                        arraylist = new ArrayList();
                    }

                    arraylist.add(watchableobject);
                }
            }

            this.e.readLock().unlock();
        }

        this.d = false;
        return arraylist;
    }

    public void a(DataOutputStream dataoutputstream) {
        this.e.readLock().lock();
        Iterator iterator = this.c.values().iterator();

        while (iterator.hasNext()) {
            WatchableObject watchableobject = (WatchableObject) iterator.next();

            a(dataoutputstream, watchableobject);
        }

        this.e.readLock().unlock();
        dataoutputstream.writeByte(127);
    }

    public List c() {
        ArrayList arraylist = null;

        this.e.readLock().lock();

        WatchableObject watchableobject;

        for (Iterator iterator = this.c.values().iterator(); iterator.hasNext(); arraylist.add(watchableobject)) {
            watchableobject = (WatchableObject) iterator.next();
            if (arraylist == null) {
                arraylist = new ArrayList();
            }
        }

        this.e.readLock().unlock();
        return arraylist;
    }

    private static void a(DataOutputStream dataoutputstream, WatchableObject watchableobject) {
        int i = (watchableobject.c() << 5 | watchableobject.a() & 31) & 255;

        dataoutputstream.writeByte(i);
        switch (watchableobject.c()) {
        case 0:
            dataoutputstream.writeByte(((Byte) watchableobject.b()).byteValue());
            break;

        case 1:
            dataoutputstream.writeShort(((Short) watchableobject.b()).shortValue());
            break;

        case 2:
            dataoutputstream.writeInt(((Integer) watchableobject.b()).intValue());
            break;

        case 3:
            dataoutputstream.writeFloat(((Float) watchableobject.b()).floatValue());
            break;

        case 4:
            Packet.a((String) watchableobject.b(), dataoutputstream);
            break;

        case 5:
            ItemStack itemstack = (ItemStack) watchableobject.b();

            Packet.a(itemstack, dataoutputstream);
            break;

        case 6:
            ChunkCoordinates chunkcoordinates = (ChunkCoordinates) watchableobject.b();

            dataoutputstream.writeInt(chunkcoordinates.x);
            dataoutputstream.writeInt(chunkcoordinates.y);
            dataoutputstream.writeInt(chunkcoordinates.z);
        }
    }

    public static List a(DataInputStream datainputstream) {
        ArrayList arraylist = null;

        for (byte b0 = datainputstream.readByte(); b0 != 127; b0 = datainputstream.readByte()) {
            if (arraylist == null) {
                arraylist = new ArrayList();
            }

            int i = (b0 & 224) >> 5;
            int j = b0 & 31;
            WatchableObject watchableobject = null;

            switch (i) {
            case 0:
                watchableobject = new WatchableObject(i, j, Byte.valueOf(datainputstream.readByte()));
                break;

            case 1:
                watchableobject = new WatchableObject(i, j, Short.valueOf(datainputstream.readShort()));
                break;

            case 2:
                watchableobject = new WatchableObject(i, j, Integer.valueOf(datainputstream.readInt()));
                break;

            case 3:
                watchableobject = new WatchableObject(i, j, Float.valueOf(datainputstream.readFloat()));
                break;

            case 4:
                watchableobject = new WatchableObject(i, j, Packet.a(datainputstream, 64));
                break;

            case 5:
                watchableobject = new WatchableObject(i, j, Packet.c(datainputstream));
                break;

            case 6:
                int k = datainputstream.readInt();
                int l = datainputstream.readInt();
                int i1 = datainputstream.readInt();

                watchableobject = new WatchableObject(i, j, new ChunkCoordinates(k, l, i1));
            }

            arraylist.add(watchableobject);
        }

        return arraylist;
    }

    public boolean d() {
        return this.a;
    }

    static {
        b.put(Byte.class, Integer.valueOf(0));
        b.put(Short.class, Integer.valueOf(1));
        b.put(Integer.class, Integer.valueOf(2));
        b.put(Float.class, Integer.valueOf(3));
        b.put(String.class, Integer.valueOf(4));
        b.put(ItemStack.class, Integer.valueOf(5));
        b.put(ChunkCoordinates.class, Integer.valueOf(6));
    }
}
