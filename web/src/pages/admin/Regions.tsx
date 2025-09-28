import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import client from '../../api/client';
export default function Regions(){
  const qc = useQueryClient();
  const regions = useQuery({ queryKey:['regions'], queryFn: async()=> (await client.get('/public/regions')).data });
  const create = useMutation({ mutationFn: async (payload:any)=> (await client.post('/admin/regions', payload)).data, onSuccess:()=>qc.invalidateQueries({queryKey:['regions']}) });
  return (
    <div style={{padding:20}}>
      <h2>Regions</h2>
      <button onClick={()=>create.mutate({ governorate:'Cairo', city:'Maadi' })}>Add Cairo/Maadi</button>
      <ul>{(regions.data||[]).map((r:any)=><li key={r.id}>{r.governorate} / {r.city}</li>)}</ul>
    </div>
  )
}
