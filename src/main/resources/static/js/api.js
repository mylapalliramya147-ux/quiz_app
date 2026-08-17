const BASE_URL = window.location.origin;

async function request(method, path, body) {
  const options = {
    method,
    headers: { 'Content-Type': 'application/json' },
  };
  if (body !== undefined) {
    options.body = JSON.stringify(body);
  }
  const res = await fetch(`${BASE_URL}${path}`, options);
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`Request failed (${res.status}): ${text || res.statusText}`);
  }
  return res.json();
}

export function saveConfig(topic, difficulty, numQuestions) {
  return request('POST', '/api/quiz/config', { topic, difficulty, numQuestions });
}

export function createSession() {
  return request('POST', '/api/quiz/sessions');
}

export function submitAnswer(sessionId, questionId, selectedAnswer) {
  return request('POST', `/api/quiz/sessions/${sessionId}/answers`, { questionId, selectedAnswer });
}

export function finishSession(sessionId) {
  return request('POST', `/api/quiz/sessions/${sessionId}/finish`);
}
