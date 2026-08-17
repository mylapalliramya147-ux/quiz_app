import state from '../state.js';
import { saveConfig, createSession, submitAnswer, finishSession } from '../api.js';
import { renderLoader } from '../components/loader.js';
import { showToast } from '../components/toast.js';

const LETTERS = ['A', 'B', 'C', 'D'];

export function renderQuiz(container) {
  if (!state.topic || !state.difficulty || !state.numQuestions) {
    window.location.hash = '#welcome';
    return;
  }

  container.innerHTML = renderLoader('Generating your quiz questions...');

  initQuiz().catch(err => {
    showToast('Failed to start quiz. Please try again.');
    window.location.hash = '#welcome';
  });
}

async function initQuiz() {
  await saveConfig(state.topic, state.difficulty, state.numQuestions);
  const session = await createSession();
  state.sessionId = session.sessionId;
  state.questions = session.questions;
  state.currentQuestion = 0;
  state.answers = {};
  renderCurrentQuestion();
}

function renderCurrentQuestion() {
  const container = document.getElementById('page-container');
  const q = state.questions[state.currentQuestion];
  const total = state.questions.length;
  const current = state.currentQuestion + 1;
  const isLast = current === total;
  const progress = (current / total) * 100;

  const progressBar = document.getElementById('progress-bar');
  if (progressBar) {
    progressBar.style.width = `${progress}%`;
  }

  const selectedAnswer = state.answers[q.id] || null;

  container.innerHTML = `
    <div class="page">
      <div class="question-card">
        <div class="question-number">Question ${current} of ${total}</div>
        <div class="question-text">${escapeHtml(q.question)}</div>
        <div class="answer-options">
          ${q.options.map((opt, i) => `
            <button
              class="answer-option${selectedAnswer === opt ? ' selected' : ''}"
              data-answer="${escapeAttr(opt)}"
              data-index="${i}"
              aria-pressed="${selectedAnswer === opt}"
            >
              <span class="answer-option-letter">${LETTERS[i]}</span>
              <span>${escapeHtml(opt)}</span>
            </button>
          `).join('')}
        </div>
      </div>
      <div class="btn-group">
        ${state.currentQuestion > 0 ? '<button id="btn-prev" class="btn btn-ghost">Previous</button>' : ''}
        <button id="btn-next" class="btn btn-primary" ${!selectedAnswer ? 'disabled' : ''}>
          ${isLast ? 'Finish Quiz' : 'Next'}
        </button>
      </div>
    </div>
  `;

  bindQuestionEvents();
}

function bindQuestionEvents() {
  const container = document.getElementById('page-container');
  const options = container.querySelectorAll('.answer-option');
  const btnNext = container.querySelector('#btn-next');
  const btnPrev = container.querySelector('#btn-prev');
  const q = state.questions[state.currentQuestion];

  options.forEach(opt => {
    opt.addEventListener('click', () => {
      const answer = opt.dataset.answer;
      state.answers[q.id] = answer;
      renderCurrentQuestion();
    });
  });

  if (btnPrev) {
    btnPrev.addEventListener('click', () => {
      if (state.currentQuestion > 0) {
        state.currentQuestion--;
        renderCurrentQuestion();
      }
    });
  }

  btnNext.addEventListener('click', async () => {
    if (!state.answers[q.id]) return;

    if (!isLastQuestion()) {
      try {
        await submitAnswer(state.sessionId, q.id, state.answers[q.id]);
      } catch (err) {
        showToast('Failed to submit answer. Please try again.');
        return;
      }
      state.currentQuestion++;
      renderCurrentQuestion();
    } else {
      submitAllAndFinish();
    }
  });
}

async function submitAllAndFinish() {
  const container = document.getElementById('page-container');
  container.innerHTML = renderLoader('Submitting your answers...');

  try {
    for (const q of state.questions) {
      if (state.answers[q.id]) {
        await submitAnswer(state.sessionId, q.id, state.answers[q.id]);
      }
    }
    state.result = await finishSession(state.sessionId);
    window.location.hash = '#result';
  } catch (err) {
    showToast('Failed to finish quiz. Please try again.');
    renderCurrentQuestion();
  }
}

function isLastQuestion() {
  return state.currentQuestion === state.questions.length - 1;
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

function escapeAttr(str) {
  return str.replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/'/g, '&#39;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}
