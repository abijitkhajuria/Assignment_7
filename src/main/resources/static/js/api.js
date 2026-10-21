/**
 * CricPulse — Real-Time Cricket Score Management System
 * Centralized REST API Client & Server-Sent Events (SSE) Live Stream Manager
 */

const CricPulseAPI = (function () {
    const BASE_URL = window.location.origin;

    async function request(endpoint, options = {}) {
        const url = `${BASE_URL}${endpoint}`;
        const defaultHeaders = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };

        // Attach token if logged in
        const token = localStorage.getItem('cricpulse_admin_token');
        if (token) {
            defaultHeaders['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            ...options,
            headers: {
                ...defaultHeaders,
                ...(options.headers || {})
            }
        };

        try {
            const response = await fetch(url, config);
            if (!response.ok) {
                let errData;
                try {
                    errData = await response.json();
                } catch (e) {
                    errData = { message: `HTTP Error ${response.status}: ${response.statusText}` };
                }
                const err = new Error(errData.message || 'API request failed');
                err.status = response.status;
                err.data = errData;
                throw err;
            }
            if (response.status === 204) return null;
            return await response.json();
        } catch (error) {
            console.error(`API Error [${endpoint}]:`, error);
            throw error;
        }
    }

    return {
        // Matches
        getLiveMatches: () => request('/api/matches/live'),
        getTodayMatches: () => request('/api/matches/today'),
        getTomorrowMatches: () => request('/api/matches/tomorrow'),
        getUpcomingMatches: () => request('/api/matches/upcoming'),
        getCompletedMatches: (format) => request(`/api/matches/completed${format ? `?format=${format}` : ''}`),
        getAllMatches: (format) => request(`/api/matches${format ? `?format=${format}` : ''}`),
        getMatchSummary: (id) => request(`/api/matches/${id}/summary`),
        getScorecard: (id) => request(`/api/matches/${id}/scorecard`),
        getLiveSnapshot: (id) => request(`/api/matches/${id}/live-snapshot`),
        getMatchInnings: (id) => request(`/api/matches/${id}/innings`),

        // Teams & Players
        getTeams: () => request('/api/teams'),
        getTeamById: (id) => request(`/api/teams/${id}`),
        getPlayers: (teamId) => request(`/api/players${teamId ? `?teamId=${teamId}` : ''}`),
        getPlayerById: (id) => request(`/api/players/${id}`),
        getPlayerStatistics: (id) => request(`/api/players/${id}/statistics`),

        // Scoring Controls
        getInningsDeliveries: (inningsId) => request(`/api/innings/${inningsId}/deliveries`),
        recordDelivery: (inningsId, payload) => request(`/api/innings/${inningsId}/deliveries`, {
            method: 'POST',
            body: JSON.stringify(payload)
        }),
        undoLastDelivery: (inningsId) => request(`/api/innings/${inningsId}/undo-last-delivery`, {
            method: 'POST'
        }),
        completeInnings: (inningsId) => request(`/api/innings/${inningsId}/complete`, {
            method: 'POST'
        }),

        // Admin Management
        adminLogin: (username, password) => request('/api/admin/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        }),
        reseedDatabase: () => request('/api/admin/reseed', {
            method: 'POST'
        }),
        createMatch: (payload) => request('/api/admin/matches', {
            method: 'POST',
            body: JSON.stringify(payload)
        }),
        startMatch: (id) => request(`/api/admin/matches/${id}/start`, {
            method: 'POST'
        }),
        finishMatch: (id, margin, winnerTeamId) => {
            let q = [];
            if (margin) q.push(`resultMargin=${encodeURIComponent(margin)}`);
            if (winnerTeamId) q.push(`winnerTeamId=${winnerTeamId}`);
            const queryStr = q.length ? `?${q.join('&')}` : '';
            return request(`/api/admin/matches/${id}/finish${queryStr}`, { method: 'POST' });
        },
        deleteMatch: (id) => request(`/api/admin/matches/${id}`, {
            method: 'DELETE'
        }),

        // Real-Time SSE Stream with Fallback Polling
        subscribeLiveStream: (matchId, callbacks = {}) => {
            const { onUpdate, onError, onConnectionChange } = callbacks;
            let eventSource = null;
            let pollingTimer = null;
            let isConnected = false;

            function startPolling() {
                if (pollingTimer) return;
                console.warn(`[LiveStream] Switching to 3s fallback polling for match ${matchId}...`);
                if (onConnectionChange) onConnectionChange('POLLING');

                // Poll immediately, then every 3 seconds
                const poll = async () => {
                    try {
                        const snapshot = await CricPulseAPI.getLiveSnapshot(matchId);
                        if (onUpdate) onUpdate(snapshot);
                    } catch (e) {
                        if (onError) onError(e);
                    }
                };
                poll();
                pollingTimer = setInterval(poll, 3000);
            }

            function stopPolling() {
                if (pollingTimer) {
                    clearInterval(pollingTimer);
                    pollingTimer = null;
                }
            }

            function initSSE() {
                if (typeof EventSource === 'undefined') {
                    startPolling();
                    return;
                }

                try {
                    const sseUrl = `${BASE_URL}/api/matches/${matchId}/live-stream`;
                    eventSource = new EventSource(sseUrl);

                    eventSource.addEventListener('INIT', (e) => {
                        isConnected = true;
                        stopPolling();
                        if (onConnectionChange) onConnectionChange('CONNECTED');
                    });

                    eventSource.addEventListener('SCORE_UPDATE', (e) => {
                        isConnected = true;
                        stopPolling();
                        if (onConnectionChange) onConnectionChange('CONNECTED');
                        try {
                            const data = JSON.parse(e.data);
                            if (onUpdate) onUpdate(data);
                        } catch (err) {
                            console.error('Error parsing SSE data:', err);
                        }
                    });

                    eventSource.onerror = (err) => {
                        console.warn('[LiveStream] SSE connection error, engaging fallback polling.');
                        if (eventSource) {
                            eventSource.close();
                            eventSource = null;
                        }
                        isConnected = false;
                        startPolling();
                    };
                } catch (err) {
                    startPolling();
                }
            }

            initSSE();

            // Return unsubscribe controller
            return {
                close: () => {
                    if (eventSource) {
                        eventSource.close();
                        eventSource = null;
                    }
                    stopPolling();
                    if (onConnectionChange) onConnectionChange('DISCONNECTED');
                }
            };
        },

        // UI Toast Notification helper
        showToast: (message, type = 'info') => {
            const container = document.getElementById('cp-toast-container') || createToastContainer();
            const toast = document.createElement('div');
            toast.className = `alert alert-${type === 'error' ? 'danger' : type} alert-dismissible fade show shadow-lg mb-2`;
            toast.style.minWidth = '280px';
            toast.innerHTML = `
                <div class="d-flex align-items-center gap-2">
                    <i class="bi bi-${type === 'error' ? 'exclamation-triangle-fill text-danger' : (type === 'success' ? 'check-circle-fill text-success' : 'info-circle-fill')}"></i>
                    <div>${message}</div>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            `;
            container.appendChild(toast);
            setTimeout(() => {
                toast.classList.remove('show');
                setTimeout(() => toast.remove(), 250);
            }, 4000);
        }
    };

    function createToastContainer() {
        const div = document.createElement('div');
        div.id = 'cp-toast-container';
        div.style.position = 'fixed';
        div.style.top = '80px';
        div.style.right = '20px';
        div.style.zIndex = '9999';
        document.body.appendChild(div);
        return div;
    }
})();
