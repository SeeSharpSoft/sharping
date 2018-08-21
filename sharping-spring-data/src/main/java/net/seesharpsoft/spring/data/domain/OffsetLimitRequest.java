package net.seesharpsoft.spring.data.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

public class OffsetLimitRequest implements Pageable {
    
    private final int offset;
    private final int limit;
    private final Sort sort;
    
    public OffsetLimitRequest(int offset, int limit, Sort sort) {
        Assert.isTrue(offset >= 0, "offset must be greater or equal than 0!");
        Assert.isTrue(limit > 0, "limit must be greater than 0!");
        this.offset = offset;
        this.limit = limit;
        this.sort = sort;
    }
    
    public OffsetLimitRequest(int offset, int limit) {
        this(offset, limit, null);
    }
    
    @Override
    public int getPageNumber() {
        return Math.floorDiv(getOffset() + getPageSize() - 1, getPageSize());
    }

    @Override
    public int getPageSize() {
        return this.limit;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return this.sort;
    }

    @Override
    public Pageable next() {
        return new OffsetLimitRequest(getOffset() + getPageSize(), getPageSize(), getSort());
    }

    @Override
    public Pageable previousOrFirst() {
        int start = getOffset() - getPageSize();
        if (start < 0) {
            start = 0;
        }
        return new OffsetLimitRequest(start, getOffset() - start, getSort());
    }

    @Override
    public Pageable first() {
        int firstPageSize = getOffset() % getPageSize();
        return new OffsetLimitRequest(0, firstPageSize, getSort());
    }

    @Override
    public boolean hasPrevious() {
        return getOffset() > 0;
    }
    
    @Override
    public String toString() {
        return String.format("Offset-Limit request [offset: %d, limit: %d, sort: %s]", getOffset(), getPageSize(), getSort());
    }
}
