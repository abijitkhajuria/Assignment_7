/**
 * CricPulse — Player Profiles & Statistics with Chart.js
 */

let allTeams = [];
let allPlayers = [];
let selectedTeamId = null;
let radarChartInstance = null;
let battingChartInstance = null;
let bowlingChartInstance = null;
let playerModal = null;

document.addEventListener('DOMContentLoaded', async () => {
    playerModal = new bootstrap.Modal(document.getElementById('playerModal'));
    await loadInitialData();
});

async function loadInitialData() {
    try {
        [allTeams, allPlayers] = await Promise.all([
            CricPulseAPI.getTeams(),
            CricPulseAPI.getPlayers()
        ]);

        renderTeamFilters();
        renderPlayersGrid();
        renderTournamentLeadersCharts();
    } catch (err) {
        console.error('Failed to load player data:', err);
    }
}

function renderTeamFilters() {
    const container = document.getElementById('teamFilterContainer');
    let html = `<button class="btn btn-sm ${selectedTeamId === null ? 'btn-cp-primary' : 'btn-outline-light'}" onclick="filterByTeam(null)">All Teams</button>`;

    allTeams.forEach(t => {
        const isActive = selectedTeamId === t.id;
        html += `
            <button class="btn btn-sm ${isActive ? 'btn-cp-primary' : 'btn-outline-light'}" onclick="filterByTeam(${t.id})">
                ${t.name}
            </button>
        `;
    });

    container.innerHTML = html;
}

function filterByTeam(teamId) {
    selectedTeamId = teamId;
    renderTeamFilters();
    renderPlayersGrid();
}

function renderPlayersGrid() {
    const grid = document.getElementById('playersGrid');
    const filtered = selectedTeamId
        ? allPlayers.filter(p => p.teamId === selectedTeamId)
        : allPlayers;

    if (!filtered || filtered.length === 0) {
        grid.innerHTML = `
            <div class="col-12">
                <div class="cp-card text-center py-4">
                    <p class="text-secondary small mb-0">No players found for this team.</p>
                </div>
            </div>
        `;
        return;
    }

    grid.innerHTML = filtered.map(p => {
        // Initials
        const initials = p.name.split(' ').map(n => n[0]).join('').substring(0, 2);
        let roleBadgeClass = 'bg-primary';
        if (p.role === 'BOWLER') roleBadgeClass = 'bg-info text-dark';
        else if (p.role === 'ALL_ROUNDER') roleBadgeClass = 'bg-success';
        else if (p.role === 'WICKET_KEEPER') roleBadgeClass = 'bg-warning text-dark';

        return `
            <div class="col-md-6 col-lg-4 col-xl-3">
                <div class="cp-card h-100 cursor-pointer" onclick="openPlayerModal(${p.id})">
                    <div class="d-flex align-items-center gap-3 mb-3">
                        <div class="team-badge" style="background-color: #1E293B; border: 1px solid var(--cp-border);">
                            ${initials}
                        </div>
                        <div class="flex-grow-1 overflow-hidden">
                            <h6 class="text-white mb-0 text-truncate" title="${p.name}">${p.name}</h6>
                            <span class="text-secondary small">${p.teamName || ''}</span>
                        </div>
                        <span class="badge ${roleBadgeClass} small" style="font-size: 0.65rem;">${p.role.replace('_', ' ')}</span>
                    </div>

                    <div class="d-flex justify-content-between align-items-center text-secondary small pt-2 border-top border-secondary border-opacity-25">
                        <span>#${p.jerseyNumber || '—'}</span>
                        <span class="text-truncate" style="max-width: 140px;">${p.battingStyle || ''}</span>
                        <span class="text-cyan fw-bold">View Stats <i class="bi bi-chevron-right small"></i></span>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

async function openPlayerModal(playerId) {
    try {
        const stats = await CricPulseAPI.getPlayerStatistics(playerId);

        document.getElementById('modalPlayerName').textContent = stats.name;
        document.getElementById('modalPlayerSubtitle').textContent = `${stats.teamName} • ${stats.role.replace('_', ' ')} (#${stats.jerseyNumber || '—'})`;
        document.getElementById('modalPlayerAvatar').textContent = stats.name.split(' ').map(n => n[0]).join('').substring(0, 2);

        // Career Baseline
        document.getElementById('mCareerMatches').textContent = stats.careerMatches || 0;
        document.getElementById('mCareerRuns').textContent = stats.careerRuns || 0;
        document.getElementById('mCareerAvg').textContent = stats.careerBattingAverage ? stats.careerBattingAverage.toFixed(1) : '0.0';
        document.getElementById('mCareerSr').textContent = stats.careerStrikeRate ? stats.careerStrikeRate.toFixed(1) : '0.0';
        document.getElementById('mCareerWickets').textContent = stats.careerWickets || 0;
        document.getElementById('mCareerBbf').textContent = stats.careerBestBowling || '—';

        // Tournament Live Recorded
        document.getElementById('mTourneyMatches').textContent = stats.tournamentMatches || 0;
        document.getElementById('mTourneyRuns').textContent = stats.tournamentRuns || 0;
        document.getElementById('mTourneyHigh').textContent = stats.tournamentHighestScore || 0;
        document.getElementById('mTourneyBoundaries').textContent = `${stats.tournamentFours || 0} / ${stats.tournamentSixes || 0}`;
        document.getElementById('mTourneyWickets').textContent = stats.tournamentWickets || 0;
        document.getElementById('mTourneyEcon').textContent = stats.tournamentEconomy ? stats.tournamentEconomy.toFixed(2) : '0.00';

        renderPlayerRadarChart(stats);
        playerModal.show();
    } catch (err) {
        console.error('Failed to open player modal:', err);
    }
}

function renderPlayerRadarChart(stats) {
    const ctx = document.getElementById('playerRadarChart').getContext('2d');
    if (radarChartInstance) {
        radarChartInstance.destroy();
    }

    // Normalized scores (0-100) based on role and numbers
    const isBatsman = stats.role === 'BATSMAN' || stats.role === 'WICKET_KEEPER';
    const isBowler = stats.role === 'BOWLER';

    const batPower = isBatsman ? Math.min(95, Math.max(50, (stats.careerStrikeRate || 120) * 0.6)) : 45;
    const boundaryHit = (stats.tournamentFours || 0) * 8 + (stats.tournamentSixes || 0) * 12 + (isBatsman ? 60 : 30);
    const strikeRot = isBatsman ? 80 : 50;
    const bowlControl = isBowler ? 88 : (stats.careerWickets > 10 ? 70 : 30);
    const economyScore = isBowler ? Math.min(95, Math.max(40, 100 - (stats.careerEconomy || 8) * 8)) : 35;
    const matchImpact = Math.min(98, Math.max(50, 60 + (stats.tournamentRuns || 0) * 0.3 + (stats.tournamentWickets || 0) * 8));

    radarChartInstance = new Chart(ctx, {
        type: 'radar',
        data: {
            labels: ['Batting Power', 'Boundary Hitting', 'Strike Rotation', 'Bowling Control', 'Economy', 'Impact Rating'],
            datasets: [{
                label: stats.name,
                data: [Math.min(95, batPower), Math.min(95, boundaryHit), strikeRot, bowlControl, economyScore, Math.min(95, matchImpact)],
                backgroundColor: 'rgba(0, 230, 118, 0.2)',
                borderColor: '#00E676',
                pointBackgroundColor: '#00F5D4',
                pointBorderColor: '#fff',
                pointHoverBackgroundColor: '#fff',
                pointHoverBorderColor: '#00E676'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                r: {
                    angleLines: { color: 'rgba(255, 255, 255, 0.1)' },
                    grid: { color: 'rgba(255, 255, 255, 0.08)' },
                    pointLabels: {
                        color: '#94A3B8',
                        font: { size: 11, family: 'Inter' }
                    },
                    ticks: { display: false, max: 100, min: 0 }
                }
            },
            plugins: {
                legend: { display: false }
            }
        }
    });
}

function renderTournamentLeadersCharts() {
    // 1. Batting Leaders Bar Chart
    const ctxBatting = document.getElementById('battingLeadersChart').getContext('2d');
    if (battingChartInstance) battingChartInstance.destroy();

    battingChartInstance = new Chart(ctxBatting, {
        type: 'bar',
        data: {
            labels: ['V. Kohli (IND)', 'H. Klaasen (SA)', 'J. Buttler (ENG)', 'R. Sharma (IND)', 'T. Head (AUS)'],
            datasets: [{
                label: 'Tournament Runs',
                data: [140, 64, 86, 66, 48],
                backgroundColor: [
                    'rgba(0, 230, 118, 0.7)',
                    'rgba(0, 245, 212, 0.7)',
                    'rgba(245, 158, 11, 0.7)',
                    'rgba(59, 130, 246, 0.7)',
                    'rgba(139, 92, 246, 0.7)'
                ],
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: {
                    grid: { color: 'rgba(255, 255, 255, 0.05)' },
                    ticks: { color: '#94A3B8' }
                },
                x: {
                    grid: { display: false },
                    ticks: { color: '#94A3B8', font: { size: 10 } }
                }
            }
        }
    });

    // 2. Bowling Leaders Doughnut Chart
    const ctxBowling = document.getElementById('bowlingLeadersChart').getContext('2d');
    if (bowlingChartInstance) bowlingChartInstance.destroy();

    bowlingChartInstance = new Chart(ctxBowling, {
        type: 'doughnut',
        data: {
            labels: ['J. Bumrah (3 W)', 'S. Curran (3 W)', 'A. Rashid (3 W)', 'A. Singh (3 W)', 'A. Zampa (2 W)'],
            datasets: [{
                data: [3, 3, 3, 3, 2],
                backgroundColor: [
                    '#00E676',
                    '#00F5D4',
                    '#3B82F6',
                    '#FFB703',
                    '#EF4444'
                ],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                    labels: { color: '#94A3B8', font: { size: 11 } }
                }
            }
        }
    });
}
