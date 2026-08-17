import state from '../state.js';

export function renderResult(container) {
  if (!state.result) {
    window.location.hash = '#welcome';
    return;
  }

  const r = state.result;
  const pct = Math.round(r.percentage);
  const circumference = 2 * Math.PI * 60;
  const offset = circumference - (pct / 100) * circumference;

  let message = 'Keep practicing!';
  if (pct >= 90) message = 'Outstanding! You\'re a master!';
  else if (pct >= 70) message = 'Great job! Well done!';
  else if (pct >= 50) message = 'Not bad! Room to improve.';

  container.innerHTML = `
    <div class="page">
      <div class="result-card">
        <div class="result-score-ring">
          <svg viewBox="0 0 140 140">
            <circle class="ring-bg" cx="70" cy="70" r="60"></circle>
            <circle class="ring-fill" cx="70" cy="70" r="60" style="stroke-dasharray:${circumference};stroke-dashoffset:${circumference};"></circle>
          </svg>
          <div class="result-score-text">
            <div class="result-score-percent" id="score-display">0%</div>
            <div class="result-score-label">Score</div>
          </div>
        </div>
        <div class="result-player-name">${escapeHtml(state.playerName)}</div>
        <div class="result-message">${message}</div>
        <div class="result-stats">
          <div class="result-stat correct">
            <div class="result-stat-value">${r.correctAnswers}</div>
            <div class="result-stat-label">Correct</div>
          </div>
          <div class="result-stat wrong">
            <div class="result-stat-value">${r.wrongAnswers}</div>
            <div class="result-stat-label">Wrong</div>
          </div>
          <div class="result-stat">
            <div class="result-stat-value">${r.answeredQuestions}/${r.totalQuestions}</div>
            <div class="result-stat-label">Answered</div>
          </div>
        </div>
      </div>
      <div class="btn-group">
        <button id="btn-play-again" class="btn btn-primary">Play Again</button>
        <button id="btn-home" class="btn btn-ghost">Home</button>
      </div>
    </div>
  `;

  requestAnimationFrame(() => {
    setTimeout(() => {
      const ring = container.querySelector('.ring-fill');
      if (ring) ring.style.strokeDashoffset = offset;
    }, 100);
  });

  const scoreEl = container.querySelector('#score-display');
  animateCounter(scoreEl, 0, pct, 1000);

  container.querySelector('#btn-play-again').addEventListener('click', () => {
    const savedName = state.playerName;
    const savedTopic = state.topic;
    state.reset();
    state.playerName = savedName;
    state.topic = savedTopic;
    window.location.hash = '#difficulty';
  });

  container.querySelector('#btn-home').addEventListener('click', () => {
    state.reset();
    window.location.hash = '#welcome';
  });
}

function animateCounter(el, from, to, duration) {
  const start = performance.now();
  function tick(now) {
    const elapsed = now - start;
    const progress = Math.min(elapsed / duration, 1);
    const eased = 1 - Math.pow(1 - progress, 3);
    const current = Math.round(from + (to - from) * eased);
    el.textContent = `${current}%`;
    if (progress < 1) requestAnimationFrame(tick);
  }
  requestAnimationFrame(tick);
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}
