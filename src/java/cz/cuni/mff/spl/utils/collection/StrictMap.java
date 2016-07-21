package cz.cuni.mff.spl.utils.collection;

public interface StrictMap<K, V> {

    public V put(K key, V value);

    public boolean containsKey(K key);

    public V get(K key);

    public void clear();
}
