const currency = (v) => `$${Number(v || 0).toFixed(2)}`;

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

document.getElementById('transaction-form').addEventListener('submit', submitTransaction);
refreshDashboard();
