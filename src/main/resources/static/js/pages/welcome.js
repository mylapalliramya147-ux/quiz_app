import state from '../state.js';

export function renderWelcome(container) {
  container.innerHTML = `
    <div class="page">
      <div class="text-center" style="margin-bottom: 32px;">
        <div style="font-size: 3rem; margin-bottom: 8px;">&#x1F9E0;</div>
        <h1 class="page-title">Welcome to Quiz</h1>
        <p class="page-subtitle">Test your knowledge across various topics</p>
      </div>
      <div class="input-group">
        <label class="input-label" for="player-name">Your Name</label>
        <input
          type="text"
          id="player-name"
          class="input-field"
          placeholder="Enter your name"
          maxlength="40"
          autocomplete="name"
          autofocus
        />
      </div>
      <div class="btn-group">
        <button id="btn-continue" class="btn btn-primary" disabled>
          Continue
        </button>
      </div>
    </div>
  `;

  const input = container.querySelector('#player-name');
  const btn = container.querySelector('#btn-continue');

  input.value = state.playerName;
  btn.disabled = !state.playerName.trim();

  input.addEventListener('input', () => {
    btn.disabled = !input.value.trim();
  });

  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && input.value.trim()) {
      state.playerName = input.value.trim();
      window.location.hash = '#topic';
    }
  });

  btn.addEventListener('click', () => {
    state.playerName = input.value.trim();
    window.location.hash = '#topic';
  });
}
