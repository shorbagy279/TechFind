import { BrowserRouter as Router, Routes, Route, Link, useLocation, Navigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Box,
  Container,
  IconButton,
  Menu,
  MenuItem,
  Avatar,
  Drawer,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  useMediaQuery,
  useTheme,
  CssBaseline,
  ThemeProvider,
  createTheme,
  Divider
} from '@mui/material';
import {
  Menu as MenuIcon,
  Language,
  Login,
  PersonAdd,
  Search,
  Home,
  Dashboard,
  Build,
  LocationOn,
  AdminPanelSettings,
  ExitToApp,
  Person
} from '@mui/icons-material';

import HomePage from './pages/public/Home';
import SearchPage from './pages/public/Search';
import LoginPage from './pages/public/Login';
import RegisterPage from './pages/public/Register';
import TechnicianProfile from './pages/public/TechnicianProfile';
import AdminDashboard from './pages/admin/TechnicianList';
import AdminFields from './pages/admin/Fields';
import AdminRegions from './pages/admin/Regions';
import UserDashboard from './pages/user/Dashboard';
import { auth } from './store/auth';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Arial", sans-serif',
  },
});

const Layout = ({ children }: { children: React.ReactNode }) => {
  const { t, i18n } = useTranslation();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const location = useLocation();
  
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [userAuth, setUserAuth] = useState(auth.get());

  useEffect(() => {
    setUserAuth(auth.get());
  }, [location]);

  useEffect(() => {
    document.documentElement.dir = i18n.language === 'ar' ? 'rtl' : 'ltr';
    document.documentElement.lang = i18n.language;
  }, [i18n.language]);

  const handleLanguageMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleLanguageClose = () => {
    setAnchorEl(null);
  };

  const changeLanguage = (lng: string) => {
    i18n.changeLanguage(lng);
    handleLanguageClose();
  };

  const handleLogout = () => {
    auth.clear();
    setUserAuth({});
    window.location.href = '/';
  };

  const drawerItems = [
    { text: 'home', icon: <Home />, path: '/', public: true },
    { text: 'search', icon: <Search />, path: '/search', public: true },
    ...(userAuth.token ? [
      ...(userAuth.role === 'ROLE_ADMIN' ? [
        { text: 'admin', icon: <AdminPanelSettings />, path: '/admin', public: false },
        { text: 'fields', icon: <Build />, path: '/admin/fields', public: false },
        { text: 'regions', icon: <LocationOn />, path: '/admin/regions', public: false },
      ] : [
        { text: 'dashboard', icon: <Dashboard />, path: '/user', public: false },
      ])
    ] : [
      { text: 'login', icon: <Login />, path: '/login', public: true },
      { text: 'register', icon: <PersonAdd />, path: '/register', public: true },
    ])
  ];

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position="sticky" elevation={2}>
        <Toolbar>
          {isMobile && (
            <IconButton
              edge="start"
              color="inherit"
              aria-label="menu"
              onClick={() => setDrawerOpen(true)}
              sx={{ mr: 2 }}
            >
              <MenuIcon />
            </IconButton>
          )}
          
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            <Link to="/" style={{ color: 'inherit', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: 1 }}>
              <Build />
              {t('title')}
            </Link>
          </Typography>

          {!isMobile && (
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button color="inherit" component={Link} to="/search" startIcon={<Search />}>
                {t('search')}
              </Button>
              {!userAuth.token ? (
                <>
                  <Button color="inherit" component={Link} to="/login" startIcon={<Login />}>
                    {t('login')}
                  </Button>
                  <Button 
                    variant="outlined" 
                    component={Link} 
                    to="/register" 
                    startIcon={<PersonAdd />}
                    sx={{ 
                      color: 'white', 
                      borderColor: 'white',
                      '&:hover': { 
                        borderColor: 'white',
                        backgroundColor: 'rgba(255,255,255,0.1)'
                      }
                    }}
                  >
                    {t('register')}
                  </Button>
                </>
              ) : (
                <>
                  {userAuth.role === 'ROLE_ADMIN' ? (
                    <Button color="inherit" component={Link} to="/admin" startIcon={<AdminPanelSettings />}>
                      {t('admin')}
                    </Button>
                  ) : (
                    <Button color="inherit" component={Link} to="/user" startIcon={<Dashboard />}>
                      {t('dashboard')}
                    </Button>
                  )}
                </>
              )}
            </Box>
          )}

          <IconButton color="inherit" onClick={handleLanguageMenu} sx={{ ml: 1 }}>
            <Language />
          </IconButton>
          
          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleLanguageClose}
          >
            <MenuItem onClick={() => changeLanguage('en')}>English</MenuItem>
            <MenuItem onClick={() => changeLanguage('ar')}>العربية</MenuItem>
          </Menu>

          {userAuth.token && (
            <Box sx={{ display: 'flex', alignItems: 'center', ml: 2 }}>
              <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main', cursor: 'pointer' }}>
                {userAuth.fullName?.charAt(0) || <Person />}
              </Avatar>
              {!isMobile && (
                <IconButton color="inherit" onClick={handleLogout} sx={{ ml: 1 }}>
                  <ExitToApp />
                </IconButton>
              )}
            </Box>
          )}
        </Toolbar>
      </AppBar>

      <Drawer
        anchor={i18n.language === 'ar' ? 'right' : 'left'}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
      >
        <Box sx={{ width: 250, pt: 2 }}>
          <Box sx={{ px: 2, pb: 2 }}>
            <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Build />
              {t('title')}
            </Typography>
          </Box>
          <Divider />
          <List>
            {drawerItems.map((item) => (
              <ListItem
                key={item.text}
                component={Link}
                to={item.path}
                onClick={() => setDrawerOpen(false)}
                sx={{ 
                  color: 'inherit', 
                  textDecoration: 'none',
                  '&:hover': {
                    backgroundColor: 'action.hover'
                  }
                }}
              >
                <ListItemIcon>{item.icon}</ListItemIcon>
                <ListItemText primary={t(item.text)} />
              </ListItem>
            ))}
            {userAuth.token && (
              <>
                <Divider sx={{ my: 1 }} />
                <ListItem onClick={handleLogout} sx={{ cursor: 'pointer' }}>
                  <ListItemIcon><ExitToApp /></ListItemIcon>
                  <ListItemText primary={t('logout')} />
                </ListItem>
              </>
            )}
          </List>
        </Box>
      </Drawer>

      <Container maxWidth="xl" sx={{ mt: 4, mb: 4, minHeight: 'calc(100vh - 200px)' }}>
        {children}
      </Container>

      <Box 
        component="footer" 
        sx={{ 
          py: 3, 
          px: 2, 
          mt: 'auto',
          backgroundColor: 'background.paper',
          borderTop: '1px solid',
          borderColor: 'divider'
        }}
      >
        <Container maxWidth="xl">
          <Typography variant="body2" color="text.secondary" align="center">
            © 2025 {t('title')}. All rights reserved.
          </Typography>
        </Container>
      </Box>
    </Box>
  );
};

const ProtectedRoute = ({ 
  children, 
  requiredRole 
}: { 
  children: React.ReactNode; 
  requiredRole?: 'ROLE_ADMIN' | 'ROLE_USER' 
}) => {
  const userAuth = auth.get();
  
  if (!userAuth.token) {
    return <Navigate to="/login" />;
  }
  
  if (requiredRole && userAuth.role !== requiredRole) {
    return <Navigate to="/" />;
  }
  
  return <>{children}</>;
};

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Layout>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/search" element={<SearchPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/technicians/:id" element={<TechnicianProfile />} />
            
            <Route 
              path="/admin" 
              element={
                <ProtectedRoute requiredRole="ROLE_ADMIN">
                  <AdminDashboard />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/admin/fields" 
              element={
                <ProtectedRoute requiredRole="ROLE_ADMIN">
                  <AdminFields />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/admin/regions" 
              element={
                <ProtectedRoute requiredRole="ROLE_ADMIN">
                  <AdminRegions />
                </ProtectedRoute>
              } 
            />
            
            <Route 
              path="/user" 
              element={
                <ProtectedRoute>
                  <UserDashboard />
                </ProtectedRoute>
              } 
            />
          </Routes>
        </Layout>
      </Router>
    </ThemeProvider>
  );
}