const $=s=>document.querySelector(s),esc=s=>String(s??'').replace(/[&<>"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c]));
const api=async(p,o)=>{const r=await fetch('/api/'+p,o),j=await r.json();if(j.error)throw new Error(j.error);return j};
const post=(p,d)=>api(p,{method:'POST',body:d instanceof FormData?d:new URLSearchParams(d)});
function toast(t){const e=$('#toast');e.textContent=t;e.hidden=false;setTimeout(()=>e.hidden=true,3000)}
function show(id){document.querySelectorAll('main,section').forEach(s=>s.hidden=s.id!==id)}
const badge=s=>`<span class="badge ${s}">${s.replace('_',' ')}</span>`;
const form=(f)=>Object.fromEntries(new FormData(f));
let me={};
async function boot(){
  me=await api('me');$('#bLogin').hidden=!!me.id;$('#bOut').hidden=!me.id;
  $('#who').textContent=me.id?`Welcome, ${me.name} (${me.role})`:'Digital Seva, Sabke Liye';
  if(me.role==='ADMIN'){show('admin');loadAdmin()}else if(me.id){show('user');loadUser()}else{show('home');pub()}
}
async function pub(){const s=await api('services');$('#pubServices').innerHTML=s.map(x=>`<div class="card"><b>${esc(x.name)}</b><p>${esc(x.description)}</p><button onclick="show('auth')">Apply Now</button></div>`).join('')}
async function loadUser(no){
  const s=await api('services');
  $('#svc').innerHTML=s.map(x=>`<div class="card"><small>${esc(x.category)}</small><br><b>${esc(x.name)}</b><p>${esc(x.description)}</p><button onclick="openApply(${x.id},'${esc(x.name)}')">Apply Now</button></div>`).join('');
  const a=no?await api('track?no='+encodeURIComponent(no)):await api('myapps');
  $('#mine').innerHTML=a.length?a.map(x=>`<tr><td>${x.app_no}</td><td>${esc(x.service)}</td><td>${x.created_at}</td><td>${badge(x.status)}</td><td>${esc(x.remarks)}</td></tr>`).join(''):'<tr><td colspan=5>Abhi koi application nahi hai. Upar se service chunein.</td></tr>';
}
function openApply(id,n){$('#dT').textContent=n;$('#fApply').serviceId.value=id;$('#dlg').showModal()}
async function loadAdmin(){
  const s=await api('admin/stats');
  $('#stats').innerHTML=[['s1','Total Applications',s.total],['s2','Pending',s.pending],['s3','Approved',s.approved],['s4','Rejected',s.rejected],['s5','Customers',s.users]].map(x=>`<div class="stat ${x[0]}">${x[1]}<b>${x[2]}</b></div>`).join('');
  const a=await api('admin/apps');
  $('#apps').innerHTML=a.map(x=>`<tr><td>${x.app_no}<br><small>${x.created_at}</small></td><td>${esc(x.customer)}<br><small>${esc(x.mobile)}</small></td><td>${esc(x.service)}</td><td>${esc(x.details)}</td><td>${x.file_name?'📎':'-'}</td><td>${badge(x.status)}</td><td><button onclick="setSt(${x.id},'APPROVED')">Approve</button> <button class="ghost" onclick="setSt(${x.id},'REJECTED')">Reject</button> <button class="ghost" onclick="setSt(${x.id},'IN_PROGRESS')">In progress</button></td></tr>`).join('')||'<tr><td colspan=7>Abhi koi application nahi aayi.</td></tr>';
}
async function setSt(id,status){const remarks=prompt('Remarks (optional)')||'';await post('admin/status',{id,status,remarks});toast('Status updated');loadAdmin()}
$('#bHome').onclick=boot;$('#bLogin').onclick=()=>show('auth');$('#bOut').onclick=async()=>{await post('logout',{});boot()};
$('#fLogin').onsubmit=async e=>{e.preventDefault();try{await post('login',form(e.target));boot()}catch(x){toast(x.message)}};
$('#fReg').onsubmit=async e=>{e.preventDefault();try{await post('register',form(e.target));toast('Account ban gaya, ab login karein');e.target.reset()}catch(x){toast(x.message)}};
$('#fApply').onsubmit=async e=>{e.preventDefault();try{const r=await post('apply',new FormData(e.target));$('#dlg').close();e.target.reset();toast('Submit hua! ID: '+r.appNo);loadUser()}catch(x){toast(x.message)}};
boot();
