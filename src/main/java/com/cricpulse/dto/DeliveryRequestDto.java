package com.cricpulse.dto;

import com.cricpulse.entity.ExtraType;
import com.cricpulse.entity.WicketType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class DeliveryRequestDto {

    @NotNull(message = "Striker ID is required")
    private Long strikerId;

    @NotNull(message = "Non-striker ID is required")
    private Long nonStrikerId;

    @NotNull(message = "Bowler ID is required")
    private Long bowlerId;

    @Min(value = 0, message = "Batter runs cannot be negative")
    @Max(value = 6, message = "Batter runs cannot exceed 6 on a single delivery")
    private Integer runsBatter = 0;

    private ExtraType extraType = ExtraType.NONE;

    @Min(value = 0, message = "Extra runs cannot be negative")
    private Integer runsExtra = 0;

    private Boolean isWicket = false;

    private WicketType wicketType = WicketType.NONE;

    private Long dismissedPlayerId;

    private Long fielderId;

    private Long newBatsmanId; // For replacement if wicket falls

    private String commentary;

    public DeliveryRequestDto() {}

    public Long getStrikerId() { return strikerId; }
    public void setStrikerId(Long strikerId) { this.strikerId = strikerId; }

    public Long getNonStrikerId() { return nonStrikerId; }
    public void setNonStrikerId(Long nonStrikerId) { this.nonStrikerId = nonStrikerId; }

    public Long getBowlerId() { return bowlerId; }
    public void setBowlerId(Long bowlerId) { this.bowlerId = bowlerId; }

    public Integer getRunsBatter() { return runsBatter != null ? runsBatter : 0; }
    public void setRunsBatter(Integer runsBatter) { this.runsBatter = runsBatter; }

    public ExtraType getExtraType() { return extraType != null ? extraType : ExtraType.NONE; }
    public void setExtraType(ExtraType extraType) { this.extraType = extraType; }

    public Integer getRunsExtra() { return runsExtra != null ? runsExtra : 0; }
    public void setRunsExtra(Integer runsExtra) { this.runsExtra = runsExtra; }

    public Boolean getIsWicket() { return isWicket != null ? isWicket : false; }
    public void setIsWicket(Boolean wicket) { isWicket = wicket; }

    public WicketType getWicketType() { return wicketType != null ? wicketType : WicketType.NONE; }
    public void setWicketType(WicketType wicketType) { this.wicketType = wicketType; }

    public Long getDismissedPlayerId() { return dismissedPlayerId; }
    public void setDismissedPlayerId(Long dismissedPlayerId) { this.dismissedPlayerId = dismissedPlayerId; }

    public Long getFielderId() { return fielderId; }
    public void setFielderId(Long fielderId) { this.fielderId = fielderId; }

    public Long getNewBatsmanId() { return newBatsmanId; }
    public void setNewBatsmanId(Long newBatsmanId) { this.newBatsmanId = newBatsmanId; }

    public String getCommentary() { return commentary; }
    public void setCommentary(String commentary) { this.commentary = commentary; }
}
