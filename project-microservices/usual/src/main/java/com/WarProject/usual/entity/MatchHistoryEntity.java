package com.WarProject.usual.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "match_history")
public class MatchHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;

    String winnerName;
    String gameUuid;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+08:00")
    Date wonAt;
}