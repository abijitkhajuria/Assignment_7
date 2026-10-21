/**
 * CricPulse — Home Dashboard Logic
 * Displays Live Today, Tomorrow, Upcoming, and Recent Results
 */

let activeFilter = 'all';
let currentFormat = '';
let liveMatchesCache = [];

document.addEventListener('DOMContentLoaded', () => {
    loadDashboardData();

    // Set up filter buttons
    document.querySelectorAll('#matchFilterTabs button').forEach(btn => {
        btn.addEventListener('click', (e) => {
            document.querySelectorAll('#matchFilterTabs button').forEach(b => b.classList.remove('active'));
            const targetBtn = e.currentTarget || e.target;
            targetBtn.classList.add('active');
            activeFilter = targetBtn.dataset.filter;
            applyFilters();
        });
    });

    // Format dropdown
    document.getElementById('formatSelect').addEventListener('change', (e) => {
        currentFormat = e.target.value;
        loadDashboardData();
    });

    // Auto-refresh dashboard summaries every 5 seconds
    setInterval(loadDashboardData, 5000);
});

async function loadDashboardData() {
    try {
        const [live, tomorrow, upcoming, completed] = await Promise.all([
            CricPulseAPI.getLiveMatches(),
            CricPulseAPI.getTomorrowMatches(),
            CricPulseAPI.getUpcomingMatches(),
            CricPulseAPI.getCompletedMatches(currentFormat)
        ]);

        liveMatchesCache = live;

        // Filter by format if selected
        const filterByFormat = (list) => {
            if (!currentFormat) return list;
            return list.filter(m => m.format === currentFormat);
        };

        const filteredLive = filterByFormat(live);
        const filteredTomorrow = filterByFormat(tomorrow);
        const filteredUpcoming = filterByFormat(upcoming);
        const filteredCompleted = completed; // completed endpoint already handles format filter

        // Update counts
        document.getElementById('liveCount').textContent = filteredLive.length;
        document.getElementById('badgeLiveCount').textContent = filteredLive.length;

        const tomorrowCountEl = document.getElementById('tomorrowCount');
        if (tomorrowCountEl) tomorrowCountEl.textContent = filteredTomorrow.length;
        const badgeTomorrowCountEl = document.getElementById('badgeTomorrowCount');
        if (badgeTomorrowCountEl) badgeTomorrowCountEl.textContent = filteredTomorrow.length;

        document.getElementById('upcomingCount').textContent = filteredUpcoming.length;
        document.getElementById('badgeUpcomingCount').textContent = filteredUpcoming.length;

        // Update Nav Live Match link to first live match if available
        if (filteredLive.length > 0) {
            document.getElementById('navLiveMatchLink').href = `/match-live.html?id=${filteredLive[0].id}`;
        }

        // Render Featured Hero Match (Prefer live match, otherwise latest completed)
        renderFeaturedMatch(filteredLive.length > 0 ? filteredLive[0] : (filteredCompleted.length > 0 ? filteredCompleted[0] : null));

        // Render Match Grids
        renderMatchGrid('liveMatchesGrid', filteredLive, 'live');
        renderMatchGrid('tomorrowMatchesGrid', filteredTomorrow, 'tomorrow');
        renderMatchGrid('upcomingMatchesGrid', filteredUpcoming, 'upcoming');
        renderMatchGrid('completedMatchesGrid', filteredCompleted, 'completed');

        applyFilters();
    } catch (err) {
        console.error('Failed to load dashboard data:', err);
    }
}

function renderFeaturedMatch(match) {
    const container = document.getElementById('featuredMatchContainer');
    if (!match) {
        container.innerHTML = `
            <div class="cp-card cp-card-featured text-center py-4">
                <i class="bi bi-calendar-x text-secondary fs-1 mb-2"></i>
                <h5 class="text-white">No active matches at the moment</h5>
                <p class="text-secondary small mb-3">Check the upcoming schedule or review completed match results.</p>
                <a href="/admin.html" class="btn btn-sm btn-cp-primary">Create or Start a Match</a>
            </div>
        `;
        return;
    }

    const isLive = match.status === 'LIVE' || match.status === 'INNINGS_BREAK';
    const statusBadge = isLive
        ? `<span class="badge-live"><span class="pulse-dot"></span> LIVE TODAY</span>`
        : `<span class="badge-completed">${match.status}</span>`;

    const statusNote = match.currentStatusNote || match.dayNote || match.resultMargin || 'Match in progress';

    container.innerHTML = `
        <div class="cp-card cp-card-featured">
            <div class="d-flex flex-wrap justify-content-between align-items-center mb-2">
                <div class="d-flex align-items-center gap-2 flex-wrap">
                    ${statusBadge}
                    <span class="badge-format">${match.format} • ${match.totalOvers} Overs</span>
                    ${match.seriesName ? `<span class="badge bg-dark text-warning border border-warning border-opacity-50"><i class="bi bi-trophy me-1"></i>${match.seriesName}</span>` : ''}
                    <span class="text-secondary small d-none d-sm-inline"><i class="bi bi-geo-alt me-1"></i>${match.venue}</span>
                </div>
                <div class="d-flex gap-2">
                    <a href="/match-live.html?id=${match.id}" class="btn btn-sm btn-cp-primary">
                        <i class="bi bi-broadcast me-1"></i> Live Match Center
                    </a>
                    <a href="/scorecard.html?id=${match.id}" class="btn btn-sm btn-cp-outline">
                        <i class="bi bi-clipboard2-data me-1"></i> Scorecard
                    </a>
                </div>
            </div>

            <div class="row align-items-center g-3 my-2">
                <!-- Team 1 -->
                <div class="col-md-5">
                    <div class="d-flex align-items-center justify-content-between">
                        <div class="d-flex align-items-center gap-3">
                            <div class="team-badge" style="background-color: ${match.team1Color || '#0077B6'};">
                                ${match.team1ShortName}
                            </div>
                            <div>
                                <h3 class="h5 mb-0 text-white">${match.team1Name}</h3>
                                <div class="text-secondary small">${match.innings1TeamName === match.team1ShortName ? '1st Innings' : 'Bowling 1st'}</div>
                            </div>
                        </div>
                        <div class="text-end">
                            <span class="h4 brand-font text-white mb-0">
                                ${match.innings1TeamName === match.team1ShortName && match.innings1Runs !== null
                                    ? `${match.innings1Runs}/${match.innings1Wickets}`
                                    : (match.innings2TeamName === match.team1ShortName && match.innings2Runs !== null
                                        ? `${match.innings2Runs}/${match.innings2Wickets}`
                                        : '—')}
                            </span>
                            <div class="text-muted small">
                                ${match.innings1TeamName === match.team1ShortName && match.innings1Overs
                                    ? `(${match.innings1Overs} ov)`
                                    : (match.innings2TeamName === match.team1ShortName && match.innings2Overs
                                        ? `(${match.innings2Overs} ov)`
                                        : '')}
                            </div>
                        </div>
                    </div>
                </div>

                <!-- VS Divider -->
                <div class="col-md-2 text-center text-muted fw-bold">
                    <span class="badge bg-dark border border-secondary px-2 py-1">VS</span>
                </div>

                <!-- Team 2 -->
                <div class="col-md-5">
                    <div class="d-flex align-items-center justify-content-between">
                        <div class="d-flex align-items-center gap-3">
                            <div class="team-badge" style="background-color: ${match.team2Color || '#E5A93C'};">
                                ${match.team2ShortName}
                            </div>
                            <div>
                                <h3 class="h5 mb-0 text-white">${match.team2Name}</h3>
                                <div class="text-secondary small">${match.innings2TeamName === match.team2ShortName ? 'Chasing' : 'Batting'}</div>
                            </div>
                        </div>
                        <div class="text-end">
                            <span class="h4 brand-font text-white mb-0">
                                ${match.innings2TeamName === match.team2ShortName && match.innings2Runs !== null
                                    ? `${match.innings2Runs}/${match.innings2Wickets}`
                                    : (match.innings1TeamName === match.team2ShortName && match.innings1Runs !== null
                                        ? `${match.innings1Runs}/${match.innings1Wickets}`
                                        : '—')}
                            </span>
                            <div class="text-muted small">
                                ${match.innings2TeamName === match.team2ShortName && match.innings2Overs
                                    ? `(${match.innings2Overs} ov)`
                                    : (match.innings1TeamName === match.team2ShortName && match.innings1Overs
                                        ? `(${match.innings1Overs} ov)`
                                        : '')}
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- In-Play Metrics & Status Bar -->
            <div class="pt-3 mt-2 border-top border-secondary border-opacity-25 d-flex flex-wrap justify-content-between align-items-center gap-2">
                <div class="d-flex align-items-center gap-3 flex-wrap">
                    <span class="text-cyan fw-semibold small">
                        <i class="bi bi-info-circle me-1"></i>${statusNote}
                    </span>
                    ${match.currentRunRate ? `<span class="badge bg-dark text-secondary border border-secondary border-opacity-50">CRR: <strong class="text-white">${match.currentRunRate}</strong></span>` : ''}
                    ${match.requiredRunRate ? `<span class="badge bg-dark text-warning border border-warning border-opacity-50">RRR: <strong class="text-warning">${match.requiredRunRate}</strong></span>` : ''}
                </div>
                <div class="text-secondary small">
                    <i class="bi bi-geo-alt me-1 text-gold"></i>${match.title}
                </div>
            </div>
        </div>
    `;
}

function renderMatchGrid(elementId, matches, type) {
    const grid = document.getElementById(elementId);
    if (!grid) return;

    if (!matches || matches.length === 0) {
        let emptyMsg = `No ${type} matches available right now.`;
        if (type === 'tomorrow') emptyMsg = 'No matches scheduled for tomorrow. Check all upcoming fixtures.';
        grid.innerHTML = `
            <div class="col-12">
                <div class="cp-card text-center py-4">
                    <p class="text-secondary small mb-0">${emptyMsg}</p>
                </div>
            </div>
        `;
        return;
    }

    grid.innerHTML = matches.map(m => {
        const isLive = m.status === 'LIVE' || m.status === 'INNINGS_BREAK';
        
        let dayBadgeHtml = '';
        if (isLive) {
            dayBadgeHtml = `<span class="badge bg-danger-subtle text-danger border border-danger border-opacity-50"><span class="pulse-dot"></span> LIVE TODAY</span>`;
        } else if (m.isTomorrow) {
            dayBadgeHtml = `<span class="badge bg-warning-subtle text-warning border border-warning border-opacity-50"><i class="bi bi-clock me-1"></i>TOMORROW • ${m.matchTime || '09:00 AM'}</span>`;
        } else if (m.isToday) {
            dayBadgeHtml = `<span class="badge bg-info-subtle text-info border border-info border-opacity-50">TODAY • ${m.matchTime || ''}</span>`;
        } else if (m.dayLabel === 'YESTERDAY') {
            dayBadgeHtml = `<span class="badge bg-secondary-subtle text-secondary border border-secondary border-opacity-50">YESTERDAY</span>`;
        } else if (m.status === 'SCHEDULED') {
            dayBadgeHtml = `<span class="badge-upcoming"><i class="bi bi-calendar3 me-1"></i>${m.dayLabel || 'SCHEDULED'}</span>`;
        } else {
            dayBadgeHtml = `<span class="badge-completed">${m.dayLabel || 'FINISHED'}</span>`;
        }

        const score1 = m.innings1Runs !== null ? `${m.innings1Runs}/${m.innings1Wickets} <span class="text-muted small">(${m.innings1Overs})</span>` : '—';
        const score2 = m.innings2Runs !== null ? `${m.innings2Runs}/${m.innings2Wickets} <span class="text-muted small">(${m.innings2Overs})</span>` : '—';

        const displayNote = m.currentStatusNote || m.dayNote || m.resultMargin || (m.status === 'SCHEDULED' ? `Starts at ${m.matchTime || 'TBD'}` : 'Match Finished');

        return `
            <div class="col-md-6 col-lg-4">
                <div class="cp-card h-100 d-flex flex-column">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <div class="d-flex align-items-center gap-2 flex-wrap">
                            ${dayBadgeHtml}
                            <span class="badge-format">${m.format}</span>
                        </div>
                        <span class="text-muted small">${m.venue ? m.venue.split(',')[0] : ''}</span>
                    </div>

                    ${m.seriesName ? `<div class="text-secondary small mb-1 text-truncate" title="${m.seriesName}"><i class="bi bi-trophy text-warning me-1"></i>${m.seriesName}</div>` : ''}
                    <h6 class="text-white mb-3 text-truncate" title="${m.title}">${m.title}</h6>

                    <!-- Teams & Scores -->
                    <div class="mb-3 flex-grow-1">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <div class="d-flex align-items-center gap-2">
                                <div class="team-badge" style="width: 28px; height: 28px; font-size: 0.75rem; background-color: ${m.team1Color || '#0077B6'};">
                                    ${m.team1ShortName}
                                </div>
                                <span class="fw-semibold text-white small">${m.team1Name}</span>
                            </div>
                            <span class="fw-bold text-white small">${m.innings1TeamName === m.team1ShortName ? score1 : (m.innings2TeamName === m.team1ShortName ? score2 : '—')}</span>
                        </div>

                        <div class="d-flex justify-content-between align-items-center">
                            <div class="d-flex align-items-center gap-2">
                                <div class="team-badge" style="width: 28px; height: 28px; font-size: 0.75rem; background-color: ${m.team2Color || '#E5A93C'};">
                                    ${m.team2ShortName}
                                </div>
                                <span class="fw-semibold text-white small">${m.team2Name}</span>
                            </div>
                            <span class="fw-bold text-white small">${m.innings2TeamName === m.team2ShortName ? score2 : (m.innings1TeamName === m.team2ShortName ? score1 : '—')}</span>
                        </div>
                    </div>

                    <div class="pt-2 border-top border-secondary border-opacity-25 d-flex justify-content-between align-items-center mt-auto">
                        <span class="text-cyan small text-truncate me-2" style="max-width: 175px;" title="${displayNote}">
                            ${displayNote}
                        </span>
                        <div class="d-flex gap-1">
                            ${isLive ? `<a href="/match-live.html?id=${m.id}" class="btn btn-sm btn-cp-primary py-1 px-2" style="font-size: 0.75rem;"><i class="bi bi-broadcast me-1"></i>Live</a>` : ''}
                            <a href="/scorecard.html?id=${m.id}" class="btn btn-sm btn-cp-outline py-1 px-2" style="font-size: 0.75rem;">Scorecard</a>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function applyFilters() {
    const liveSec = document.getElementById('sectionLive');
    const tomorrowSec = document.getElementById('sectionTomorrow');
    const upSec = document.getElementById('sectionUpcoming');
    const compSec = document.getElementById('sectionCompleted');

    if (activeFilter === 'all') {
        if (liveSec) liveSec.style.display = 'block';
        if (tomorrowSec) tomorrowSec.style.display = 'block';
        if (upSec) upSec.style.display = 'block';
        if (compSec) compSec.style.display = 'block';
    } else if (activeFilter === 'live') {
        if (liveSec) liveSec.style.display = 'block';
        if (tomorrowSec) tomorrowSec.style.display = 'none';
        if (upSec) upSec.style.display = 'none';
        if (compSec) compSec.style.display = 'none';
    } else if (activeFilter === 'tomorrow') {
        if (liveSec) liveSec.style.display = 'none';
        if (tomorrowSec) tomorrowSec.style.display = 'block';
        if (upSec) upSec.style.display = 'none';
        if (compSec) compSec.style.display = 'none';
    } else if (activeFilter === 'upcoming') {
        if (liveSec) liveSec.style.display = 'none';
        if (tomorrowSec) tomorrowSec.style.display = 'block';
        if (upSec) upSec.style.display = 'block';
        if (compSec) compSec.style.display = 'none';
    } else if (activeFilter === 'completed') {
        if (liveSec) liveSec.style.display = 'none';
        if (tomorrowSec) tomorrowSec.style.display = 'none';
        if (upSec) upSec.style.display = 'none';
        if (compSec) compSec.style.display = 'block';
    }
}
