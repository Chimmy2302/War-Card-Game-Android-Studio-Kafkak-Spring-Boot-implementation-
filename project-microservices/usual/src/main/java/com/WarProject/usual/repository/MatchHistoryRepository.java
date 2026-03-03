package com.WarProject.usual.repository;

import com.WarProject.usual.entity.MatchHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchHistoryRepository extends JpaRepository<MatchHistoryEntity, Integer> {
}