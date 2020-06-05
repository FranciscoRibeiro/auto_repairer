import java.util.ArrayList;
import java.util.List;

public class ByteArrayHashMap<T> {

    /**
     * The default initial capacity - MUST be a power of two.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    protected Entry<T>[] table;

    protected int size;

    private int threshold;

    final float loadFactor;

    @SuppressWarnings("unchecked")
    public ByteArrayHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 1)
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        if (initialCapacity < MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity) capacity <<= 1;
        this.loadFactor = loadFactor;
        threshold = (int) (capacity * loadFactor);
        table = new Entry[capacity];
    }

    public ByteArrayHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    @SuppressWarnings("unchecked")
    public ByteArrayHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        threshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
        table = new Entry[DEFAULT_INITIAL_CAPACITY];
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public T get(byte[] key, int offset, int len) {
        byte[] k = new byte[len];
        System.arraycopy(key, offset, k, 0, len);
        return (get(k));
    }

    public T get(byte[] key) {
        int hash = hash(key);
        int i = indexFor(hash, table.length);
        Entry<T> e = table[i];
        while (true) {
            if (e == null)
                return null;
            if (e.hash == hash && eq(key, e.key))
                return e.value;
            e = e.next;
        }
    }

    public boolean containsKey(byte[] key) {
        int hash = hash(key);
        int i = indexFor(hash, table.length);
        Entry<T> e = table[i];
        while (true) {
            if (e == null)
                return (false);
            if (e.hash == hash && eq(key, e.key))
                return (true);
            e = e.next;
        }
    }

    public T put(byte[] key, T value) {
        int hash = hash(key);
        int i = indexFor(hash, table.length);
        for (Entry<T> e = table[i]; e != null; e = e.next) {
            if (e.hash == hash && eq(key, e.key)) {
                T oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        addEntry(hash, key, value, i);
        return null;
    }

    public T remove(byte[] key) {
        Entry<T> e = removeEntryForKey(key);
        return (e == null ? null : e.value);
    }

    public void clear() {
        Entry<T>[] tab = table;
        for (int i = 0; i < tab.length; i++) tab[i] = null;
        size = 0;
    }

    public List<byte[]> keys() {
        List<byte[]> res = new ArrayList<byte[]>();
        for (int j = 0; j < table.length; j++) {
            Entry<T> e = table[j];
            while (e != null) {
                res.add(e.key);
                e = e.next;
            }
        }
        return (res);
    }

    public List<T> values() {
        List<T> res = new ArrayList<T>();
        for (int j = 0; j < table.length; j++) {
            Entry<T> e = table[j];
            while (e != null) {
                res.add(e.value);
                e = e.next;
            }
        }
        return (res);
    }

    /**
     * Bit inefficient at the moment
     *
     * @return
     */
    public ByteArrayHashMap<T> duplicate() {
        ByteArrayHashMap<T> res = new ByteArrayHashMap<T>(size, loadFactor);
        for (int j = 0; j < table.length; j++) {
            Entry<T> e = table[j];
            while (e != null) {
                res.put(e.key, e.value);
                e = e.next;
            }
        }
        return (res);
    }

    @SuppressWarnings("unchecked")
    void resize(int newCapacity) {
        Entry<T>[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }
        Entry<T>[] newTable = new Entry[newCapacity];
        transfer(newTable);
        table = newTable;
        threshold = (int) (newCapacity * loadFactor);
    }

    void transfer(Entry<T>[] newTable) {
        Entry<T>[] src = table;
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            Entry<T> e = src[j];
            if (e != null) {
                src[j] = null;
                do {
                    Entry<T> next = e.next;
                    int i = indexFor(e.hash, newCapacity);
                    e.next = newTable[i];
                    newTable[i] = e;
                    e = next;
                } while (e != null);
            }
        }
    }

    Entry<T> removeEntryForKey(byte[] key) {
        int hash = hash(key);
        int i = indexFor(hash, table.length);
        Entry<T> prev = table[i];
        Entry<T> e = prev;
        while (e != null) {
            Entry<T> next = e.next;
            if (e.hash == hash && eq(key, e.key)) {
                size--;
                if (prev == e)
                    table[i] = next;
                else
                    prev.next = next;
                return e;
            }
            prev = e;
            e = next;
        }
        return e;
    }

    protected static class Entry<S> {

        public final byte[] key;

        public S value;

        public final int hash;

        public Entry<S> next;

        /**
         * Create new entry.
         */
        Entry(int h, byte[] k, S v, Entry<S> n) {
            value = v;
            next = n;
            key = k;
            hash = h;
        }

        public byte[] getKey() {
            return key;
        }

        public S getValue() {
            return value;
        }
    }

    void addEntry(int hash, byte[] key, T value, int bucketIndex) {
        table[bucketIndex] = new Entry<T>(hash, key, value, table[bucketIndex]);
        if (size++ >= threshold)
            resize(2 * table.length);
    }

    void createEntry(int hash, byte[] key, T value, int bucketIndex) {
        table[bucketIndex] = new Entry<T>(hash, key, value, table[bucketIndex]);
        size++;
    }

    private static final int hash(byte[] x) {
        int hash = 0;
        int len = x.length;
        for (int i = 0; i < len; i++) {
            hash = 31 * hash + x[i];
        }
        return (hash);
    }

    private static final boolean eq(byte[] x, byte[] y) {
        if (x == y) {
            return (true);
        }
        int len = x.length;
        if (len != y.length) {
            return (false);
        }
        for (int i = 0; i < len; i++) {
            if (x[i] != y[i]) {
                return (false);
            }
        }
        return (true);
    }

    private static final int indexFor(int h, int length) {
        return h & (length - 1);
    }
}
