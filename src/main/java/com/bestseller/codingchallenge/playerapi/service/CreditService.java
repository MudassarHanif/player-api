package com.bestseller.codingchallenge.playerapi.service;

import com.bestseller.codingchallenge.exception.DataAccessException;
import com.bestseller.codingchallenge.exception.GameLevelNotFoundException;
import com.bestseller.codingchallenge.playerapi.domain.PlayerCredit;
import com.bestseller.codingchallenge.playerapi.repository.PlayerGameLevelRepository;
import com.bestseller.codingchallenge.playerapi.repository.entity.GameEntity;
import com.bestseller.codingchallenge.playerapi.repository.entity.PlayerEntity;
import com.bestseller.codingchallenge.playerapi.repository.entity.PlayerGameLevelEntity;
import com.bestseller.codingchallenge.util.AsyncUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final PlayerGameLevelRepository playerGameLevelRepository;

    public CompletableFuture<PlayerCredit> updatePlayerCredits(Integer playerId, Integer gameId, Double creditAmount) {
        return AsyncUtil.execute(() -> playerGameLevelRepository.findByPlayerAndGame(new PlayerEntity(playerId), new GameEntity(gameId)).orElseThrow(GameLevelNotFoundException::new))
                .thenApply(playerGameLevel -> {
                            playerGameLevel.setCreditAmount(playerGameLevel.getCreditAmount() + creditAmount);
                            final PlayerGameLevelEntity gameLevelEntity = playerGameLevelRepository.save(playerGameLevel);
                            return PlayerCredit.builder()
                                    .playerId(playerId)
                                    .GameId(gameId)
                                    .creditAmount(gameLevelEntity.getCreditAmount())
                                    .levelId(playerGameLevel.getPlayerGameLevelId())
                                    .build();

                        }
                );
    }

    public CompletableFuture<List<PlayerCredit>> getPlayerWithMaxCredits(Integer gameId) {

        return AsyncUtil.execute(() -> playerGameLevelRepository.findPlayerByMaxCredit(gameId))
                .thenApply(playerGameLevelEntityList -> playerGameLevelEntityList.stream()
                        .map(playerGameLevel -> PlayerCredit.builder()
                                .playerId(playerGameLevel.getPlayer().getPlayerId())
                                .GameId(gameId)
                                .creditAmount(playerGameLevel.getCreditAmount())
                                .levelId(playerGameLevel.getPlayerGameLevelId())
                                .build()
                        ).collect(Collectors.toList()))
                .exceptionally(throwable -> {
                    throw new DataAccessException("could not fetch game level", throwable.getCause());
                });
    }
}
