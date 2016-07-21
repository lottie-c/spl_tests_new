/*
 * Copyright (c) 2012, František Haas, Martin Lacina, Jaroslav Kotrč, Jiří Daniel
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package cz.cuni.mff.spl.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Stores elements as a set but has different method for adding new value.
 * Stored objects cannot be null.
 * If the added object is present (equals returns true) then new object is not
 * added.
 * Either way it returns reference to object present in the set after adding
 * which is equal to the added one.
 * Can not use just HashSet - it does not have method <code>get</code> so there
 * is no method how to get equal object.
 * 
 * @author Jaroslav Kotrc
 * @author Martin Lacina
 */
public class ReturningSet<T> implements Set<T> {

    private final Map<T, T> data = new HashMap<>();

    /**
     * Add object into set. Object cannot be null. If the object is already
     * present (equals returns true) then new object is not added.
     * Either way it returns reference to object present in the set after adding
     * which is equal to the added one.
     * 
     * @param object
     *            Object to add
     * @return Object presented in the set after adding. If the added object was
     *         not presented then it returns it else returns equal object
     *         presented in the set before adding.
     */
    public T returningAdd(T object) {
        T stored = data.get(object);
        if (stored != null) {
            return stored;
        } else {
            data.put(object, object);
            return object;
        }
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return data.containsKey(o);
    }

    @Override
    public Iterator<T> iterator() {
        return data.values().iterator();
    }

    @Override
    public Object[] toArray() {
        return data.values().toArray();
    }

    @Override
    public <S> S[] toArray(S[] a) {
        return data.values().toArray(a);
    }

    @Override
    public boolean add(T e) {
        if (data.containsKey(e)) {
            return false;
        } else {
            data.put(e, e);
            return true;
        }
    }

    @Override
    public boolean remove(Object o) {
        return data.remove(o) != null;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return data.keySet().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean result = false;
        for (T o : c) {
            result = this.add(o) || result;
        }
        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return data.keySet().retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return data.keySet().removeAll(c);
    }

    @Override
    public void clear() {
        this.data.clear();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ReturningSet) {
            return data.equals(((ReturningSet) obj).data);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
