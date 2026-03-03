package com.WarProject.usual.controller;

import com.WarProject.usual.model.*;
import com.WarProject.usual.service.GameService;
import com.WarProject.usual.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class GameController {

    private final GameService    gameService;
    private final HistoryService historyService;

    @PostMapping("/games")
    public ResponseEntity<CreateGameResponse> createGame() {
        String[] result = gameService.createGame();
        return ResponseEntity.ok(new CreateGameResponse(result[0], result[1]));
    }

    @PostMapping("/games/join")
    public ResponseEntity<JoinGameResponse> joinGame(@RequestBody JoinGameRequest request) {
        String[] result = gameService.joinGame(request.getGameId());
        return ResponseEntity.ok(new JoinGameResponse(result[0], result[1]));
    }

    @PostMapping("/games/next-round")
    public ResponseEntity<Void> nextRound(@RequestBody NextRoundRequest request) {
        gameService.requestNextRound(request.getGameId(), request.getPlayerId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/games/winner")
    public ResponseEntity<Void> submitWinner(@RequestBody SubmitWinnerRequest request) {
        historyService.saveWinner(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<HistoryEntryDto>> getHistory() {
        return ResponseEntity.ok(historyService.getHistory());
    }
}