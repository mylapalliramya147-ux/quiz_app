const state = {
  playerName: '',
  topic: null,
  difficulty: null,
  numQuestions: null,
  sessionId: null,
  questions: [],
  currentQuestion: 0,
  answers: {},
  result: null,
  topics: [
    { id: 'javascript', name: 'JavaScript', icon: '⚡', desc: 'Test your JS knowledge from closures to async/await.' },
    { id: 'python', name: 'Python', icon: '🐍', desc: 'From data structures to decorators and beyond.' },
    { id: 'java', name: 'Java', icon: '☕', desc: 'OOP, streams, concurrency, and the JVM.' },
    { id: 'web', name: 'Web Dev', icon: '🌐', desc: 'HTML, CSS, HTTP, and browser fundamentals.' },
    { id: 'database', name: 'Databases', icon: '🗄️', desc: 'SQL, NoSQL, indexing, and data modeling.' },
    { id: 'devops', name: 'DevOps', icon: '🚀', desc: 'CI/CD, containers, cloud, and infrastructure.' },
    { id: 'algorithms', name: 'Algorithms', icon: '🧮', desc: 'Sorting, searching, graph theory, and complexity.' },
    { id: 'security', name: 'Security', icon: '🔒', desc: 'Cryptography, auth, OWASP, and secure coding.' },
  ],
  difficulties: [
    { id: 'EASY', name: 'Easy', icon: '🌱', color: '#00B894', desc: 'Great for beginners' },
    { id: 'MEDIUM', name: 'Medium', icon: '🔥', color: '#FDCB6E', desc: 'A balanced challenge' },
    { id: 'HARD', name: 'Hard', icon: '💀', color: '#E17055', desc: 'Only for the brave' },
  ],
  questionCounts: [
    { count: 5, icon: '⚡', desc: 'Quick round' },
    { count: 10, icon: '🎯', desc: 'Standard quiz' },
    { count: 15, icon: '🏆', desc: 'Extended challenge' },
  ],
  reset() {
    this.playerName = '';
    this.topic = null;
    this.difficulty = null;
    this.numQuestions = null;
    this.sessionId = null;
    this.questions = [];
    this.currentQuestion = 0;
    this.answers = {};
    this.result = null;
  }
};

export default state;
