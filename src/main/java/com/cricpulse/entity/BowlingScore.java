package com.cricpulse.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bowling_scores")
public class BowlingScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "innings_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Innings innings;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Player player;

    private Integer legalBallsBowled = 0;
    private Integer maidens = 0;
    private Integer runsConceded = 0;
    private Integer wickets = 0;
    private Double economy = 0.0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public BowlingScore() {}

    public BowlingScore(Innings innings, Player player) {
        this.innings = innings;
        this.player = player;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        calculateEconomy();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateEconomy();
    }

    public void calculateEconomy() {
        if (legalBallsBowled != null && legalBallsBowled > 0 && runsConceded != null) {
            double overs = (legalBallsBowled / 6) + ((legalBallsBowled % 6) / 6.0);
            this.economy = Math.round((runsConceded / overs) * 100.0) / 100.0;
        } else {
            this.economy = 0.0;
        }
    }

    @Transient
    public String getOversFormatted() {
        if (legalBallsBowled == null || legalBallsBowled <= 0) return "0.0";
        return (legalBallsBowled / 6) + "." + (legalBallsBowled % 6);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Innings getInnings() { return innings; }
    public void setInnings(Innings innings) { this.innings = innings; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Integer getLegalBallsBowled() { return legalBallsBowled != null ? legalBallsBowled : 0; }
    public void setLegalBallsBowled(Integer legalBallsBowled) { this.legalBallsBowled = legalBallsBowled; }

    public Integer getMaidens() { return maidens != null ? maidens : 0; }
    public void setMaidens(Integer maidens) { this.maidens = maidens; }

    public Integer getRunsConceded() { return runsConceded != null ? runsConceded : 0; }
    public void setRunsConceded(Integer runsConceded) { this.runsConceded = runsConceded; }

    public Integer getWickets() { return wickets != null ? wickets : 0; }
    public void setWickets(Integer wickets) { this.wickets = wickets; }

    public Double getEconomy() { return economy != null ? economy : 0.0; }
    public void setEconomy(Double economy) { this.economy = economy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
