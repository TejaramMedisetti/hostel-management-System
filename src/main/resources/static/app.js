'use strict';
const $ = s => document.querySelector(s);
const esc = v => String(v ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const fmt = s => s ? String(s).replace('T', ' ').slice(0, 16) : '—';
const money = n => '₹' + Number(n ?? 0).toLocaleString('en-IN');

let session = JSON.parse(localStorage.getItem('hms-session') || 'null');
let actions = {};

/* ---------- API ---------- */
async function api(path, method = 'GET', body) {
  const headers = {'Content-Type': 'application/json'};
  if (session) headers.Authorization = 'Bearer ' + session.token;
  const res = await fetch(path, {method, headers, body: body ? JSON.stringify(body) : undefined});
  if (res.status === 401 && !path.startsWith('/api/auth')) { logout(); throw new Error('Session expired, please sign in again'); }
  if (res.status === 403) throw new Error('You do not have permission to do that');
  if (res.status === 204) return null;
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch (e) { /* not json */ }
  if (!res.ok) {
    const m = data && data.message;
    throw new Error(m && typeof m === 'object' ? Object.values(m).join(', ') : (m || 'Request failed'));
  }
  return data;
}

/* ---------- helpers ---------- */
const content = html => { $('#content').innerHTML = html; };
const btn = (act, id, label, cls = '') => `<button class="btn small ${cls}" data-act="${act}" data-id="${id}">${label}</button>`;
const badge = s => `<span class="badge ${esc(String(s).toLowerCase())}">${esc(String(s).replace('_', ' '))}</span>`;
const card = (l, n) => `<div class="card"><div class="l">${l}</div><div class="n">${esc(n)}</div></div>`;
function table(heads, rows) {
  const body = rows.length
    ? rows.map(r => `<tr>${r.map(c => `<td>${c}</td>`).join('')}</tr>`).join('')
    : `<tr><td colspan="${heads.length}" class="empty">No records yet</td></tr>`;
  return `<div class="tw"><table><thead><tr>${heads.map(h => `<th>${h}</th>`).join('')}</tr></thead><tbody>${body}</tbody></table></div>`;
}

function fieldHtml(f, value) {
  const v = value ?? '';
  const req = f.required ? 'required' : '';
  if (f.type === 'select') {
    const opts = f.options.map(o => `<option value="${esc(o.value)}" ${String(o.value) === String(v) ? 'selected' : ''}>${esc(o.label)}</option>`).join('');
    return `<label>${esc(f.label)}<select name="${f.name}" ${req}>${f.required ? '<option value="">Select...</option>' : ''}${opts}</select></label>`;
  }
  const step = f.step ? `step="${f.step}"` : '';
  const ro = f.readonly ? 'readonly' : '';
  return `<label>${esc(f.label)}<input name="${f.name}" type="${f.type || 'text'}" value="${esc(v)}" ${req} ${ro} ${step}></label>`;
}

function closeModal() { $('#modal').classList.remove('open'); $('#modal').innerHTML = ''; }

function openForm(title, fields, values, onSubmit) {
  const m = $('#modal');
  m.innerHTML = `<div class="modal-box"><h3>${esc(title)}</h3><form id="mf">${fields.map(f => fieldHtml(f, values[f.name])).join('')}
    <div class="err" id="mferr"></div>
    <div class="actions"><button type="button" class="btn ghost" id="mcancel">Cancel</button><button class="btn">Save</button></div></form></div>`;
  m.classList.add('open');
  $('#mcancel').onclick = closeModal;
  $('#mf').onsubmit = async e => {
    e.preventDefault();
    const data = {};
    fields.forEach(f => {
      const raw = e.target.elements[f.name].value;
      if (raw === '') data[f.name] = null;
      else if (f.type === 'number' || f.num) data[f.name] = Number(raw);
      else data[f.name] = raw;
    });
    try { await onSubmit(data); closeModal(); } catch (err) { $('#mferr').textContent = err.message; }
  };
}

/* ---------- admin views ---------- */
const views = {};

views.dashboard = async () => {
  const d = await api('/api/dashboard');
  content(`<h2>Dashboard</h2><div class="cards">
    ${card('Students', d.totalStudents)}${card('Rooms', d.totalRooms)}${card('Total beds', d.totalBeds)}
    ${card('Occupied beds', d.occupiedBeds)}${card('Available beds', d.availableBeds)}
    ${card('Open complaints', d.openComplaints)}${card('Visitors inside', d.visitorsInside)}
    ${card('Pending payments', d.pendingPayments)}${card('Fees collected', money(d.revenueCollected))}${card('Fees pending', money(d.revenuePending))}
  </div>`);
};

const studentFields = creating => [
  {name: 'fullName', label: 'Full name', required: true},
  {name: 'email', label: creating ? 'Email (used as login)' : 'Email', type: 'email', required: true, readonly: !creating},
  {name: 'phone', label: 'Phone'},
  {name: 'course', label: 'Course'},
  {name: 'academicYear', label: 'Year', type: 'number'},
  {name: 'guardianName', label: 'Guardian name'},
  {name: 'guardianPhone', label: 'Guardian phone'},
  {name: 'address', label: 'Address'},
  ...(creating ? [{name: 'password', label: 'Initial password (default: student123)', type: 'password'}] : [])
];

views.students = async (q = '') => {
  const list = await api('/api/students' + (q ? '?search=' + encodeURIComponent(q) : ''));
  const byId = Object.fromEntries(list.map(s => [s.id, s]));
  content(`<div class="head"><h2>Students</h2><div><input id="search" placeholder="Search by name + Enter" value="${esc(q)}"><button class="btn" data-act="add">+ Add student</button></div></div>` +
    table(['ID', 'Name', 'Email', 'Phone', 'Course', 'Year', 'Actions'],
      list.map(s => [s.id, esc(s.fullName), esc(s.email), esc(s.phone || '—'), esc(s.course || '—'), esc(s.academicYear || '—'),
        btn('edit', s.id, 'Edit', 'ghost') + btn('del', s.id, 'Delete', 'danger')])));
  $('#search').onkeydown = e => { if (e.key === 'Enter') views.students(e.target.value.trim()); };
  actions = {
    add: () => openForm('Add student', studentFields(true), {}, async d => { await api('/api/students', 'POST', d); views.students(q); }),
    edit: id => openForm('Edit student', studentFields(false), byId[id], async d => { await api('/api/students/' + id, 'PUT', d); views.students(q); }),
    del: async id => {
      if (!confirm('Delete this student?')) return;
      try { await api('/api/students/' + id, 'DELETE'); views.students(q); } catch (e) { alert(e.message); }
    }
  };
};

const roomFields = [
  {name: 'roomNumber', label: 'Room number', required: true},
  {name: 'block', label: 'Block', required: true},
  {name: 'floor', label: 'Floor', type: 'number', required: true},
  {name: 'capacity', label: 'Capacity (beds)', type: 'number', required: true},
  {name: 'type', label: 'Type', type: 'select', required: true, options: ['SINGLE', 'DOUBLE', 'TRIPLE', 'DORMITORY'].map(t => ({value: t, label: t}))},
  {name: 'monthlyRent', label: 'Monthly rent (₹)', type: 'number', step: '0.01', required: true}
];

views.rooms = async () => {
  const list = await api('/api/rooms');
  const byId = Object.fromEntries(list.map(r => [r.id, r]));
  content(`<div class="head"><h2>Rooms</h2><button class="btn" data-act="add">+ Add room</button></div>` +
    table(['Room', 'Block', 'Floor', 'Type', 'Occupancy', 'Available', 'Rent', 'Actions'],
      list.map(r => [esc(r.roomNumber), esc(r.block), r.floor, esc(r.type), `${r.occupied}/${r.capacity}`,
        r.available > 0 ? badge('PAID').replace('PAID', r.available + ' free') : badge('PENDING').replace('PENDING', 'Full'),
        money(r.monthlyRent), btn('edit', r.id, 'Edit', 'ghost') + btn('del', r.id, 'Delete', 'danger')])));
  actions = {
    add: () => openForm('Add room', roomFields, {}, async d => { await api('/api/rooms', 'POST', d); views.rooms(); }),
    edit: id => openForm('Edit room', roomFields, byId[id], async d => { await api('/api/rooms/' + id, 'PUT', d); views.rooms(); }),
    del: async id => {
      if (!confirm('Delete this room?')) return;
      try { await api('/api/rooms/' + id, 'DELETE'); views.rooms(); } catch (e) { alert(e.message); }
    }
  };
};

views.allocations = async () => {
  const list = await api('/api/allocations');
  content(`<div class="head"><h2>Room allocations</h2><button class="btn" data-act="add">+ Allocate room</button></div>` +
    table(['Student', 'Room', 'Block', 'Allocated on', 'Actions'],
      list.map(a => [esc(a.student.fullName), esc(a.room.roomNumber), esc(a.room.block), esc(a.allocatedOn), btn('vacate', a.id, 'Vacate', 'danger')])));
  actions = {
    add: async () => {
      try {
        const [students, rooms] = await Promise.all([api('/api/students'), api('/api/rooms?availableOnly=true')]);
        openForm('Allocate room', [
          {name: 'studentId', label: 'Student', type: 'select', num: true, required: true, options: students.map(s => ({value: s.id, label: `${s.fullName} (${s.email})`}))},
          {name: 'roomId', label: 'Room (available only)', type: 'select', num: true, required: true, options: rooms.map(r => ({value: r.id, label: `${r.roomNumber} - ${r.available} free`}))}
        ], {}, async d => { await api('/api/allocations', 'POST', d); views.allocations(); });
      } catch (e) { alert(e.message); }
    },
    vacate: async id => {
      if (!confirm('Vacate this room?')) return;
      try { await api(`/api/allocations/${id}/vacate`, 'PUT'); views.allocations(); } catch (e) { alert(e.message); }
    }
  };
};

views.payments = async () => {
  const list = await api('/api/payments');
  content(`<div class="head"><h2>Payments</h2><button class="btn" data-act="add">+ Add payment</button></div>` +
    table(['Student', 'Month', 'Amount', 'Status', 'Method', 'Paid on', 'Actions'],
      list.map(p => [esc(p.student.fullName), esc(p.forMonth), money(p.amount), badge(p.status), esc(p.method || '—'), esc(p.paidOn || '—'),
        p.status === 'PENDING' ? btn('pay', p.id, 'Mark paid', 'ok') : ''])));
  actions = {
    add: async () => {
      try {
        const students = await api('/api/students');
        const month = new Date().toISOString().slice(0, 7);
        openForm('Add payment', [
          {name: 'studentId', label: 'Student', type: 'select', num: true, required: true, options: students.map(s => ({value: s.id, label: s.fullName}))},
          {name: 'amount', label: 'Amount (₹)', type: 'number', step: '0.01', required: true},
          {name: 'forMonth', label: 'For month (YYYY-MM)', required: true},
          {name: 'method', label: 'Method', type: 'select', options: ['CASH', 'UPI', 'CARD'].map(m => ({value: m, label: m}))},
          {name: 'status', label: 'Status', type: 'select', options: ['PAID', 'PENDING'].map(m => ({value: m, label: m}))}
        ], {forMonth: month, status: 'PAID', method: 'CASH'}, async d => { await api('/api/payments', 'POST', d); views.payments(); });
      } catch (e) { alert(e.message); }
    },
    pay: async id => {
      const method = prompt('Payment method (CASH / UPI / CARD)', 'CASH');
      if (!method) return;
      try { await api(`/api/payments/${id}/pay?method=${encodeURIComponent(method)}`, 'PUT'); views.payments(); } catch (e) { alert(e.message); }
    }
  };
};

views.complaints = async () => {
  const list = await api('/api/complaints');
  const byId = Object.fromEntries(list.map(c => [c.id, c]));
  content(`<div class="head"><h2>Complaints</h2><button class="btn" data-act="add">+ Log complaint</button></div>` +
    table(['Student', 'Title', 'Description', 'Status', 'Resolution', 'Raised', 'Actions'],
      list.map(c => [esc(c.student.fullName), esc(c.title), esc(c.description), badge(c.status), esc(c.resolution || '—'), fmt(c.createdAt),
        btn('update', c.id, 'Update', 'ghost')])));
  actions = {
    add: async () => {
      try {
        const students = await api('/api/students');
        openForm('Log complaint', [
          {name: 'studentId', label: 'Student', type: 'select', num: true, required: true, options: students.map(s => ({value: s.id, label: s.fullName}))},
          {name: 'title', label: 'Title', required: true},
          {name: 'description', label: 'Description', required: true}
        ], {}, async d => { await api('/api/complaints', 'POST', d); views.complaints(); });
      } catch (e) { alert(e.message); }
    },
    update: id => openForm('Update complaint', [
      {name: 'status', label: 'Status', type: 'select', required: true, options: ['OPEN', 'IN_PROGRESS', 'RESOLVED'].map(s => ({value: s, label: s}))},
      {name: 'resolution', label: 'Resolution note'}
    ], byId[id], async d => { await api('/api/complaints/' + id, 'PUT', d); views.complaints(); })
  };
};

views.visitors = async () => {
  const list = await api('/api/visitors');
  content(`<div class="head"><h2>Visitors</h2><button class="btn" data-act="add">+ Check in visitor</button></div>` +
    table(['Visitor', 'Visiting', 'Purpose', 'In', 'Out', 'Actions'],
      list.map(v => [esc(v.visitorName), esc(v.student.fullName), esc(v.purpose || '—'), fmt(v.inTime), fmt(v.outTime),
        v.outTime ? '' : btn('out', v.id, 'Check out', 'ok')])));
  actions = {
    add: async () => {
      try {
        const students = await api('/api/students');
        openForm('Check in visitor', [
          {name: 'studentId', label: 'Visiting student', type: 'select', num: true, required: true, options: students.map(s => ({value: s.id, label: s.fullName}))},
          {name: 'visitorName', label: 'Visitor name', required: true},
          {name: 'purpose', label: 'Purpose'}
        ], {}, async d => { await api('/api/visitors', 'POST', d); views.visitors(); });
      } catch (e) { alert(e.message); }
    },
    out: async id => { try { await api(`/api/visitors/${id}/checkout`, 'PUT'); views.visitors(); } catch (e) { alert(e.message); } }
  };
};

/* ---------- student views ---------- */
views.myprofile = async () => {
  const s = await api('/api/me/profile');
  const f = (l, v) => `<div><span>${l}</span>${esc(v || '—')}</div>`;
  content(`<h2 style="margin-bottom:18px">My profile</h2><div class="profile">
    ${f('Name', s.fullName)}${f('Email', s.email)}${f('Phone', s.phone)}${f('Course', s.course)}${f('Year', s.academicYear)}
    ${f('Guardian', s.guardianName)}${f('Guardian phone', s.guardianPhone)}${f('Address', s.address)}</div>`);
  actions = {};
};

views.myroom = async () => {
  const list = await api('/api/me/allocations');
  content(`<h2 style="margin-bottom:18px">My room</h2>` + table(['Room', 'Block', 'Floor', 'Allocated on', 'Vacated on', 'Status'],
    list.map(a => [esc(a.room.roomNumber), esc(a.room.block), a.room.floor, esc(a.allocatedOn), esc(a.vacatedOn || '—'), badge(a.active ? 'PAID' : 'PENDING').replace(/>.*</, `>${a.active ? 'Current' : 'Past'}<`)])));
  actions = {};
};

views.mypayments = async () => {
  const list = await api('/api/me/payments');
  content(`<h2 style="margin-bottom:18px">My payments</h2>` + table(['Month', 'Amount', 'Status', 'Method', 'Paid on'],
    list.map(p => [esc(p.forMonth), money(p.amount), badge(p.status), esc(p.method || '—'), esc(p.paidOn || '—')])));
  actions = {};
};

views.mycomplaints = async () => {
  const list = await api('/api/me/complaints');
  content(`<div class="head"><h2>My complaints</h2><button class="btn" data-act="add">+ Raise complaint</button></div>` +
    table(['Title', 'Description', 'Status', 'Resolution', 'Raised'],
      list.map(c => [esc(c.title), esc(c.description), badge(c.status), esc(c.resolution || '—'), fmt(c.createdAt)])));
  actions = {
    add: () => openForm('Raise complaint', [
      {name: 'title', label: 'Title', required: true},
      {name: 'description', label: 'Describe the problem', required: true}
    ], {}, async d => { await api('/api/me/complaints', 'POST', d); views.mycomplaints(); })
  };
};

/* ---------- shell ---------- */
const NAV = {
  staff: [['dashboard', 'Dashboard'], ['students', 'Students'], ['rooms', 'Rooms'], ['allocations', 'Allocations'],
          ['payments', 'Payments'], ['complaints', 'Complaints'], ['visitors', 'Visitors']],
  student: [['myprofile', 'My profile'], ['myroom', 'My room'], ['mypayments', 'My payments'], ['mycomplaints', 'My complaints']]
};

async function show(name) {
  document.querySelectorAll('#nav button').forEach(b => b.classList.toggle('active', b.dataset.view === name));
  actions = {};
  try { await views[name](); }
  catch (e) { if (session) content(`<div class="toast">${esc(e.message)}</div>`); }
}

function startApp() {
  $('#login-screen').hidden = true;
  $('#app').hidden = false;
  $('#who').textContent = `${session.username} (${session.role})`;
  const items = session.role === 'STUDENT' ? NAV.student : NAV.staff;
  $('#nav').innerHTML = items.map(([v, l]) => `<button data-view="${v}">${l}</button>`).join('');
  show(items[0][0]);
}

function logout() {
  session = null;
  localStorage.removeItem('hms-session');
  $('#app').hidden = true;
  $('#login-screen').hidden = false;
  closeModal();
}

$('#nav').onclick = e => { const b = e.target.closest('button[data-view]'); if (b) show(b.dataset.view); };
$('#content').onclick = e => {
  const b = e.target.closest('[data-act]');
  if (b && actions[b.dataset.act]) actions[b.dataset.act](b.dataset.id);
};
$('#logout').onclick = logout;
$('#modal').onclick = e => { if (e.target.id === 'modal') closeModal(); };

$('#login-form').onsubmit = async e => {
  e.preventDefault();
  $('#login-err').textContent = '';
  const f = e.target.elements;
  try {
    const r = await api('/api/auth/login', 'POST', {username: f.username.value.trim(), password: f.password.value});
    session = r;
    localStorage.setItem('hms-session', JSON.stringify(r));
    e.target.reset();
    startApp();
  } catch (err) { $('#login-err').textContent = err.message; }
};

if (session) startApp();
