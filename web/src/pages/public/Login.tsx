import { useState } from 'react';
import client from '../../api/client';
import { auth } from '../../store/auth';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export default function Login(){
  const { t, i18n } = useTranslation();
  const [email,setEmail]=useState('admin@example.com');
  const [password,setPassword]=useState('password');
  const nav = useNavigate();
  const submit = async () => {
    const { data } = await client.post('/auth/login',{ email,password });
    auth.set({ token:data.token, role: data.role as any, fullName: data.fullName });
    nav(data.role==='ROLE_ADMIN'?'/admin':'/user');
  };
  return (
    <div style={{padding:20, direction: i18n.language==='ar'?'rtl':'ltr'}}>
      <h2>{t('login')}</h2>
      <input value={email} onChange={e=>setEmail(e.target.value)} placeholder="email" />
      <input type="password" value={password} onChange={e=>setPassword(e.target.value)} placeholder="password" />
      <button onClick={submit}>{t('login')}</button>
    </div>
  );
}
