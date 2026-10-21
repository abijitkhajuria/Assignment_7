/**
 * CricPulse — Match Results Archive Logic
 */

document.addEventListener('DOMContentLoaded', () => {
    loadResults();

    document.getElementById('resultsFormatSelect').addEventListener('change', (e) => {
        loadResults(e.target.value);
    });
});

async function loadResults(format = '') {
    const container = document.getElementById('resultsListContainer');
    try {
        const matches = await CricPulseAPI.getCompletedMatches(format);
        if (!matches || matches.length === 0) {
            container.innerHTML = `
                <div class="col-12">
                    <div class="cp-card text-center py-5">
                        <i class="bi bi-calendar-check text-secondary fs-1 mb-2"></i>
                        <h5 class="text-white">No completed matches found</h5>
                        <p class="text-secondary small">Completed matches and historical scorecards will be archived here.</p>
                    </div>
                </div>
            `;
            return;
        }

        container.innerHTML = matches.map(m => {
            const dateStr = new Date(m.matchDate).toLocaleDateString('en-US', {
                month: 'short', day: 'numeric', year: 'numeric'
            });

            const score1 = m.innings1Runs !== null ? `${m.innings1Runs}/${m.innings1Wickets} (${m.innings1Overs} ov)` : '—';
            const score2 = m.innings2Runs !== null ? `${m.innings2Runs}/${m.innings2Wickets} (${m.innings2Overs} ov)` : '—';

            return `
                <div class="col-lg-6">
                    <div class="cp-card h-100 d-flex flex-column">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <div class="d-flex align-items-center gap-2">
                                <span class="badge-format">${m.format}</span>
                                <span class="badge-completed">COMPLETED</span>
                            </div>
                            <span class="text-secondary small"><i class="bi bi-calendar3 me-1"></i>${dateStr}</span>
                        </div>

                        <h5 class="text-white mb-1">${m.title}</h5>
                        <p class="text-secondary small mb-3"><i class="bi bi-geo-alt me-1"></i>${m.venue}</p>

                        <!-- Teams & Scores -->
                        <div class="p-3 rounded bg-dark border border-secondary border-opacity-50 mb-3">
                            <!-- Team 1 -->
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <div class="d-flex align-items-center gap-2">
                                    <div class="team-badge" style="width: 32px; height: 32px; font-size: 0.8rem; background-color: ${m.team1Color || '#0077B6'};">
                                        ${m.team1ShortName}
                                    </div>
                                    <span class="text-white fw-semibold">${m.team1Name}</span>
                                </div>
                                <span class="h6 brand-font text-white mb-0">${m.innings1TeamName === m.team1ShortName ? score1 : (m.innings2TeamName === m.team1ShortName ? score2 : '—')}</span>
                            </div>

                            <!-- Team 2 -->
                            <div class="d-flex justify-content-between align-items-center">
                                <div class="d-flex align-items-center gap-2">
                                    <div class="team-badge" style="width: 32px; height: 32px; font-size: 0.8rem; background-color: ${m.team2Color || '#E5A93C'};">
                                        ${m.team2ShortName}
                                    </div>
                                    <span class="text-white fw-semibold">${m.team2Name}</span>
                                </div>
                                <span class="h6 brand-font text-white mb-0">${m.innings2TeamName === m.team2ShortName ? score2 : (m.innings1TeamName === m.team2ShortName ? score1 : '—')}</span>
                            </div>
                        </div>

                        <!-- Result Margin -->
                        <div class="mb-3 text-warning fw-bold small">
                            <i class="bi bi-trophy-fill me-1 text-gold"></i>${m.resultMargin || 'Match Finished'}
                        </div>

                        <div class="pt-2 border-top border-secondary border-opacity-25 d-flex justify-content-between align-items-center mt-auto">
                            <span class="text-secondary small">Official Scorecard Archived</span>
                            <a href="/scorecard.html?id=${m.id}" class="btn btn-sm btn-cp-outline">
                                <i class="bi bi-clipboard2-data me-1"></i> View Full Scorecard
                            </a>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    } catch (err) {
        console.error('Failed to load results:', err);
    }
}
