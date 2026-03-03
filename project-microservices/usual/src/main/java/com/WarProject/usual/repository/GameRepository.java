package com.WarProject.usual.repository;

import com.WarProject.usual.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Integer> {

    Optional<GameEntity> findByGameUuid(String gameUuid);
}