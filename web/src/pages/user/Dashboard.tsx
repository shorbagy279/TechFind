import { useTranslation } from 'react-i18next';
import client from '../../api/client';
export default function Dashboard(){
  const { t, i18n } = useTranslation();
  const pay = async () => {
    const { data } = await client.post('/user/payments/create-session', { amount: 100, currency: 'EGP' });
    window.location.href = data.sessionUrl;
  };
  return (
    <div style={{padding:20, direction: i18n.language==='ar'?'rtl':'ltr'}}>
      <h2>{t('user')} Dashboard</h2>
      <button onClick={pay}>{t('pay')}</button>
    </div>
  )
}
