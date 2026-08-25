import state from '../state.js';

export function renderTopic(container) {
  const welcomeText = state.playerName
    ? `Welcome, ${escapeHtml(state.playerName)}!`
    : 'Choose a Topic';

  container.innerHTML = `
    <div class="page">
      <h1 class="page-title">${welcomeText}</h1>
      <p class="page-subtitle">Tap a card to select a topic</p>
      <div class="card-grid">
        ${state.topics.map(t => `
          <div class="flip-card" data-topic-id="${t.id}" tabindex="0" role="button" aria-label="${t.name}">
            <div class="flip-card-inner">
              <div class="flip-card-front">
                <div class="flip-card-icon">${t.icon}</div>
                <div class="flip-card-label">${t.name}</div>
              </div>
              <div class="flip-card-back">
                <div class="flip-card-label">${t.name}</div>
                <div class="flip-card-desc">${t.desc}</div>
              </div>
            </div>
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
  const cards = container.querySelectorAll('.flip-card');

  function syncSelection() {
    cards.forEach(card => {
      const id = card.dataset.topicId;
      card.classList.toggle('flipped', id === state.topic);
    });
    btnContinue.disabled = !state.topic;
  }

  syncSelection();

  cards.forEach(card => {
    const id = card.dataset.topicId;

    card.addEventListener('click', () => {
      state.topic = state.topic === id ? null : id;
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
    if (state.topic) {
      window.location.hash = '#difficulty';
    }
  });

  btnBack.addEventListener('click', () => {
    window.location.hash = '#welcome';
  });
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}
