package com.cricpulse.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries", indexes = {
    @Index(name = "idx_delivery_innings_seq", columnList = "innings_id, sequenceOrder")
})
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "innings_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Innings innings;

    @Column(nullable = false)
    private Integer overNumber; // 0-based over index (0, 1, 2...)

    @Column(nullable = false)
    private Integer ballNumberInOver; // Ball delivery order within over

    private Integer legalBallNumber; // Cumulative legal balls count

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "striker_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Player striker;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "non_striker_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Player nonStriker;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bowler_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Player bowler;

    @Column(nullable = false)
    private Integer runsBatter = 0;

    @Column(nullable = false)
    private Integer runsExtra = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExtraType extraType = ExtraType.NONE;

    @Column(nullable = false)
    private Boolean isLegalDelivery = true;

    @Column(nullable = false)
    private Boolean isWicket = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private WicketType wicketType = WicketType.NONE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dismissed_player_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Player dismissedPlayer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fielder_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Player fielder;

    @Column(length = 255)
    private String commentary;

    private Integer teamRunsAtBall;
    private Integer wicketsAtBall;

    @Column(length = 15)
    private String oversAtBallFormatted;

    @Column(nullable = false)
    private Integer sequenceOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public Delivery() {}

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    @Transient
    public int getTotalDeliveryRuns() {
        return (runsBatter != null ? runsBatter : 0) + (runsExtra != null ? runsExtra : 0);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Innings getInnings() { return innings; }
    public void setInnings(Innings innings) { this.innings = innings; }

    public Integer getOverNumber() { return overNumber; }
    public void setOverNumber(Integer overNumber) { this.overNumber = overNumber; }

    public Integer getBallNumberInOver() { return ballNumberInOver; }
    public void setBallNumberInOver(Integer ballNumberInOver) { this.ballNumberInOver = ballNumberInOver; }

    public Integer getLegalBallNumber() { return legalBallNumber; }
    public void setLegalBallNumber(Integer legalBallNumber) { this.legalBallNumber = legalBallNumber; }

    public Player getStriker() { return striker; }
    public void setStriker(Player striker) { this.striker = striker; }

    public Player getNonStriker() { return nonStriker; }
    public void setNonStriker(Player nonStriker) { this.nonStriker = nonStriker; }

    public Player getBowler() { return bowler; }
    public void setBowler(Player bowler) { this.bowler = bowler; }

    public Integer getRunsBatter() { return runsBatter != null ? runsBatter : 0; }
    public void setRunsBatter(Integer runsBatter) { this.runsBatter = runsBatter; }

    public Integer getRunsExtra() { return runsExtra != null ? runsExtra : 0; }
    public void setRunsExtra(Integer runsExtra) { this.runsExtra = runsExtra; }

    public ExtraType getExtraType() { return extraType; }
    public void setExtraType(ExtraType extraType) { this.extraType = extraType; }

    public Boolean getIsLegalDelivery() { return isLegalDelivery; }
    public void setIsLegalDelivery(Boolean legalDelivery) { isLegalDelivery = legalDelivery; }

    public Boolean getIsWicket() { return isWicket; }
    public void setIsWicket(Boolean wicket) { isWicket = wicket; }

    public WicketType getWicketType() { return wicketType; }
    public void setWicketType(WicketType wicketType) { this.wicketType = wicketType; }

    public Player getDismissedPlayer() { return dismissedPlayer; }
    public void setDismissedPlayer(Player dismissedPlayer) { this.dismissedPlayer = dismissedPlayer; }

    public Player getFielder() { return fielder; }
    public void setFielder(Player fielder) { this.fielder = fielder; }

    public String getCommentary() { return commentary; }
    public void setCommentary(String commentary) { this.commentary = commentary; }

    public Integer getTeamRunsAtBall() { return teamRunsAtBall; }
    public void setTeamRunsAtBall(Integer teamRunsAtBall) { this.teamRunsAtBall = teamRunsAtBall; }

    public Integer getWicketsAtBall() { return wicketsAtBall; }
    public void setWicketsAtBall(Integer wicketsAtBall) { this.wicketsAtBall = wicketsAtBall; }

    public String getOversAtBallFormatted() { return oversAtBallFormatted; }
    public void setOversAtBallFormatted(String oversAtBallFormatted) { this.oversAtBallFormatted = oversAtBallFormatted; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
