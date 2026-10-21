package com.cricpulse.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "batting_scores")
public class BattingScore {

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

    private Integer battingOrder;
    private Integer runs = 0;
    private Integer ballsFaced = 0;
    private Integer fours = 0;
    private Integer sixes = 0;
    private Double strikeRate = 0.0;

    private Boolean isOut = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private WicketType wicketType = WicketType.NONE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bowler_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Player bowler;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fielder_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Player fielder;

    @Column(length = 150)
    private String dismissalText = "not out";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public BattingScore() {}

    public BattingScore(Innings innings, Player player, Integer battingOrder) {
        this.innings = innings;
        this.player = player;
        this.battingOrder = battingOrder;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        calculateStrikeRate();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateStrikeRate();
    }

    public void calculateStrikeRate() {
        if (ballsFaced != null && ballsFaced > 0 && runs != null) {
            this.strikeRate = Math.round((runs * 100.0 / ballsFaced) * 100.0) / 100.0;
        } else {
            this.strikeRate = 0.0;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Innings getInnings() { return innings; }
    public void setInnings(Innings innings) { this.innings = innings; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Integer getBattingOrder() { return battingOrder; }
    public void setBattingOrder(Integer battingOrder) { this.battingOrder = battingOrder; }

    public Integer getRuns() { return runs != null ? runs : 0; }
    public void setRuns(Integer runs) { this.runs = runs; }

    public Integer getBallsFaced() { return ballsFaced != null ? ballsFaced : 0; }
    public void setBallsFaced(Integer ballsFaced) { this.ballsFaced = ballsFaced; }

    public Integer getFours() { return fours != null ? fours : 0; }
    public void setFours(Integer fours) { this.fours = fours; }

    public Integer getSixes() { return sixes != null ? sixes : 0; }
    public void setSixes(Integer sixes) { this.sixes = sixes; }

    public Double getStrikeRate() { return strikeRate != null ? strikeRate : 0.0; }
    public void setStrikeRate(Double strikeRate) { this.strikeRate = strikeRate; }

    public Boolean getIsOut() { return isOut != null ? isOut : false; }
    public void setIsOut(Boolean out) { isOut = out; }

    public WicketType getWicketType() { return wicketType; }
    public void setWicketType(WicketType wicketType) { this.wicketType = wicketType; }

    public Player getBowler() { return bowler; }
    public void setBowler(Player bowler) { this.bowler = bowler; }

    public Player getFielder() { return fielder; }
    public void setFielder(Player fielder) { this.fielder = fielder; }

    public String getDismissalText() { return dismissalText; }
    public void setDismissalText(String dismissalText) { this.dismissalText = dismissalText; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
