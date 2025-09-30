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
  Container,
  Grid
} from '@mui/material';
import { PersonAdd, Login as LoginIcon } from '@mui/icons-material';
import client from '../../api/client';
import { auth } from '../../store/auth';

export default function Register() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    fullName: '',
    phone: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (field: string) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({ ...prev, [field]: e.target.value }));
  };

  const validateForm = () => {
    if (!formData.fullName.trim()) {
      return i18n.language === 'ar' ? 'الاسم مطلوب' : 'Full name is required';
    }
    if (!formData.email.trim()) {
      return i18n.language === 'ar' ? 'البريد الإلكتروني مطلوب' : 'Email is required';
    }
    if (formData.password.length < 6) {
      return i18n.language === 'ar' 
        ? 'كلمة المرور يجب أن تكون 6 أحرف على الأقل'
        : 'Password must be at least 6 characters';
    }
    if (formData.password !== formData.confirmPassword) {
      return i18n.language === 'ar' 
        ? 'كلمة المرور غير متطابقة'
        : 'Passwords do not match';
    }
    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const validationError = validateForm();
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);

    try {
      const { data } = await client.post('/auth/register', {
        email: formData.email,
        password: formData.password,
        fullName: formData.fullName,
        phone: formData.phone,
        locale: i18n.language
      });
      
      auth.set({ 
        token: data.token, 
        role: data.role as any, 
        fullName: data.fullName 
      });
      
      navigate('/user');
    } catch (err: any) {
      setError(
        err.response?.data?.message || 
        (i18n.language === 'ar' 
          ? 'حدث خطأ أثناء التسجيل'
          : 'An error occurred during registration')
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
              <PersonAdd sx={{ fontSize: 60, color: 'primary.main', mb: 2 }} />
              <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
                {t('register')}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {i18n.language === 'ar'
                  ? 'أنشئ حسابك للبدء'
                  : 'Create your account to get started'}
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
                label={i18n.language === 'ar' ? 'الاسم الكامل' : 'Full Name'}
                value={formData.fullName}
                onChange={handleChange('fullName')}
                required
                autoFocus
                sx={{ mb: 2 }}
                disabled={loading}
              />

              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'البريد الإلكتروني' : 'Email'}
                type="email"
                value={formData.email}
                onChange={handleChange('email')}
                required
                sx={{ mb: 2 }}
                disabled={loading}
              />

              <TextField
                fullWidth
                label={i18n.language === 'ar' ? 'رقم الهاتف' : 'Phone Number'}
                value={formData.phone}
                onChange={handleChange('phone')}
                sx={{ mb: 2 }}
                disabled={loading}
              />

              <Grid container spacing={2} sx={{ mb: 3 }}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    fullWidth
                    label={i18n.language === 'ar' ? 'كلمة المرور' : 'Password'}
                    type="password"
                    value={formData.password}
                    onChange={handleChange('password')}
                    required
                    disabled={loading}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    fullWidth
                    label={i18n.language === 'ar' ? 'تأكيد كلمة المرور' : 'Confirm Password'}
                    type="password"
                    value={formData.confirmPassword}
                    onChange={handleChange('confirmPassword')}
                    required
                    disabled={loading}
                  />
                </Grid>
              </Grid>

              <Button
                fullWidth
                type="submit"
                variant="contained"
                size="large"
                disabled={loading}
                startIcon={loading ? <CircularProgress size={20} /> : <PersonAdd />}
              >
                {loading 
                  ? (i18n.language === 'ar' ? 'جاري التسجيل...' : 'Registering...')
                  : t('register')}
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
                  ? 'لديك حساب بالفعل؟'
                  : 'Already have an account?'}
              </Typography>
              <Button
                component={Link}
                to="/login"
                variant="outlined"
                startIcon={<LoginIcon />}
                fullWidth
              >
                {t('login')}
              </Button>
            </Box>
          </CardContent>
        </Card>
      </Box>
    </Container>
  );
}
