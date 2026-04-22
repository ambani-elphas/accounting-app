const currency = (v) => `$${Number(v || 0).toFixed(2)}`;

const translations = {
  en: {
    "ui.title": "Accounting Dashboard",
    "ui.transactions": "Transactions",
    "ui.income": "Income",
    "ui.expenses": "Expenses",
    "ui.balance": "Balance",
    "ui.add_transaction": "Add Transaction",
    "ui.description": "Description",
    "ui.category": "Category",
    "ui.amount": "Amount",
    "ui.save": "Save",
    "ui.recent_transactions": "Recent Transactions",
    "ui.latest": "(latest 5)",
    "ui.top_categories": "Top Categories",
    "ui.language": "Language",
  },
  es: {
    "ui.title": "Panel de Contabilidad",
    "ui.transactions": "Transacciones",
    "ui.income": "Ingresos",
    "ui.expenses": "Gastos",
    "ui.balance": "Balance",
    "ui.add_transaction": "Añadir transacción",
    "ui.description": "Descripción",
    "ui.category": "Categoría",
    "ui.amount": "Importe",
    "ui.save": "Guardar",
    "ui.recent_transactions": "Transacciones recientes",
    "ui.latest": "(últimas 5)",
    "ui.top_categories": "Categorías principales",
    "ui.language": "Idioma",
  },
  fr: {
    "ui.title": "Tableau de bord comptable",
    "ui.transactions": "Transactions",
    "ui.income": "Revenus",
    "ui.expenses": "Dépenses",
    "ui.balance": "Solde",
    "ui.add_transaction": "Ajouter une transaction",
    "ui.description": "Description",
    "ui.category": "Catégorie",
    "ui.amount": "Montant",
    "ui.save": "Enregistrer",
    "ui.recent_transactions": "Transactions récentes",
    "ui.latest": "(5 dernières)",
    "ui.top_categories": "Catégories principales",
    "ui.language": "Langue",
  },
};

let selectedLanguage = localStorage.getItem("language") || "en";

function applyTranslations() {
  const locale = translations[selectedLanguage] || translations.en;

  document.querySelectorAll("[data-i18n]").forEach((element) => {
    const key = element.getAttribute("data-i18n");
    if (locale[key]) {
      element.textContent = locale[key];
    }
  });

  document.querySelectorAll("[data-i18n-placeholder]").forEach((element) => {
    const key = element.getAttribute("data-i18n-placeholder");
    if (locale[key]) {
      element.setAttribute("placeholder", locale[key]);
    }
  });

  document.documentElement.lang = selectedLanguage;
  document.title = locale["ui.title"];
}

async function apiFetch(path, options = {}) {
  const headers = {
    ...(options.headers || {}),
    "Accept-Language": selectedLanguage,
  };
  return fetch(path, { ...options, headers });
}

async function refreshDashboard() {
  const response = await apiFetch('/api/transactions/dashboard');
async function refreshDashboard() {
  const response = await fetch('/api/transactions/dashboard');
  const dashboard = await response.json();

  document.getElementById('count').textContent = dashboard.transactionCount;
  document.getElementById('income').textContent = currency(dashboard.income);
  document.getElementById('expenses').textContent = currency(dashboard.expenses);
  document.getElementById('balance').textContent = currency(dashboard.balance);

  const transactionsBody = document.getElementById('transactions-body');
  transactionsBody.innerHTML = '';

  for (const tx of dashboard.recentTransactions) {
    const row = document.createElement('tr');
    row.innerHTML = `<td>${tx.description}</td><td>${tx.category}</td><td>${tx.type}</td><td>${currency(tx.amount)}</td>`;
    transactionsBody.appendChild(row);
  }

  const categoriesBody = document.getElementById('categories-body');
  categoriesBody.innerHTML = '';

  for (const category of dashboard.topCategories) {
    const row = document.createElement('tr');
    row.innerHTML = `<td>${category.category}</td><td>${currency(category.income)}</td><td>${currency(category.expenses)}</td><td>${currency(category.balance)}</td>`;
    categoriesBody.appendChild(row);
  }
}

async function submitTransaction(event) {
  event.preventDefault();
  const form = event.target;
  const message = document.getElementById('form-message');
  message.textContent = '';

  const payload = {
    description: form.description.value,
    category: form.category.value,
    amount: Number(form.amount.value),
    type: form.type.value,
  };

  const response = await apiFetch('/api/transactions', {
  const response = await fetch('/api/transactions', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const error = await response.json();
    message.textContent = error.message;
    return;
  }

  form.reset();
  await refreshDashboard();
}

function handleLanguageChange(event) {
  selectedLanguage = event.target.value;
  localStorage.setItem("language", selectedLanguage);
  applyTranslations();
  refreshDashboard();
}

const languageSelect = document.getElementById('language-select');
languageSelect.value = selectedLanguage;
languageSelect.addEventListener('change', handleLanguageChange);

document.getElementById('transaction-form').addEventListener('submit', submitTransaction);
applyTranslations();
document.getElementById('transaction-form').addEventListener('submit', submitTransaction);
refreshDashboard();
