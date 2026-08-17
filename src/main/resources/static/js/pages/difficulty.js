import state from '../state.js';

export function renderDifficulty(container) {
  container.innerHTML = `
    <div class="page">
      <h1 class="page-title">Select Difficulty</h1>
      <p class="page-subtitle">How challenging should the quiz be?</p>
      <div class="card-grid-3">
        ${state.difficulties.map(d => `
          <div class="select-card${state.difficulty === d.id ? ' selected' : ''}" data-difficulty="${d.id}" tabindex="0" role="radio" aria-checked="${state.difficulty === d.id}" aria-label="${d.name}">
            <div class="select-card-icon">${d.icon}</div>
            <div class="select-card-label">${d.name}</div>
            <div style="font-size:0.8rem;color:var(--color-text-secondary);margin-top:4px;">${d.desc}</div>
          </div>
        `).join('')}
      </div>
      <div class="btn-group">
        <button id="btn-back" class="btn btn-ghost">Back</button>
        <button id="btn-continue" class="btn btn-primary" disabled>Continue</button>
      </div>
    </div>
  `;

  const btnContinue = container.querySelector('#btn-continue');
  const btnBack = container.querySelector('#btn-back');
  const cards = container.querySelectorAll('.select-card');

  function syncSelection() {
    cards.forEach(card => {
      const selected = card.dataset.difficulty === state.difficulty;
      card.classList.toggle('selected', selected);
      card.setAttribute('aria-checked', selected);
    });
    btnContinue.disabled = !state.difficulty;
  }

  syncSelection();

  cards.forEach(card => {
    card.addEventListener('click', () => {
      state.difficulty = card.dataset.difficulty;
      syncSelection();
    });
    card.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        card.click();
      }
    });
  });

  btnContinue.addEventListener('click', () => {
    if (state.difficulty) {
      window.location.hash = '#num-questions';
    }
  });

  btnBack.addEventListener('click', () => {
    window.location.hash = '#topic';
  });
}
