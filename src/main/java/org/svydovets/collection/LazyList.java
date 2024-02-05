package org.svydovets.collection;

import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@Log4j2
public class LazyList<T> implements List<T> {

    private final Supplier<List<T>> listSupplier;

    private List<T> nestedList;

    public LazyList(Supplier<List<T>> listSupplier) {
        this.listSupplier = listSupplier;
    }

    public List<T> getNestedList() {
        if (nestedList == null) {
            log.trace("lazy load list");

            nestedList = listSupplier.get();
        }

        return nestedList;
    }

    @Override
    public int size() {
        return getNestedList().size();
    }

    @Override
    public boolean isEmpty() {
        return getNestedList().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return getNestedList().contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return getNestedList().iterator();
    }

    @Override
    public Object[] toArray() {
        return getNestedList().toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return getNestedList().toArray(a);
    }

    @Override
    public boolean add(T t) {
        return getNestedList().add(t);
    }

    @Override
    public boolean remove(Object o) {
        return getNestedList().remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return getNestedList().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return getNestedList().addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return getNestedList().addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return getNestedList().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return getNestedList().retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        getNestedList().replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super T> c) {
        getNestedList().sort(c);
    }

    @Override
    public void clear() {
        getNestedList().clear();
    }

    @Override
    public boolean equals(Object o) {
        return getNestedList().equals(o);
    }

    @Override
    public int hashCode() {
        return getNestedList().hashCode();
    }

    @Override
    public T get(int index) {
        return getNestedList().get(index);
    }

    @Override
    public T set(int index, T element) {
        return getNestedList().set(index, element);
    }

    @Override
    public void add(int index, T element) {
        getNestedList().add(index, element);
    }

    @Override
    public T remove(int index) {
        return getNestedList().remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return getNestedList().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return getNestedList().lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return getNestedList().listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return getNestedList().listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return getNestedList().subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<T> spliterator() {
        return getNestedList().spliterator();
    }

    @Override
    public <T1> T1[] toArray(IntFunction<T1[]> generator) {
        return getNestedList().toArray(generator);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return getNestedList().removeIf(filter);
    }

    @Override
    public Stream<T> stream() {
        return getNestedList().stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return getNestedList().parallelStream();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        getNestedList().forEach(action);
    }
}
