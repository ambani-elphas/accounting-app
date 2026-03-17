const currency = (v) => `$${Number(v || 0).toFixed(2)}`;

async function refreshDashboard() {
  const response = await fetch('/api/transactions/dashboard');
  const data = await response.json();

  document.getElementById('count').textContent = data.transactionCount;
  document.getElementById('income').textContent = currency(data.income);
  document.getElementById('expenses').textContent = currency(data.expenses);
  document.getElementById('balance').textContent = currency(data.balance);

  const body = document.getElementById('transactions-body');
  body.innerHTML = '';

  for (const tx of data.recentTransactions) {
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
