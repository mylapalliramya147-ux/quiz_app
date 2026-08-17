import state from '../state.js';

export function renderNumQuestions(container) {
  container.innerHTML = `
    <div class="page">
      <h1 class="page-title">How Many Questions?</h1>
      <p class="page-subtitle">Choose the length of your quiz</p>
      <div class="card-grid-3">
        ${state.questionCounts.map(q => `
          <div class="select-card${state.numQuestions === q.count ? ' selected' : ''}" data-count="${q.count}" tabindex="0" role="radio" aria-checked="${state.numQuestions === q.count}" aria-label="${q.count} questions">
            <div class="select-card-icon">${q.icon}</div>
            <div class="select-card-label">${q.count} Questions</div>
            <div style="font-size:0.8rem;color:var(--color-text-secondary);margin-top:4px;">${q.desc}</div>
          </div>
        `).join('')}
      </div>
      <div class="btn-group">
        <button id="btn-back" class="btn btn-ghost">Back</button>
        <button id="btn-start" class="btn btn-success" disabled>Start Quiz</button>
      </div>
    </div>
  `;

  const btnStart = container.querySelector('#btn-start');
  const btnBack = container.querySelector('#btn-back');
  const cards = container.querySelectorAll('.select-card');

  function syncSelection() {
    cards.forEach(card => {
      const selected = parseInt(card.dataset.count) === state.numQuestions;
      card.classList.toggle('selected', selected);
      card.setAttribute('aria-checked', selected);
    });
    btnStart.disabled = !state.numQuestions;
  }

  syncSelection();

  cards.forEach(card => {
    card.addEventListener('click', () => {
      state.numQuestions = parseInt(card.dataset.count);
      syncSelection();
    });
    card.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        card.click();
      }
    });
  });

  btnStart.addEventListener('click', () => {
    if (state.numQuestions) {
      window.location.hash = '#quiz';
    }
  });

  btnBack.addEventListener('click', () => {
    window.location.hash = '#difficulty';
  });
}
