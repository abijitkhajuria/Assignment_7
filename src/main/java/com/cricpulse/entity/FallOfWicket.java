package com.cricpulse.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "fall_of_wickets")
public class FallOfWicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "innings_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Innings innings;

    @Column(nullable = false)
    private Integer wicketNumber;

    @Column(nullable = false)
    private Integer teamScore;

    @Column(nullable = false, length = 15)
    private String oversFormatted;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dismissed_player_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Player dismissedPlayer;

    public FallOfWicket() {}

    public FallOfWicket(Innings innings, Integer wicketNumber, Integer teamScore, String oversFormatted, Player dismissedPlayer) {
        this.innings = innings;
        this.wicketNumber = wicketNumber;
        this.teamScore = teamScore;
        this.oversFormatted = oversFormatted;
        this.dismissedPlayer = dismissedPlayer;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Innings getInnings() { return innings; }
    public void setInnings(Innings innings) { this.innings = innings; }

    public Integer getWicketNumber() { return wicketNumber; }
    public void setWicketNumber(Integer wicketNumber) { this.wicketNumber = wicketNumber; }

    public Integer getTeamScore() { return teamScore; }
    public void setTeamScore(Integer teamScore) { this.teamScore = teamScore; }

    public String getOversFormatted() { return oversFormatted; }
    public void setOversFormatted(String oversFormatted) { this.oversFormatted = oversFormatted; }

    public Player getDismissedPlayer() { return dismissedPlayer; }
    public void setDismissedPlayer(Player dismissedPlayer) { this.dismissedPlayer = dismissedPlayer; }
}
