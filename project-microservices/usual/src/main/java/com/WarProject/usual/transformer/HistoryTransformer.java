package com.WarProject.usual.transformer;

import com.WarProject.usual.entity.MatchHistoryEntity;
import com.WarProject.usual.model.HistoryEntryDto;
import org.springframework.stereotype.Component;

@Component
public class HistoryTransformer {

    public HistoryEntryDto toDto(MatchHistoryEntity entity) {
        return new HistoryEntryDto(
                entity.getId(),
                entity.getWinnerName(),
                entity.getWonAt() != null ? entity.getWonAt().toString() : ""
        );
    }
}