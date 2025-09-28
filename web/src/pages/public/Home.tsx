import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
export default function Home(){
  const { t, i18n } = useTranslation();
  const toggle = () => i18n.changeLanguage(i18n.language==='en'?'ar':'en');
  return (
    <div style={{padding:20, direction: i18n.language==='ar'?'rtl':'ltr'}}>
      <h1>{t('title')}</h1>
      <button onClick={toggle}>{i18n.language==='en'?'AR':'EN'}</button>
      <nav style={{gap:10, display:'flex', marginTop:10}}>
        <Link to="/search">{t('search')}</Link>
        <Link to="/login">{t('login')}</Link>
        <Link to="/register">{t('register')}</Link>
      </nav>
    </div>
  );
}
