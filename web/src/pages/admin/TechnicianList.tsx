import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import client from '../../api/client';
import { useTranslation } from 'react-i18next';

export default function TechnicianList(){
  const { t, i18n } = useTranslation();
  const qc = useQueryClient();
  const regions = useQuery({ queryKey:['regions'], queryFn: async()=> (await client.get('/public/regions')).data });
  const fields = useQuery({ queryKey:['fields'], queryFn: async()=> (await client.get('/public/fields')).data });
  const techs = useQuery({ queryKey:['admin-techs'], queryFn: async()=> (await client.get('/public/technicians',{ params:{ governorate:'', city:'' } })).data });
  const create = useMutation({ mutationFn: async (payload:any)=> (await client.post('/admin/technicians', payload)).data, onSuccess:()=>qc.invalidateQueries({queryKey:['admin-techs']}) });

  return (
    <div style={{padding:20, direction: i18n.language==='ar'?'rtl':'ltr'}}>
      <h2>{t('admin')} - Technicians</h2>
      <div style={{marginBottom:16}}>
        <button onClick={()=>create.mutate({ fullName:'New Tech', phone:'', email:'', summary:'', region: regions.data?.[0], fields:[fields.data?.[0]], active:true })}>Add</button>
      </div>
      {(techs.data||[]).map((t:any)=>(
        <div key={t.id} style={{border:'1px solid #ddd', padding:8, marginBottom:8}}>
          <b>{t.fullName}</b> — {t.region?.governorate} / {t.region?.city}
        </div>
      ))}
    </div>
  )
}
