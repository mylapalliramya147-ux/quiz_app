import { renderWelcome } from './pages/welcome.js';
import { renderTopic } from './pages/topic.js';
import { renderDifficulty } from './pages/difficulty.js';
import { renderNumQuestions } from './pages/numQuestions.js';
import { renderQuiz } from './pages/quiz.js';
import { renderResult } from './pages/result.js';

const routes = {
  '': renderWelcome,
  'welcome': renderWelcome,
  'topic': renderTopic,
  'difficulty': renderDifficulty,
  'num-questions': renderNumQuestions,
  'quiz': renderQuiz,
  'result': renderResult,
};

const PAGES_WITH_HEADER = new Set(['quiz', 'result']);

function getRoute() {
  return (window.location.hash || '#').slice(1).split('/')[0];
}

function navigate() {
  const route = getRoute();
  const render = routes[route] || routes[''];
  const container = document.getElementById('page-container');
  const header = document.getElementById('app-header');
  const progress = document.getElementById('header-progress');

  if (PAGES_WITH_HEADER.has(route)) {
    header.classList.remove('hidden');
    progress.classList.remove('hidden');
    container.classList.add('has-header');
  } else {
    header.classList.add('hidden');
    progress.classList.add('hidden');
    container.classList.remove('has-header');
  }

  render(container);
}

window.addEventListener('hashchange', navigate);
window.addEventListener('DOMContentLoaded', navigate);
