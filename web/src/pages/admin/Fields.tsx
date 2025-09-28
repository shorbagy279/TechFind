import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import client from '../../api/client';
export default function Fields(){
  const qc = useQueryClient();
  const fields = useQuery({ queryKey:['fields'], queryFn: async()=> (await client.get('/public/fields')).data });
  const create = useMutation({ mutationFn: async (payload:any)=> (await client.post('/admin/fields', payload)).data, onSuccess:()=>qc.invalidateQueries({queryKey:['fields']}) });
  return (
    <div style={{padding:20}}>
      <h2>Fields</h2>
      <button onClick={()=>create.mutate({ name:'Electrical' })}>Add "Electrical"</button>
      <ul>{(fields.data||[]).map((f:any)=><li key={f.id}>{f.name}</li>)}</ul>
    </div>
  )
}
