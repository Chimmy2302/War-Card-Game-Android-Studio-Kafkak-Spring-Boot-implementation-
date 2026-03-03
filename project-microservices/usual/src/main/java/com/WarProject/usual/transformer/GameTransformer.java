package com.WarProject.usual.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.WarProject.usual.entity.GameEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GameTransformer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public List<String> deckFromEntity(GameEntity entity) {
        try {
            return MAPPER.readValue(entity.getDeck(), new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public String deckToJson(List<String> deck) {
        try {
            return MAPPER.writeValueAsString(deck);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}