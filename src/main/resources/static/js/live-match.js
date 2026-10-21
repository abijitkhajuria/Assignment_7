/**
 * CricPulse — Live Match Center Logic
 * Subscribes to real-time Server-Sent Events (SSE) with automatic fallback polling
 */

let currentMatchId = null;
let streamSubscription = null;

document.addEventListener('DOMContentLoaded', async () => {
    const urlParams = new URLSearchParams(window.location.search);
    currentMatchId = urlParams.get('id');

    if (!currentMatchId) {
        // Auto-select first live match
        try {
            const liveMatches = await CricPulseAPI.getLiveMatches();
            if (liveMatches && liveMatches.length > 0) {
                currentMatchId = liveMatches[0].id;
                // Update URL quietly
                window.history.replaceState({}, '', `?id=${currentMatchId}`);
            } else {
                const all = await CricPulseAPI.getAllMatches();
                if (all && all.length > 0) {
                    currentMatchId = all[0].id;
                }
            }
        } catch (e) {
            console.error('Failed to locate active live match:', e);
        }
    }

    if (currentMatchId) {
        initLiveMatch(currentMatchId);
    } else {
        document.getElementById('matchTitle').textContent = 'No Matches Available';
        document.getElementById('tossResultText').textContent = 'Please create or start a match in the Admin Console.';
    }
});

function initLiveMatch(matchId) {
    document.getElementById('fullScorecardBtn').href = `/scorecard.html?id=${matchId}`;
    loadLiveMatchSwitcher(matchId);

    // Subscribe to real-time SSE stream with automatic fallback polling
    streamSubscription = CricPulseAPI.subscribeLiveStream(matchId, {
        onUpdate: (snapshot) => {
            renderLiveSnapshot(snapshot);
        },
        onError: (err) => {
            console.warn('[LiveMatch] Stream error, handled by fallback');
        },
        onConnectionChange: (status) => {
            const badge = document.getElementById('sseConnectionIndicator');
            const text = document.getElementById('sseStatusText');
            if (status === 'CONNECTED') {
                badge.className = 'live-stream-status';
                text.textContent = 'Live Stream Connected';
            } else if (status === 'POLLING') {
                badge.className = 'live-stream-status text-warning border-warning border-opacity-50';
                text.textContent = 'Live Polling Active';
            } else {
                badge.className = 'live-stream-status text-danger border-danger border-opacity-50';
                text.textContent = 'Disconnected';
            }
        }
    });

    // Also fetch initial snapshot immediately via REST
    CricPulseAPI.getLiveSnapshot(matchId)
        .then(renderLiveSnapshot)
        .catch(console.error);
}

function renderLiveSnapshot(data) {
    if (!data) return;

    // Header info
    document.getElementById('matchTitle').textContent = data.title || 'Cricket Match';
    document.getElementById('matchVenue').innerHTML = `<i class="bi bi-geo-alt me-1"></i>${data.venue || ''}`;
    document.getElementById('matchFormatBadge').textContent = `${data.format || 'T20'} • ${data.totalOvers || 20} Ov`;
    document.getElementById('tossResultText').textContent = data.tossInfo || 'Toss information pending';

    // Status Badge
    const statusBadge = document.getElementById('matchStatusBadge');
    if (data.status === 'LIVE') {
        statusBadge.className = 'badge-live';
        statusBadge.innerHTML = `<span class="pulse-dot"></span> LIVE`;
    } else if (data.status === 'INNINGS_BREAK') {
        statusBadge.className = 'badge-completed text-warning border-warning';
        statusBadge.innerHTML = `INNINGS BREAK`;
    } else if (data.status === 'COMPLETED') {
        statusBadge.className = 'badge-completed';
        statusBadge.innerHTML = `FINISHED`;
    } else {
        statusBadge.className = 'badge-upcoming';
        statusBadge.innerHTML = data.status;
    }

    // Big Score
    const totalRuns = data.totalRuns !== undefined ? data.totalRuns : 0;
    const wickets = data.wickets !== undefined ? data.wickets : 0;
    const overs = data.oversFormatted || '0.0';

    document.getElementById('liveScoreBig').textContent = `${totalRuns}/${wickets}`;
    document.getElementById('liveOversBig').textContent = `(${overs} ov)`;
    document.getElementById('liveBattingTeamPill').textContent = data.battingTeamShortName || 'BAT';
    if (data.battingTeamColor) {
        document.getElementById('liveBattingTeamPill').style.backgroundColor = data.battingTeamColor;
    }

    // Target Banner
    const targetBanner = document.getElementById('targetChaseBanner');
    if (data.inningsNumber === 2 && data.targetRuns) {
        if (data.status === 'COMPLETED') {
            targetBanner.innerHTML = `<span class="text-emerald fw-bold"><i class="bi bi-trophy-fill me-1 text-gold"></i>${data.resultMargin || 'Match Finished'}</span>`;
        } else {
            targetBanner.innerHTML = `${data.battingTeamName || 'Batting team'} need <strong class="text-white">${data.runsNeeded}</strong> runs in <strong class="text-white">${data.remainingBalls}</strong> balls (Target: ${data.targetRuns})`;
        }
    } else if (data.status === 'INNINGS_BREAK') {
        targetBanner.innerHTML = `<span class="text-warning">Innings break. Target for 2nd innings will be ${totalRuns + 1} runs.</span>`;
    } else if (data.status === 'COMPLETED') {
        targetBanner.innerHTML = `<span class="text-emerald fw-bold"><i class="bi bi-trophy-fill me-1 text-gold"></i>${data.resultMargin || 'Match Completed'}</span>`;
    } else {
        targetBanner.innerHTML = `${data.battingTeamName || 'Batting team'} batting 1st innings`;
    }

    // CRR & RRR
    document.getElementById('statCrr').textContent = data.currentRunRate !== null && data.currentRunRate !== undefined ? data.currentRunRate.toFixed(2) : '0.00';
    document.getElementById('statRrr').textContent = data.requiredRunRate !== null && data.requiredRunRate !== undefined ? data.requiredRunRate.toFixed(2) : '—';

    // Recent Balls Chips
    const recentBallsContainer = document.getElementById('recentBallsContainer');
    if (data.recentBalls && data.recentBalls.length > 0) {
        // Reverse so latest is on right or standard chronological
        const balls = [...data.recentBalls].reverse();
        recentBallsContainer.innerHTML = balls.map(b => {
            let chipClass = 'chip-0';
            if (b.isWicket) chipClass = 'chip-w';
            else if (b.ballDisplay === '4') chipClass = 'chip-4';
            else if (b.ballDisplay === '6') chipClass = 'chip-6';
            else if (b.ballDisplay.includes('wd') || b.ballDisplay.includes('nb')) chipClass = 'chip-extra';
            else if (['1', '2', '3'].includes(b.ballDisplay)) chipClass = 'chip-1';

            return `<span class="ball-chip ${chipClass}" title="Over ${b.overAndBall}">${b.ballDisplay}</span>`;
        }).join('');
    } else {
        recentBallsContainer.innerHTML = `<span class="text-secondary small">No deliveries yet in this innings</span>`;
    }

    // Batters at Crease Table
    const battersTbody = document.getElementById('battersCreaseTable');
    let battersHtml = '';

    if (data.striker) {
        battersHtml += `
            <tr class="table-active">
                <td>
                    <span class="striker-indicator">▶</span>
                    <strong class="text-white">${data.striker.name}</strong>
                    <span class="badge bg-success bg-opacity-25 text-success ms-1 small">Striker *</span>
                </td>
                <td class="text-center fw-bold text-white">${data.striker.runs}</td>
                <td class="text-center text-secondary">${data.striker.ballsFaced}</td>
                <td class="text-center">${data.striker.fours}</td>
                <td class="text-center">${data.striker.sixes}</td>
                <td class="text-end fw-semibold text-cyan">${data.striker.strikeRate.toFixed(1)}</td>
            </tr>
        `;
    }

    if (data.nonStriker) {
        battersHtml += `
            <tr>
                <td>
                    <span class="text-white">${data.nonStriker.name}</span>
                    <span class="text-secondary ms-1 small">Non-Striker</span>
                </td>
                <td class="text-center fw-bold text-white">${data.nonStriker.runs}</td>
                <td class="text-center text-secondary">${data.nonStriker.ballsFaced}</td>
                <td class="text-center">${data.nonStriker.fours}</td>
                <td class="text-center">${data.nonStriker.sixes}</td>
                <td class="text-end fw-semibold text-cyan">${data.nonStriker.strikeRate.toFixed(1)}</td>
            </tr>
        `;
    }

    if (!data.striker && !data.nonStriker) {
        battersHtml = `<tr><td colspan="6" class="text-center text-secondary py-3">Innings break or no active batters at crease</td></tr>`;
    }
    battersTbody.innerHTML = battersHtml;

    // Bowler Table
    const bowlerTbody = document.getElementById('bowlerCreaseTable');
    if (data.currentBowler) {
        bowlerTbody.innerHTML = `
            <tr>
                <td><strong class="text-white">${data.currentBowler.name}</strong></td>
                <td class="text-center fw-bold text-white">${data.currentBowler.oversFormatted}</td>
                <td class="text-center text-secondary">${data.currentBowler.maidens}</td>
                <td class="text-center text-white">${data.currentBowler.runsConceded}</td>
                <td class="text-center fw-bold text-emerald">${data.currentBowler.wickets}</td>
                <td class="text-end fw-semibold text-cyan">${data.currentBowler.economy.toFixed(2)}</td>
            </tr>
        `;
    } else {
        bowlerTbody.innerHTML = `<tr><td colspan="6" class="text-center text-secondary py-3">No active bowler assigned</td></tr>`;
    }

    // Commentary Timeline
    const commTimeline = document.getElementById('liveCommentaryTimeline');
    if (data.recentCommentary && data.recentCommentary.length > 0) {
        commTimeline.innerHTML = data.recentCommentary.map(c => {
            let bulletClass = '';
            let badgeHtml = '';

            if (c.badge === 'W') {
                bulletClass = 'bullet-wicket';
                badgeHtml = `<span class="badge bg-danger ms-1">WICKET</span>`;
            } else if (c.badge === '4') {
                bulletClass = 'bullet-boundary';
                badgeHtml = `<span class="badge bg-warning text-dark ms-1">FOUR</span>`;
            } else if (c.badge === '6') {
                bulletClass = 'bullet-boundary';
                badgeHtml = `<span class="badge bg-success ms-1">SIX</span>`;
            }

            return `
                <div class="commentary-item">
                    <div class="commentary-bullet ${bulletClass}"></div>
                    <div class="d-flex align-items-center gap-2 mb-1">
                        <strong class="text-cyan small">${c.overFormatted || ''}</strong>
                        ${badgeHtml}
                    </div>
                    <p class="text-secondary small mb-0">${c.text}</p>
                </div>
            `;
        }).join('');
    } else {
        commTimeline.innerHTML = `<p class="text-secondary small">Waiting for deliveries to commence...</p>`;
    }
}

// Cleanup on navigate away
window.addEventListener('beforeunload', () => {
    if (streamSubscription) {
        streamSubscription.close();
    }
});

async function loadLiveMatchSwitcher(activeId) {
    try {
        const liveMatches = await CricPulseAPI.getLiveMatches();
        const bar = document.getElementById('liveMatchSwitcherBar');
        if (bar && liveMatches && liveMatches.length > 0) {
            bar.innerHTML = liveMatches.map(m => {
                const isSelected = String(m.id) === String(activeId);
                return `
                    <a href="/match-live.html?id=${m.id}" class="btn btn-sm ${isSelected ? 'btn-cp-primary' : 'btn-dark border-secondary'} text-nowrap">
                        <span class="pulse-dot me-1"></span> ${m.team1ShortName} vs ${m.team2ShortName} (${m.format})
                    </a>
                `;
            }).join('');
        }
    } catch (e) {
        console.error('Failed to load live match switcher:', e);
    }
}

