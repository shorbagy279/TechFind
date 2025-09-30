import axios from 'axios';


// Base points to backend root (without trailing /api because we include it here)
const API_HOST = (import.meta.env.VITE_API_URL || 'http://localhost:8080');
const client = axios.create({ baseURL: API_HOST + '/api' });


client.interceptors.request.use(cfg => {
const token = localStorage.getItem('token');
if (token) {
cfg.headers = cfg.headers ?? {};
cfg.headers.Authorization = `Bearer ${token}`;
}
return cfg;
});


export default client;