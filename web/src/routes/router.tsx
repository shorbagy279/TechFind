import { createBrowserRouter, Navigate } from 'react-router-dom';
import type { ReactNode } from 'react';

import Home from '../pages/public/Home';
import Search from '../pages/public/Search';
import Login from '../pages/public/Login';
import Register from '../pages/public/Register';
import TechnicianProfile from '../pages/public/TechnicianProfile';
import AdminDashboard from '../pages/admin/TechnicianList';
import AdminFields from '../pages/admin/Fields';
import AdminRegions from '../pages/admin/Regions';
import UserDashboard from '../pages/user/Dashboard';

const isAuthed = () => !!localStorage.getItem('token');
const role = () => (JSON.parse(localStorage.getItem('auth') || '{}').role);

const AdminGuard = ({ children }: { children: ReactNode }) =>
  role() === 'ROLE_ADMIN' ? children : <Navigate to="/" />;

const UserGuard = ({ children }: { children: ReactNode }) =>
  isAuthed() ? children : <Navigate to="/login" />;

export default createBrowserRouter([
  { path: '/', element: <Home /> },
  { path: '/search', element: <Search /> },
  { path: '/login', element: <Login /> },
  { path: '/register', element: <Register /> },
  { path: '/technicians/:id', element: <TechnicianProfile /> },
  { path: '/admin', element: <AdminGuard><AdminDashboard /></AdminGuard> },
  { path: '/admin/fields', element: <AdminGuard><AdminFields /></AdminGuard> },
  { path: '/admin/regions', element: <AdminGuard><AdminRegions /></AdminGuard> },
  { path: '/user', element: <UserGuard><UserDashboard /></UserGuard> }
]);
