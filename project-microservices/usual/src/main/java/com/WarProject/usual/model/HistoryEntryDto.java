package com.WarProject.usual.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryEntryDto {
    int    id;
    String winnerName;
    String wonAt;
}