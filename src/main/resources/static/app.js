const currency = (v) => `$${Number(v || 0).toFixed(2)}`;

async function refreshDashboard() {
  const [dashboardResponse, pageResponse] = await Promise.all([
    fetch('/api/transactions/dashboard'),
    fetch('/api/transactions?page=0&size=5'),
  ]);

  const dashboard = await dashboardResponse.json();
  const page = await pageResponse.json();

  document.getElementById('count').textContent = dashboard.transactionCount;
  document.getElementById('income').textContent = currency(dashboard.income);
  document.getElementById('expenses').textContent = currency(dashboard.expenses);
  document.getElementById('balance').textContent = currency(dashboard.balance);

  const body = document.getElementById('transactions-body');
  body.innerHTML = '';

  for (const tx of page.items) {
    const row = document.createElement('tr');
    row.innerHTML = `<td>${tx.description}</td><td>${tx.category}</td><td>${tx.type}</td><td>${currency(tx.amount)}</td>`;
    body.appendChild(row);
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
