package datastructures.byteset;


import java.util.Arrays;

@SuppressWarnings("Duplicates")
public class ByteArraySet {
    /**
     * The default initial capacity - MUST be a power of two.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    Entry[] table;
    int size;
    int threshold;
    final float loadFactor;

    public ByteArraySet() {
        loadFactor = DEFAULT_LOAD_FACTOR;
    }

    static final int hashCode(byte[] key) {
        return Arrays.hashCode(key);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public boolean contains(byte[] key) {
        return getEntry(hashCode(key), key) != null;
    }

    public byte[] get(byte[] key) {
        Entry e;
        return (e = getEntry(hashCode(key), key)) == null ? null : e.key;
    }

    public byte[] add(byte[] key) {
        Entry e;
        return (e = addEntry(hashCode(key), key)) == null ? null : e.key;
    }

    public byte[] remove(byte[] key) {
        Entry e;
        return (e = removeNode(hashCode(key), key)) == null ?
                null : e.key;
    }

    /**
     * Removes all of the mappings from this map.
     * The set will be empty after this call returns.
     */
    public void clear() {
        Entry[] tab;
        if ((tab = table) != null && size > 0) {
            size = 0;
            for (int i = 0; i < tab.length; ++i)
                tab[i] = null;
        }
    }

    Entry getEntry(int hash, byte[] key) {
        Entry[] tab = table;
        Entry first, e;
        int n;
        byte[] k;

        if (key == null
                || tab == null
                || (n = tab.length) < 1) {
            return null;
        }
        first = tab[(n-1) & hash];

        if (first == null) return null;

        if (first.hash == hash && ((k = first.key) == key || Arrays.equals(key, k))) {
            return first;
        }
        if ((e = first.next) == null) {
            return null;
        }

        do {
            if (e.hash == hash && ((k = e.key) == key || Arrays.equals(key, k))) {
                return e;
            }
        } while ((e = e.next) != null);
        return null;
    }

    Entry addEntry(int hash, byte[] key) {
        if (key == null) {
            return null;
        }

        Entry[] tab = table;
        int n;

        if (tab == null || (n = tab.length) < 1) {
            tab = resize();
            n = tab.length;
        }

        int i;
        Entry first = tab[i = (n-1) & hash];
        if (first == null) {
            tab[i] = new Entry(hash, key, null);
            hit();
            //the entry was not in: return null
            return null;
        }

        byte[] k;
        if (first.hash == hash && ((k = first.key) == key || Arrays.equals(key, k))) {
            //the entry is already in
            return first;
        }
        Entry e;
        if ((e=first.next) == null) {
            first.next = new Entry(hash, key, null);
            hit();
            return null;
        }
        Entry prev;
        do {
            if (e.hash == hash && ((k = e.key) == key || Arrays.equals(key, k))) {
                //the entry is already in
                return e;
            }
            prev = e;
        } while ((e = e.next) != null);

        //reached the end of the bucket and did not find it, so add it at the end
        prev.next = new Entry(hash, key, null);
        hit();
        return null;
    }

    Entry[] resize() {
        Entry[] oldTable = table;
        int oldCapacity = (oldTable == null) ? 0 : oldTable.length;
        int oldThreshold = threshold;

        int newCapacity;
        int newThreshold = 0;

        if (oldCapacity > 0) {
            if (oldCapacity >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTable;
            }
            else if ((newCapacity = oldCapacity << 1) < MAXIMUM_CAPACITY &&
                    oldCapacity >= DEFAULT_INITIAL_CAPACITY)
                newThreshold = oldThreshold << 1; // double threshold
        }
        else if (oldThreshold > 0) // initial capacity was placed in threshold
            newCapacity = oldThreshold;
        else {               // zero initial threshold signifies using defaults
            newCapacity = DEFAULT_INITIAL_CAPACITY;
            newThreshold = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThreshold == 0) {
            float ft = (float)newCapacity * loadFactor;
            newThreshold = (newCapacity < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                    (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThreshold;

        Entry[] newTable = new Entry[newCapacity];
        table = newTable;

        if (oldTable != null) {
            for (int j = 0; j < oldCapacity; ++j) {
                Entry e;
                if ((e = oldTable[j]) != null) {
                    oldTable[j] = null;
                    if (e.next == null)
                        newTable[e.hash & (newCapacity - 1)] = e;
                    else { // preserve order
                        Entry loHead = null, loTail = null;
                        Entry hiHead = null, hiTail = null;
                        Entry next;
                        do {
                            next = e.next;
                            if ((e.hash & oldCapacity) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTable[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTable[j + oldCapacity] = hiHead;
                        }
                    }
                }
            }
        }

        return newTable;
    }

    Entry removeNode(int hash, byte[] key) {
        Entry[] tab = table;
        Entry first, e;
        int n = tab.length;
        byte[] k;

        if (key == null
                || tab == null
                || n < 1) {
            return null;
        }
        int i;
        first = tab[i = (n-1) & hash];

        if (first == null) return null;

        if (first.hash == hash && ((k = first.key) == key || Arrays.equals(key, k))) {
            tab[i] = first.next;
            unhit();
            return first;
        }

        Entry previous = first;
        if ((e = first.next) == null) {
            return null;
        }

        do {
            if (e.hash == hash && ((k = e.key) == key || Arrays.equals(key, k))) {
                previous.next = e.next;
                unhit();
                return e;
            }
            previous = e;
        } while ((e = e.next) != null);
        return null;
    }

    void hit() {
        if (++size > threshold) {
            resize();
        }
    }

    void unhit() {
        --size;
    }

    class Entry {
        final int hash;
        final byte[] key;
        Entry next;

        Entry(int hash, byte[] key, Entry next) {
            this.hash = hash;
            this.key = key;
            this.next = next;
        }

        public byte[] getKey() {
            return key;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Entry) {
                Entry e = (Entry)o;
                return Arrays.equals(key, e.getKey());
            }
            return false;
        }
    }
}
