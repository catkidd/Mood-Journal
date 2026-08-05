/* ============================================================
   Games Hub JavaScript — MoodJournal
   ============================================================ */

/* ── 1. Tab Switcher ───────────────────────────────────────── */
function openTab(tabName) {
    const tabs = ['snake', 'tictactoe', 'memory', 'breathing'];
    tabs.forEach(function(name) {
        const btn  = document.getElementById(name + '-tab');
        const pane = document.getElementById(name + '-pane');

        if (name === tabName) {
            if (btn) btn.classList.add('active');
            if (pane) {
                pane.style.display = 'block';
                pane.classList.add('active', 'show');
            }
        } else {
            if (btn) btn.classList.remove('active');
            if (pane) {
                pane.style.display = 'none';
                pane.classList.remove('active', 'show');
            }
        }
    });

    if (tabName !== 'breathing' && window.pauseBreathing) {
        window.pauseBreathing();
    }

    if (tabName === 'snake' && window.initSnake) window.initSnake();
    if (tabName === 'tictactoe' && window.tttReset) window.tttReset();
    if (tabName === 'memory' && window.memoryStart) window.memoryStart();
}
window.openTab = openTab;
window.switchGameTab = openTab;

/* ── 2. CSRF & AJAX Play Logger ────────────────────────────── */
function getCsrfHeader() {
    const el = document.querySelector('meta[name="_csrf_header"]');
    return el ? el.getAttribute('content') : 'X-CSRF-TOKEN';
}
function getCsrfToken() {
    const el = document.querySelector('meta[name="_csrf"]');
    return el ? el.getAttribute('content') : '';
}

function recordPlay(gameName) {
    const token = getCsrfToken();
    const header = getCsrfHeader();
    if (!token) return Promise.resolve();

    return fetch('/games/play', {
        method:  'POST',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        },
        body: JSON.stringify({ game: gameName })
    })
    .then(r => r.json())
    .then(data => {
        if (data && data.newBadge) showBadgeToast(data.newBadge);
        const pillId = gameName === 'tictactoe' ? 'tictactoePlaysPill' : gameName + 'PlaysPill';
        const pill = document.getElementById(pillId);
        if (pill) {
            const count = parseInt(pill.textContent) + 1;
            pill.textContent = count + ' play' + (count !== 1 ? 's' : '');
        }
    })
    .catch(err => console.error('Error logging play:', err));
}

function showBadgeToast(name) {
    const inner = document.getElementById('badgeToastInner');
    if (!inner) return;
    document.getElementById('toastBadgeName').textContent = name;
    inner.style.display = 'flex';
    setTimeout(() => { inner.style.display = 'none'; }, 4500);
}

/* ── 3. Snake Game Engine ───────────────────────────────────── */
let CELL = 20, COLS = 39, ROWS = 22;
let canvas, ctx, snake, dir, nextDir, food, score = 0, highScore = 0, running = false, loopTimer = null;

function getHighScore() {
    try { return parseInt(localStorage.getItem('snakeHigh') || '0'); }
    catch (e) { return 0; }
}
function setHighScore(val) {
    try { localStorage.setItem('snakeHigh', val.toString()); }
    catch (e) {}
}

function initSnake() {
    canvas = document.getElementById('snakeCanvas');
    if (!canvas) return;
    ctx = canvas.getContext('2d');
    COLS = Math.floor(canvas.width / CELL);
    ROWS = Math.floor(canvas.height / CELL);
    highScore = getHighScore();
    const scoreEl = document.getElementById('snakeHigh');
    if (scoreEl) scoreEl.textContent = highScore;
    drawIdle();
}
window.initSnake = initSnake;

function drawIdle() {
    if (!ctx) return;
    ctx.fillStyle = '#0f172a';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    ctx.fillStyle = '#818cf8';
    ctx.font = 'bold 26px Inter, sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText('🐍 SNAKE GAME', canvas.width / 2, canvas.height / 2 - 20);

    ctx.fillStyle = '#ffffff';
    ctx.font = '16px Inter, sans-serif';
    ctx.fillText('Click "Start Game" below to play!', canvas.width / 2, canvas.height / 2 + 15);

    ctx.fillStyle = '#94a3b8';
    ctx.font = '13px Inter, sans-serif';
    ctx.fillText('Controls: Arrow Keys, WASD, or D-Pad', canvas.width / 2, canvas.height / 2 + 45);
}

function snakeStart() {
    initSnake();
    if (loopTimer) clearInterval(loopTimer);

    snake   = [{x:15, y:10}, {x:14, y:10}, {x:13, y:10}];
    dir     = {x:1, y:0};
    nextDir = {x:1, y:0};
    score   = 0;
    running = true;

    const scoreEl = document.getElementById('snakeScore');
    if (scoreEl) scoreEl.textContent = '0';
    const banner = document.getElementById('snakeBanner');
    if (banner) banner.classList.remove('show');

    placeFood();
    draw();
    loopTimer = setInterval(gameLoop, 110);

    const btn = document.getElementById('snakeStartBtn');
    if (btn) btn.innerHTML = '<i class="bi bi-arrow-counterclockwise me-1"></i>Restart Game';
}
window.snakeStart = snakeStart;

function snakeChangeDir(x, y) {
    if (!running) return;
    if (x !== 0 && dir.x === 0) nextDir = {x, y};
    if (y !== 0 && dir.y === 0) nextDir = {x, y};
}
window.snakeChangeDir = snakeChangeDir;

function placeFood() {
    let pos;
    do {
        pos = {x: Math.floor(Math.random() * COLS), y: Math.floor(Math.random() * ROWS)};
    } while (snake.some(s => s.x === pos.x && s.y === pos.y));
    food = pos;
}

function gameLoop() {
    if (!running) return;
    dir = nextDir;
    const head = {x: snake[0].x + dir.x, y: snake[0].y + dir.y};

    if (head.x < 0 || head.x >= COLS || head.y < 0 || head.y >= ROWS) {
        endGame();
        return;
    }
    if (snake.some(s => s.x === head.x && s.y === head.y)) {
        endGame();
        return;
    }

    snake.unshift(head);

    if (food && head.x === food.x && head.y === food.y) {
        score++;
        const scoreEl = document.getElementById('snakeScore');
        if (scoreEl) scoreEl.textContent = score;
        placeFood();
    } else {
        snake.pop();
    }

    draw();
}

function draw() {
    if (!ctx) return;
    ctx.fillStyle = '#0f172a';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    ctx.fillStyle = 'rgba(255,255,255,0.05)';
    for (let x = 0; x < COLS; x++) {
        for (let y = 0; y < ROWS; y++) {
            ctx.fillRect(x * CELL + CELL / 2 - 1, y * CELL + CELL / 2 - 1, 2, 2);
        }
    }

    if (food) {
        ctx.fillStyle = '#f093fb';
        ctx.beginPath();
        ctx.arc(food.x * CELL + CELL / 2, food.y * CELL + CELL / 2, CELL / 2 - 2, 0, Math.PI * 2);
        ctx.fill();
    }

    snake.forEach((s, i) => {
        ctx.fillStyle = (i === 0) ? '#a78bfa' : '#6366f1';
        ctx.fillRect(s.x * CELL + 2, s.y * CELL + 2, CELL - 4, CELL - 4);
    });
}

function endGame() {
    if (loopTimer) clearInterval(loopTimer);
    running = false;

    if (score > highScore) {
        highScore = score;
        setHighScore(highScore);
        const highEl = document.getElementById('snakeHigh');
        if (highEl) highEl.textContent = highScore;
    }

    const banner = document.getElementById('snakeBanner');
    if (banner) {
        banner.className = 'game-over-banner game-over-loss show';
        banner.textContent = `💀 Game Over! Score: ${score}`;
    }
    recordPlay('snake');
}

document.addEventListener('keydown', function(e) {
    const keyMap = {
        ArrowUp: {x:0, y:-1}, KeyW: {x:0, y:-1},
        ArrowDown: {x:0, y:1}, KeyS: {x:0, y:1},
        ArrowLeft: {x:-1, y:0}, KeyA: {x:-1, y:0},
        ArrowRight: {x:1, y:0}, KeyD: {x:1, y:0}
    };
    if (keyMap[e.code] && running) {
        e.preventDefault();
        snakeChangeDir(keyMap[e.code].x, keyMap[e.code].y);
    }
});

/* ── 4. Tic-Tac-Toe AI Engine ───────────────────────────────── */
let board = Array(9).fill(''), gameActive = true, isAiThinking = false;
let wins = 0, draws = 0, losses = 0;
const WIN_LINES = [ [0,1,2], [3,4,5], [6,7,8], [0,3,6], [1,4,7], [2,5,8], [0,4,8], [2,4,6] ];

function checkWinner(b) {
    for (const [a, c, d] of WIN_LINES) {
        if (b[a] && b[a] === b[c] && b[a] === b[d]) return b[a];
    }
    return b.includes('') ? null : 'draw';
}

function minimax(b, isMax) {
    const result = checkWinner(b);
    if (result === 'O') return 10;
    if (result === 'X') return -10;
    if (result === 'draw') return 0;

    let best = isMax ? -Infinity : Infinity;
    for (let i = 0; i < 9; i++) {
        if (!b[i]) {
            b[i] = isMax ? 'O' : 'X';
            const val = minimax(b, !isMax);
            b[i] = '';
            best = isMax ? Math.max(best, val) : Math.min(best, val);
        }
    }
    return best;
}

function getBestMove(b) {
    let bestScore = -Infinity, move = -1;
    for (let i = 0; i < 9; i++) {
        if (!b[i]) {
            b[i] = 'O';
            const score = minimax(b, false);
            b[i] = '';
            if (score > bestScore) {
                bestScore = score;
                move = i;
            }
        }
    }
    return move;
}

function tttClick(i) {
    if (!gameActive || isAiThinking || board[i] !== '') return;

    board[i] = 'X';
    renderBoard();

    const result = checkWinner(board);
    if (result) {
        endTTT(result);
        return;
    }

    isAiThinking = true;
    const statusEl = document.getElementById('tttStatus');
    if (statusEl) statusEl.textContent = '🤖 AI is thinking...';

    setTimeout(() => {
        const aiMove = getBestMove(board);
        if (aiMove >= 0) {
            board[aiMove] = 'O';
            renderBoard();
            const aiResult = checkWinner(board);
            if (aiResult) {
                endTTT(aiResult);
            } else if (statusEl) {
                statusEl.textContent = 'Your turn (X)!';
            }
        }
        isAiThinking = false;
    }, 280);
}
window.tttClick = tttClick;

function renderBoard() {
    board.forEach((v, i) => {
        const cell = document.getElementById('ttt' + i);
        if (!cell) return;

        cell.textContent = v;
        if (v === 'X') {
            cell.className = 'ttt-cell taken x-cell';
        } else if (v === 'O') {
            cell.className = 'ttt-cell taken o-cell';
        } else {
            cell.className = 'ttt-cell';
        }
    });
}

function endTTT(result) {
    gameActive = false;
    isAiThinking = false;
    let statusMsg = '';

    if (result === 'X') {
        wins++;
        statusMsg = '🎉 You Win!';
        highlightWinner('X');
        recordPlay('tictactoe');
    } else if (result === 'O') {
        losses++;
        statusMsg = '🤖 AI Wins!';
        highlightWinner('O');
        recordPlay('tictactoe');
    } else {
        draws++;
        statusMsg = '🤝 It\'s a Draw!';
        recordPlay('tictactoe');
    }

    const statusEl = document.getElementById('tttStatus');
    if (statusEl) statusEl.textContent = statusMsg;
    const winsEl = document.getElementById('tttWins');
    if (winsEl) winsEl.textContent = wins;
    const drawsEl = document.getElementById('tttDraws');
    if (drawsEl) drawsEl.textContent = draws;
    const lossesEl = document.getElementById('tttLosses');
    if (lossesEl) lossesEl.textContent = losses;
}

function highlightWinner(mark) {
    for (const [a, b, c] of WIN_LINES) {
        if (board[a] === mark && board[b] === mark && board[c] === mark) {
            [a, b, c].forEach(idx => {
                const cell = document.getElementById('ttt' + idx);
                if (cell) cell.classList.add('winner');
            });
            break;
        }
    }
}

function tttReset() {
    board = Array(9).fill('');
    gameActive = true;
    isAiThinking = false;
    const statusEl = document.getElementById('tttStatus');
    if (statusEl) statusEl.textContent = 'Click any square to start!';
    renderBoard();
}
window.tttReset = tttReset;

/* ── 5. Memory Match Engine ─────────────────────────────────── */
const EMOJIS = ['🌸','🌈','🦋','🎸','🍕','🚀','🐬','💎'];
let cards = [], flipped = [], matchedCount = 0, moves = 0, timer = null, elapsed = 0;

function memoryStart() {
    if (timer) clearInterval(timer);
    moves = 0; matchedCount = 0; elapsed = 0; flipped = [];

    const movesEl   = document.getElementById('memMoves');
    const matchedEl = document.getElementById('memMatched');
    const timeEl    = document.getElementById('memTime');

    if (movesEl)   movesEl.textContent   = '0';
    if (matchedEl) matchedEl.textContent = '0';
    if (timeEl)    timeEl.textContent    = '0s';

    const banner = document.getElementById('memoryBanner');
    if (banner) banner.classList.remove('show');

    cards = [...EMOJIS, ...EMOJIS].sort(() => Math.random() - 0.5);

    const grid = document.getElementById('memoryGrid');
    if (!grid) return;

    grid.innerHTML = '';
    cards.forEach((emoji, i) => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'mem-card';
        btn.dataset.index = i.toString();
        btn.innerHTML = `
            <div class="card-face card-back">❓</div>
            <div class="card-face card-front">${emoji}</div>
        `;
        btn.onclick = () => memFlip(i);
        grid.appendChild(btn);
    });

    timer = setInterval(() => {
        elapsed++;
        const tEl = document.getElementById('memTime');
        if (tEl) tEl.textContent = elapsed + 's';
    }, 1000);
}
window.memoryStart = memoryStart;

function memFlip(i) {
    const card = document.querySelector(`.mem-card[data-index="${i}"]`);
    if (!card || card.classList.contains('flipped') || card.classList.contains('matched') || flipped.length >= 2) {
        return;
    }

    card.classList.add('flipped');
    flipped.push(i);

    if (flipped.length === 2) {
        moves++;
        const movesEl = document.getElementById('memMoves');
        if (movesEl) movesEl.textContent = moves;
        const [a, b] = flipped;

        if (cards[a] === cards[b]) {
            setTimeout(() => {
                [a, b].forEach(idx => {
                    const el = document.querySelector(`.mem-card[data-index="${idx}"]`);
                    if (el) el.classList.add('matched');
                });
                matchedCount++;
                const matchedEl = document.getElementById('memMatched');
                if (matchedEl) matchedEl.textContent = matchedCount;
                flipped = [];

                if (matchedCount === EMOJIS.length) {
                    clearInterval(timer);
                    const banner = document.getElementById('memoryBanner');
                    if (banner) {
                        banner.className = 'game-over-banner game-over-win show';
                        banner.textContent = `🎉 Solved in ${moves} moves and ${elapsed} seconds!`;
                    }
                    recordPlay('memory');
                }
            }, 380);
        } else {
            setTimeout(() => {
                [a, b].forEach(idx => {
                    const el = document.querySelector(`.mem-card[data-index="${idx}"]`);
                    if (el) el.classList.remove('flipped');
                });
                flipped = [];
            }, 850);
        }
    }
}

/* ── 6. Animatic Breathing Exercise Engine ──────────────────── */
let breathMode = '478';
let breathActive = false;
let breathTimerId = null;
let breathCycles = 0;
let breathPhaseIndex = 0;
let secondsRemaining = 0;
let playLoggedForSession = false;

const BREATH_PATTERNS = {
    '478': [
        { name: 'Inhale', duration: 4, class: 'inhale', text: 'Breathe in peace & clarity...', scale: 'scale(1.58)' },
        { name: 'Hold',   duration: 7, class: 'hold',   text: 'Pause... let your mind be still', scale: 'scale(1.58)' },
        { name: 'Exhale', duration: 8, class: 'exhale', text: 'Slowly release all tension & stress...', scale: 'scale(1.0)' }
    ],
    'box': [
        { name: 'Inhale', duration: 4, class: 'inhale', text: 'Inhale deeply & fill your lungs...', scale: 'scale(1.58)' },
        { name: 'Hold',   duration: 4, class: 'hold',   text: 'Hold your breath calmly...', scale: 'scale(1.58)' },
        { name: 'Exhale', duration: 4, class: 'exhale', text: 'Exhale smoothly & completely...', scale: 'scale(1.0)' },
        { name: 'Hold',   duration: 4, class: 'hold',   text: 'Rest quietly in empty breath...', scale: 'scale(1.0)' }
    ],
    '711': [
        { name: 'Inhale', duration: 7, class: 'inhale', text: 'Deeply inhale through your nose...', scale: 'scale(1.58)' },
        { name: 'Exhale', duration: 11, class: 'exhale', text: 'Long, slow exhale releasing panic & stress...', scale: 'scale(1.0)' }
    ],
    '424': [
        { name: 'Inhale', duration: 4, class: 'inhale', text: 'Breathe in fresh energy & focus...', scale: 'scale(1.58)' },
        { name: 'Hold',   duration: 2, class: 'hold',   text: 'Brief pause for balance...', scale: 'scale(1.58)' },
        { name: 'Exhale', duration: 4, class: 'exhale', text: 'Smooth, steady exhale...', scale: 'scale(1.0)' }
    ]
};

function setBreathMode(mode) {
    if (breathActive) resetBreathing();
    breathMode = mode;
    
    ['478', 'box', '711', '424'].forEach(m => {
        const btn = document.getElementById('btnMode' + (m === 'box' ? 'Box' : m));
        if (btn) {
            if (m === mode) btn.classList.add('active');
            else btn.classList.remove('active');
        }
    });
}
window.setBreathMode = setBreathMode;

function toggleBreathing() {
    if (breathActive) {
        pauseBreathing();
    } else {
        startBreathing();
    }
}
window.toggleBreathing = toggleBreathing;

function startBreathing() {
    breathActive = true;
    const btn = document.getElementById('breathToggleBtn');
    if (btn) btn.innerHTML = '<i class="bi bi-pause-fill me-1"></i>Pause Exercise';
    
    const container = document.getElementById('breathContainer');
    if (container) container.classList.add('active');

    runBreathPhase();
    if (breathTimerId) clearInterval(breathTimerId);
    breathTimerId = setInterval(tickBreathing, 1000);
}
window.startBreathing = startBreathing;

function pauseBreathing() {
    breathActive = false;
    if (breathTimerId) clearInterval(breathTimerId);
    
    const btn = document.getElementById('breathToggleBtn');
    if (btn) btn.innerHTML = '<i class="bi bi-play-fill me-1"></i>Resume Exercise';
    
    const container = document.getElementById('breathContainer');
    if (container) container.classList.remove('active');

    const circle = document.getElementById('breathCircle');
    if (circle) {
        circle.style.transition = 'all 1s ease';
        circle.style.transform = 'scale(1.0)';
        circle.className = 'breath-circle';
    }
}
window.pauseBreathing = pauseBreathing;

function resetBreathing() {
    pauseBreathing();
    breathPhaseIndex = 0;
    breathCycles = 0;
    secondsRemaining = 0;
    playLoggedForSession = false;

    const cyclesEl = document.getElementById('breathCycles');
    if (cyclesEl) cyclesEl.textContent = '0';

    const phaseEl = document.getElementById('breathPhase');
    if (phaseEl) phaseEl.textContent = 'Ready';

    const timerEl = document.getElementById('breathTimer');
    if (timerEl) timerEl.textContent = '0s';

    const subtextEl = document.getElementById('breathSubtext');
    if (subtextEl) subtextEl.textContent = 'Press "Start Breathing" to begin your session';

    const btn = document.getElementById('breathToggleBtn');
    if (btn) btn.innerHTML = '<i class="bi bi-play-fill me-1"></i>Start Breathing';
}
window.resetBreathing = resetBreathing;

function runBreathPhase() {
    const pattern = BREATH_PATTERNS[breathMode];
    const currentPhase = pattern[breathPhaseIndex];
    
    secondsRemaining = currentPhase.duration;
    
    const phaseEl = document.getElementById('breathPhase');
    if (phaseEl) phaseEl.textContent = currentPhase.name;
    
    const timerEl = document.getElementById('breathTimer');
    if (timerEl) timerEl.textContent = secondsRemaining + 's';
    
    const subtextEl = document.getElementById('breathSubtext');
    if (subtextEl) subtextEl.textContent = currentPhase.text;

    const circle = document.getElementById('breathCircle');
    if (circle) {
        circle.style.transition = `transform ${currentPhase.duration}s cubic-bezier(0.4, 0, 0.2, 1), background 1.2s ease, box-shadow 1.2s ease`;
        circle.style.transform  = currentPhase.scale;
        circle.className        = 'breath-circle ' + currentPhase.class;
    }
}

function tickBreathing() {
    if (!breathActive) return;
    
    secondsRemaining--;
    const timerEl = document.getElementById('breathTimer');
    if (timerEl) timerEl.textContent = secondsRemaining + 's';
    
    if (secondsRemaining <= 0) {
        const pattern = BREATH_PATTERNS[breathMode];
        breathPhaseIndex = (breathPhaseIndex + 1) % pattern.length;
        
        if (breathPhaseIndex === 0) {
            breathCycles++;
            const cyclesEl = document.getElementById('breathCycles');
            if (cyclesEl) cyclesEl.textContent = breathCycles.toString();
            
            if (breathCycles >= 2 && !playLoggedForSession) {
                playLoggedForSession = true;
                recordPlay('breathing');
            }
        }
        
        runBreathPhase();
    }
}

/* ── 7. Boot Sequence ───────────────────────────────────────── */
function boot() {
    initSnake();
    tttReset();
    memoryStart();

    const dataEl = document.getElementById('gameHighlightData');
    const highlight = (dataEl && dataEl.dataset) ? dataEl.dataset.highlight : '';
    if (highlight) {
        openTab(highlight.toLowerCase());
    } else {
        openTab('snake');
    }
}

if (document.readyState !== 'loading') {
    boot();
} else {
    document.addEventListener('DOMContentLoaded', boot);
}
