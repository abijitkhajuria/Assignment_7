/**
 * CricPulse — Admin Dashboard & Live Scoring Console Logic
 */

let currentMatchId = null;
let currentInnings = null;
let currentInningsList = [];
let battingRoster = [];
let bowlingRoster = [];
let allTeamsList = [];
let wicketModalInstance = null;
let finishModalInstance = null;

document.addEventListener('DOMContentLoaded', () => {
    wicketModalInstance = new bootstrap.Modal(document.getElementById('wicketModal'));
    finishModalInstance = new bootstrap.Modal(document.getElementById('finishModal'));

    // Check login state
    checkAuth();

    // Login Form
    document.getElementById('adminLoginForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const u = document.getElementById('loginUsername').value;
        const p = document.getElementById('loginPassword').value;
        try {
            const res = await CricPulseAPI.adminLogin(u, p);
            localStorage.setItem('cricpulse_admin_token', res.token);
            localStorage.setItem('cricpulse_admin_user', res.fullName || res.username);
            CricPulseAPI.showToast('Authentication successful! Welcome to Admin Console.', 'success');
            checkAuth();
        } catch (err) {
            CricPulseAPI.showToast(err.message || 'Invalid credentials', 'error');
        }
    });

    // Create Match Form
    document.getElementById('createMatchForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        await handleCreateMatch();
    });

    // Match Selector Change
    document.getElementById('adminMatchSelect').addEventListener('change', async (e) => {
        currentMatchId = e.target.value;
        await loadMatchScoringState(currentMatchId);
    });
});

function checkAuth() {
    const token = localStorage.getItem('cricpulse_admin_token');
    const user = localStorage.getItem('cricpulse_admin_user');

    if (token) {
        document.getElementById('loginSection').classList.add('d-none');
        document.getElementById('adminHubSection').classList.remove('d-none');
        document.getElementById('adminSessionPill').classList.remove('d-none');
        document.getElementById('btnSignOut').classList.remove('d-none');
        document.getElementById('adminUserLabel').textContent = user || 'Admin';

        initAdminHub();
    } else {
        document.getElementById('loginSection').classList.remove('d-none');
        document.getElementById('adminHubSection').classList.add('d-none');
        document.getElementById('adminSessionPill').classList.add('d-none');
        document.getElementById('btnSignOut').classList.add('d-none');
    }
}

function logoutAdmin() {
    localStorage.removeItem('cricpulse_admin_token');
    localStorage.removeItem('cricpulse_admin_user');
    CricPulseAPI.showToast('Signed out of admin session.', 'info');
    checkAuth();
}

function switchAdminTab(tab) {
    if (tab === 'scorer') {
        document.getElementById('viewScorer').classList.remove('d-none');
        document.getElementById('viewMatches').classList.add('d-none');
        document.getElementById('tabScorerBtn').classList.add('active');
        document.getElementById('tabMatchesBtn').classList.remove('active');
    } else {
        document.getElementById('viewScorer').classList.add('d-none');
        document.getElementById('viewMatches').classList.remove('d-none');
        document.getElementById('tabScorerBtn').classList.remove('active');
        document.getElementById('tabMatchesBtn').classList.add('active');
        loadAdminMatchesList();
    }
}

async function initAdminHub() {
    try {
        allTeamsList = await CricPulseAPI.getTeams();
        populateTeamDropdowns();

        const matches = await CricPulseAPI.getAllMatches();
        const select = document.getElementById('adminMatchSelect');

        if (matches && matches.length > 0) {
            select.innerHTML = matches.map(m => `
                <option value="${m.id}" ${m.status === 'LIVE' ? 'selected' : ''}>
                    [${m.status}] ${m.title} (${m.team1ShortName} vs ${m.team2ShortName})
                </option>
            `).join('');

            // Select live match or first
            const liveMatch = matches.find(m => m.status === 'LIVE') || matches[0];
            currentMatchId = liveMatch.id;
            select.value = currentMatchId;

            await loadMatchScoringState(currentMatchId);
        } else {
            select.innerHTML = `<option value="">No matches available</option>`;
        }
    } catch (err) {
        console.error('Failed to init admin hub:', err);
    }
}

async function loadMatchScoringState(matchId) {
    if (!matchId) return;

    try {
        const [inningsList, snapshot] = await Promise.all([
            CricPulseAPI.getMatchInnings(matchId),
            CricPulseAPI.getLiveSnapshot(matchId)
        ]);

        currentInningsList = inningsList;
        if (inningsList && inningsList.length > 0) {
            // Find active innings or last innings
            currentInnings = inningsList.find(i => !i.isCompleted) || inningsList[inningsList.length - 1];
        }

        if (currentInnings) {
            // Load rosters
            const [batPlayers, bowlPlayers] = await Promise.all([
                CricPulseAPI.getPlayers(currentInnings.battingTeam.id),
                CricPulseAPI.getPlayers(currentInnings.bowlingTeam.id)
            ]);
            battingRoster = batPlayers;
            bowlingRoster = bowlPlayers;

            populateCreaseDropdowns(snapshot);
        }

        updateLiveMiniPreview(snapshot);
    } catch (err) {
        console.error('Failed to load match scoring state:', err);
    }
}

function populateCreaseDropdowns(snapshot) {
    const sStriker = document.getElementById('selectStriker');
    const sNonStriker = document.getElementById('selectNonStriker');
    const sBowler = document.getElementById('selectBowler');

    const strikerId = snapshot && snapshot.striker ? snapshot.striker.playerId : null;
    const nonStrikerId = snapshot && snapshot.nonStriker ? snapshot.nonStriker.playerId : null;
    const bowlerId = snapshot && snapshot.currentBowler ? snapshot.currentBowler.playerId : null;

    sStriker.innerHTML = battingRoster.map(p => `
        <option value="${p.id}" ${p.id === strikerId ? 'selected' : ''}>${p.name}</option>
    `).join('');

    sNonStriker.innerHTML = battingRoster.map(p => `
        <option value="${p.id}" ${p.id === nonStrikerId ? 'selected' : ''}>${p.name}</option>
    `).join('');

    // If striker & non-striker match, bump non-striker to next
    if (sStriker.value === sNonStriker.value && battingRoster.length > 1) {
        sNonStriker.selectedIndex = (sStriker.selectedIndex + 1) % battingRoster.length;
    }

    sBowler.innerHTML = bowlingRoster.map(p => `
        <option value="${p.id}" ${p.id === bowlerId ? 'selected' : ''}>${p.name}</option>
    `).join('');
}

function updateLiveMiniPreview(data) {
    if (!data) return;

    document.getElementById('adminScorePreview').textContent = `${data.totalRuns || 0}/${data.wickets || 0} (${data.oversFormatted || '0.0'} ov)`;
    document.getElementById('adminInningsTeamPreview').textContent = `${data.battingTeamName || 'Batting Team'} (Inn ${data.inningsNumber || 1})`;

    // Crease status card
    if (data.striker) {
        document.getElementById('previewStrikerName').textContent = `* ${data.striker.name} (Striker)`;
        document.getElementById('previewStrikerStats').textContent = `${data.striker.runs} (${data.striker.ballsFaced}b, ${data.striker.fours}x4, ${data.striker.sixes}x6)`;
    } else {
        document.getElementById('previewStrikerName').textContent = '—';
        document.getElementById('previewStrikerStats').textContent = '0 (0b)';
    }

    if (data.nonStriker) {
        document.getElementById('previewNonStrikerName').textContent = `${data.nonStriker.name} (Non-Striker)`;
        document.getElementById('previewNonStrikerStats').textContent = `${data.nonStriker.runs} (${data.nonStriker.ballsFaced}b)`;
    } else {
        document.getElementById('previewNonStrikerName').textContent = '—';
        document.getElementById('previewNonStrikerStats').textContent = '0 (0b)';
    }

    if (data.currentBowler) {
        document.getElementById('previewBowlerName').textContent = data.currentBowler.name;
        document.getElementById('previewBowlerStats').textContent = `${data.currentBowler.oversFormatted} ov, ${data.currentBowler.wickets}-${data.currentBowler.runsConceded}`;
    } else {
        document.getElementById('previewBowlerName').textContent = '—';
        document.getElementById('previewBowlerStats').textContent = '0.0 ov, 0-0';
    }

    // Recent balls
    const container = document.getElementById('adminRecentBallsContainer');
    if (data.recentBalls && data.recentBalls.length > 0) {
        const balls = [...data.recentBalls].reverse();
        container.innerHTML = balls.map(b => {
            let chipClass = 'chip-0';
            if (b.isWicket) chipClass = 'chip-w';
            else if (b.ballDisplay === '4') chipClass = 'chip-4';
            else if (b.ballDisplay === '6') chipClass = 'chip-6';
            else if (b.ballDisplay.includes('wd') || b.ballDisplay.includes('nb')) chipClass = 'chip-extra';
            else if (['1', '2', '3'].includes(b.ballDisplay)) chipClass = 'chip-1';

            return `<span class="ball-chip ${chipClass}" style="width: 28px; height: 28px; font-size: 0.75rem;">${b.ballDisplay}</span>`;
        }).join('');
    } else {
        container.innerHTML = `<span class="text-secondary small">No deliveries yet</span>`;
    }
}

async function submitDelivery(runsBatter, extraType = 'NONE', extraRuns = 0) {
    if (!currentInnings) {
        CricPulseAPI.showToast('No active innings selected.', 'error');
        return;
    }

    const strikerId = parseInt(document.getElementById('selectStriker').value);
    const nonStrikerId = parseInt(document.getElementById('selectNonStriker').value);
    const bowlerId = parseInt(document.getElementById('selectBowler').value);

    if (strikerId === nonStrikerId) {
        CricPulseAPI.showToast('Striker and non-striker cannot be the same player!', 'error');
        return;
    }

    const payload = {
        strikerId,
        nonStrikerId,
        bowlerId,
        runsBatter,
        extraType,
        runsExtra: extraRuns,
        isWicket: false
    };

    try {
        await CricPulseAPI.recordDelivery(currentInnings.id, payload);
        CricPulseAPI.showToast(`Recorded: ${extraType !== 'NONE' ? extraType : `${runsBatter} run(s)`}!`, 'success');
        await loadMatchScoringState(currentMatchId);
    } catch (err) {
        CricPulseAPI.showToast(err.message || 'Scoring action failed', 'error');
    }
}

async function undoLastBall() {
    if (!currentInnings) return;
    try {
        await CricPulseAPI.undoLastDelivery(currentInnings.id);
        CricPulseAPI.showToast('Last delivery rolled back successfully.', 'info');
        await loadMatchScoringState(currentMatchId);
    } catch (err) {
        CricPulseAPI.showToast(err.message || 'Undo failed', 'error');
    }
}

function openWicketModal() {
    if (!currentInnings) return;

    const sStriker = document.getElementById('selectStriker');
    const sNonStriker = document.getElementById('selectNonStriker');

    // Populate dismissed player dropdown
    const wDismissed = document.getElementById('wDismissed');
    wDismissed.innerHTML = `
        <option value="${sStriker.value}">Striker: ${sStriker.options[sStriker.selectedIndex]?.text}</option>
        <option value="${sNonStriker.value}">Non-Striker: ${sNonStriker.options[sNonStriker.selectedIndex]?.text}</option>
    `;

    // Populate fielders (bowling team)
    const wFielder = document.getElementById('wFielder');
    wFielder.innerHTML = `<option value="">None / Direct</option>` + bowlingRoster.map(p => `
        <option value="${p.id}">${p.name}</option>
    `).join('');

    // Populate new batter (batting team members not already at crease)
    const wNext = document.getElementById('wNextBatter');
    const activeIds = [parseInt(sStriker.value), parseInt(sNonStriker.value)];
    const availableBatters = battingRoster.filter(p => !activeIds.includes(p.id));

    wNext.innerHTML = availableBatters.map(p => `
        <option value="${p.id}">${p.name} (#${p.jerseyNumber || '—'})</option>
    `).join('') + `<option value="">None / All Out</option>`;

    wicketModalInstance.show();
}

async function confirmWicketSubmission() {
    const strikerId = parseInt(document.getElementById('selectStriker').value);
    const nonStrikerId = parseInt(document.getElementById('selectNonStriker').value);
    const bowlerId = parseInt(document.getElementById('selectBowler').value);

    const wicketType = document.getElementById('wType').value;
    const dismissedPlayerId = parseInt(document.getElementById('wDismissed').value);
    const fielderIdVal = document.getElementById('wFielder').value;
    const newBatsmanIdVal = document.getElementById('wNextBatter').value;

    const payload = {
        strikerId,
        nonStrikerId,
        bowlerId,
        runsBatter: 0,
        extraType: 'NONE',
        runsExtra: 0,
        isWicket: true,
        wicketType,
        dismissedPlayerId,
        fielderId: fielderIdVal ? parseInt(fielderIdVal) : null,
        newBatsmanId: newBatsmanIdVal ? parseInt(newBatsmanIdVal) : null
    };

    try {
        await CricPulseAPI.recordDelivery(currentInnings.id, payload);
        wicketModalInstance.hide();
        CricPulseAPI.showToast(`WICKET RECORDED! (${wicketType})`, 'error');
        await loadMatchScoringState(currentMatchId);
    } catch (err) {
        CricPulseAPI.showToast(err.message || 'Wicket recording failed', 'error');
    }
}

async function completeCurrentInnings() {
    if (!currentInnings) return;
    if (!confirm('Are you sure you want to officially end this innings?')) return;

    try {
        await CricPulseAPI.completeInnings(currentInnings.id);
        CricPulseAPI.showToast('Innings completed!', 'success');
        await loadMatchScoringState(currentMatchId);
    } catch (err) {
        CricPulseAPI.showToast(err.message || 'Complete innings failed', 'error');
    }
}

function openFinishModal() {
    if (!currentMatchId) return;

    const winnerSelect = document.getElementById('finishWinnerTeam');
    winnerSelect.innerHTML = `<option value="">None / Match Tied</option>` + allTeamsList.map(t => `
        <option value="${t.id}">${t.name} (${t.shortName})</option>
    `).join('');

    finishModalInstance.show();
}

async function confirmFinishMatch() {
    const margin = document.getElementById('finishMargin').value;
    const winnerIdVal = document.getElementById('finishWinnerTeam').value;
    const winnerId = winnerIdVal ? parseInt(winnerIdVal) : null;

    try {
        await CricPulseAPI.finishMatch(currentMatchId, margin, winnerId);
        finishModalInstance.hide();
        CricPulseAPI.showToast('Match marked as COMPLETED!', 'success');
        await initAdminHub();
    } catch (err) {
        CricPulseAPI.showToast(err.message || 'Failed to finish match', 'error');
    }
}

// ----------------------------------------------------
// Match Manager View Functions
// ----------------------------------------------------
function populateTeamDropdowns() {
    const t1 = document.getElementById('mTeam1');
    const t2 = document.getElementById('mTeam2');

    const opts = allTeamsList.map(t => `<option value="${t.id}">${t.name} (${t.shortName})</option>`).join('');
    t1.innerHTML = opts;
    t2.innerHTML = opts;
    if (t2.options.length > 1) {
        t2.selectedIndex = 1;
    }
}

async function handleCreateMatch() {
    const team1Id = parseInt(document.getElementById('mTeam1').value);
    const team2Id = parseInt(document.getElementById('mTeam2').value);

    if (team1Id === team2Id) {
        CricPulseAPI.showToast('Team 1 and Team 2 must be different teams!', 'error');
        return;
    }

    const payload = {
        title: document.getElementById('mTitle').value,
        venue: document.getElementById('mVenue').value,
        format: document.getElementById('mFormat').value,
        totalOvers: parseInt(document.getElementById('mOvers').value) || 20,
        team1Id,
        team2Id
    };

    const dateVal = document.getElementById('mDate').value;
    if (dateVal) {
        payload.matchDate = dateVal;
    }

    try {
        const created = await CricPulseAPI.createMatch(payload);
        CricPulseAPI.showToast('New match scheduled successfully!', 'success');
        document.getElementById('createMatchForm').reset();
        await initAdminHub();
        await loadAdminMatchesList();
    } catch (err) {
        CricPulseAPI.showToast(err.message || 'Failed to create match', 'error');
    }
}

async function loadAdminMatchesList() {
    const tbody = document.getElementById('adminMatchesTableBody');
    try {
        const matches = await CricPulseAPI.getAllMatches();
        if (!matches || matches.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center text-secondary py-3">No matches found</td></tr>`;
            return;
        }

        tbody.innerHTML = matches.map(m => {
            const isLive = m.status === 'LIVE' || m.status === 'INNINGS_BREAK';
            return `
                <tr>
                    <td>
                        <strong class="text-white">${m.title}</strong>
                        <div class="text-muted small">${m.format} • ${m.totalOvers} Ov</div>
                    </td>
                    <td>
                        <span class="badge bg-primary bg-opacity-25 text-primary me-1">${m.team1ShortName}</span>
                        <span class="text-muted small">vs</span>
                        <span class="badge bg-warning bg-opacity-25 text-warning ms-1">${m.team2ShortName}</span>
                    </td>
                    <td>
                        <span class="${isLive ? 'badge-live' : (m.status === 'COMPLETED' ? 'badge-completed' : 'badge-upcoming')}">${m.status}</span>
                    </td>
                    <td class="text-secondary small">${m.venue.split(',')[0]}</td>
                    <td class="text-end">
                        <div class="btn-group btn-group-sm">
                            <button class="btn btn-cp-primary py-0 px-2" onclick="switchToScoring(${m.id})">
                                <i class="bi bi-bullseye"></i> Score
                            </button>
                            ${m.status === 'SCHEDULED' ? `
                                <button class="btn btn-outline-success py-0 px-2" onclick="startMatchAdmin(${m.id})">
                                    <i class="bi bi-play-fill"></i> Start
                                </button>
                            ` : ''}
                            <button class="btn btn-outline-danger py-0 px-2" onclick="deleteMatchAdmin(${m.id})">
                                <i class="bi bi-trash"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    } catch (err) {
        console.error('Failed to load admin matches:', err);
    }
}

function switchToScoring(matchId) {
    currentMatchId = matchId;
    document.getElementById('adminMatchSelect').value = matchId;
    switchAdminTab('scorer');
    loadMatchScoringState(matchId);
}

async function startMatchAdmin(matchId) {
    try {
        await CricPulseAPI.startMatch(matchId);
        CricPulseAPI.showToast('Match is now LIVE!', 'success');
        await loadAdminMatchesList();
        await initAdminHub();
    } catch (err) {
        CricPulseAPI.showToast(err.message || 'Failed to start match', 'error');
    }
}

async function deleteMatchAdmin(matchId) {
    if (!confirm('Are you sure you want to delete this match record?')) return;
    try {
        await CricPulseAPI.deleteMatch(matchId);
        CricPulseAPI.showToast('Match deleted.', 'info');
        await loadAdminMatchesList();
        await initAdminHub();
    } catch (err) {
        CricPulseAPI.showToast(err.message || 'Failed to delete match', 'error');
    }
}

async function reseedFixtures() {
    if (!confirm('This will refresh the database with accurate 2024 fixtures (IND vs SA Live Today, IND vs NZ 2nd Test, AUS vs PAK Tomorrow, etc.). Proceed?')) return;
    try {
        await CricPulseAPI.reseedDatabase();
        CricPulseAPI.showToast('Database successfully re-seeded with realistic fixtures!', 'success');
        setTimeout(() => location.reload(), 800);
    } catch (err) {
        CricPulseAPI.showToast('Failed to reseed database: ' + err.message, 'error');
    }
}

