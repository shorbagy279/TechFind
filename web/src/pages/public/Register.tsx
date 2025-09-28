import { useState } from 'react';
import client from '../../api/client';
import { auth } from '../../store/auth';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export default function Register(){
  const { t, i18n } = useTranslation();
  const [email,setEmail]=useState('');
  const [password,setPassword]=useState('');
  const [fullName,setFullName]=useState('');
  const [phone,setPhone]=useState('');
  const nav = useNavigate();
  const submit = async () => {
    const { data } = await client.post('/auth/register',{ email,password,fullName,phone, locale: i18n.language });
    auth.set({ token:data.token, role:data.role as any, fullName:data.fullName });
    nav('/user');
  };
  return (
    <div style={{padding:20, direction: i18n.language==='ar'?'rtl':'ltr'}}>
      <h2>{t('register')}</h2>
      <input value={fullName} onChange={e=>setFullName(e.target.value)} placeholder="Full name" />
      <input value={email} onChange={e=>setEmail(e.target.value)} placeholder="Email" />
      <input type="password" value={password} onChange={e=>setPassword(e.target.value)} placeholder="Password" />
      <input value={phone} onChange={e=>setPhone(e.target.value)} placeholder="Phone" />
      <button onClick={submit}>{t('register')}</button>
    </div>
  );
}
