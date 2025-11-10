package org.nosulkora.postmaker.repository;

import java.util.List;

public interface GenericRepository<T, ID> {

    T save(T t);

    T update(T t);

    T getById(ID id);

    List<T> getAll();

    boolean deleteById(ID id);
}
