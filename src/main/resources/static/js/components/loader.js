export function renderLoader(text = 'Loading...') {
  return `
    <div class="loader">
      <div class="spinner"></div>
      <div class="loader-text">${text}</div>
    </div>
  `;
}
