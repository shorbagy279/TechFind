import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
  Divider,
  Container
} from '@mui/material';
import { Login as LoginIcon, PersonAdd } from '@mui/icons-material';
import client from '../../api/client';
import { auth } from '../../store/auth';

export default function Login() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const { data } = await client.post('/auth/login', { email, password });
      auth.set({ 
        token: data.token, 
        role: data.role as any, 
        fullName: data.fullName 
      });
      
      // Navigate based on role
      if (data.role === 'ROLE_ADMIN') {
        navigate('/admin');
      } else {
        navigate('/user');
      }
    } catch (err: any) {
      setError(
        err.response?.data?.message || 
        (i18n.language === 'ar' 
          ? 'البريد الإلكتروني أو كلمة المرور غير صحيحة'
          : 'Invalid email or password')
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Card elevation={3}>
          <CardContent sx={{ p: 4 }}>
            <Box sx={{ textAlign: 'center', mb: 3 }}>
              <LoginIcon sx={{ fontSize: 60, color: 'primary.main', mb: 2 }} />
              <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
                {t('login')}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {i18n.language === 'ar'
                  ? 'أدخل بياناتك للوصول إلى حسابك'
                  : 'Enter your credentials to access your account'}
              </Typography>
            </Box>

            {error && (
              <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError('')}>
                {error}
              </Alert>
            )}

            <form onSubmit={handleSubmit}>
              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'البريد الإلكتروني' : 'Email'}
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoFocus
                sx={{ mb: 2 }}
                disabled={loading}
              />

              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'كلمة المرور' : 'Password'}
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                sx={{ mb: 3 }}
                disabled={loading}
              />

              <Button
                fullWidth
                type="submit"
                variant="contained"
                size="large"
                disabled={loading}
                startIcon={loading ? <CircularProgress size={20} /> : <LoginIcon />}
              >
                {loading 
                  ? (i18n.language === 'ar' ? 'جاري تسجيل الدخول...' : 'Logging in...')
                  : t('login')}
              </Button>
            </form>

            <Divider sx={{ my: 3 }}>
              <Typography variant="body2" color="text.secondary">
                {i18n.language === 'ar' ? 'أو' : 'OR'}
              </Typography>
            </Divider>

            <Box sx={{ textAlign: 'center' }}>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                {i18n.language === 'ar' 
                  ? 'ليس لديك حساب؟'
                  : "Don't have an account?"}
              </Typography>
              <Button
                component={Link}
                to="/register"
                variant="outlined"
                startIcon={<PersonAdd />}
                fullWidth
              >
                {t('register')}
              </Button>
            </Box>

            {/* Demo credentials hint */}
            <Box sx={{ mt: 3, p: 2, bgcolor: 'info.lighter', borderRadius: 1 }}>
              <Typography variant="caption" display="block" color="info.dark">
                <strong>{i18n.language === 'ar' ? 'تجريبي:' : 'Demo:'}</strong>
              </Typography>
              <Typography variant="caption" display="block" color="info.dark">
                Admin: admin@example.com / password
              </Typography>
            </Box>
          </CardContent>
        </Card>
      </Box>
    </Container>
  );
}