package datastructures.byteset;


import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("Duplicates")
public class ByteArrayMap<V> {
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

    Entry<V>[] table;
    int size;
    int threshold;
    final float loadFactor;

    public ByteArrayMap() {
        loadFactor = DEFAULT_LOAD_FACTOR;
    }

    static final int hash(byte[] key) {
        return Arrays.hashCode(key);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public boolean contains(byte[] key) {
        return getEntry(hash(key), key) != null;
    }

    public V get(byte[] key) {
        Entry<V> e;
        return (e = getEntry(hash(key), key)) == null ? null : e.value;
    }

    public V put(byte[] key, V value) {
        return putEntry(hash(key), key, value);
    }

    public V remove(byte[] key) {
        Entry<V> e;
        return (e = removeNode(hash(key), key)) == null ?
                null : e.value;
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

    public boolean containsKey(byte[] key) {
        return getEntry(hash(key), key) != null;
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value
     */
    public boolean containsValue(Object value) {
        Entry<V>[] tab; V v;
        if ((tab = table) != null && size > 0) {
            for (int i = 0; i < tab.length; ++i) {
                for (Entry<V> e = tab[i]; e != null; e = e.next) {
                    if ((v = e.value) == value ||
                            (value != null && value.equals(v)))
                        return true;
                }
            }
        }
        return false;
    }

    public int hashCode() {
        int h = 0;
        Iterator<Entry<V>> i = entrySet().iterator();
        while (i.hasNext())
            h += i.next().hashCode();
        return h;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Map))
            return false;
        ByteArrayMap<?> m = (ByteArrayMap<?>) o;
        if (m.size() != size())
            return false;

        try {
            Iterator<Entry<V>> i = entrySet().iterator();
            while (i.hasNext()) {
                Entry<V> e = i.next();
                byte[] key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(m.get(key)==null && m.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(m.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
    }

    Entry<V> getEntry(int hash, byte[] key) {
        Entry<V>[] tab = table;
        Entry<V> first, e;
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

    V putEntry(int hash, byte[] key, V value) {
        if (key == null) {
            return null;
        }

        Entry<V>[] tab = table;
        int n;

        if (tab == null || (n = tab.length) < 1) {
            tab = resize();
            n = tab.length;
        }

        int i;
        Entry<V> first = tab[i = (n-1) & hash];
        if (first == null) {
            tab[i] = new Entry<>(hash, key, value, null);
            hit();
            //the entry was not in: return null
            return null;
        }

        byte[] k;
        if (first.hash == hash && ((k = first.key) == key || Arrays.equals(key, k))) {
            //the entry is already in
            V oldValue = first.value;
            first.value = value;
            return oldValue;
        }
        Entry<V> e;
        if ((e=first.next) == null) {
            first.next = new Entry<>(hash, key, value, null);
            hit();
            return null;
        }
        Entry<V> prev;
        do {
            if (e.hash == hash && ((k = e.key) == key || Arrays.equals(key, k))) {
                //the entry is already in
                V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
            prev = e;
        } while ((e = e.next) != null);

        //reached the end of the bucket and did not find it, so add it at the end
        prev.next = new Entry<>(hash, key, value,null);
        hit();
        return null;
    }

    Entry<V>[] resize() {
        Entry<V>[] oldTable = table;
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

        @SuppressWarnings(value="unchecked")
        Entry<V>[] newTable = (Entry<V>[])new Entry[newCapacity];
        table = newTable;

        if (oldTable != null) {
            for (int j = 0; j < oldCapacity; ++j) {
                Entry<V> e;
                if ((e = oldTable[j]) != null) {
                    oldTable[j] = null;
                    if (e.next == null)
                        newTable[e.hash & (newCapacity - 1)] = e;
                    else { // preserve order
                        Entry<V> loHead = null, loTail = null;
                        Entry<V> hiHead = null, hiTail = null;
                        Entry<V> next;
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

    Entry<V> removeNode(int hash, byte[] key) {
        Entry<V>[] tab = table;
        Entry<V> first, e;
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

        Entry<V> previous = first;
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

    public static class Entry<T> {
        final int hash;
        final byte[] key;
        T value;
        Entry<T> next;

        Entry(int hash, byte[] key, T value, Entry<T> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public byte[] getKey() {
            return key;
        }

        public T getValue() {
            return value;
        }

        public final int hashCode() {
            return Arrays.hashCode(key) ^ Objects.hashCode(value);
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Entry) {
                Entry<?> e = (Entry<?>)o;
                return Arrays.equals(key, e.getKey()) && Objects.equals(value, e.value);
            }
            return false;
        }
    }

    transient Collection<V> values;
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;
        }
        return vs;
    }

    final class Values extends AbstractCollection<V> {
        public final int size()                 { return size; }
        public final void clear()               { ByteArrayMap.this.clear(); }
        public final Iterator<V> iterator()     { return new ValueIterator(); }
        public final boolean contains(Object o) { return containsValue(o); }

        public final void forEach(Consumer<? super V> action) {
            Entry<V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                for (int i = 0; i < tab.length; ++i) {
                    for (Entry<V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.value);
                }
            }
        }
    }

    transient Set<Entry<V>> entrySet;
    public Set<Entry<V>> entrySet() {
        Set<Entry<V>> es;
        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
    }

    final class EntrySet extends AbstractSet<Entry<V>> {
        public final int size()                 { return size; }
        public final void clear()               { ByteArrayMap.this.clear(); }
        public final Iterator<Entry<V>> iterator() {
            return new EntryIterator();
        }
        public final boolean contains(Object o) {
            if (!(o instanceof ByteArrayMap.Entry))
                return false;
            Entry<?> e = (Entry<?>) o;
            byte[] key = e.getKey();
            Entry<V> candidate = getEntry(hash(key), key);
            return candidate != null && candidate.equals(e);
        }
        public final boolean remove(Object o) {
            if (o instanceof Entry) {
                Entry e = (Entry) o;
                byte[] key = e.getKey();
                Object value = e.getValue();
                return removeNode(hash(key), key) != null;
            }
            return false;
        }
        public final void forEach(Consumer<? super Entry<V>> action) {
            Entry<V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                for (int i = 0; i < tab.length; ++i) {
                    for (Entry<V> e = tab[i]; e != null; e = e.next)
                        action.accept(e);
                }
            }
        }
    }

    abstract class HashIterator {
        Entry<V> next;        // next entry to return
        Entry<V> current;     // current entry
        int index;             // current slot

        HashIterator() {
            Entry<V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // advance to first entry
                do {} while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Entry<V> nextNode() {
            Entry<V>[] t;
            Entry<V> e = next;
            if (e == null)
                throw new NoSuchElementException();
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {} while (index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        public final void remove() {
            Entry<V> p = current;
            if (p == null)
                throw new IllegalStateException();
            current = null;
            byte[] key = p.key;
            removeNode(hash(key), key);
        }
    }

    final class KeyIterator extends HashIterator
            implements Iterator<byte[]> {
        public final byte[] next() { return nextNode().key; }
    }

    final class ValueIterator extends HashIterator
            implements Iterator<V> {
        public final V next() { return nextNode().value; }
    }

    final class EntryIterator extends HashIterator
            implements Iterator<Entry<V>> {
        public final Entry<V> next() { return nextNode(); }
    }
}
