const dashboard = document.getElementById('dashboard');
const historyPanel = document.getElementById('history-panel');
const testPanel = document.getElementById('test-panel');
const resultsPanel = document.getElementById('results-panel');
const questionCard = document.getElementById('question-card');
const testTitle = document.getElementById('test-title');
const testSubject = document.getElementById('test-subject');
const backButton = document.getElementById('back-to-dashboard');
const testBackButton = document.getElementById('test-back-button');
const prevButton = document.getElementById('prev-button');
const nextButton = document.getElementById('next-button');
const submitButton = document.getElementById('submit-button');
const progressLabel = document.getElementById('progress-label');
const progressFill = document.getElementById('progress-fill');
const resultHeading = document.getElementById('result-heading');
const scoreValue = document.getElementById('score-value');
const scorePercent = document.getElementById('score-percent');
const resultInsight = document.getElementById('result-insight');
const resultsList = document.getElementById('results-list');
const questionCountSelect = document.getElementById('question-count');
const gradeSelect = document.getElementById('grade-select');
const timerDisplay = document.getElementById('timer-display');
const leaderboardList = document.getElementById('leaderboard-list');
const startJavaLabButton = document.getElementById('start-java-lab');
const javaLabLanding = document.getElementById('java-lab-landing');

const STORAGE_KEY = 'smartStudyHistory';
const LEADERBOARD_KEY = 'smartStudyLeaderboard';
const pageMode = document.body.dataset.page || 'general';
const TECHNICAL_SUBJECTS = ['java', 'java-coding', 'spring-boot', 'microservices'];
const GENERAL_SUBJECTS = ['math', 'science', 'gk'];
const INTERMEDIATE_STREAMS = {
    mpc: ['eamcet-mpc','intermediate-math', 'physics', 'chemistry'],
    bpc: ['eamcet-bpc','biology', 'physics', 'chemistry']
};

const state = {
    subjects: [],
    questions: [],
    currentIndex: 0,
    selectedAnswers: {},
    submitted: false,
    selectedCount: Number(questionCountSelect?.value || 25),
    selectedGrade: gradeSelect?.value || '7-8',
    selectedDifficulty: 'Beginner',
    history: loadRecentHistory(),
    leaderboard: loadLeaderboard(),
    timerInterval: null,
    timeLeft: 0,
    currentSubjectSlug: null,
    intermediateStream: 'mpc'
};

function loadRecentHistory() {
    try {
        const raw = localStorage.getItem(STORAGE_KEY);
        return raw ? JSON.parse(raw) : [];
    } catch (error) {
        return [];
    }
}

function saveRecentHistory() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state.history.slice(0, 8)));
}

function loadLeaderboard() {
    try {
        const raw = localStorage.getItem(LEADERBOARD_KEY);
        return raw ? JSON.parse(raw) : [];
    } catch (error) {
        return [];
    }
}

function saveLeaderboard() {
    localStorage.setItem(LEADERBOARD_KEY, JSON.stringify(state.leaderboard.slice(0, 8)));
}

function shuffleArray(items) {
    const copy = [...items];
    for (let i = copy.length - 1; i > 0; i -= 1) {
        const j = Math.floor(Math.random() * (i + 1));
        [copy[i], copy[j]] = [copy[j], copy[i]];
    }
    return copy;
}

function getVisibleSubjects() {
    if (!state.subjects.length) return [];
    const allowed = pageMode === 'technical'
        ? TECHNICAL_SUBJECTS
        : pageMode === 'java-lab'
            ? ['java-coding']
            : pageMode === 'intermediate'
                ? INTERMEDIATE_STREAMS[state.intermediateStream] || INTERMEDIATE_STREAMS.mpc
                : GENERAL_SUBJECTS;

    // For intermediate streams we should preserve the order defined in INTERMEDIATE_STREAMS
    if (pageMode === 'intermediate') {
        return allowed.map(slug => state.subjects.find(s => s.slug === slug)).filter(Boolean);
    }

    // For other modes keep the subjects in the order they appear in the fetched subject list
    return state.subjects.filter(subject => allowed.includes(subject.slug));
}

function renderDifficultyButtons() {
    const buttons = document.querySelectorAll('.difficulty-button');
    buttons.forEach(button => {
        const isActive = button.dataset.difficulty === state.selectedDifficulty;
        button.classList.toggle('active', isActive);
        button.setAttribute('aria-pressed', String(isActive));
    });
}

function renderIntermediateStreams() {
    const buttons = document.querySelectorAll('.intermediate-stream-button');
    buttons.forEach(button => {
        const isActive = button.dataset.stream === state.intermediateStream;
        button.classList.toggle('active', isActive);
        button.setAttribute('aria-pressed', String(isActive));
    });
}

function renderHistory() {
    if (!historyPanel) return;

    if (!state.history.length) {
        historyPanel.innerHTML = `
            <div class="history-header">
                <h3>Previous test details</h3>
            </div>
            <p class="empty-state">No tests attempted yet. Start a quiz to save your progress.</p>
        `;
        return;
    }

    historyPanel.innerHTML = `
        <div class="history-header">
            <h3>Previous test details</h3>
            <button id="clear-history" class="secondary">Delete all</button>
        </div>
        <div class="history-list">
            ${state.history.map((item, index) => `
                <div class="history-item">
                    <strong>${item.subject}</strong>
                    <span>${item.score}/${item.total} • ${item.percentage}%</span>
                    <small>${new Date(item.date).toLocaleString()}</small>
                    <button class="delete-history-entry secondary" data-index="${index}" style="margin-top: 10px; width: 100%;">Delete this test</button>
                </div>
            `).join('')}
        </div>
    `;
}

function renderLeaderboard() {
    if (!leaderboardList) return;
    const entries = state.leaderboard.slice(0, 5);
    if (!entries.length) {
        leaderboardList.innerHTML = '<li>No scores yet. Be the first to top the board.</li>';
        return;
    }

    leaderboardList.innerHTML = entries.map((entry, index) => `
        <li><strong>#${index + 1}</strong> ${entry.subject} — ${entry.score}/${entry.total} (${entry.percentage}%)</li>
    `).join('');
}

function renderDashboard() {
    const visibleSubjects = getVisibleSubjects();
    if (!dashboard) return;

    dashboard.innerHTML = visibleSubjects.map(subject => {
        const questionCount = subject.slug === 'eamcet' ? 50 : (subject.slug === 'eamcet-mpc' || subject.slug === 'eamcet-bpc' ? 60 : state.selectedCount);
        return `
            <button class="subject-card ${subject.slug}" data-subject="${subject.slug}">
                <div>
                    <h3>${subject.name}</h3>
                    <p>${subject.description}</p>
                </div>
                <div class="card-action">
                    <strong>${questionCount} Questions</strong>
                    <span class="primary" style="padding: 10px 14px; display: inline-block;">Start</span>
                </div>
            </button>
        `;
    }).join('');

    document.querySelectorAll('.subject-card').forEach(card => {
        card.addEventListener('click', () => startQuiz(card.dataset.subject));
    });
}

function getDifficultyTimeLimit(questionCount = state.selectedCount) {
    const limitMap = {
        Beginner: Math.max(120, Number(questionCount) * 18),
        Intermediate: Math.max(180, Number(questionCount) * 22),
        Advanced: Math.max(240, Number(questionCount) * 28)
    };
    return limitMap[state.selectedDifficulty] || 180;
}

function formatTime(seconds) {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${String(minutes).padStart(2, '0')}:${String(remainingSeconds).padStart(2, '0')}`;
}

function renderTimer() {
    if (!timerDisplay) return;
    timerDisplay.textContent = formatTime(state.timeLeft);
}

function stopTimer() {
    if (state.timerInterval) {
        clearInterval(state.timerInterval);
        state.timerInterval = null;
    }
}

function startTimer() {
    stopTimer();
    state.timerInterval = setInterval(() => {
        state.timeLeft -= 1;
        renderTimer();
        if (state.timeLeft <= 0) {
            stopTimer();
            submitQuiz();
        }
    }, 1000);
}

async function loadDashboard() {
    const response = await fetch('/api/subjects');
    state.subjects = await response.json();
    // initialize grade select if present
    if (gradeSelect && state.selectedGrade) gradeSelect.value = state.selectedGrade;
    renderIntermediateStreams();
    renderDashboard();
    renderHistory();
    renderLeaderboard();
    renderDifficultyButtons();
}

function startJavaLab() {
    state.selectedCount = Number(questionCountSelect?.value || 25);
    state.selectedDifficulty = document.querySelector('.difficulty-button.active')?.dataset.difficulty || 'Beginner';
    renderDifficultyButtons();

    const selected = state.subjects.find(subject => subject.slug === 'java-coding');
    if (!selected) return;

    startQuiz(selected.slug);
    if (javaLabLanding) javaLabLanding.classList.add('hidden');
}

async function startQuiz(subjectSlug) {
    const selected = state.subjects.find(subject => subject.slug === subjectSlug);
    if (!selected) return;

    state.selectedCount = Number(questionCountSelect?.value || 25);
    state.selectedDifficulty = document.querySelector('.difficulty-button.active')?.dataset.difficulty || state.selectedDifficulty;
    state.subtitle = selected.name;
    state.currentIndex = 0;
    state.selectedAnswers = {};
    state.submitted = false;
    state.currentSubjectSlug = subjectSlug;

    const gradeParam = (pageMode === 'intermediate' || pageMode === 'java-lab') ? '9-10' : state.selectedGrade;
    const response = await fetch(`/api/questions/${subjectSlug}?grade=${encodeURIComponent(gradeParam)}`);
    const questions = await response.json();
    let filteredQuestions;
    const questionLimit = subjectSlug === 'eamcet' ? 50 : (subjectSlug === 'eamcet-mpc' || subjectSlug === 'eamcet-bpc' ? 60 : state.selectedCount);

    // For eamcet-mpc / eamcet-bpc keep subject blocks in order (20 questions each) but randomize within each subject
    if (subjectSlug === 'eamcet-mpc' || subjectSlug === 'eamcet-bpc') {
        const perSubject = 20;
        filteredQuestions = [];
        for (let i = 0; i < 3; i++) {
            const start = i * perSubject;
            const chunk = questions.slice(start, start + perSubject);
            // Preserve subject block order and question order as returned by the server (no shuffling)
            filteredQuestions = filteredQuestions.concat(chunk);
        }
    } else {
        filteredQuestions = shuffleArray(questions);
    }

    if (subjectSlug === 'java-coding') {
        const difficultyMap = {
            Beginner: 10,
            Intermediate: 15,
            Advanced: 20
        };
        const limit = difficultyMap[state.selectedDifficulty] || 10;
        filteredQuestions = filteredQuestions.slice(0, Math.min(limit, state.selectedCount));
    }

    state.questions = filteredQuestions.slice(0, questionLimit);
    state.timeLeft = getDifficultyTimeLimit(state.questions.length || questionLimit);
    renderTimer();
    startTimer();

    // Show the chosen grade range for student-facing subjects, except for Intermediate MPC / EAMCET subjects.
    testSubject.textContent = pageMode === 'technical' || pageMode === 'java-lab' || pageMode === 'intermediate'
        ? selected.grade
        : (`Grades ${state.selectedGrade}`);
    testTitle.textContent = `${selected.name} Practice Test`;
    if (dashboard) dashboard.classList.add('hidden');
    if (resultsPanel) resultsPanel.classList.add('hidden');
    if (testPanel) testPanel.classList.remove('hidden');
    if (backButton) backButton.classList.remove('hidden');
    if (testBackButton) testBackButton.classList.remove('hidden');
    renderQuestion();
}

function renderQuestion() {
    const question = state.questions[state.currentIndex];
    if (!question) return;

    const selectedAnswer = state.selectedAnswers[question.id];

    questionCard.innerHTML = `
        <span class="question-number">Question ${state.currentIndex + 1} of ${state.questions.length}</span>
        <h3>${question.question}</h3>
        <div class="option-list">
            ${question.options.map((option, index) => {
                const isSelected = selectedAnswer === option;
                return `
                    <label class="option">
                        <input type="radio" name="question-${question.id}" value="${option}" ${isSelected ? 'checked' : ''}>
                        <span>${String.fromCharCode(65 + index)}. ${option}</span>
                    </label>
                `;
            }).join('')}
        </div>
    `;

    questionCard.querySelectorAll('input[type="radio"]').forEach(input => {
        input.addEventListener('change', (event) => {
            const answer = event.target.value;
            state.selectedAnswers[question.id] = answer;
            renderQuestion();
        });
    });

    progressLabel.textContent = `Question ${state.currentIndex + 1} of ${state.questions.length}`;
    progressFill.style.width = `${((state.currentIndex + 1) / state.questions.length) * 100}%`;

    prevButton.disabled = state.currentIndex === 0;
    nextButton.classList.toggle('hidden', state.currentIndex === state.questions.length - 1);
    submitButton.classList.toggle('hidden', state.currentIndex !== state.questions.length - 1);
}

function goToNextQuestion() {
    if (state.currentIndex < state.questions.length - 1) {
        state.currentIndex += 1;
        renderQuestion();
    }
}

function goToPreviousQuestion() {
    if (state.currentIndex > 0) {
        state.currentIndex -= 1;
        renderQuestion();
    }
}

function submitQuiz() {
    const answeredCount = Object.keys(state.selectedAnswers).length;
    if (answeredCount !== state.questions.length) {
        alert(`Please answer all ${state.questions.length} questions before submitting the test.`);
        return;
    }

    const correctCount = state.questions.reduce((count, question) => {
        return count + (state.selectedAnswers[question.id] === question.correctAnswer ? 1 : 0);
    }, 0);

    const percentage = Math.round((correctCount / state.questions.length) * 100);
    const insight = percentage >= 85
        ? 'Excellent work! You have strong understanding of this topic.'
        : percentage >= 60
            ? 'Good job! A little more practice will help you reach top marks.'
            : 'Keep going! Review the explanations below and try the topic again.';

    const questionPrefix = state.questions[0]?.id.split('-')[0] || '';
    const currentSubject = state.subjects.find(subject => {
        if (!state.questions[0]) return false;
        return subject.slug === questionPrefix || subject.slug.startsWith(questionPrefix + '-') || subject.slug.startsWith(questionPrefix);
    }) || state.subjects[0];
    const chosenSubject = currentSubject?.name || 'Practice Test';

    resultHeading.textContent = percentage >= 70 ? 'Great effort!' : 'Nice try!';
    scoreValue.textContent = `${correctCount}/${state.questions.length}`;
    scorePercent.textContent = `${percentage}%`;
    resultInsight.textContent = insight;

    resultsList.innerHTML = state.questions.map((question, index) => {
        const candidate = state.selectedAnswers[question.id];
        const isCorrect = candidate === question.correctAnswer;
        return `
            <div class="result-item ${isCorrect ? 'correct' : 'incorrect'}">
                <strong>Q${index + 1}: ${question.question}</strong>
                <p><strong>Your answer:</strong> ${candidate || 'No answer selected'}</p>
                <p><strong>Correct answer:</strong> ${question.correctAnswer}</p>
                <p><strong>Explanation:</strong> ${question.explanation}</p>
            </div>
        `;
    }).join('');

    const attempt = {
        subject: `${chosenSubject} (${state.selectedDifficulty})`,
        score: correctCount,
        total: state.questions.length,
        percentage,
        date: new Date().toISOString()
    };

    state.history = [attempt, ...state.history].slice(0, 8);
    state.leaderboard = [...state.leaderboard, attempt].sort((a, b) => b.percentage - a.percentage).slice(0, 8);
    saveRecentHistory();
    saveLeaderboard();
    renderHistory();
    renderLeaderboard();

    if (testPanel) testPanel.classList.add('hidden');
    if (resultsPanel) resultsPanel.classList.remove('hidden');
    state.submitted = true;
    stopTimer();
}

if (questionCountSelect) {
    questionCountSelect.addEventListener('change', (event) => {
        state.selectedCount = Number(event.target.value || 25);
        renderDashboard();
        if (state.currentSubjectSlug && !state.submitted && !testPanel?.classList.contains('hidden')) {
            startQuiz(state.currentSubjectSlug);
        }
    });
}

if (gradeSelect) {
    gradeSelect.addEventListener('change', (event) => {
        state.selectedGrade = event.target.value;
        renderDashboard();
        if (state.currentSubjectSlug && !state.submitted && !testPanel?.classList.contains('hidden')) {
            startQuiz(state.currentSubjectSlug);
        }
    });
}

if (historyPanel) {
    historyPanel.addEventListener('click', (event) => {
        const target = event.target;
        if (!target) return;

        if (target.id === 'clear-history') {
            state.history = [];
            localStorage.removeItem(STORAGE_KEY);
            renderHistory();
            return;
        }

        if (target.classList.contains('delete-history-entry')) {
            const index = Number(target.dataset.index);
            if (!Number.isNaN(index)) {
                state.history.splice(index, 1);
                saveRecentHistory();
                renderHistory();
            }
        }
    });
}

if (startJavaLabButton) {
    startJavaLabButton.addEventListener('click', startJavaLab);
}

document.querySelectorAll('.difficulty-button').forEach(button => {
    button.addEventListener('click', () => {
        state.selectedDifficulty = button.dataset.difficulty || 'Beginner';
        renderDifficultyButtons();
    });
});

document.querySelectorAll('.intermediate-stream-button').forEach(button => {
    button.addEventListener('click', () => {
        state.intermediateStream = button.dataset.stream || 'mpc';
        renderIntermediateStreams();
        renderDashboard();
        if (state.currentSubjectSlug && !state.submitted && !testPanel?.classList.contains('hidden')) {
            goBackToDashboard();
        }
    });
});

function goBackToDashboard() {
    state.questions = [];
    state.selectedAnswers = {};
    state.currentIndex = 0;
    state.submitted = false;
    state.currentSubjectSlug = null;
    if (resultsPanel) resultsPanel.classList.add('hidden');
    if (testPanel) testPanel.classList.add('hidden');
    if (dashboard) dashboard.classList.remove('hidden');
    if (javaLabLanding) javaLabLanding.classList.remove('hidden');
    if (backButton) backButton.classList.add('hidden');
    if (testBackButton) testBackButton.classList.add('hidden');
    stopTimer();
}

if (backButton) {
    backButton.addEventListener('click', goBackToDashboard);
}

if (testBackButton) {
    testBackButton.addEventListener('click', goBackToDashboard);
}

if (prevButton) prevButton.addEventListener('click', goToPreviousQuestion);
if (nextButton) nextButton.addEventListener('click', goToNextQuestion);
if (submitButton) submitButton.addEventListener('click', submitQuiz);

loadDashboard();
