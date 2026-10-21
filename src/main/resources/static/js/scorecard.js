/**
 * CricPulse — Full Scorecard Logic
 */

let scorecardData = null;

document.addEventListener('DOMContentLoaded', async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const matchId = urlParams.get('id');

    if (!matchId) {
        // Fallback to first available match
        try {
            const matches = await CricPulseAPI.getAllMatches();
            if (matches && matches.length > 0) {
                window.location.href = `/scorecard.html?id=${matches[0].id}`;
                return;
            }
        } catch (e) {
            console.error(e);
        }
    }

    if (matchId) {
        document.getElementById('btnBackToLive').href = `/match-live.html?id=${matchId}`;
        loadScorecard(matchId);
    }
});

async function loadScorecard(matchId) {
    try {
        scorecardData = await CricPulseAPI.getScorecard(matchId);
        renderScorecard(scorecardData);
    } catch (err) {
        console.error('Failed to load scorecard:', err);
        document.getElementById('inningsContentContainer').innerHTML = `
            <div class="cp-card text-center py-4">
                <i class="bi bi-exclamation-triangle text-danger fs-1 mb-2"></i>
                <h5 class="text-white">Failed to load scorecard</h5>
                <p class="text-secondary small">${err.message}</p>
            </div>
        `;
    }
}

function renderScorecard(data) {
    if (!data) return;

    // Header info
    document.getElementById('scTitle').textContent = data.title;
    document.getElementById('scFormat').textContent = data.format;
    document.getElementById('scVenue').innerHTML = `<i class="bi bi-geo-alt me-1"></i>${data.venue}`;
    document.getElementById('scToss').textContent = data.tossInfo || 'Toss details unavailable';

    const resultBanner = document.getElementById('scResultMargin');
    if (data.resultMargin) {
        resultBanner.style.display = 'block';
        resultBanner.innerHTML = `<i class="bi bi-trophy-fill me-2 text-gold"></i>${data.resultMargin}`;
    } else {
        resultBanner.innerHTML = `<span class="text-cyan"><i class="bi bi-activity me-1"></i>Match in progress (${data.status})</span>`;
    }

    if (data.playerOfTheMatchName) {
        document.getElementById('scPlayerOfMatch').innerHTML = `
            <span class="text-warning"><i class="bi bi-star-fill me-1"></i>Player of the Match:</span>
            <strong class="text-white">${data.playerOfTheMatchName}</strong>
        `;
    }

    // Build Innings Tabs
    const tabsContainer = document.getElementById('inningsTabs');
    const contentContainer = document.getElementById('inningsContentContainer');

    if (!data.innings || data.innings.length === 0) {
        tabsContainer.innerHTML = '';
        contentContainer.innerHTML = `
            <div class="cp-card text-center py-4">
                <p class="text-secondary mb-0">No innings recorded for this match yet.</p>
            </div>
        `;
        return;
    }

    // Render Tabs
    tabsContainer.innerHTML = data.innings.map((inn, idx) => `
        <li class="nav-item">
            <button class="nav-link ${idx === 0 ? 'active' : ''}" 
                    data-bs-toggle="tab" 
                    data-bs-target="#inningsTabPane${inn.inningsId}">
                ${inn.battingTeamName} (${inn.totalRuns}/${inn.wickets})
            </button>
        </li>
    `).join('');

    // Render Tab Content
    contentContainer.innerHTML = `
        <div class="tab-content">
            ${data.innings.map((inn, idx) => `
                <div class="tab-pane fade ${idx === 0 ? 'show active' : ''}" id="inningsTabPane${inn.inningsId}">
                    ${renderSingleInnings(inn)}
                </div>
            `).join('')}
        </div>
    `;
}

function renderSingleInnings(inn) {
    return `
        <!-- Batting Scorecard -->
        <div class="cp-card mb-4">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="mb-0 brand-font text-white">
                    <span class="text-cyan">${inn.battingTeamName}</span> Batting
                </h5>
                <span class="badge bg-dark border border-secondary text-cyan">
                    ${inn.totalRuns}/${inn.wickets} (${inn.oversFormatted} Ov) • CRR: ${inn.runRate ? inn.runRate.toFixed(2) : '0.00'}
                </span>
            </div>

            <div class="table-responsive">
                <table class="cp-table">
                    <thead>
                        <tr>
                            <th>Batter</th>
                            <th>Dismissal</th>
                            <th class="text-center">R</th>
                            <th class="text-center">B</th>
                            <th class="text-center">4s</th>
                            <th class="text-center">6s</th>
                            <th class="text-end">SR</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${inn.battingCards && inn.battingCards.length > 0 ? inn.battingCards.map(b => `
                            <tr>
                                <td>
                                    <strong class="text-white">${b.playerName}</strong>
                                    ${!b.isOut ? '<span class="text-success ms-1 small">*</span>' : ''}
                                </td>
                                <td class="text-secondary small">${b.dismissalText || (b.isOut ? 'out' : 'not out')}</td>
                                <td class="text-center fw-bold text-white">${b.runs}</td>
                                <td class="text-center text-secondary">${b.ballsFaced}</td>
                                <td class="text-center">${b.fours}</td>
                                <td class="text-center">${b.sixes}</td>
                                <td class="text-end fw-semibold text-cyan">${b.strikeRate ? b.strikeRate.toFixed(1) : '0.0'}</td>
                            </tr>
                        `).join('') : `<tr><td colspan="7" class="text-center text-secondary py-3">No batting data recorded</td></tr>`}

                        <!-- Extras Row -->
                        <tr class="table-dark">
                            <td colspan="2">
                                <strong class="text-white">Extras</strong>
                                <span class="text-secondary small ms-2">(b ${inn.extrasBye || 0}, lb ${inn.extrasLegBye || 0}, w ${inn.extrasWide || 0}, nb ${inn.extrasNoBall || 0})</span>
                            </td>
                            <td class="text-center fw-bold text-white" colspan="5">${inn.extrasTotal || 0}</td>
                        </tr>

                        <!-- Total Row -->
                        <tr class="table-dark">
                            <td colspan="2">
                                <strong class="h6 mb-0 text-white">Total Runs</strong>
                                <span class="text-secondary small ms-2">(${inn.wickets} wickets, ${inn.oversFormatted} overs)</span>
                            </td>
                            <td class="text-center fw-bold h5 text-emerald mb-0" colspan="5">${inn.totalRuns}</td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <!-- Did Not Bat List -->
            ${inn.didNotBat && inn.didNotBat.length > 0 ? `
                <div class="p-3 border-top border-secondary border-opacity-25 mt-2">
                    <span class="text-secondary small fw-bold me-2">Yet To Bat:</span>
                    ${inn.didNotBat.map(name => `
                        <span class="badge bg-dark border border-secondary text-secondary me-1 mb-1">${name}</span>
                    `).join('')}
                </div>
            ` : ''}
        </div>

        <!-- Bowling Scorecard -->
        <div class="cp-card mb-4">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="mb-0 brand-font text-white">
                    <span class="text-info">${inn.bowlingTeamName}</span> Bowling
                </h5>
            </div>

            <div class="table-responsive">
                <table class="cp-table">
                    <thead>
                        <tr>
                            <th>Bowler</th>
                            <th class="text-center">O</th>
                            <th class="text-center">M</th>
                            <th class="text-center">R</th>
                            <th class="text-center">W</th>
                            <th class="text-end">ECON</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${inn.bowlingCards && inn.bowlingCards.length > 0 ? inn.bowlingCards.map(bw => `
                            <tr>
                                <td><strong class="text-white">${bw.playerName}</strong></td>
                                <td class="text-center fw-bold text-white">${bw.oversFormatted}</td>
                                <td class="text-center text-secondary">${bw.maidens}</td>
                                <td class="text-center text-white">${bw.runsConceded}</td>
                                <td class="text-center fw-bold text-emerald">${bw.wickets}</td>
                                <td class="text-end fw-semibold text-cyan">${bw.economy ? bw.economy.toFixed(2) : '0.00'}</td>
                            </tr>
                        `).join('') : `<tr><td colspan="6" class="text-center text-secondary py-3">No bowling data recorded</td></tr>`}
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Fall of Wickets -->
        ${inn.fallOfWickets && inn.fallOfWickets.length > 0 ? `
            <div class="cp-card mb-4">
                <h5 class="mb-3 brand-font text-white"><i class="bi bi-diagram-3 me-2 text-warning"></i>Fall of Wickets</h5>
                <div class="table-responsive">
                    <table class="cp-table">
                        <thead>
                            <tr>
                                <th>Wicket</th>
                                <th>Score</th>
                                <th>Over</th>
                                <th>Batter Dismissed</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${inn.fallOfWickets.map(f => `
                                <tr>
                                    <td><span class="badge bg-danger bg-opacity-25 text-danger fw-bold">${f.wicketNumber}</span></td>
                                    <td class="fw-bold text-white">${f.teamScore}</td>
                                    <td class="text-cyan">${f.oversFormatted} ov</td>
                                    <td class="text-white">${f.playerName}</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        ` : ''}
    `;
}
