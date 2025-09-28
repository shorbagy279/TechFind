import { useQuery } from '@tanstack/react-query';
import client from '../../api/client';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';

export default function Search(){
  const { t, i18n } = useTranslation();
  const [gov, setGov] = useState('Cairo');
  const [city, setCity] = useState('Maadi');
  const [field, setField] = useState<string|undefined>();
  const fields = useQuery({ queryKey:['fields'], queryFn: async()=> (await client.get('/public/fields')).data });
  const search = useQuery({ queryKey:['search',gov,city,field], queryFn: async()=> (await client.get('/public/technicians',{ params:{ governorate:gov, city, field } })).data });

  return (
    <div style={{padding:20, direction: i18n.language==='ar'?'rtl':'ltr'}}>
      <h2>{t('search')}</h2>
      <div style={{display:'grid', gap:8, gridTemplateColumns:'repeat(4, 1fr)'}}>
        <input value={gov} onChange={e=>setGov(e.target.value)} placeholder={t('governorate')} />
        <input value={city} onChange={e=>setCity(e.target.value)} placeholder={t('city')} />
        <select value={field||''} onChange={e=>setField(e.target.value||undefined)}>
          <option value="">{t('field')}</option>
          {(fields.data||[]).map((f:any)=><option key={f.id} value={f.name}>{f.name}</option>)}
        </select>
        <button onClick={()=>search.refetch()}>{t('search')}</button>
      </div>
      <div style={{marginTop:16}}>
        {(search.data||[]).length===0 ? <p>{t('no_results')}</p> :
          (search.data||[]).map((tch:any)=>(
            <div key={tch.id} style={{border:'1px solid #ddd', padding:8, marginBottom:8}}>
              <b>{tch.fullName}</b> — {tch.summary} — {tch.region?.governorate} / {tch.region?.city}
            </div>
          ))
        }
      </div>
    </div>
  )
}
