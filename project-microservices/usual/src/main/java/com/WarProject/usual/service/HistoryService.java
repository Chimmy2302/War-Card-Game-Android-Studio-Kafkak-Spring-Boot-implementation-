package com.WarProject.usual.service;

import com.WarProject.usual.entity.MatchHistoryEntity;
import com.WarProject.usual.model.HistoryEntryDto;
import com.WarProject.usual.model.SubmitWinnerRequest;
import com.WarProject.usual.repository.MatchHistoryRepository;
import com.WarProject.usual.transformer.HistoryTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final MatchHistoryRepository historyRepository;
    private final HistoryTransformer     historyTransformer;

    public void saveWinner(SubmitWinnerRequest request) {
        MatchHistoryEntity entity = new MatchHistoryEntity();
        entity.setWinnerName(request.getWinnerName());
        entity.setGameUuid(request.getGameId());
        historyRepository.save(entity);
    }

    public List<HistoryEntryDto> getHistory() {
        return historyRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(MatchHistoryEntity::getWonAt).reversed())
                .map(historyTransformer::toDto)
                .collect(Collectors.toList());
    }
}