/* ===== 全局状态 ===== */
const state = {
  token: localStorage.getItem('token') || null,
  userId: localStorage.getItem('userId') || null,
  role: localStorage.getItem('role') || null,
  userName: localStorage.getItem('userName') || null,
};

const ROLE_LABEL = { READER: '读者', ADMIN: '管理员' };
const BORROW_STATUS_LABEL = { BORROWED: '在借', RETURNED: '已还', OVERDUE: '逾期', LOST: '丢失' };
const BORROW_STATUS_CLASS = { BORROWED: 'status-borrowed', RETURNED: 'status-returned', OVERDUE: 'status-overdue', LOST: 'status-lost' };
const NOTIFICATION_TYPE_LABEL = { DUE_REMIND: '到期提醒', OVERDUE: '逾期', AVAILABLE: '可借提醒', SYSTEM: '系统公告' };

let myBorrowsStatus = '';
let myActiveBorrowBookIds = new Set();
let myFavoriteBookIds = new Set();
let currentBookPage = 1;

/* ===== 工具函数 ===== */
function $(sel) { return document.querySelector(sel); }

function toast(msg, type) {
  const t = $('#toast');
  t.textContent = msg;
  t.className = 'toast ' + (type || 'info');
  t.style.display = 'block';
  clearTimeout(t._timer);
  t._timer = setTimeout(function () { t.style.display = 'none'; }, 2600);
}

function esc(s) {
  if (s === null || s === undefined) return '';
  return String(s).replace(/[&<>"']/g, function (c) {
    return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
  });
}

function fmtTime(s) {
  if (!s) return '-';
  return String(s).replace('T', ' ').slice(0, 16);
}

/* ===== 请求封装 ===== */
async function api(method, url, body) {
  const headers = { 'Content-Type': 'application/json' };
  if (state.token) headers['Authorization'] = 'Bearer ' + state.token;

  let res;
  try {
    res = await fetch(url, { method: method, headers: headers, body: body ? JSON.stringify(body) : undefined });
  } catch (e) {
    throw new Error('网络请求失败，请确认后端已启动');
  }

  const data = await res.json().catch(function () { return { code: -1, message: '响应解析失败' }; });

  if (res.status === 401 && data.code === 40100 && state.token) {
    logout();
    throw new Error('登录已过期，请重新登录');
  }
  if (data.code !== 0) {
    throw new Error(data.message || '请求失败');
  }
  return data.data;
}

/* ===== 当前用户借阅/收藏状态（图书浏览按钮态用） ===== */
async function loadMyBookStates() {
  if (state.role !== 'READER') {
    myActiveBorrowBookIds = new Set();
    myFavoriteBookIds = new Set();
    return;
  }
  try {
    const results = await Promise.all([
      api('GET', '/api/borrows/my/book-ids'),
      api('GET', '/api/favorites/book-ids'),
    ]);
    myActiveBorrowBookIds = new Set(results[0] || []);
    myFavoriteBookIds = new Set(results[1] || []);
  } catch (e) {
    myActiveBorrowBookIds = new Set();
    myFavoriteBookIds = new Set();
  }
}

/* ===== 登录 / 注册 / 登出 ===== */
async function doLogin() {
  const userName = $('#login-username').value.trim();
  const password = $('#login-password').value;
  if (!userName || !password) return toast('请输入用户名和密码', 'error');
  try {
    const data = await api('POST', '/api/auth/login', { userName: userName, password: password });
    state.token = data.token;
    state.userId = data.userId;
    state.role = data.role;
    state.userName = userName;
    localStorage.setItem('token', data.token);
    localStorage.setItem('userId', data.userId);
    localStorage.setItem('role', data.role);
    localStorage.setItem('userName', userName);
    enterApp();
  } catch (e) { toast(e.message, 'error'); }
}

async function doRegister() {
  const userName = $('#reg-username').value.trim();
  const password = $('#reg-password').value;
  if (!userName || !password) return toast('请输入用户名和密码', 'error');
  try {
    await api('POST', '/api/auth/register', { userName: userName, password: password });
    toast('注册成功，请用该账号登录', 'success');
    $('#login-username').value = userName;
    $('#login-password').value = '';
    $('#reg-password').value = '';
  } catch (e) { toast(e.message, 'error'); }
}

function fillAdmin() {
  $('#login-username').value = 'admin';
  $('#login-password').value = 'admin123456';
}

async function logout() {
  const token = state.token;
  // 先清本地状态并切回登录视图（同步，保证立即退出）
  state.token = state.userId = state.role = state.userName = null;
  localStorage.removeItem('token');
  localStorage.removeItem('userId');
  localStorage.removeItem('role');
  localStorage.removeItem('userName');
  $('#app-view').style.display = 'none';
  $('#login-view').style.display = 'flex';
  $('#login-password').value = '';

  // 再尽力通知后端拉黑当前 token（失败不阻塞本地退出）
  if (token) {
    try {
      await fetch('/api/auth/logout', {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token },
      });
    } catch (e) {
      // 忽略：网络异常/后端不可用也不影响本地退出
    }
  }
}

function enterApp() {
  $('#login-view').style.display = 'none';
  $('#app-view').style.display = 'flex';
  $('#user-info').textContent = (state.userName || '用户') + '（' + (ROLE_LABEL[state.role] || state.role) + '）';
  renderMenu();
  go('books');
}

/* ===== 菜单与路由 ===== */
function renderMenu() {
  const items = [];
  items.push({ id: 'books', label: '📚 图书浏览' });
  if (state.role === 'READER') {
    items.push({ id: 'myBorrows', label: '📖 我的借阅' });
    items.push({ id: 'favorites', label: '⭐ 我的收藏' });
  }
  items.push({ id: 'notifications', label: '🔔 我的通知' });
  if (state.role === 'ADMIN') {
    items.push({ id: 'adminBooks', label: '🛠 书籍管理' });
    items.push({ id: 'adminCategories', label: '🗂 分类管理' });
    items.push({ id: 'adminUsers', label: '👤 用户管理' });
    items.push({ id: 'adminBorrows', label: '📋 借阅管理' });
  }
  $('#menu').innerHTML = items.map(function (i) {
    return '<a class="menu-item" data-page="' + i.id + '" onclick="go(\'' + i.id + '\')">' + i.label + '</a>';
  }).join('');
}

function go(page) {
  currentPage = page;
  document.querySelectorAll('.menu-item').forEach(function (a) {
    a.classList.toggle('active', a.dataset.page === page);
  });
  const fn = PAGES[page];
  if (fn) fn();
}

/* ===== 通用弹窗 ===== */
function openModal(html) {
  $('#modal-box').innerHTML = html;
  $('#modal-overlay').style.display = 'flex';
}
function closeModal() {
  $('#modal-overlay').style.display = 'none';
  $('#modal-box').innerHTML = '';
}

/* ===== 封面上传 ===== */
function coverThumbHtml(url) {
  return url
    ? '<img class="cover-thumb" src="' + esc(url) + '" onerror="this.style.display=\'none\'">'
    : '<span class="cover-empty">—</span>';
}

function showCoverPreview(url) {
  const box = document.getElementById('cover-preview');
  if (!box) return;
  if (url) {
    box.innerHTML = '<img src="' + esc(url) + '" alt="封面预览">';
  } else {
    box.innerHTML = '<span class="cover-empty">暂无封面</span>';
  }
}

function pickCover() {
  const input = document.getElementById('cover-file');
  if (input) input.click();
}

async function uploadFile(file) {
  const fd = new FormData();
  fd.append('file', file);
  const res = await fetch('/api/files/cover', {
    method: 'POST',
    headers: { 'Authorization': 'Bearer ' + state.token },
    body: fd,
  });
  const data = await res.json().catch(function () { return { code: -1, message: '响应解析失败' }; });
  if (res.status === 401 && data.code === 40100 && state.token) {
    logout();
    throw new Error('登录已过期，请重新登录');
  }
  if (data.code !== 0) {
    throw new Error(data.message || '上传失败');
  }
  return data.data.url;
}

function handleCoverPick(input) {
  const file = input.files && input.files[0];
  if (!file) return;
  const btn = document.getElementById('cover-upload-btn');
  if (btn) { btn.disabled = true; btn.textContent = '上传中…'; }
  uploadFile(file).then(function (url) {
    const coverInput = document.getElementById('bf-cover');
    if (coverInput) coverInput.value = url;
    showCoverPreview(url);
    toast('封面上传成功', 'success');
  }).catch(function (e) {
    toast(e.message, 'error');
  }).finally(function () {
    if (btn) { btn.disabled = false; btn.textContent = '上传封面'; }
  });
}

/* ===== 分页 ===== */
function pagerHtml(data, fnName) {
  const pageSize = data.pageSize || 10;
  const totalPages = Math.max(1, Math.ceil((data.total || 0) / pageSize));
  const p = data.page || 1;
  return '<div class="pager">' +
    '<button ' + (p <= 1 ? 'disabled' : '') + ' onclick="' + fnName + '(' + (p - 1) + ')">上一页</button>' +
    '<span>第 ' + p + ' / ' + totalPages + ' 页，共 ' + (data.total || 0) + ' 条</span>' +
    '<button ' + (p >= totalPages ? 'disabled' : '') + ' onclick="' + fnName + '(' + (p + 1) + ')">下一页</button>' +
    '</div>';
}

/* ===== 图书浏览（公开） ===== */
async function loadCategories(all) {
  try {
    return await api('GET', '/api/categories' + (all ? '/all' : ''));
  } catch (e) {
    toast(e.message, 'error');
    return [];
  }
}

async function renderBooks() {
  await loadMyBookStates();
  const cats = await loadCategories(false);
  $('#content').innerHTML =
    '<h2>图书浏览</h2>' +
    '<div class="toolbar">' +
    '  <select id="book-cat"><option value="">全部分类</option>' +
    cats.map(function (c) { return '<option value="' + c.id + '">' + esc(c.name) + '</option>'; }).join('') +
    '  </select>' +
    '  <input id="book-keyword" placeholder="书名关键词">' +
    '  <label><input type="checkbox" id="book-only-avail"> 仅看可借</label>' +
    '  <button class="primary" onclick="loadBooks(1)">查询</button>' +
    '</div>' +
    '<div id="book-list"></div>';
  loadBooks(1);
}

async function loadBooks(page) {
  currentBookPage = page;
  const categoryId = $('#book-cat').value;
  const keyword = $('#book-keyword').value.trim();
  const onlyAvailable = $('#book-only-avail').checked;
  const qs = new URLSearchParams({ page: page, pageSize: 10 });
  if (categoryId) qs.set('categoryId', categoryId);
  if (keyword) qs.set('keyword', keyword);
  if (onlyAvailable) qs.set('onlyAvailable', 'true');
  try {
    const data = await api('GET', '/api/books?' + qs.toString());
    $('#book-list').innerHTML = bookTableHtml(data);
  } catch (e) {
    toast(e.message, 'error');
    $('#book-list').innerHTML = '';
  }
}

function bookTableHtml(data) {
  const items = data.items || [];
  const rows = items.map(function (b) {
    const outOfStock = (b.availableCount || 0) <= 0;
    const borrowed = myActiveBorrowBookIds.has(b.id);
    const favorited = myFavoriteBookIds.has(b.id);
    let borrowBtn = '';
    if (state.role === 'READER') {
      if (borrowed) borrowBtn = '<button disabled title="您已借阅该书">已借阅</button>';
      else if (outOfStock) borrowBtn = '<button disabled title="暂无库存">暂无库存</button>';
      else borrowBtn = '<button class="btn-borrow" onclick="openBorrow(' + b.id + ')">借阅</button>';
    }
    const favBtn = (state.role === 'READER')
      ? (favorited
          ? '<button class="btn-unfavorite" onclick="unfavoriteFromBooks(' + b.id + ')">取消收藏</button>'
          : '<button class="btn-favorite" onclick="addFavorite(' + b.id + ')">收藏</button>')
      : '';
    return '<tr>' +
      '<td>' + b.id + '</td>' +
      '<td>' + coverThumbHtml(b.coverUrl) + '</td>' +
      '<td>' + esc(b.name) + (outOfStock ? ' <span class="tag tag-danger">缺货</span>' : '') + '</td>' +
      '<td>' + esc(b.author) + '</td>' +
      '<td class="' + (outOfStock ? 'text-danger' : '') + '">' + (b.availableCount || 0) + ' / ' + (b.totalCount || 0) + '</td>' +
      '<td class="actions">' +
      '  <button onclick="openBookDetail(' + b.id + ')">详情</button>' +
      borrowBtn + favBtn +
      '</td></tr>';
  }).join('');
  return '<table class="table"><thead><tr><th>ID</th><th>封面</th><th>书名</th><th>作者</th><th>可借/馆藏</th><th>操作</th></tr></thead>' +
    '<tbody>' + (rows || '<tr><td colspan="6" class="empty">暂无图书</td></tr>') + '</tbody></table>' +
    pagerHtml(data, 'loadBooks');
}

async function openBookDetail(id) {
  try {
    const b = await api('GET', '/api/books/' + id);
    openModal(
      '<h3>' + esc(b.name) + '</h3>' +
      (b.coverUrl ? '<div class="detail-cover"><img src="' + esc(b.coverUrl) + '"></div>' : '') +
      '<div class="detail">' +
      '  <p><b>ISBN：</b>' + esc(b.isbn) + '</p>' +
      '  <p><b>作者：</b>' + esc(b.author) + '</p>' +
      '  <p><b>出版社：</b>' + esc(b.publisher || '-') + '</p>' +
      '  <p><b>年份：</b>' + (b.publishYear || '-') + '</p>' +
      '  <p><b>简介：</b>' + esc(b.description || '-') + '</p>' +
      '  <p><b>可借/馆藏：</b>' + (b.availableCount || 0) + ' / ' + (b.totalCount || 0) + '</p>' +
      '</div>' +
      '<div class="modal-actions">' +
      (state.role === 'READER' && b.availableCount > 0 ? '<button class="primary" onclick="closeModal();openBorrow(' + b.id + ')">借阅</button>' : '') +
      '  <button onclick="closeModal()">关闭</button>' +
      '</div>'
    );
  } catch (e) { toast(e.message, 'error'); }
}

/* ===== 借阅 ===== */
function openBorrow(bookId) {
  openModal(
    '<h3>借阅图书 #' + bookId + '</h3>' +
    '<div class="form"><label>借阅天数' +
    '<select id="borrow-days"><option value="7">7 天</option><option value="15">15 天</option><option value="30">30 天</option></select>' +
    '</label></div>' +
    '<div class="modal-actions"><button class="primary" onclick="doBorrow(' + bookId + ')">确认借阅</button><button onclick="closeModal()">取消</button></div>'
  );
}

async function doBorrow(bookId) {
  const days = parseInt($('#borrow-days').value, 10);
  try {
    await api('POST', '/api/borrows', { bookId: bookId, days: days });
    closeModal();
    toast('借阅成功', 'success');
    myActiveBorrowBookIds.add(bookId);
    if (currentPage === 'books') loadBooks(currentBookPage);
  } catch (e) { toast(e.message, 'error'); }
}

/* ===== 收藏 ===== */
async function addFavorite(bookId) {
  try {
    await api('POST', '/api/favorites', { bookId: bookId });
    toast('已收藏', 'success');
    myFavoriteBookIds.add(bookId);
    if (currentPage === 'books') loadBooks(currentBookPage);
  } catch (e) { toast(e.message, 'error'); }
}

async function unfavoriteFromBooks(bookId) {
  try {
    await api('DELETE', '/api/favorites/' + bookId);
    toast('已取消收藏', 'success');
    myFavoriteBookIds.delete(bookId);
    if (currentPage === 'books') loadBooks(currentBookPage);
  } catch (e) { toast(e.message, 'error'); }
}

async function renderFavorites(page) {
  page = page || 1;
  $('#content').innerHTML = '<h2>我的收藏</h2><div id="fav-list"></div>';
  try {
    const data = await api('GET', '/api/favorites?page=' + page + '&pageSize=10');
    const items = data.items || [];
    const rows = items.map(function (f) {
      return '<tr><td>' + esc(f.bookName) + '</td><td>' + esc(f.author) + '</td>' +
        '<td class="actions"><button onclick="removeFavorite(' + f.bookId + ', ' + page + ')">取消收藏</button></td></tr>';
    }).join('');
    $('#fav-list').innerHTML =
      '<table class="table"><thead><tr><th>书名</th><th>作者</th><th>操作</th></tr></thead>' +
      '<tbody>' + (rows || '<tr><td colspan="3" class="empty">暂无收藏</td></tr>') + '</tbody></table>' +
      pagerHtml(data, 'renderFavorites');
  } catch (e) { toast(e.message, 'error'); }
}

async function removeFavorite(bookId, page) {
  try {
    await api('DELETE', '/api/favorites/' + bookId);
    toast('已取消收藏', 'success');
    myFavoriteBookIds.delete(bookId);
    renderFavorites(page);
  } catch (e) { toast(e.message, 'error'); }
}

/* ===== 我的借阅 ===== */
async function renderMyBorrows() {
  $('#content').innerHTML =
    '<h2>我的借阅</h2>' +
    '<div class="toolbar"><select id="borrow-status" onchange="myBorrowsStatus=this.value;loadMyBorrows(1)">' +
    '<option value="">全部状态</option>' +
    '<option value="BORROWED">在借</option>' +
    '<option value="OVERDUE">逾期</option>' +
    '<option value="RETURNED">已还</option>' +
    '<option value="LOST">丢失</option>' +
    '</select></div>' +
    '<div id="borrow-list"></div>';
  $('#borrow-status').value = myBorrowsStatus;
  loadMyBorrows(1);
}

async function loadMyBorrows(page) {
  page = page || 1;
  const qs = new URLSearchParams({ page: page, pageSize: 10 });
  if (myBorrowsStatus) qs.set('status', myBorrowsStatus);
  try {
    const data = await api('GET', '/api/borrows/my?' + qs.toString());
    $('#borrow-list').innerHTML = borrowTableHtml(data, false, 'loadMyBorrows');
  } catch (e) { toast(e.message, 'error'); }
}

function borrowTableHtml(data, admin, pagerFn) {
  const items = data.items || [];
  const rows = items.map(function (r) {
    const canReturn = admin && (r.status === 'BORROWED' || r.status === 'OVERDUE');
    return '<tr>' +
      '<td>' + r.borrowId + '</td>' +
      (admin ? '<td>' + esc(r.userName || '-') + '</td>' : '') +
      '<td>' + esc(r.bookName || '-') + '</td>' +
      '<td>' + fmtTime(r.borrowDate) + '</td>' +
      '<td>' + fmtTime(r.expectedDueDate) + '</td>' +
      '<td>' + fmtTime(r.returnedAt) + '</td>' +
      '<td><span class="status-badge ' + (BORROW_STATUS_CLASS[r.status] || '') + '">' + (BORROW_STATUS_LABEL[r.status] || r.status) + '</span></td>' +
      (admin ? '<td class="actions">' + (canReturn ? '<button onclick="returnBook(' + r.borrowId + ')">归还</button>' : '') + '</td>' : '') +
      '</tr>';
  }).join('');
  const cols = admin ? 8 : 6;
  const head = '<thead><tr><th>借阅ID</th>' + (admin ? '<th>用户名</th>' : '') +
    '<th>书名</th><th>借出</th><th>应还</th><th>实际归还</th><th>状态</th>' +
    (admin ? '<th>操作</th>' : '') + '</tr></thead>';
  return '<table class="table">' + head +
    '<tbody>' + (rows || '<tr><td colspan="' + cols + '" class="empty">暂无记录</td></tr>') + '</tbody></table>' +
    pagerHtml(data, pagerFn);
}

/* ===== 通知 ===== */
async function renderNotifications(page) {
  page = page || 1;
  $('#content').innerHTML =
    '<h2>我的通知</h2>' +
    '<div class="toolbar">' +
    (state.role === 'ADMIN' ? '<button class="primary" onclick="openBroadcast()">+ 发布公告</button>' : '') +
    '</div>' +
    '<div id="notif-list"></div>';
  try {
    const data = await api('GET', '/api/notifications?page=' + page + '&pageSize=10');
    const items = data.items || [];
    const rows = items.map(function (n) {
      const isBroadcast = n.broadcast === true;
      return '<tr>' +
        '<td>' + esc(NOTIFICATION_TYPE_LABEL[n.type] || n.type) + (isBroadcast ? ' <span class="tag">公告</span>' : '') + '</td>' +
        '<td>' + esc(n.content) + '</td>' +
        '<td>' + fmtTime(n.createdAt) + '</td>' +
        '<td>' + (isBroadcast ? '-' : (n.readAt ? '已读' : '未读')) + '</td>' +
        '<td class="actions">' + ((!isBroadcast && !n.readAt) ? '<button onclick="markRead(' + n.id + ', ' + page + ')">标记已读</button>' : '') + '</td></tr>';
    }).join('');
    $('#notif-list').innerHTML =
      '<table class="table"><thead><tr><th>类型</th><th>内容</th><th>时间</th><th>状态</th><th>操作</th></tr></thead>' +
      '<tbody>' + (rows || '<tr><td colspan="5" class="empty">暂无通知</td></tr>') + '</tbody></table>' +
      pagerHtml(data, 'renderNotifications');
  } catch (e) { toast(e.message, 'error'); }
}

async function markRead(id, page) {
  try {
    await api('POST', '/api/notifications/' + id + '/read');
    toast('已标记为已读', 'success');
    renderNotifications(page);
  } catch (e) { toast(e.message, 'error'); }
}

function openBroadcast() {
  openModal(
    '<h3>发布系统公告</h3>' +
    '<div class="form"><label>公告内容<textarea id="bc-content" rows="4" placeholder="请输入公告内容"></textarea></label></div>' +
    '<div class="modal-actions"><button class="primary" onclick="doBroadcast()">发布</button><button onclick="closeModal()">取消</button></div>'
  );
}

async function doBroadcast() {
  const content = $('#bc-content').value.trim();
  if (!content) return toast('公告内容不能为空', 'error');
  try {
    await api('POST', '/api/notifications/broadcast', { content: content });
    closeModal();
    toast('公告已发布', 'success');
    renderNotifications(1);
  } catch (e) { toast(e.message, 'error'); }
}

/* ===== 管理端：书籍 ===== */
async function renderAdminBooks() {
  $('#content').innerHTML =
    '<h2>书籍管理</h2>' +
    '<div class="toolbar">' +
    '  <input id="admin-book-keyword" placeholder="书名关键词">' +
    '  <button class="primary" onclick="loadAdminBooks(1)">查询</button>' +
    '  <button onclick="openBookForm()">+ 新增书籍</button>' +
    '</div>' +
    '<div id="admin-book-list"></div>';
  loadAdminBooks(1);
}

async function loadAdminBooks(page) {
  page = page || 1;
  const keyword = $('#admin-book-keyword') ? $('#admin-book-keyword').value.trim() : '';
  const qs = new URLSearchParams({ page: page, pageSize: 10 });
  if (keyword) qs.set('keyword', keyword);
  try {
    const data = await api('GET', '/api/books/all?' + qs.toString());
    const items = data.items || [];
    const rows = items.map(function (b) {
      return '<tr>' +
        '<td>' + b.id + '</td>' +
        '<td>' + coverThumbHtml(b.coverUrl) + '</td>' +
        '<td>' + esc(b.name) + '</td>' +
        '<td>' + esc(b.author) + '</td>' +
        '<td>' + (b.availableCount || 0) + ' / ' + (b.totalCount || 0) + '</td>' +
        '<td>' + (b.categoryId || '') + '</td>' +
        '<td>' + (b.enabled ? '上架' : '下架') + '</td>' +
        '<td class="actions">' +
        '  <button onclick="openBookForm(' + b.id + ')">编辑</button>' +
        '  <button class="' + (b.enabled ? 'btn-disable' : 'btn-enable') + '" onclick="toggleBookEnabled(' + b.id + ', ' + (!b.enabled) + ')">' + (b.enabled ? '下架' : '上架') + '</button>' +
        '</td></tr>';
    }).join('');
    $('#admin-book-list').innerHTML =
      '<table class="table"><thead><tr><th>ID</th><th>封面</th><th>书名</th><th>作者</th><th>可借/馆藏</th><th>分类ID</th><th>状态</th><th>操作</th></tr></thead>' +
      '<tbody>' + (rows || '<tr><td colspan="8" class="empty">暂无图书</td></tr>') + '</tbody></table>' +
      pagerHtml(data, 'loadAdminBooks');
  } catch (e) { toast(e.message, 'error'); }
}

async function openBookForm(id) {
  const cats = await loadCategories(true);
  const isEdit = (id !== undefined && id !== null);
  let b = {};
  if (isEdit) {
    try { b = await api('GET', '/api/books/' + id); } catch (e) { toast(e.message, 'error'); return; }
  } else if (cats.length) {
    b.categoryId = cats[0].id;
  }
  const catOptions = cats.map(function (c) {
    return '<option value="' + c.id + '"' + (c.id === b.categoryId ? ' selected' : '') + '>' + esc(c.name) + (c.enabled ? '' : '（停用）') + '</option>';
  }).join('');
  openModal(
    '<h3>' + (isEdit ? '编辑书籍' : '新增书籍') + '</h3>' +
    '<div class="form">' +
    (isEdit ? '' : '<label>ISBN<input id="bf-isbn" value="' + esc(b.isbn || '') + '"></label>') +
    '<label>书名<input id="bf-name" value="' + esc(b.name || '') + '"></label>' +
    '<label>作者<input id="bf-author" value="' + esc(b.author || '') + '"></label>' +
    '<label>出版社<input id="bf-publisher" value="' + esc(b.publisher || '') + '"></label>' +
    '<label>出版年份<input id="bf-year" type="number" value="' + (b.publishYear || '') + '"></label>' +
    '<label>封面URL<input id="bf-cover" value="' + esc(b.coverUrl || '') + '" oninput="showCoverPreview(this.value)"></label>' +
    '<div class="cover-actions"><button type="button" id="cover-upload-btn" onclick="pickCover()">上传封面</button>' +
    '<input type="file" id="cover-file" accept="image/*" style="display:none" onchange="handleCoverPick(this)"></div>' +
    '<div id="cover-preview" class="cover-preview">' + (b.coverUrl ? '<img src="' + esc(b.coverUrl) + '">' : '') + '</div>' +
    '<label>分类<select id="bf-cat">' + catOptions + '</select></label>' +
    '<label>简介<textarea id="bf-desc" rows="3">' + esc(b.description || '') + '</textarea></label>' +
    (isEdit ? '' : '<label>馆藏数量<input id="bf-count" type="number" value="1"></label>') +
    '</div>' +
    '<div class="modal-actions"><button class="primary" onclick="saveBook(' + (isEdit ? id : 'null') + ')">保存</button><button onclick="closeModal()">取消</button></div>'
  );
}

async function saveBook(id) {
  const payload = {
    name: $('#bf-name').value.trim(),
    author: $('#bf-author').value.trim(),
    publisher: $('#bf-publisher').value.trim() || null,
    publishYear: $('#bf-year').value ? parseInt($('#bf-year').value, 10) : null,
    description: $('#bf-desc').value.trim() || null,
    coverUrl: $('#bf-cover').value.trim() || null,
    categoryId: parseInt($('#bf-cat').value, 10),
  };
  try {
    if (id === null) {
      payload.isbn = $('#bf-isbn').value.trim();
      payload.totalCount = parseInt($('#bf-count').value, 10);
      await api('POST', '/api/books', payload);
    } else {
      await api('PUT', '/api/books/' + id, payload);
    }
    closeModal();
    toast('保存成功', 'success');
    renderAdminBooks();
  } catch (e) { toast(e.message, 'error'); }
}

async function toggleBookEnabled(id, enabled) {
  try {
    await api('PATCH', '/api/books/' + id + '/enabled', { enabled: enabled });
    toast(enabled ? '已上架' : '已下架', 'success');
    renderAdminBooks();
  } catch (e) { toast(e.message, 'error'); }
}

/* ===== 管理端：分类 ===== */
let adminCats = [];

async function renderAdminCategories() {
  $('#content').innerHTML =
    '<h2>分类管理</h2>' +
    '<div class="toolbar"><button class="primary" onclick="openCategoryForm()">+ 新增分类</button></div>' +
    '<div id="admin-cat-list"></div>';
  loadAdminCategories();
}

async function loadAdminCategories() {
  try {
    adminCats = await api('GET', '/api/categories/all');
    const rows = adminCats.map(function (c) {
      return '<tr>' +
        '<td>' + c.id + '</td>' +
        '<td>' + esc(c.name) + '</td>' +
        '<td>' + (c.enabled ? '启用' : '停用') + '</td>' +
        '<td class="actions">' +
        '  <button onclick="openCategoryForm(' + c.id + ')">编辑</button>' +
        '  <button class="' + (c.enabled ? 'btn-disable' : 'btn-enable') + '" onclick="toggleCategoryEnabled(' + c.id + ', ' + (!c.enabled) + ')">' + (c.enabled ? '停用' : '启用') + '</button>' +
        '</td></tr>';
    }).join('');
    $('#admin-cat-list').innerHTML =
      '<table class="table"><thead><tr><th>ID</th><th>名称</th><th>状态</th><th>操作</th></tr></thead>' +
      '<tbody>' + (rows || '<tr><td colspan="4" class="empty">暂无分类</td></tr>') + '</tbody></table>';
  } catch (e) { toast(e.message, 'error'); }
}

function openCategoryForm(id) {
  const c = (id !== undefined && id !== null) ? adminCats.find(function (x) { return x.id === id; }) : null;
  const isEdit = c != null;
  openModal(
    '<h3>' + (isEdit ? '编辑分类' : '新增分类') + '</h3>' +
    '<div class="form"><label>名称<input id="cf-name" value="' + esc(c ? c.name : '') + '"></label></div>' +
    '<div class="modal-actions"><button class="primary" onclick="saveCategory(' + (isEdit ? id : 'null') + ')">保存</button><button onclick="closeModal()">取消</button></div>'
  );
}

async function saveCategory(id) {
  const name = $('#cf-name').value.trim();
  if (!name) return toast('名称不能为空', 'error');
  try {
    if (id === null) await api('POST', '/api/categories', { name: name });
    else await api('PUT', '/api/categories/' + id, { name: name });
    closeModal();
    toast('保存成功', 'success');
    renderAdminCategories();
  } catch (e) { toast(e.message, 'error'); }
}

async function toggleCategoryEnabled(id, enabled) {
  try {
    await api('PATCH', '/api/categories/' + id + '/enabled', { enabled: enabled });
    toast(enabled ? '已启用' : '已停用', 'success');
    renderAdminCategories();
  } catch (e) { toast(e.message, 'error'); }
}

/* ===== 管理端：用户 ===== */
async function renderAdminUsers() {
  $('#content').innerHTML = '<h2>用户管理</h2><div id="admin-user-list"></div>';
  loadAdminUsers(1);
}

async function loadAdminUsers(page) {
  page = page || 1;
  try {
    const data = await api('GET', '/api/users?page=' + page + '&pageSize=10');
    const items = data.items || [];
    const rows = items.map(function (u) {
      const active = u.status === 'ACTIVE';
      return '<tr>' +
        '<td>' + u.id + '</td>' +
        '<td>' + esc(u.userName) + '</td>' +
        '<td>' + (ROLE_LABEL[u.role] || u.role) + '</td>' +
        '<td>' + (active ? '正常' : '禁用') + '</td>' +
        '<td class="actions"><button onclick="toggleUserStatus(' + u.id + ', ' + (!active) + ')">' + (active ? '禁用' : '启用') + '</button></td></tr>';
    }).join('');
    $('#admin-user-list').innerHTML =
      '<table class="table"><thead><tr><th>ID</th><th>用户名</th><th>角色</th><th>状态</th><th>操作</th></tr></thead>' +
      '<tbody>' + (rows || '<tr><td colspan="5" class="empty">暂无用户</td></tr>') + '</tbody></table>' +
      pagerHtml(data, 'loadAdminUsers');
  } catch (e) { toast(e.message, 'error'); }
}

async function toggleUserStatus(id, active) {
  try {
    await api('PATCH', '/api/users/' + id + '/status', { active: active });
    toast('已更新', 'success');
    loadAdminUsers(1);
  } catch (e) { toast(e.message, 'error'); }
}

/* ===== 管理端：借阅 ===== */
async function renderAdminBorrows() {
  $('#content').innerHTML =
    '<h2>借阅管理（全部）</h2>' +
    '<div class="toolbar">' +
    '  <input id="borrow-user-name" placeholder="按用户名查询">' +
    '  <button class="primary" onclick="loadAdminBorrows(1)">查询</button>' +
    '</div>' +
    '<div id="admin-borrow-list"></div>';
  loadAdminBorrows(1);
}

async function loadAdminBorrows(page) {
  page = page || 1;
  const userName = $('#borrow-user-name') ? $('#borrow-user-name').value.trim() : '';
  const qs = new URLSearchParams({ page: page, pageSize: 10 });
  if (userName) qs.set('userName', userName);
  try {
    const data = await api('GET', '/api/borrows?' + qs.toString());
    $('#admin-borrow-list').innerHTML = borrowTableHtml(data, true, 'loadAdminBorrows');
  } catch (e) { toast(e.message, 'error'); }
}

async function returnBook(borrowId) {
  try {
    await api('POST', '/api/borrows/' + borrowId + '/return');
    toast('已归还', 'success');
    loadAdminBorrows(1);
  } catch (e) { toast(e.message, 'error'); }
}

/* ===== 页面注册表 ===== */
const PAGES = {
  books: renderBooks,
  myBorrows: renderMyBorrows,
  favorites: renderFavorites,
  notifications: renderNotifications,
  adminBooks: renderAdminBooks,
  adminCategories: renderAdminCategories,
  adminUsers: renderAdminUsers,
  adminBorrows: renderAdminBorrows,
};

let currentPage = 'books';

/* ===== 启动 ===== */
(function init() {
  if (state.token) {
    enterApp();
  } else {
    $('#login-view').style.display = 'flex';
  }
})();
