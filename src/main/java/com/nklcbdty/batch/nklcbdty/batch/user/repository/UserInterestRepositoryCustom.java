package com.nklcbdty.batch.nklcbdty.batch.user.repository;

import java.util.List;

import com.querydsl.core.Tuple;

public interface UserInterestRepositoryCustom {
    List<Tuple> findGroupedByUserTypeAndUserId();
}
