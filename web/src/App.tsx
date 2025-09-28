import { BrowserRouter as Router, Routes, Route, Link, useLocation } from 'react-router-dom';
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
  createTheme
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
  ExitToApp
} from '@mui/icons-material';

// Pages
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

// Theme
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
    { text: 'home', icon: <Home />, path: '/' },
    { text: 'search', icon: <Search />, path: '/search' },
    ...(userAuth.token ? [
      ...(userAuth.role === 'ROLE_ADMIN' ? [
        { text: 'admin', icon: <AdminPanelSettings />, path: '/admin' },
        { text: 'fields', icon: <Build />, path: '/admin/fields' },
        { text: 'regions', icon: <LocationOn />, path: '/admin/regions' },
      ] : [
        { text: 'dashboard', icon: <Dashboard />, path: '/user' },
      ])
    ] : [
      { text: 'login', icon: <Login />, path: '/login' },
      { text: 'register', icon: <PersonAdd />, path: '/register' },
    ])
  ];

  return (
    <Box sx={{ flexGrow: 1, direction: i18n.language === 'ar' ? 'rtl' : 'ltr' }}>
      <AppBar position="static">
        <Toolbar>
          {isMobile && (
            <IconButton
              edge="start"
              color="inherit"
              aria-label="menu"
              onClick={() => setDrawerOpen(true)}
            >
              <MenuIcon />
            </IconButton>
          )}
          
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            <Link to="/" style={{ color: 'inherit', textDecoration: 'none' }}>
              {t('title')}
            </Link>
          </Typography>

          {!isMobile && (
            <Box sx={{ display: 'flex', gap: 2 }}>
              <Button color="inherit" component={Link} to="/search">
                {t('search')}
              </Button>
              {!userAuth.token ? (
                <>
                  <Button color="inherit" component={Link} to="/login">
                    {t('login')}
                  </Button>
                  <Button color="inherit" component={Link} to="/register">
                    {t('register')}
                  </Button>
                </>
              ) : (
                <>
                  {userAuth.role === 'ROLE_ADMIN' ? (
                    <Button color="inherit" component={Link} to="/admin">
                      {t('admin')}
                    </Button>
                  ) : (
                    <Button color="inherit" component={Link} to="/user">
                      Dashboard
                    </Button>
                  )}
                  <Button color="inherit" onClick={handleLogout}>
                    {t('logout')}
                  </Button>
                </>
              )}
            </Box>
          )}

          <IconButton color="inherit" onClick={handleLanguageMenu}>
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
            <IconButton color="inherit" sx={{ ml: 1 }}>
              <Avatar sx={{ width: 32, height: 32 }}>
                {userAuth.fullName?.charAt(0)}
              </Avatar>
            </IconButton>
          )}
        </Toolbar>
      </AppBar>

      <Drawer
        anchor={i18n.language === 'ar' ? 'right' : 'left'}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
      >
        <Box sx={{ width: 250 }}>
          <List>
            {drawerItems.map((item) => (
              <ListItem
                key={item.text}
                component={Link}
                to={item.path}
                onClick={() => setDrawerOpen(false)}
                sx={{ color: 'inherit', textDecoration: 'none' }}
              >
                <ListItemIcon>{item.icon}</ListItemIcon>
                <ListItemText primary={t(item.text)} />
              </ListItem>
            ))}
            {userAuth.token && (
              <ListItem onClick={handleLogout}>
                <ListItemIcon><ExitToApp /></ListItemIcon>
                <ListItemText primary={t('logout')} />
              </ListItem>
            )}
          </List>
        </Box>
      </Drawer>

      <Container maxWidth="lg" sx={{ mt: 3, mb: 3 }}>
        {children}
      </Container>
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
    return <LoginPage />;
  }
  
  if (requiredRole && userAuth.role !== requiredRole) {
    return <div>Access Denied</div>;
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